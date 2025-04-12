package com.kaua.events.platform.infrastructure.configurations.properties;

import com.kaua.events.platform.infrastructure.oauth.OAuthClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration(proxyBeanMethods = false)
@ConfigurationProperties(prefix = "application.oauth")
public class OAuthClients implements InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(OAuthClients.class);

    private Map<String, OAuthClient> clients;

    @Override
    public void afterPropertiesSet() {
        if (clients == null || clients.isEmpty()) {
            log.warn("No OAuth clients configured");
        } else {
            log.info("OAuth clients configured: {}", clients);
        }
    }

    public Map<String, OAuthClient> getClients() {
        return clients;
    }

    public void setClients(Map<String, OAuthClient> clients) {
        this.clients = clients;
    }

    @Override
    public String toString() {
        final var sb = new StringBuilder("OAuthClients(");

        sb.append("clients=").append(clients);
        sb.append(')');
        return sb.toString();
    }
}
