package com.mae.flag.systemdesign.redis;

import com.mae.flag.systemdesign.BaseUnitTest;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import redis.embedded.RedisServer;

@SpringBootTest
@RunWith(SpringRunner.class)
public class RedisTokenBucketServiceUnitTest extends BaseUnitTest  {

    private Logger logger = LoggerFactory.getLogger(RedisTokenBucketServiceUnitTest.class);

    private RedisServer redisServer = null;

    @Autowired
    private RedisTokenBucketService redisTokenBucketService;

    @Before
    public void init() {
        redisServer = new RedisServer();
        redisServer.start();
        redisTokenBucketService.initBucket();
    }

    @After
    public void teardown() {
        if (redisServer != null) {
            redisServer.stop();
            redisServer = null;
        }
    }


    @Test
    public void testGetBucketToken()  {
        Assert.assertNotNull(redisTokenBucketService);
        Assert.assertFalse(redisTokenBucketService.getBucketToken() == -1);
    }

    @Test
    public void testGetBucketTokenAfterPutBucket() {
        redisTokenBucketService.putBucket();
        Assert.assertTrue(redisTokenBucketService.getBucketToken() != -1);
    }

    @Test
    public void testTryLock() {
        String rateLimterInstanceUID1 = "UID1";
        String rateLimiterInstanceUID2 = "UID2";

        // the first rateLimiter instance get the lock
        Assert.assertTrue(redisTokenBucketService.tryLock(rateLimterInstanceUID1));

        // the second arrived rate limiter instance get lock failed
        Assert.assertFalse(redisTokenBucketService.tryLock(rateLimiterInstanceUID2));

        // release lock
        Assert.assertTrue(redisTokenBucketService.releaseLock());

        // then second arrived rate limiter instance try to get lock will success
        Assert.assertTrue(redisTokenBucketService.tryLock(rateLimiterInstanceUID2));
    }

    @Test
    public void expiredTimeTest() {
        // we set leader's expired time is 1000 ms
        // which means current leader will become expired after 1000ms
        long expireDuration = 1000;

        String UID1 = "UID1";
        String UID2 = "UID2";
        Assert.assertTrue(redisTokenBucketService.tryLock(UID1));
        // rate limiter UID1 put token bucket
        redisTokenBucketService.putBucket();
        long expiredTimestamp = redisTokenBucketService.getLeaderElectionTimestamp() + expireDuration;
        while (expiredTimestamp <= System.currentTimeMillis());
        logger.info("Leader expired, release lock.");
        // leader expired, and all tokens in the buckets are all expired
        redisTokenBucketService.releaseLock();
        Assert.assertTrue(redisTokenBucketService.tryLock(UID2));
        logger.info("Leader " + UID2 + " put token to bucket");
        redisTokenBucketService.putBucket();
    }
}
