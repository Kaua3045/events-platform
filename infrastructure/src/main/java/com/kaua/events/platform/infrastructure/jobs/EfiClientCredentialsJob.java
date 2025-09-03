package com.kaua.events.platform.infrastructure.jobs;

import com.kaua.events.platform.infrastructure.configurations.annotations.EfiChargesClient;
import com.kaua.events.platform.infrastructure.configurations.annotations.EfiPixClient;
import com.kaua.events.platform.infrastructure.configurations.authentication.client.RefreshClientCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Component
@Profile("!test-integration")
@ConditionalOnProperty(prefix = "payments.efi.pix", name = "enabled", havingValue = "true")
public class EfiClientCredentialsJob {

    private static final Logger log = LoggerFactory.getLogger(EfiClientCredentialsJob.class);

    private final RefreshClientCredentials refreshClientCredentialsPix;
    private final RefreshClientCredentials refreshClientCredentialsCharges;

    public EfiClientCredentialsJob(
            @EfiPixClient final RefreshClientCredentials refreshClientCredentialsPix,
            @EfiChargesClient final RefreshClientCredentials refreshClientCredentialsCharges
    ) {
        this.refreshClientCredentialsPix = Objects.requireNonNull(refreshClientCredentialsPix);
        this.refreshClientCredentialsCharges = Objects.requireNonNull(refreshClientCredentialsCharges);
    }

    @Scheduled(
            fixedRateString = "${jobs.efi.pix.client-credentials.refresh-rate-minutes}",
            initialDelayString = "${jobs.efi.pix.client-credentials.refresh-initial-delay-minutes}",
            timeUnit = TimeUnit.MINUTES
    )
    public void refreshPixClientCredentials() {
        log.info("Refreshing efi pix client credentials");
        this.refreshClientCredentialsPix.refresh();
        log.info("Client efi pix credentials refreshed");
    }

    @Scheduled(
            fixedRateString = "${jobs.efi.charges.client-credentials.refresh-rate-minutes}",
            initialDelayString = "${jobs.efi.charges.client-credentials.refresh-initial-delay-minutes}",
            timeUnit = TimeUnit.MINUTES
    )
    public void refreshChargesClientCredentials() {
        log.info("Refreshing efi charges client credentials");
        this.refreshClientCredentialsCharges.refresh();
        log.info("Client efi charges credentials refreshed");
    }
}
