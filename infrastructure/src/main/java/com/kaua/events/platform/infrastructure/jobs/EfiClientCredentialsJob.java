package com.kaua.events.platform.infrastructure.jobs;

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

    private final RefreshClientCredentials refreshClientCredentials;

    public EfiClientCredentialsJob(final RefreshClientCredentials refreshClientCredentials) {
        this.refreshClientCredentials = Objects.requireNonNull(refreshClientCredentials);
    }

    @Scheduled(
            fixedRateString = "${jobs.efi.client-credentials.refresh-rate-minutes}",
            initialDelayString = "${jobs.efi.client-credentials.refresh-initial-delay-minutes}",
            timeUnit = TimeUnit.MINUTES
    )
    public void refreshClientCredentials() {
        log.info("Refreshing efi client credentials");
        this.refreshClientCredentials.refresh();
        log.info("Client efi credentials refreshed");
    }
}
