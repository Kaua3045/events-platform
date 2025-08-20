package com.kaua.events.platform;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.kaua.events.platform.infrastructure.configurations.WebServerConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test-integration")
@AutoConfigureWireMock(port = 0)
@SpringBootTest(classes = {WebServerConfig.class})
@Tag("integrationTest")
public class AbstractRestClientTest {

    @Autowired
    private ObjectMapper mapper;

    @BeforeEach
    void beforeEach() {
        WireMock.reset();
        WireMock.resetAllRequests();
    }

    protected String writeValueAsString(final Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
