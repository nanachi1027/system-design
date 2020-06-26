package com.mae.systemdesign.ratelimitcloud.util;

public class RedisHandler {
    public String getLeaderUID() {
        return "";
    }

    public Long getLeaderPutTimestamp() {
        return 0L;
    }

    public void putBucket() {
    }

    public boolean tryLockFailed(String uid) {
        return false;
    }

    public long getBucketPutExpires() {
        return 0L;
    }

    public void releaseLock() {
    }
}
