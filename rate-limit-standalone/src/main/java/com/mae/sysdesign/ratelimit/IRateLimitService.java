package com.mae.sysdesign.ratelimit;

public interface IRateLimitService {
    // acquire specified `tokenNum` in block way
    void acquire(long tokenNum);
    // try to acquire specified `tokenNum` tokens from token bucket
    // if not enough will try to wait for `blockTimeInMS` then re-acquire
    boolean tryAcquire(long tokenNum, long blockTimeInMS);

    // try to acquire specified `tokenNum` tokens from token bucket in non-block way
    boolean tryAcquire(long tokenNum);

    static IRateLimitService getInstance(double QPS,long refreshDelay, long period, boolean overflow) {
        return RateLimiterLocalMode.getInstance(QPS, refreshDelay, period, overflow);
    }

    void shutDown();

    long getNotExpiredTokenNum();
}
