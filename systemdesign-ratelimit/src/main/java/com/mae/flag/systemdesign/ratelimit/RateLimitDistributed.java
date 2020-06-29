package com.mae.flag.systemdesign.ratelimit;

import com.mae.flag.systemdesign.redis.RedisTokenBucketService;
import com.mae.flag.systemdesign.util.RateLimitRateLimitSpringContextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RateLimitDistributed implements IRateLimit {

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
     * @param QPS query per second.
     * @param refreshDelay thread pool schedule param delay refresh time in milliseconds.
     * @param bucket token bucket name.
     * @param expiredTime leader expired time in milliseconds.
     */
    public RateLimitDistributed (double QPS, long refreshDelay, String bucket, long expiredTime) {
        this.period = new Double(1000 * 1000 * 1000 / QPS).longValue();
        this.refreshDelay = refreshDelay;
        this.redisTokenBucketHandler = RateLimitRateLimitSpringContextUtil.getBean(RedisTokenBucketService.class);
    }

    @Override
    public void acquire() {

    }

    @Override
    public boolean tryAcquire(long blockTimeInMS) {
        return false;
    }

    @Override
    public boolean tryAcquire() {
        return false;
    }

    public static IRateLimit getInstance(double QPS, long refreshDelay, String bucket, long expiredTime) {
        return new RateLimitDistributed(QPS, refreshDelay, bucket, expiredTime);
    }
}
