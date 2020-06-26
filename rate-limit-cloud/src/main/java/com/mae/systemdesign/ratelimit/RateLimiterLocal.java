package com.mae.systemdesign.ratelimit;

public class RateLimiterLocal implements IRateLimitService {

    public static IRateLimitService getInstance(double QPS, long refreshDelay, long period) {
        return new RateLimiterLocal(QPS, refreshDelay, period);
    }

    public RateLimiterLocal(double QPS, long refreDelay, long period) {

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
