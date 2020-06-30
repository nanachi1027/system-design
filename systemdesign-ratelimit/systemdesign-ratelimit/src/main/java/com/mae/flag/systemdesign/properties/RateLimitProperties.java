package com.mae.flag.systemdesign.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "rate.limit")
public class RateLimitProperties {

    /**
     * enable interface's rate limit
     */
    private boolean enabled = false;

    /**
     * enable distributed or standalone rate limit
     */
    private boolean standAlone = false;

    /**
     * thread pool thread num (concurrency)
     */
    private int threadPoolCoreNum = 10;

    // ---
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isStandAlone() {
        return standAlone;
    }

    public void setStandAlone(boolean standAlone) {
        this.standAlone = standAlone;
    }

    public int getThreadPoolCoreNum() {
        return threadPoolCoreNum;
    }

    public void setThreadPoolCoreNum(int threadPoolCoreNum) {
        this.threadPoolCoreNum = threadPoolCoreNum;
    }
}
