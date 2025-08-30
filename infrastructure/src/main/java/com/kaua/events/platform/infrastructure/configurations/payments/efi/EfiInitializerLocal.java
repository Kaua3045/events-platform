package com.kaua.events.platform.infrastructure.configurations.payments.efi;

import com.kaua.events.platform.infrastructure.configurations.annotations.EfiPixClient;
import com.kaua.events.platform.infrastructure.configurations.authentication.client.GetClientCredentials;
import com.kaua.events.platform.infrastructure.configurations.authentication.client.RefreshClientCredentials;
import com.kaua.events.platform.infrastructure.configurations.properties.payments.EfiPixProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
@ConditionalOnProperty(prefix = "payments.efi.pix", name = "ngrok", havingValue = "true")
public class EfiInitializerLocal implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(EfiInitializerLocal.class);

    @Value("${payments.efi.pix.ngrok-url}")
    private String ngrokUrlConfig;

    private final WebClient webClient;
    private final GetClientCredentials getClientCredentials;
    private final RefreshClientCredentials refreshClientCredentialsPix;
    private final EfiPixProperties efiPixProperties;

    public EfiInitializerLocal(
            @EfiPixClient final WebClient webClient,
            @EfiPixClient final GetClientCredentials getClientCredentials,
            @EfiPixClient final RefreshClientCredentials refreshClientCredentialsPix,
            final EfiPixProperties efiPixProperties
    ) {
        this.webClient = Objects.requireNonNull(webClient);
        this.getClientCredentials = Objects.requireNonNull(getClientCredentials);
        this.refreshClientCredentialsPix = Objects.requireNonNull(refreshClientCredentialsPix);
        this.efiPixProperties = Objects.requireNonNull(efiPixProperties);
    }

    @Override
    public void run(String... args) throws Exception {
        this.refreshClientCredentialsPix.refresh();
        Thread.sleep(Duration.ofMillis(15));
        final var aToken = getClientCredentials.retrieve();

        final var ngrokUrl = getNgrokUrl() + "/api/webhooks/pix?ignorar=";

        this.efiPixProperties.getPixKeys().forEach(pixKey -> {
            final var response = this.webClient.put()
                    .uri("/v2/webhook/" + pixKey)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + aToken)
                    .header("x-skip-mtls-checking", Boolean.toString(this.efiPixProperties.isSkipMtlsChecking()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(Map.of("webhookUrl", ngrokUrl))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.info("Webhook Pix created sucessfully: {}", response);
        });
    }

    private String getNgrokUrl() {
        final var ngrokApi = WebClient.create(ngrokUrlConfig);
        final var response = ngrokApi.get()
                .uri("/api/tunnels")
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        final var tunnels = (List<Map<String, Object>>) response.get("tunnels");
        for (Map<String, Object> tunnel : tunnels) {
            if ("https".equals(tunnel.get("proto"))) {
                return (String) tunnel.get("public_url");
            }
        }
        throw new IllegalStateException("Ngrok HTTPS tunnel does not found");
    }
}
