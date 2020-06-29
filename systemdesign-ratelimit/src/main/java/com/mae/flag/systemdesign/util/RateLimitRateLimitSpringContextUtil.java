package com.mae.flag.systemdesign.util;

import com.mae.flag.systemdesign.properties.RateLimitProperties;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class RateLimitRateLimitSpringContextUtil implements ApplicationContextAware {

    private static ApplicationContext appContext;

    private static String appName;
    private static String port;
    private static int corePoolSize;
    private static boolean localMode;


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        RateLimitRateLimitSpringContextUtil.appContext = applicationContext;
        RateLimitRateLimitSpringContextUtil.appName = applicationContext.getEnvironment().getProperty("spring.application.name");
        RateLimitRateLimitSpringContextUtil.appName = RateLimitRateLimitSpringContextUtil.appName == null ? "application" : RateLimitRateLimitSpringContextUtil.appName;
        RateLimitRateLimitSpringContextUtil.port = applicationContext.getEnvironment().getProperty("server.port");
        RateLimitRateLimitSpringContextUtil.port = RateLimitRateLimitSpringContextUtil.port == null ? "8080" : RateLimitRateLimitSpringContextUtil.port;
        RateLimitRateLimitSpringContextUtil.corePoolSize = applicationContext.getBean(RateLimitProperties.class).getThreadPoolCoreNum();
        RateLimitRateLimitSpringContextUtil.localMode = applicationContext.getBean(RateLimitProperties.class).isStandAlone();
    }

    public static String getAppName() {
        return appName;
    }

    public static int port() {
        return Integer.parseInt(port);
    }

    public static boolean getLocalMode() {
        return localMode;
    }

    public static int getCorePoolSize() {
        return corePoolSize;
    }

    public static <T> T getBean(Class<T> cls) {
        return appContext.getBean(cls);
    }
}
