package com.mae.sysdesign.ratelimit;

import com.mae.sysdesign.util.ThreadLogger;
import com.sun.tools.javac.util.Assert;
import org.apache.log4j.Logger;

import java.util.PriorityQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RateLimiterLocalMode implements IRateLimitService {

    private static Logger logger = ThreadLogger.getLogger("RateLimiterLocalMode");

    private static final int DEFAULT_BUCKET_CAPACITY = 50;
    // how many tokens needs to added to bucket every period
    private final long refillTokenRatePerPeriod;
    // token bucket's capacity
    private int capacity;
    // frequency of adding token to bucket
    private long period;
    // delay of refreshing configs
    private long refreshDelay;
    // token bucket
    private PriorityQueue<Token> tokenBucket;

    // token producer thread pool
    ScheduledExecutorService tokeGenerator;

    public static RateLimiterLocalMode getInstance(double qps, long refreshDelay, long period, boolean overflow) {
        return new RateLimiterLocalMode(qps, refreshDelay, period, overflow);
    }

    public RateLimiterLocalMode(double qps, long refreshDelay, long period, boolean overflow) {
        this.capacity = overflow ? capacity : (qps < 1 ? capacity : new Double(qps).intValue());
        tokenBucket = new PriorityQueue<>(capacity, (t1, t2) ->
                (t1.getGeneratedTimestamp().compareTo(t2.getGeneratedTimestamp())));

        this.refreshDelay = refreshDelay;

        // frequency of adding token to bucket
        this.period = period;

        // how many tokens will be refilled into bucket every period
        // QPS / 1000 -> total quest in ms * 1 period (ms) = how many tokens will be added to token bucket every period.
        this.refillTokenRatePerPeriod = qps != 0 ? new Double(qps / 1000 * period).longValue() : Integer.MAX_VALUE;

        logger.info("RateLimiter capacity=" + capacity + ", period=" + period + "ms,\n" +
                "refillTokenRatePerPeriod=" + refillTokenRatePerPeriod + "tokens/period.");

        tokeGenerator = Executors.newScheduledThreadPool(1);

        if (qps != 0) {
            // if qps != 0, we invoke rate limit and begin refill tokens to token bucket
            scheduledRefill();
        }
    }

    private void scheduledRefill() {
        this.tokeGenerator.scheduleAtFixedRate(() -> {
                    // refer to doc: rate limit's refill strategy for more detail
                    // 1. check and remove expired token from token bucket
                    synchronized (tokenBucket) {
                        int originalSize = tokenBucket.size();
                        tokenBucket.stream().filter(item -> !item.isTokenExpired());

                        logger.info("TokenBucket removed expired token num=" + (tokenBucket.size() - originalSize));

                        long refillTokenNum = (capacity < (tokenBucket.size() + refillTokenRatePerPeriod)) ?
                                (refillTokenRatePerPeriod + tokenBucket.size() - capacity) :
                                refillTokenRatePerPeriod;

                        logger.info("TokenBucket needs refill " + refillTokenNum + " tokens.");
                        for (int i = 0; i < refillTokenNum; i++) {
                            Token token = new Token(period);
                            logger.info("New generated token=" + token);
                            tokenBucket.offer(token);
                        }
                        logger.info("TokenBucket size= " + tokenBucket.size() + " after refilling.");
                    }
                },
                refreshDelay, period, TimeUnit.MILLISECONDS);
    }

    // customer apply requireTokenNum from token bucket in block way
    public void acquire(long requireTokenNum) {
        while (tokenBucket.stream().filter(item -> !item.isTokenExpired()).count() >= requireTokenNum) ;
        while (requireTokenNum > 0) {
            Token token = tokenBucket.poll();
            if (!token.isTokenExpired()) requireTokenNum--;
        }
    }

    // customer trys to acquire requireTokenNum from token bucket
    // if not enough token remained return false immediately
    public boolean tryAcquire(long requireTokenNum) {
        Assert.check(requireTokenNum > 0, "requireTokenNum should > 0");
        synchronized (tokenBucket) {
            if (tokenBucket.stream().filter(item -> !item.isTokenExpired()).count() >= requireTokenNum) {
                while (requireTokenNum > 0) {
                    Token token = tokenBucket.poll();
                    if (!token.isTokenExpired()) requireTokenNum--;
                }
            }
        }

        return requireTokenNum == 0 ? true : false;
    }


    // customer trys to acquire requireTokenNum from token bucket
    // if not enough token remained, block for `blockTime`
    // if still not enough token left return false immediately
    public boolean tryAcquire(long requireTokenNum, long blockTimeInMS) {
        Assert.check(requireTokenNum > 0, "requireTokenNum should > 0");
        synchronized (tokenBucket) {
            if (tokenBucket.size() == 0 || tokenBucket.stream().filter(item -> !item.isTokenExpired()).count() < requireTokenNum) {
                try {
                    Thread.sleep(blockTimeInMS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if (tokenBucket.stream().filter(item -> !item.isTokenExpired()).count() >= requireTokenNum) {
                while (requireTokenNum > 0) {
                    Token acquireToken = tokenBucket.poll();
                    if (!acquireToken.isTokenExpired()) requireTokenNum--;
                }
            }
        }
        return requireTokenNum == 0 ? true : false;
    }

    public void shutDown() {
        if (this.tokeGenerator != null) {
            logger.info("TokenGenerator will be exit!");
            this.tokeGenerator.shutdown();
            logger.info("TokenGenerator status " + tokeGenerator.isShutdown());
        }
    }

    @Override
    public long getNotExpiredTokenNum() {
        if (tokenBucket.size() == 0) return tokenBucket.size();
        return tokenBucket.stream().filter(item -> !item.isTokenExpired()).count();
    }
}
