package com.kaua.events.platform.infrastructure.configurations.properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration(proxyBeanMethods = false)
@ConfigurationProperties(prefix = "application.cors")
public class CorsProperties implements InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(CorsProperties.class);

    private List<String> allowedOrigins;
    private List<String> allowedHeaders;
    private boolean allowCredentials;
    private long maxAgePreflight;

    @Override
    public void afterPropertiesSet() {
        if (allowedOrigins == null || allowedOrigins.isEmpty()) {
            log.warn("CORS allowed origins are not set. Defaulting to *.");
            allowedOrigins = List.of("*");
        }
        log.debug("CORS properties initialized {}", this);
    }

    public List<String> getAllowedOrigins() {
        return allowedOrigins;
    }

    public void setAllowedOrigins(List<String> allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }

    public List<String> getAllowedHeaders() {
        return allowedHeaders;
    }

    public void setAllowedHeaders(List<String> allowedHeaders) {
        this.allowedHeaders = allowedHeaders;
    }

    public boolean isAllowCredentials() {
        return allowCredentials;
    }

    public void setAllowCredentials(boolean allowCredentials) {
        this.allowCredentials = allowCredentials;
    }

    public long getMaxAgePreflight() {
        return maxAgePreflight;
    }

    public void setMaxAgePreflight(long maxAgePreflight) {
        this.maxAgePreflight = maxAgePreflight;
    }

    @Override
    public String toString() {
        final var sb = new StringBuilder("CorsProperties(");

        sb.append("allowedOrigins=").append(allowedOrigins);
        sb.append(", allowedHeaders=").append(allowedHeaders);
        sb.append(", allowCredentials=").append(allowCredentials);
        sb.append(", maxAgePreflight=").append(maxAgePreflight);
        sb.append(')');
        return sb.toString();
    }
}
