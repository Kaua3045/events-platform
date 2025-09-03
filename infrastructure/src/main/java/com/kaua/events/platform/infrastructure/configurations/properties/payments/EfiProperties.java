package com.kaua.events.platform.infrastructure.configurations.properties.payments;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@ConfigurationProperties(prefix = "payments.efi.charges")
public class EfiProperties implements InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(EfiProperties.class);

    private String baseUrl;
    private String oauthTokenPath;
    private String notificationUrl;
    private boolean skipMtlsChecking;
    private boolean enabled;
    private boolean ngrok;
    private String ngrokUrl;

    private String clientId;
    private String clientSecret;

    @Override
    public void afterPropertiesSet() {
        log.debug("Efi properties initialized {}", this);
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getOauthTokenPath() {
        return oauthTokenPath;
    }

    public void setOauthTokenPath(String oauthTokenPath) {
        this.oauthTokenPath = oauthTokenPath;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public boolean isSkipMtlsChecking() {
        return skipMtlsChecking;
    }

    public void setSkipMtlsChecking(boolean skipMtlsChecking) {
        this.skipMtlsChecking = skipMtlsChecking;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getNotificationUrl() {
        return notificationUrl;
    }

    public void setNotificationUrl(String notificationUrl) {
        this.notificationUrl = notificationUrl;
    }

    public boolean isNgrok() {
        return ngrok;
    }

    public void setNgrok(boolean ngrok) {
        this.ngrok = ngrok;
    }

    public String getNgrokUrl() {
        return ngrokUrl;
    }

    public void setNgrokUrl(String ngrokUrl) {
        this.ngrokUrl = ngrokUrl;
    }

    @Override
    public String toString() {
        return "EfiPixProperties(" +
                "baseUrl='" + baseUrl + '\'' +
                ", oauthTokenPath='" + oauthTokenPath + '\'' +
                ", notificationUrl='" + notificationUrl + '\'' +
                ", skipMtlsChecking=" + skipMtlsChecking +
                ", enabled=" + enabled +
                ", clientId='" + clientId + '\'' +
                ", clientSecret='" + clientSecret + '\'' +
                ", ngrok=" + ngrok +
                ", ngrokUrl='" + ngrokUrl + '\'' +
                ')';
    }
}
