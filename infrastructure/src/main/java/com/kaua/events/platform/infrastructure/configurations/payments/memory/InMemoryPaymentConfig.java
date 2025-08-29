package com.kaua.events.platform.infrastructure.configurations.payments.memory;

import com.kaua.events.platform.application.gateways.PaymentGateway;
import com.kaua.events.platform.infrastructure.configurations.annotations.InMemoryPaymentClient;
import com.kaua.events.platform.infrastructure.configurations.properties.WebClientProperties;
import com.kaua.events.platform.infrastructure.configurations.properties.payments.InMemoryPixProperties;
import com.kaua.events.platform.infrastructure.gateways.InMemoryPaymentGateway;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration(proxyBeanMethods = false)
public class InMemoryPaymentConfig {

    @Bean
    @ConditionalOnProperty(prefix = "payments.in-memory.pix", name = "enabled", havingValue = "true")
    public PaymentGateway inMemoryPaymentGateway(
            @InMemoryPaymentClient final WebClient webClient
    ) {
        return new InMemoryPaymentGateway(webClient);
    }

    @Bean
    @ConditionalOnProperty(prefix = "payments.in-memory.pix", name = "enabled", havingValue = "true")
    @ConfigurationProperties(prefix = "web-client.payments")
    @InMemoryPaymentClient
    public WebClientProperties efiWebClientProperties() {
        return new WebClientProperties();
    }

    @InMemoryPaymentClient
    @Bean
    @ConditionalOnProperty(prefix = "payments.in-memory.pix", name = "enabled", havingValue = "true")
    public WebClient inMemoryWebClient(
            @InMemoryPaymentClient final WebClientProperties webClientProperties,
            final InMemoryPixProperties props
    ) {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, webClientProperties.getConnectTimeout())
                .responseTimeout(Duration.ofMillis(webClientProperties.getReadTimeout()))
                .doOnConnected(conn ->
                        conn.addHandlerLast(new ReadTimeoutHandler(webClientProperties.getReadTimeout(), TimeUnit.MILLISECONDS))
                                .addHandlerLast(new WriteTimeoutHandler(webClientProperties.getReadTimeout(), TimeUnit.MILLISECONDS)));

        return WebClient.builder()
                .baseUrl(props.getBaseUrl())
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}
