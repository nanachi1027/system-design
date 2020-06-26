package com.mae.systemdesign.ratelimitcloud.util;

import org.springframework.stereotype.Component;

@Component
public class CurrentProperties {

    // turn on cloud rate limit
    private boolean cloudEnabled = false;

    // thread pool size
    private int corePoolSize = 10;

    public void setCloudEnabled(boolean cloudEnabled) {
        this.cloudEnabled = cloudEnabled;
    }

    public void setCorePoolSize(int corePoolSize) {
        this.corePoolSize = corePoolSize;
    }

    public boolean isCloudEnabled() {
        return cloudEnabled;
    }

    public int getCorePoolSize() {
        return corePoolSize;
    }
}
