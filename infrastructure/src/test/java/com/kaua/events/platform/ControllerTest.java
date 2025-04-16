package com.kaua.events.platform;

import com.kaua.events.platform.infrastructure.configurations.OtelConfig;
import com.kaua.events.platform.infrastructure.configurations.SecurityConfig;
import com.kaua.events.platform.infrastructure.configurations.properties.CorsProperties;
import com.kaua.events.platform.infrastructure.configurations.properties.OAuthClients;
import com.kaua.events.platform.infrastructure.idempotency.gateways.InMemoryIdempotencyKeyGateway;
import com.kaua.events.platform.infrastructure.utils.ObservationHelper;
import org.junit.jupiter.api.Tag;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@ActiveProfiles("test-integration")
@WebMvcTest
@TestPropertySource(properties = "application.otel.memory-exporter=true")
@Import({SecurityConfig.class, IntegrationTestConfig.class, OAuthClients.class, OtelConfig.class, CorsProperties.class, InMemoryIdempotencyKeyGateway.class, ObservationHelper.class})
@Tag("integrationTest")
public @interface ControllerTest {

    @AliasFor(annotation = WebMvcTest.class, attribute = "controllers")
    Class<?>[] controllers() default {};
}
