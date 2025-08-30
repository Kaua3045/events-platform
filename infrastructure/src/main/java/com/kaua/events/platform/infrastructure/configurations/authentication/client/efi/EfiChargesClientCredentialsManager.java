package com.kaua.events.platform.infrastructure.configurations.authentication.client.efi;

import com.kaua.events.platform.infrastructure.configurations.annotations.EfiChargesClient;
import com.kaua.events.platform.infrastructure.configurations.authentication.client.AuthenticationGateway;
import com.kaua.events.platform.infrastructure.configurations.authentication.client.GetClientCredentials;
import com.kaua.events.platform.infrastructure.configurations.authentication.client.RefreshClientCredentials;
import com.kaua.events.platform.infrastructure.configurations.properties.payments.EfiProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import static com.kaua.events.platform.infrastructure.configurations.authentication.client.AuthenticationGateway.ClientCredentialsInput;

@Component
@ConditionalOnProperty(prefix = "payments.efi.charges", name = "enabled", havingValue = "true")
@EfiChargesClient
public class EfiChargesClientCredentialsManager implements GetClientCredentials, RefreshClientCredentials {

    private final AtomicReference<ClientCredentials> credentials = new AtomicReference<>();

    private final AuthenticationGateway authenticationGateway;
    private final EfiProperties efiProperties;

    public EfiChargesClientCredentialsManager(
            @EfiChargesClient final AuthenticationGateway authenticationGateway,
            final EfiProperties efiProperties
    ) {
        this.authenticationGateway = Objects.requireNonNull(authenticationGateway);
        this.efiProperties = Objects.requireNonNull(efiProperties);
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
        return this.efiProperties.getClientId();
    }

    private String clientSecret() {
        return this.efiProperties.getClientSecret();
    }

    record ClientCredentials(String accessToken) {
    }
}
