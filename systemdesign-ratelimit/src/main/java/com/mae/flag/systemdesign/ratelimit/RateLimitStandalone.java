package com.mae.flag.systemdesign.ratelimit;

public class RateLimitStandalone implements IRateLimit {

    public RateLimitStandalone(double QPS, long refreshDelay, long period) {

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

    public static IRateLimit getInstance(double QPS, long refreshDelay, long period) {
        return null;
    }
}
