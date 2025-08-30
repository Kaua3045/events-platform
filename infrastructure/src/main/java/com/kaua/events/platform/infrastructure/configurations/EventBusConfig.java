package com.kaua.events.platform.infrastructure.configurations;

import com.kaua.events.platform.infrastructure.services.eventbus.ApplicationEventBus;
import com.kaua.events.platform.infrastructure.services.eventbus.EventBus;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class EventBusConfig {

    @Bean
    @ConditionalOnProperty(name = "application.eventbus", havingValue = "in-memory", matchIfMissing = true)
    public EventBus inMemoryEventBus(final ApplicationContext applicationContext) {
        return new ApplicationEventBus(applicationContext);
    }
}
