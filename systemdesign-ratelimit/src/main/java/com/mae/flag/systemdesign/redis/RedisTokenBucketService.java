package com.mae.flag.systemdesign.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisTokenBucketService {

    @Autowired
    private StringRedisTemplate redisHandler;

    // todo: replaced by application.properties ratelimit.bucket.capacity
    private long bucketCapacity = 100;

    // todo: replaced by application.properties ratelimit.bucket.name
    private String bucketName = "TokenBucket";
    private final static String bucketLeader = "BUCKET_LEADER";
    private final static String bucketLeaderElectionTimestamp = "BUCKET_LEADER_ELECTION_TIMESTAMP";

    public RedisTokenBucketService() {
    }

    public void initBucket() {
        // init key: bucketName, value: tokenCapacity
        redisHandler.opsForValue()
                .setIfAbsent(bucketName, String.valueOf(bucketCapacity));

        // the first time there is not leader, we set current timestamp as leader's election timestamp
        redisHandler.opsForValue()
                .setIfAbsent(bucketLeaderElectionTimestamp, String.valueOf(System.currentTimeMillis()));
    }

    //  --- RedisTokenBucketHandler APIs ---
    /**
     * Get 1 token from bucket, if not tokens left return -1.
     */
    public long getBucketToken() {
        return redisHandler.opsForValue().increment(bucketName, -1);
    }

    /**
     * Try to get & occupy lock, if success get lock return {@code true},
     * if lock is occupied by other rate limit instance, failed return {@code false}.
     * @param UID of rate limiter instance
     */
    public boolean tryLock(String UID) {
        return redisHandler.opsForValue().setIfAbsent(bucketLeader, UID);
    }

    /**
     * Release lock
     */
    public boolean releaseLock() {
        return redisHandler.delete(bucketLeader);
    }

    /**
     * Get leader's election timestamp.
     */
    public Long getLeaderElectionTimestamp() {
        return Long.valueOf(redisHandler.opsForValue().get(bucketLeaderElectionTimestamp));
    }

    /**
     * Put given capacity nums of token to bucket.
     * And refresh leader's election start timestamp.
     */
    public void putBucket() {
        // first put token , token num = capacity
        redisHandler.opsForValue().set(bucketName, String.valueOf(bucketCapacity));

        // then put leader's timestamp
        redisHandler.opsForValue().set(bucketLeaderElectionTimestamp, System.currentTimeMillis() + "");
    }
}
