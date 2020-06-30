package com.mae.flag.systemdesign.ratelimit;

import com.mae.flag.systemdesign.ratelimit.IRateLimit;
import com.mae.flag.systemdesign.ratelimit.RateLimitStandalone;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class RateLimitStandaloneUnitTest {
    private IRateLimit rateLimit = null;

    @Before
    public void init() {
        long QPS = 10000;
        long refreshDelay = 0; // ns
        rateLimit = new RateLimitStandalone(QPS, refreshDelay);
    }

    @Test
    public void testAcquire() {
        boolean ret = rateLimit.acquire();
        Assert.assertTrue(ret);
    }

    @Test
    public void testTryAcquire() {
        boolean ret = rateLimit.tryAcquire();
        Assert.assertFalse(ret);
        // period = 1000 * 1000 * 1000 / 10000 = 100000 ns = 100ms 1 token
        // delay = 0, so wait 100ms the token bucket will generate 1 token
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Assert.assertTrue(rateLimit.tryAcquire());
    }

    @Test
    public void testTryAcquireWithBlockTime() {
        // reset token num to 0
        rateLimit.refreshBucket();
        // 2. we set block time > period
        long blockTimeInMs = 1000;
        Assert.assertTrue(rateLimit.tryAcquire(blockTimeInMs));
    }
}
