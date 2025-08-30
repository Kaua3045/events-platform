package com.kaua.events.platform.infrastructure.configurations;

import com.kaua.events.platform.infrastructure.configurations.annotations.EfiChargesClient;
import com.kaua.events.platform.infrastructure.configurations.annotations.EfiPixClient;
import com.kaua.events.platform.infrastructure.configurations.authentication.client.RefreshClientCredentials;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration(proxyBeanMethods = false)
@ComponentScan(basePackages = "com.kaua.events.platform")
@EnableScheduling
public class WebServerConfig {

    @Bean
    @Profile("!test-integration")
    ApplicationListener<ContextRefreshedEvent> refreshClientCredentialsEfi(
            @Autowired(required = false) @EfiPixClient RefreshClientCredentials refreshClientCredentialsPix,
            @Autowired(required = false) @EfiChargesClient RefreshClientCredentials refreshClientCredentialsCharges
    ) {
        return event -> {
            if (refreshClientCredentialsPix != null) {
                refreshClientCredentialsPix.refresh();
            }
            if (refreshClientCredentialsCharges != null) {
                refreshClientCredentialsCharges.refresh();
            }
        };
    }

}
