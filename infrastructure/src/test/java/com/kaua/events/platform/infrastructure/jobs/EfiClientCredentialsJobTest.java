package com.kaua.events.platform.infrastructure.jobs;

import com.kaua.events.platform.domain.UnitTest;
import com.kaua.events.platform.infrastructure.configurations.authentication.client.RefreshClientCredentials;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EfiClientCredentialsJobTest extends UnitTest {

    @Mock
    private RefreshClientCredentials refreshClientCredentials;

    @InjectMocks
    private EfiClientCredentialsJob clientCredentialsJob;

    @Test
    void shouldRefreshClientCredentials() {
        clientCredentialsJob.refreshClientCredentials();

        verify(refreshClientCredentials, times(2)).refresh();
    }
}