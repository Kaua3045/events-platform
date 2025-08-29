package com.kaua.events.platform;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.kaua.events.platform.infrastructure.configurations.WebServerConfig;
import com.kaua.events.platform.infrastructure.gateways.EfiPaymentGateway;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

@ActiveProfiles("test-integration")
@AutoConfigureWireMock(port = 0)
@SpringBootTest(classes = {WebServerConfig.class})
@Tag("integrationTest")
public class AbstractRestClientTest {

    protected static final String PAYMENTS = EfiPaymentGateway.NAMESPACE_NAME;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private BulkheadRegistry bulkheadRegistry;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @BeforeEach
    void beforeEach() {
        WireMock.reset();
        WireMock.resetAllRequests();
        List.of(PAYMENTS).forEach(this::resetFaultTolerance);
    }

    protected void checkCircuitBreakerState(final String name, final CircuitBreaker.State expectedState) {
        final var cb = circuitBreakerRegistry.circuitBreaker(name);
        Assertions.assertEquals(expectedState, cb.getState());
    }

    protected void transitionToOpenState(final String name) {
        circuitBreakerRegistry.circuitBreaker(name).transitionToOpenState();
    }

    protected void transitionToClosedState(final String name) {
        circuitBreakerRegistry.circuitBreaker(name).transitionToClosedState();
    }

    protected void acquireBulkheadPermission(final String name) {
        bulkheadRegistry.bulkhead(name).acquirePermission();
    }

    protected void releaseBulkheadPermission(final String name) {
        bulkheadRegistry.bulkhead(name).releasePermission();
    }

    protected String writeValueAsString(final Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void resetFaultTolerance(final String name) {
        circuitBreakerRegistry.circuitBreaker(name).reset();
    }
}
