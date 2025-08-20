package com.kaua.events.platform.infrastructure.configurations.properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@ConfigurationProperties(prefix = "efi.pix")
public class EfiPixProperties implements InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(EfiPixProperties.class);

    private String baseUrl;
    private String oauthTokenPath;
    private String oauthScopes;

    private String clientId;
    private String clientSecret;
    private String p12Password;

    private String p12Source;
    private String p12LocalPath;

    @Override
    public void afterPropertiesSet() {
        log.debug("Efi pix properties initialized {}", this);
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

    public String getOauthScopes() {
        return oauthScopes;
    }

    public void setOauthScopes(String oauthScopes) {
        this.oauthScopes = oauthScopes;
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

    public String getP12Password() {
        return p12Password;
    }

    public void setP12Password(String p12Password) {
        this.p12Password = p12Password;
    }

    public String getP12Source() {
        return p12Source;
    }

    public void setP12Source(String p12Source) {
        this.p12Source = p12Source;
    }

    public String getP12LocalPath() {
        return p12LocalPath;
    }

    public void setP12LocalPath(String p12LocalPath) {
        this.p12LocalPath = p12LocalPath;
    }

    @Override
    public String toString() {
        return "EfiPixProperties(" +
                "baseUrl='" + baseUrl + '\'' +
                ", oauthTokenPath='" + oauthTokenPath + '\'' +
                ", oauthScopes='" + oauthScopes + '\'' +
                ", p12Source='" + p12Source + '\'' +
                ", p12LocalPath='" + p12LocalPath + '\'' +
                ')';
    }
}
