package com.mae.flag.systemdesign.ratelimit;

import com.mae.flag.systemdesign.BaseUnitTest;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import redis.embedded.RedisServer;

@SpringBootTest
@RunWith(SpringRunner.class)
public class RateLimitDistributedUnitTest extends BaseUnitTest {
    private IRateLimit iRateLimit = null;
    private RedisServer redisServer = null;

    @Before
    public void init() {
        redisServer = new RedisServer();
        redisServer.start();

        long QPS = 100000; // 100000 request per second
        long refreshDelay = 500; // 500 ms
        long expiredTime = 15000; //15s
        iRateLimit = IRateLimit.getInstance(QPS, refreshDelay, expiredTime);
        Assert.assertNotNull(iRateLimit);
    }

    @After
    public void tearDown() {
        if (redisServer != null) {
            redisServer.stop();
            redisServer = null;
        }
        iRateLimit = null;
    }

    @Test
    public void testAcquire() {
        Assert.assertTrue(iRateLimit.acquire());
    }

    @Test
    public void testTryAcquire() {

    }

    @Test
    public void testTryAcquireInBlockTime() {

    }
}
