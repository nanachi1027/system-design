package com.mae.sysdesign.ratelimit;

import com.mae.sysdesign.util.ThreadLogger;
import org.apache.log4j.Logger;

public class MockRemoteServer implements IRemoteServer {
    private static Logger logger = ThreadLogger.getLogger("RemoteServer");

    @Override
    public String sendRequest(String request) {
        logger.info("Received request " + request);
        return "RemoteServer received " + request + ", and Response: " + request;
    }
}
