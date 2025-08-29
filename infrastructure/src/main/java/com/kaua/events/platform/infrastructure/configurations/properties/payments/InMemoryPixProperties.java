package com.kaua.events.platform.infrastructure.configurations.properties.payments;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@ConfigurationProperties(prefix = "payments.in-memory.pix")
public class InMemoryPixProperties implements InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(InMemoryPixProperties.class);

    private String baseUrl;

    @Override
    public void afterPropertiesSet() {
        log.debug("InMemory pix properties initialized {}", this);
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    @Override
    public String toString() {
        return "InMemoryPixProperties(" +
                "baseUrl='" + baseUrl + '\'' +
                ')';
    }
}
