package com.kaua.events.platform.infrastructure.configurations.authentication.client;

import com.kaua.events.platform.infrastructure.configurations.properties.payments.EfiPixProperties;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import static com.kaua.events.platform.infrastructure.configurations.authentication.client.AuthenticationGateway.ClientCredentialsInput;

@Component
public class ClientCredentialsManager implements GetClientCredentials, RefreshClientCredentials {

    private final AtomicReference<ClientCredentials> credentials = new AtomicReference<>();

    private final AuthenticationGateway authenticationGateway;
    private final EfiPixProperties efiPixProperties;

    public ClientCredentialsManager(
            final AuthenticationGateway authenticationGateway,
            final EfiPixProperties efiPixProperties
    ) {
        this.authenticationGateway = Objects.requireNonNull(authenticationGateway);
        this.efiPixProperties = Objects.requireNonNull(efiPixProperties);
    }

    @Override
    public String retrieve() {
        return this.credentials.get().accessToken();
    }

    @Override
    public void refresh() {
        final var aResult = this.authenticationGateway
                .login(new ClientCredentialsInput(clientId(), clientSecret()));

        this.credentials.set(new ClientCredentials(aResult.accessToken()));
    }

    private String clientId() {
        return this.efiPixProperties.getClientId();
    }

    private String clientSecret() {
        return this.efiPixProperties.getClientSecret();
    }

    record ClientCredentials(String accessToken) {
    }
}
