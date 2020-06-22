package com.mae.sysdesign.util;

import org.apache.log4j.Logger;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadLoggerUnitTest {
    static Logger logger = ThreadLogger.getLogger("main-process-log");

    @Test
    public void testMultiThreadLogger() {
        try {
            ExecutorService executorService = Executors.newScheduledThreadPool(10);
            logger.info("begin task");

            for (int i = 0; i < 200; i++) {
                ThreadExecutor executor = new ThreadExecutor("Thread" + i);
                executorService.submit(executor);
            }

            executorService.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }

        logger.info("END");
    }
}

class ThreadExecutor implements Runnable {
    private String threadName ;
    private Logger logHandler;

    public ThreadExecutor(String threadName) {
        this.threadName = threadName;
        logHandler = ThreadLogger.getLogger(threadName);
        logHandler.info("Thread " + threadName + " started.");
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        logHandler.info("Thread " + threadName + " finished.");
    }

    @Override
    public void run() {
        System.out.println("Name: " + this.threadName);
    }
}
