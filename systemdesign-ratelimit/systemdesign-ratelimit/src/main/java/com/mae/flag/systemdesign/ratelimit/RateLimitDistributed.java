package com.mae.flag.systemdesign.ratelimit;

import com.mae.flag.systemdesign.redis.RedisTokenBucketService;
import com.mae.flag.systemdesign.util.RateLimitRateLimitSpringContextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class RateLimitDistributed implements IRateLimit {

    private final double QPS;
    private RedisTokenBucketService redisTokenBucketHandler;

    // transfer from QPS, how many ms gonna add one token to bucket
    private long period;

    // scheduled thread pool refresh delay time
    private long refreshDelay;

    // how long will current leader goes expired and invoke re-election
    private long expirationTime;

    // rate limiter instance global identifier: UID
    private final String UID = RateLimitRateLimitSpringContextUtil.getAppName() + RateLimitRateLimitSpringContextUtil.port() + this.hashCode();
    private Logger logger = LoggerFactory.getLogger(RateLimitDistributed.class);

    // backup standalone rate limiter
    private RateLimitStandalone rateLimitStandalone = null;


    /**
     * @param QPS          query per second.
     * @param refreshDelay thread pool schedule param delay refresh time in milliseconds.
     * @param expiredTime  leader expired time in milliseconds.
     */
    public RateLimitDistributed(double QPS, long refreshDelay, long expiredTime) {
        this.QPS = QPS;
        this.period = new Double(1000 * 1000 * 1000 / QPS).longValue();
        this.expirationTime = expiredTime;
        this.refreshDelay = refreshDelay;
        this.redisTokenBucketHandler = RateLimitRateLimitSpringContextUtil.getBean(RedisTokenBucketService.class);
        refill();
    }

    @Override
    public boolean acquire() {
        Long token = redisTokenBucketHandler.getBucketToken();

        while (token < 0) {
            try {
                logger.info("Failed to get 1 token from bucket, sleep for a while and retry...s");
                Thread.sleep(expirationTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            token = redisTokenBucketHandler.getBucketToken();
        }
        logger.info("Success get 1 token from bucket.");
        return true;
    }

    @Override
    public boolean tryAcquire(long blockTimeInMS) {
        // if blockTimeInMs < expirationTime transfer non-block way
        // else wait for blockTimeInMS ms then retry to retrieve token
        if (blockTimeInMS < expirationTime) {
            return tryAcquire();
        }

        Long token = redisTokenBucketHandler.getBucketToken();
        if (token < 0) {
            logger.warn("Failed to get 1 token from bucket. sleep for " + blockTimeInMS + " ms, and retry...");
            try {
                Thread.sleep(blockTimeInMS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // retry to get token
            token = redisTokenBucketHandler.getBucketToken();
        }
        return token > 0;
    }

    @Override
    public boolean tryAcquire() {
        try {
            Long token = redisTokenBucketHandler.getBucketToken();
            if (token < 0) {
                logger.info("Token Bucket is empty, not token can be retrieve.");
                return false;
            }
        } catch (Exception e) {
            logger.info("Redis service is down, switch to local rate limit mode.");
            if (rateLimitStandalone == null) {
                rateLimitStandalone = new RateLimitStandalone(QPS, refreshDelay);
            }
            return rateLimitStandalone.tryAcquire();
        }
        return true;
    }

    @Override
    public void refill() {
        Executors.newScheduledThreadPool(1).scheduleAtFixedRate(() -> {
                    // get leader name from redis
                    String leaderUID = redisTokenBucketHandler.getBucketLeader();

                    if (UID.equals(leaderUID) || (leaderUID == null && redisTokenBucketHandler.tryLock(UID))) {
                        // cond1: i'm leader or i'm not leader leader is null && i try to get lock and succeed be the leader
                        redisTokenBucketHandler.putBucket();
                    } else {
                        // cond2: i'm not leader i'll get leader's timestamp and check leader is expired
                        // if not expired, i gonna sleep expired time
                        // if expired i'll call release lock and invoke re-election
                        Long duration = redisTokenBucketHandler.getLeaderElectionTimestamp();
                        if (System.currentTimeMillis() - duration > expirationTime) {
                            // leader expired & release lock
                            redisTokenBucketHandler.releaseLock();
                        } else {
                            try {
                                Thread.sleep(expirationTime);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                },
                // time to delay when first execution
                refreshDelay,
                // schedule frequency
                period,
                // schedule frequency's time unit
                TimeUnit.NANOSECONDS);

    }

    @Override
    public void refreshBucket() {
        redisTokenBucketHandler.refreshToken();
    }

    private void sleep() {
        if (this.period < 1000 * 1000) {
            return;
        }
        try {
            Thread.sleep(new Double(this.period / 1000 / 1000).longValue());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static IRateLimit getInstance(double QPS, long refreshDelay, long expiredTime) {
        return new RateLimitDistributed(QPS, refreshDelay, expiredTime);
    }
}
