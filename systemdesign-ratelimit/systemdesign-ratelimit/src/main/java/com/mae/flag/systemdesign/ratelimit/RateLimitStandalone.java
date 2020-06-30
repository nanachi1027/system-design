package com.mae.flag.systemdesign.ratelimit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.PriorityQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class RateLimitStandalone implements IRateLimit {

    private static Logger logger = LoggerFactory.getLogger(RateLimitStandalone.class);

    private long period;
    private long refreshDelay;
    private AtomicLong tokenBucket;
    private long bucketCapacity;


    public RateLimitStandalone(double QPS, long refreshDelay) {
        this.period = new Double(1000 * 1000 * 1000 / QPS).longValue();
        this.refreshDelay = refreshDelay;
        tokenBucket = new AtomicLong(0);
        bucketCapacity = 100;

        // when rate limiter is constructed, call refill to fill bucket with tokens
        // and add 1 token every period nano-seconds
        refill();
    }

    @Override
    public boolean acquire() {
        long token = tokenBucket.get();
        while (token <= 0) {
            logger.info("Failed to get token from bucket, wait " + 100 + " ms and retry ...");
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            token = tokenBucket.get();
        }
        // minus 1 and return
        tokenBucket.getAndDecrement();
        return true;
    }

    @Override
    public boolean tryAcquire(long blockTimeInMS) {
        long token = tokenBucket.get();
        if (token <= 0) {
            // wait blockTimeInMS then retry
            logger.info("Failed to get token from bucket, wait " + blockTimeInMS + " ms and retry");
            try {
                Thread.sleep(blockTimeInMS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            token = tokenBucket.get();
        }

        if (token <= 0) {
            logger.info("Failed to get token from bucket after wait " + blockTimeInMS + " ms");
            // here recovery value in case of failed get token and also minus token num
            return false;
        } else {
            tokenBucket.getAndDecrement();
            return true;
        }
    }

    @Override
    public boolean tryAcquire() {
        long token = tokenBucket.get();
        if (token > 0) {
            tokenBucket.getAndDecrement();
            return true;
        } else {
            tokenBucket.incrementAndGet();
            return false;
        }
    }

    @Override
    public void refill() {
        Executors.newScheduledThreadPool(1).scheduleAtFixedRate(() -> {
                    // add 1 token every period ns
                    // make sure token num <= capacity
                    if (tokenBucket.get() == bucketCapacity) {
                        tokenBucket.get();
                    } else {
                        tokenBucket.getAndIncrement();
                    }
                },
                refreshDelay,
                period,
                TimeUnit.NANOSECONDS);
    }

    /**
     * Set token num in bucket to 0,
     * used for testing.
     */
    @Override
    public void refreshBucket() {
        tokenBucket.set(0);
    }

    public static IRateLimit getInstance(double QPS, long refreshDelay) {
        return new RateLimitStandalone(QPS, refreshDelay);
    }
}
