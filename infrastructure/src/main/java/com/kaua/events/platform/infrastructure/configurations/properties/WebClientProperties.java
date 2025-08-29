package com.kaua.events.platform.infrastructure.configurations.properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class WebClientProperties implements InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(WebClientProperties.class);

    private int readTimeout;
    private int connectTimeout;

    @Override
    public void afterPropertiesSet() throws Exception {
        log.debug("WebClientProperties initialized: {}", this);
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    @Override
    public String toString() {
        return "WebClientProperties(" +
                "readTimeout=" + readTimeout +
                ", connectTimeout=" + connectTimeout +
                ')';
    }
}
