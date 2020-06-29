package com.mae.flag.systemdesign.ratelimit;

import com.mae.flag.systemdesign.util.RateLimitRateLimitSpringContextUtil;

public interface IRateLimit {
   /**
    * acquire 1 token from bucket, if no tokens left
    * will block wait until bucket refill by leader.
    */
    void acquire();


    /**
     * try to acquire 1 token from bucket, if
     * no tokens left, will wait blockTime milliseconds
     * if still no tokens available, return false.
     * else return true.
     */
    boolean tryAcquire(long blockTimeInMS);

    /**
     * try to acquire 1 token from bucket,
     * if not tokens left, return false immediately
     * else return true.
     */
    boolean tryAcquire();

    static IRateLimit getInstance(double QPS, long refreshDelay, long period, String bucket, long expiredTime) {
        if (RateLimitRateLimitSpringContextUtil.getLocalMode()) {
            return RateLimitStandalone.getInstance(QPS, refreshDelay, period);
        } else {
            return RateLimitDistributed.getInstance(QPS, refreshDelay, period, bucket, expiredTime);
        }
    }
}
