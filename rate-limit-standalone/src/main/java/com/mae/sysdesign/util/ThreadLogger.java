package com.mae.sysdesign.util;

import org.apache.log4j.*;

import java.io.IOException;
import java.text.SimpleDateFormat;

public class ThreadLogger {
    public static Logger getLogger(String logName) {
        Logger logger = null;
        logger = Logger.getLogger(logName);
        PatternLayout layout = new PatternLayout("[%d{MM-dd HH:mm:ss}] %-5p %-8t %m%n");

        // organized log files by datetime
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String logPath = ClassLoader.getSystemClassLoader().getResource(".").getPath();

        // output file
        ThreadLogger.ThreadFileAppender threadFileAppender = null;

        try {
            threadFileAppender = new ThreadFileAppender(layout, logPath, logName, "yyyy-MM-dd");
        } catch (IOException e) {
            e.printStackTrace();
        }

        threadFileAppender.setAppend(false);
        threadFileAppender.setImmediateFlush(true);
        threadFileAppender.setThreshold(Level.DEBUG);

        // bind pattern to logger
        logger.setLevel(Level.DEBUG);
        logger.addAppender(threadFileAppender);

        return logger;
    }

    static class ThreadFileAppender extends DailyRollingFileAppender {
        public ThreadFileAppender(Layout layout, String filePath, String fileName, String datePattern)
                throws IOException {
            super(layout, filePath + fileName + ".log", datePattern);
        }
    }
}

