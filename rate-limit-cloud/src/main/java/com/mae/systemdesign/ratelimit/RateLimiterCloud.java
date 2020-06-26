package com.mae.systemdesign.ratelimit;

import com.mae.systemdesign.ratelimitcloud.util.RatelimiterContextUtil;
import com.mae.systemdesign.ratelimitcloud.util.RedisHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class RateLimiterCloud implements IRateLimitService {
    // token bucket's capacity
    private long capacity;

    // how many nano-seconds add one token to bucket
    private long period;

    // how many milliseconds delay when refresh parameter
    private long refreshDelay;

    // rate limiter expiration time
    private LocalDateTime expirationTime;

    // rate limiter global (in distributed environment) identifier
    private final String UID = RatelimiterContextUtil.getApplicationName() + RatelimiterContextUtil.getPort() + this.hashCode();

    // redis operation handler
    private RedisHandler redisHandler = null;

    // logger
    private Logger logger = LoggerFactory.getLogger(RateLimiterCloud.class);

    // standalone back up when redis service is unaccessible
    private RateLimiterLocal rateLimiterLocal = null;

    /**
     * @param QPS          query per second, if QPS == 0, close rate limiter
     * @param refreshDelay thread pool delay refresh time(ms)
     * @param bucketName   bucket name
     */
    public RateLimiterCloud(double QPS, long refreshDelay, String bucketName) {
        this.capacity = (QPS < 1 ? 1 : new Double(QPS).longValue());
        this.refreshDelay = refreshDelay * 1000 * 1000; // transfer ms to ns (ms, 1000 us, 1000 * 1000 ns)
        // how many ns add 1 token to bucket
        // suppose QPS = how many quests 1 second
        // 1/QPS = how many seconds 1 quest -> 1000 * 1000 * 1000 / QPS - how many ns 1 quest
        // we keep adding tokens the same speed as 10 ^ 3 quests 1 second
        this.period = QPS != 0 ? new Double(1000 * 1000 * 1000 / QPS).longValue() : Integer.MAX_VALUE;

        // init token bucket
        init(bucketName);
        // if QPS == 0, close rate limiter, no token will add to bucket
        if (QPS != 0) {
            putScheduled();
        }
    }

    private void init(String bucket) {
        this.redisHandler = RatelimiterContextUtil.getApplicationContext().getBean(RedisHandler.class);
    }

    /**
     * Suppose in distributed environment, we have multiple RatelimiterCloud instance,
     * each one is identified by global identifier UID, each Rate Limiter owns a thread pool
     * to put a token to bucket(redis) at specified frequency -- period, but every time
     * only one rate limiter is allowed to add token to thread pool -- which one ?
     * It depends on who is the leader, so here we preemption mechanism via redis.
     * Which instance of rate limiter first occupy redis lock it will be the leader.
     * <p>
     * And we also set an expire time if the rate limiter instance doesn't get the lock this time,
     * it will go sleep for expire time, and after wake it will call redis handler
     * to release lock and re-invoke election.
     */
    private void putScheduled() {
        Executors.newScheduledThreadPool(2).scheduleAtFixedRate(() -> {
                    String leaderUID = redisHandler.getLeaderUID();
                    if (this.UID.equals(leaderUID) || (leaderUID == null && redisHandler.tryLockFailed(this.UID))) {
                        // i'm the leader, add token to bucket
                        redisHandler.putBucket();
                    } else {
                        // i'm not leader TVT
                        // 1) i gonna check whether current leader is expired then i have chance to re-join election
                        Long leaderTimestamp = Long.valueOf(Objects.requireNonNull(redisHandler.getLeaderPutTimestamp()));
                        if (System.currentTimeMillis() - leaderTimestamp > redisHandler.getBucketPutExpires()) {
                            // release the lock that held by current expired leader
                            // and re-invoke election
                            redisHandler.releaseLock();
                        } else {
                            // 2) leader not expired, i have nothing to do, sleep for a while (expired time) then re-join election
                            try {
                                Thread.sleep(redisHandler.getBucketPutExpires());
                            } catch (InterruptedException ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                },
                refreshDelay, period, TimeUnit.NANOSECONDS);
    }


    @Override
    public void acquire(long tokenNum) {

    }

    @Override
    public boolean tryAcquire(long tokenNum, long blockTimeInMS) {
        return false;
    }

    @Override
    public boolean tryAcquire(long tokenNum) {
        return false;
    }

    @Override
    public void shutDown() {

    }

    @Override
    public long getNotExpiredTokenNum() {
        return 0;
    }
}
