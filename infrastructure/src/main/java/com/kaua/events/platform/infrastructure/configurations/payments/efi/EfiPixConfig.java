package com.kaua.events.platform.infrastructure.configurations.efi;

import com.kaua.events.platform.application.gateways.PaymentGateway;
import com.kaua.events.platform.infrastructure.configurations.annotations.EfiClient;
import com.kaua.events.platform.infrastructure.configurations.authentication.client.GetClientCredentials;
import com.kaua.events.platform.infrastructure.configurations.properties.EfiPixProperties;
import com.kaua.events.platform.infrastructure.gateways.EfiPaymentGateway;
import com.kaua.events.platform.infrastructure.services.certificates.LocalP12Loader;
import com.kaua.events.platform.infrastructure.services.certificates.P12Loader;
import io.netty.handler.ssl.SslContext;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

@Configuration(proxyBeanMethods = false)
public class EfiPixConfig {

    @Bean
    @ConditionalOnProperty(prefix = "efi.pix", name = "p12-source", havingValue = "local")
    public P12Loader localP12Loader(final EfiPixProperties properties) {
        return new LocalP12Loader(properties.getP12LocalPath());
    }

    @EfiClient
    @Bean
    public WebClient efiPixWebClient(final EfiPixProperties props, final P12Loader loader) {
        byte[] p12 = loader.loadP12Bytes();
        SslContext sslContext = PixSslContextFactory.buildClientSslContext(p12, props.getP12Password());

        HttpClient httpClient = HttpClient.create().secure(spec -> spec.sslContext(sslContext));

//        httpClient.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, properties.getConnectTimeout())
//                .responseTimeout(Duration.ofMillis(properties.getReadTimeout()))
//                .doOnConnected(conn ->
//                        conn.addHandlerLast(new ReadTimeoutHandler(properties.getReadTimeout()))
//                                .addHandlerLast(new WriteTimeoutHandler(properties.getReadTimeout())));

        return WebClient.builder()
                .baseUrl(props.getBaseUrl())
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    @Bean
    public PaymentGateway paymentGateway(
            @EfiClient final WebClient webClient,
            final GetClientCredentials getClientCredentials,
            final EfiPixProperties efiPixProperties
    ) {
        return new EfiPaymentGateway(
                webClient,
                getClientCredentials,
                efiPixProperties
        );
    }
}
