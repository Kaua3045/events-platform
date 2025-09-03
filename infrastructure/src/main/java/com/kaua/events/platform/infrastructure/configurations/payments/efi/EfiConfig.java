package com.kaua.events.platform.infrastructure.configurations.payments.efi;

import com.kaua.events.platform.application.gateways.PaymentGateway;
import com.kaua.events.platform.infrastructure.configurations.annotations.EfiChargesClient;
import com.kaua.events.platform.infrastructure.configurations.annotations.EfiPixClient;
import com.kaua.events.platform.infrastructure.configurations.authentication.client.GetClientCredentials;
import com.kaua.events.platform.infrastructure.configurations.properties.WebClientProperties;
import com.kaua.events.platform.infrastructure.configurations.properties.payments.EfiPixProperties;
import com.kaua.events.platform.infrastructure.configurations.properties.payments.EfiProperties;
import com.kaua.events.platform.infrastructure.gateways.EfiPaymentGateway;
import com.kaua.events.platform.infrastructure.services.certificates.LocalP12Loader;
import com.kaua.events.platform.infrastructure.services.certificates.P12Loader;
import io.netty.channel.ChannelOption;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import io.opentelemetry.api.trace.Tracer;
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
public class EfiConfig {

    @Bean
    @ConditionalOnProperty(prefix = "payments.efi.pix", name = "enabled", havingValue = "true")
    public PaymentGateway efiPaymentGateway(
            @EfiPixClient final WebClient webClient,
            @EfiChargesClient final WebClient webClientCharges,
            @EfiPixClient GetClientCredentials getClientCredentials,
            @EfiChargesClient GetClientCredentials getClientCredentialsCharges,
            EfiPixProperties efiPixProperties,
            EfiProperties efiProperties,
            Tracer tracer
    ) {
        return new EfiPaymentGateway(
                webClient,
                webClientCharges,
                getClientCredentials,
                getClientCredentialsCharges,
                efiPixProperties,
                efiProperties,
                tracer
        );
    }

    @Bean
    @ConditionalOnProperty(prefix = "payments.efi.pix", name = "p12-source", havingValue = "local")
    public P12Loader localP12Loader(final EfiPixProperties properties) {
        return new LocalP12Loader(properties.getP12LocalPath());
    }

    @Bean
    @ConditionalOnProperty(prefix = "payments.efi.pix", name = "enabled", havingValue = "true")
    @ConfigurationProperties(prefix = "web-client.payments")
    @EfiPixClient
    public WebClientProperties efiWebClientProperties() {
        return new WebClientProperties();
    }

    @EfiPixClient
    @Bean
    @ConditionalOnProperty(prefix = "payments.efi.pix", name = "enabled", havingValue = "true")
    public WebClient efiPixWebClient(
            @EfiPixClient final WebClientProperties webClientProperties,
            final EfiPixProperties props,
            final P12Loader loader
    ) {
        byte[] p12 = loader.loadP12Bytes();
        SslContext sslContext = PixSslContextFactory.buildClientSslContext(p12, props.getP12Password());

        HttpClient httpClient = HttpClient.create()
                .secure(spec -> spec.sslContext(sslContext))
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, webClientProperties.getConnectTimeout())
                .responseTimeout(Duration.ofMillis(webClientProperties.getReadTimeout()))
                .doOnConnected(conn ->
                        conn.addHandlerLast(new ReadTimeoutHandler(webClientProperties.getReadTimeout(), TimeUnit.MILLISECONDS))
                                .addHandlerLast(new WriteTimeoutHandler(webClientProperties.getReadTimeout(), TimeUnit.MILLISECONDS))
                );

        return WebClient.builder()
                .baseUrl(props.getBaseUrl())
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    @Bean
    @ConditionalOnProperty(prefix = "payments.efi.charges", name = "enabled", havingValue = "true")
    @ConfigurationProperties(prefix = "web-client.payments")
    @EfiChargesClient
    public WebClientProperties efiChargesWebClientProperties() {
        return new WebClientProperties();
    }

    @EfiChargesClient
    @Bean
    @ConditionalOnProperty(prefix = "payments.efi.charges", name = "enabled", havingValue = "true")
    public WebClient efiChargeWebClient(
            @EfiChargesClient final WebClientProperties webClientProperties,
            final EfiProperties props
    ) {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, webClientProperties.getConnectTimeout())
                .responseTimeout(Duration.ofMillis(webClientProperties.getReadTimeout()))
                .doOnConnected(conn ->
                        conn.addHandlerLast(new ReadTimeoutHandler(webClientProperties.getReadTimeout(), TimeUnit.MILLISECONDS))
                                .addHandlerLast(new WriteTimeoutHandler(webClientProperties.getReadTimeout(), TimeUnit.MILLISECONDS))
                );

        return WebClient.builder()
                .baseUrl(props.getBaseUrl())
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}
