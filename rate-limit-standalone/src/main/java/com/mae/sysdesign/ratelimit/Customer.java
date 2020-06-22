package com.mae.sysdesign.ratelimit;

import com.mae.sysdesign.util.ThreadLogger;
import lombok.Data;
import org.apache.log4j.Logger;

@Data
public class Customer {
    private static Logger logger = ThreadLogger.getLogger(Customer.class.getSimpleName());
    private String id;
    private IRateLimitService rateLimitHandler;
    private IRemoteServer remoteServer;

    public Customer(String id, IRateLimitService rateLimitHandler, IRemoteServer remoteServer) {
        this.id = id;
        this.rateLimitHandler = rateLimitHandler;
        this.remoteServer = remoteServer;
    }

    /**
     * We suppose that each request send from customer to server needs one token.
     * @param request request content
     * @param requestCounter how many times this customer trys to send request to server
     */
    public boolean sendRequestsInTotal(String request, long requestCounter) {
        boolean flag = rateLimitHandler.tryAcquire(requestCounter);
        logger.info("Customer " + id + " try to acquire token " + requestCounter + ", status " + flag);
        if (flag) {
            for (int i = 0; i < requestCounter; i++) {
                logger.info("Res:" + remoteServer.sendRequest(request));
            }
        }
        return flag;
    }

    public void sendRequestsInOrder(String request, long requestCounter) {
        for (int i = 0; i < requestCounter; i++) {
            boolean flag = rateLimitHandler.tryAcquire(1, 2000);
            if (flag) {
                logger.info("Customer " + id + " try to acquire token " + 1 + ", status " + flag);
                logger.info("Res: " + remoteServer.sendRequest(request));
            }
        }
    }
}
