package com.kaua.events.platform.infrastructure.configurations.properties;

import com.kaua.events.platform.domain.auth.OAuthClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.Optional;

@Configuration(proxyBeanMethods = false)
@ConfigurationProperties(prefix = "application.oauth")
public class OAuthClients implements InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(OAuthClients.class);

    private Map<String, OAuthClient> clients;
    private String issuer;

    @Override
    public void afterPropertiesSet() {
        if (clients == null || clients.isEmpty()) {
            log.warn("No OAuth clients configured");
        } else {
            log.info("OAuth clients configured: {}", this);
        }
    }

    public Map<String, OAuthClient> getClients() {
        return clients;
    }

    public Optional<OAuthClient> getClient(String clientId) {
        return Optional.ofNullable(this.clients.get(clientId));
    }

    public void setClients(Map<String, OAuthClient> clients) {
        this.clients = clients;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    @Override
    public String toString() {
        final var sb = new StringBuilder("OAuthClients(");

        sb.append("clients=").append(clients);
        sb.append(", issuer='").append(issuer).append('\'');
        sb.append(')');
        return sb.toString();
    }
}
