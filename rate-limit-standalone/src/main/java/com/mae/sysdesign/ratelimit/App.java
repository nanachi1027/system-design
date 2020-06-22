package com.mae.sysdesign.ratelimit;

public class App {
    public static void main(String[] args) throws InterruptedException {
        IRemoteServer remoteServer = new MockRemoteServer();
        // concurrency of server should be kept < QPS request per second
        double QPS = 800;
        // long refreshDelay, long period, boolean overflow
        long refreshDelayInMS = 50;
        long periodInMS = 100;
        boolean allowOverflow = false;

        IRateLimitService rateLimiter = IRateLimitService.getInstance(QPS, refreshDelayInMS, periodInMS, allowOverflow);


        // create a customer every period (ms)
        // and token request num per customer should be <= QPS/1000 * periodInMs
        for (int i = 0; i < 10; i++) {
            Thread.sleep(periodInMS);
            Customer customer = new Customer("customer-" + i, rateLimiter, remoteServer);
            long tokenCounter = new Double(QPS / 1000 * periodInMS).longValue();
            boolean flag = customer.sendRequestsInTotal("Customer " + customer.getId(), tokenCounter);
            System.out.println("Customer: " + customer.getId() + ", apply token " + tokenCounter +
                    ", remained not expired token num=" + rateLimiter.getNotExpiredTokenNum() + ", status:" + flag);
        }

        // if token request > QPS/1000 * periodInMs request will failed
        long applyTokenCounter = new Double(QPS / 1000).longValue() * periodInMS + 2;
        Customer customer = new Customer("customer-x", rateLimiter, remoteServer);
        boolean flag = customer.sendRequestsInTotal("Customer " + customer.getId(), applyTokenCounter);

        // token request > rate limit token per period(100ms) request token will be denied
        System.out.println("Request result = " + flag);
        rateLimiter.shutDown();
    }
}
