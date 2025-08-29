package com.kaua.events.platform.infrastructure.gateways.helpers;

import com.kaua.events.platform.domain.UnitTest;
import com.kaua.events.platform.domain.exceptions.DomainException;
import com.kaua.events.platform.domain.exceptions.InternalErrorException;
import com.kaua.events.platform.domain.exceptions.NotFoundException;
import com.kaua.events.platform.domain.exceptions.ValidationException;
import com.kaua.events.platform.infrastructure.exceptions.ConflictException;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HttpClientHandlersTest extends UnitTest {

    HttpClientHandlers handlers;
    Logger logger;

    @BeforeEach
    void setup() {
        logger = mock(Logger.class);

        Tracer tracer = mock(Tracer.class);

        SpanBuilder spanBuilder = mock(SpanBuilder.class);

        // Mock do Span
        Span span = mock(Span.class);

        // Mock do Scope (try-with-resources)
        Scope scope = mock(io.opentelemetry.context.Scope.class);

        // Encadeamento dos mocks
        when(tracer.spanBuilder(Mockito.anyString())).thenReturn(spanBuilder);
        when(spanBuilder.startSpan()).thenReturn(span);
        when(span.makeCurrent()).thenReturn(scope);

        // Handlers reais com mocks
        handlers = new HttpClientHandlers() {
            @Override
            public String namespace() {
                return "testNamespace";
            }

            @Override
            public Logger logger() {
                return logger;
            }

            @Override
            public Tracer tracer() {
                return tracer;
            }
        };
    }

    private ClientResponse mockResponse(String body, int status) {
        ClientResponse response = mock(ClientResponse.class);

        // Mock do request
        HttpRequest request = mock(HttpRequest.class);
        when(request.getMethod()).thenReturn(HttpMethod.GET);

        when(response.request()).thenReturn(request);
        when(response.statusCode()).thenReturn(org.springframework.http.HttpStatus.valueOf(status));
        when(response.bodyToMono(String.class)).thenReturn(Mono.just(body));

        return response;
    }

    @Test
    void notFoundHandler_shouldReturnNotFoundException() {
        ClientResponse response = mockResponse("", 404);

        Throwable ex = assertThrows(NotFoundException.class,
                () -> handlers.notFoundHandler("123").apply(response).block());

        assertTrue(ex.getMessage().contains("testNamespace"));
        assertTrue(ex.getMessage().contains("123"));
    }

    @Test
    void conflictHandler_shouldReturnConflictException() {
        String body = """
                {"message": "Conflict error", "errors": [{"message": "already exists", "property": "code"}]}
                """;
        ClientResponse response = mockResponse(body, 409);

        final var ex = assertThrows(ConflictException.class,
                () -> handlers.conflictHandler("abc").apply(response).block());

        assertNotNull(ex.getErrors());
        assertEquals(1, ex.getErrors().size());
        assertEquals("already exists", ex.getErrors().getFirst().message());
        assertEquals("code", ex.getErrors().getFirst().property());
    }

    @Test
    void badRequestHandler_shouldReturnValidationException() {
        String body = """
                {"message": "Bad request", "errors": [{"message": "invalid field", "property": "name"}]}
                """;
        ClientResponse response = mockResponse(body, 400);

        final var ex = assertThrows(ValidationException.class,
                () -> handlers.badRequestHandler("id1").apply(response).block());

        assertNotNull(ex.getErrors());
        assertEquals(1, ex.getErrors().size());
        assertEquals("invalid field", ex.getErrors().getFirst().message());
        assertEquals("name", ex.getErrors().getFirst().property());
    }

    @Test
    void unprocessableEntityHandler_shouldReturnDomainException() {
        String body = """
                {"message": "Validation failed", "errors": [{"message": "invalid", "property": "field"}]}
                """;
        ClientResponse response = mockResponse(body, 422);

        final var ex = assertThrows(DomainException.class,
                () -> handlers.unprocessableEntityHandler("id2").apply(response).block());

        assertNotNull(ex.getErrors());
        assertEquals(1, ex.getErrors().size());
        assertEquals("invalid", ex.getErrors().getFirst().message());
        assertEquals("field", ex.getErrors().getFirst().property());
    }

    @Test
    void a5xxHandler_shouldReturnInternalErrorException() {
        String body = "Server crashed";
        ClientResponse response = mockResponse(body, 500);

        Throwable ex = assertThrows(InternalErrorException.class,
                () -> handlers.a5xxHandler("id5", "action1").apply(response).block());

        assertTrue(ex.getMessage().contains("action1"));
        assertTrue(ex.getMessage().contains("testNamespace"));
    }

    @Test
    void a5xxHandler_withAction_shouldIncludeActionInMessage() {
        String body = "Server crashed";
        ClientResponse response = mockResponse(body, 500);

        Throwable ex = assertThrows(InternalErrorException.class,
                () -> handlers.a5xxHandler("id5", "action1").apply(response).block());

        assertTrue(ex.getMessage().contains("action1"));
        assertTrue(ex.getMessage().contains("testNamespace"));
        assertTrue(ex.getMessage().contains("Server crashed"));
    }

    @Test
    void a5xxHandler_withoutAction_shouldNotIncludeActionInMessage() {
        String body = "Server crashed";
        ClientResponse response = mockResponse(body, 500);

        Throwable ex = assertThrows(InternalErrorException.class,
                () -> handlers.a5xxHandler("id5").apply(response).block());

        assertFalse(ex.getMessage().contains("action1"));
        assertTrue(ex.getMessage().contains("testNamespace"));
        assertTrue(ex.getMessage().contains("Server crashed"));
    }

    @Test
    void conflictHandler_shouldUseMessage_whenErrorsEmpty() {
        String body = """
                {"message": "Conflict occurred", "errors": []}
                """;
        ClientResponse response = mockResponse(body, 409);

        Throwable ex = assertThrows(ConflictException.class,
                () -> handlers.conflictHandler("idConflict").apply(response).block());

        assertTrue(ex.getMessage().contains("Conflict occurred"));
    }

    @Test
    void handleClientError_shouldCallHandleErrorWithoutResponse_whenBodyIsEmpty() {
        ClientResponse response = mock(ClientResponse.class);

        HttpRequest request = mock(HttpRequest.class);
        when(request.getMethod()).thenReturn(HttpMethod.GET);
        when(response.request()).thenReturn(request);
        when(response.statusCode()).thenReturn(org.springframework.http.HttpStatus.valueOf(400));

        when(response.bodyToMono(String.class)).thenReturn(Mono.empty());

        final var ex = assertThrows(DomainException.class,
                () -> handlers.badRequestHandler("emptyBodyId").apply(response).block());

        assertTrue(ex.getMessage().contains("Bad request observed from testNamespace [method:GET] [resourceId:emptyBodyId]"));
    }

    @Test
    void badRequestHandler_shouldReturnValidationException_forUnknownJsonFormat() {
        String body = "{\"foo\":\"bar\"}";
        ClientResponse response = mockResponse(body, 400);

        ValidationException ex = assertThrows(ValidationException.class,
                () -> handlers.badRequestHandler("idUnknown").apply(response).block());

        assertTrue(ex.getMessage().contains("ValidationException"));
        assertEquals("{\"foo\":\"bar\"}", ex.getErrors().getFirst().message());
    }

    @Test
    void badRequestHandler_shouldReturnValidationException_forInvalidJson() {
        String body = "invalid json";
        ClientResponse response = mockResponse(body, 400);

        ValidationException ex = assertThrows(ValidationException.class,
                () -> handlers.badRequestHandler("idInvalid").apply(response).block());

        assertTrue(ex.getMessage().contains("ValidationException"));
        assertEquals(body, ex.getErrors().getFirst().message());
    }
}
