package com.kaua.events.platform.infrastructure.configurations.authentication.client.efi;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kaua.events.platform.infrastructure.configurations.annotations.EfiClient;
import com.kaua.events.platform.infrastructure.configurations.authentication.client.AuthenticationGateway;
import com.kaua.events.platform.infrastructure.configurations.properties.payments.EfiPixProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Base64;
import java.util.Map;
import java.util.Objects;

@Component
@ConditionalOnProperty(prefix = "payments.efi.pix", name = "enabled", havingValue = "true")
public class EfiAuthenticationGateway implements AuthenticationGateway {

    private static final Logger log = LoggerFactory.getLogger(EfiAuthenticationGateway.class);

    private final WebClient webClient;
    private final String tokenUri;

    public EfiAuthenticationGateway(
            @EfiClient final WebClient webClient,
            final EfiPixProperties efiPixProperties
    ) {
        this.webClient = Objects.requireNonNull(webClient);
        this.tokenUri = Objects.requireNonNull(efiPixProperties.getOauthTokenPath());
    }

    // TODO    @Retry(name = NAMESPACE_NAME)
    @Override
    public AuthenticationResult login(final ClientCredentialsInput input) {
        log.debug("Creating efi client credentials");

        final var aCredentials = input.clientId() + ":" + input.clientSecret();
        final var aEncodedCredentials = Base64.getEncoder().encodeToString(aCredentials.getBytes());

        final var aOutput = this.webClient.post()
                .uri(tokenUri)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Basic " + aEncodedCredentials)
                .bodyValue(Map.of("grant_type", "client_credentials"))
                .retrieve()
                .bodyToMono(AuthServerAuthenticationResult.class)
                .block();

        // TODO need validate output return null for prevent null pointer exception

        log.info("Efi client credentials created");

        return new AuthenticationResult(aOutput.accessToken);
    }

    record AuthServerAuthenticationResult(@JsonProperty("access_token") String accessToken, @JsonProperty("scope") String scope) {
    }
}
