package com.mae.systemdesign.ratelimitcloud.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class RatelimiterContextUtil implements ApplicationContextAware  {

    private static ApplicationContext applicationContext;

    private static String appName;
    private static String port;
    private static int corePoolSize;
    private static boolean cloudEnable;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        RatelimiterContextUtil.applicationContext = applicationContext;
        RatelimiterContextUtil.appName = applicationContext.getEnvironment().getProperty("spring.application.name");
        RatelimiterContextUtil.appName = RatelimiterContextUtil.appName == null ? "application" : RatelimiterContextUtil.appName;
        RatelimiterContextUtil.port = RatelimiterContextUtil.port == null ? "8080" : RatelimiterContextUtil.port;
        RatelimiterContextUtil.corePoolSize = applicationContext.getBean(CurrentProperties.class).getCorePoolSize();
        RatelimiterContextUtil.cloudEnable = applicationContext.getBean(CurrentProperties.class).isCloudEnabled();
    }


    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public static String getPort() {
        return port;
    }

    public static String getApplicationName() {
        return appName;
    }

    public static boolean isCloudEnable() {
        return cloudEnable;
    }

    public static Object getBean(String name) throws BeansException {
        return applicationContext.getBean(name);
    }

    public static int getCorePoolSize() {
        return corePoolSize;
    }

    public static <T> T getBean(Class<T> clz) throws BeansException {
        return applicationContext.getBean(clz);
    }
}
