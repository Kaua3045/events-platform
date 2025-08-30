package com.kaua.events.platform.infrastructure.gateways.helpers;

import com.fasterxml.jackson.databind.JsonNode;
import com.kaua.events.platform.domain.exceptions.*;
import com.kaua.events.platform.domain.utils.Generated;
import com.kaua.events.platform.domain.validation.Error;
import com.kaua.events.platform.infrastructure.configurations.json.Json;
import com.kaua.events.platform.infrastructure.exceptions.ConflictException;
import com.kaua.events.platform.infrastructure.exceptions.TooManyRequestsException;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.TextMapSetter;
import org.slf4j.Logger;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Generated
public interface HttpClientHandlers {

    String namespace();

    Logger logger();

    Tracer tracer();

    default Function<ClientResponse, Mono<? extends Throwable>> notFoundHandler(final String id) {
        return response -> {
            final var span = startSpan("http.not_found", "resourceId", id, response);
            return Mono.<Throwable>error(
                            NotFoundException.with("Not found observed from %s [resourceId:%s]".formatted(namespace(), id))
                    )
                    .doOnError(ex -> recordException(span, ex))
                    .doFinally(signal -> span.end());
        };
    }

//    default Function<ClientResponse, Mono<? extends Throwable>> forbiddenHandler(final String id, final String... actionParam) {
//        return response -> handleClientErrorWithTracing(response, id, "Forbidden observed",
//                err -> ForbiddenException.with(err.errors().isEmpty() ? err.message() : err.errors().get(0).message()),
//                actionParam);
//    }
//
//    default Function<ClientResponse, Mono<? extends Throwable>> unauthorizedHandler(final String id, final String... actionParam) {
//        return response -> handleClientErrorWithTracing(response, id, "Unauthorized observed",
//                err -> UnauthorizedException.with(err.errors().isEmpty() ? err.message() : err.errors().get(0).message()),
//                actionParam);
//    }

    default Function<ClientResponse, Mono<? extends Throwable>> conflictHandler(final String id, final String... actionParam) {
        return response -> handleClientErrorWithTracing(response, id, "Conflict observed",
                err -> err.errors().isEmpty() ? ConflictException.with(err.message()) : ConflictException.with(err.errors()),
                actionParam);
    }

    default Function<ClientResponse, Mono<? extends Throwable>> badRequestHandler(final String id, final String... actionParam) {
        return response -> handleClientErrorWithTracing(response, id, "Bad request observed",
                err -> err.errors().isEmpty() ? ValidationException.with(err.message()) : ValidationException.with(err.errors()),
                actionParam);
    }

    default Function<ClientResponse, Mono<? extends Throwable>> unprocessableEntityHandler(final String id, final String... actionParam) {
        return response -> handleClientErrorWithTracing(response, id, "Unprocessable entity observed",
                err -> err.errors().isEmpty() ? DomainException.with(err.message()) : DomainException.with(err.errors()),
                actionParam);
    }

    default Function<ClientResponse, Mono<? extends Throwable>> tooManyRequestsHandler(final String id, final String... actionParam) {
        return response -> handleClientErrorWithTracing(response, id, "Too many requests observed",
                err -> err.errors().isEmpty() ? TooManyRequestsException.with(err.message()) : TooManyRequestsException.with(err.errors()),
                actionParam);
    }

    default Function<ClientResponse, Mono<? extends Throwable>> a5xxHandler(final String id, final String... actionParam) {
        return response -> {
            final var span = startSpan("http.5xx", "resourceId", id, response);
            final var method = response.request().getMethod().name();
            final var status = response.statusCode().value();
            final var action = (actionParam != null && actionParam.length > 0) ? actionParam[0] : null;

            return response.bodyToMono(String.class)
                    .flatMap(body -> {
                        final var message = action != null
                                ? "Error observed during %s from %s [method:%s] [resourceId:%s] [status:%s] [response:%s]"
                                .formatted(action, namespace(), method, id, status, body)
                                : "Error observed from %s [method:%s] [resourceId:%s] [status:%s] [response:%s]"
                                .formatted(namespace(), method, id, status, body);

                        logger().error(message);
                        final var ex = InternalErrorException.with(message);
                        recordException(span, ex);
                        return Mono.error(ex);
                    })
                    .switchIfEmpty(Mono.defer(() -> handleErrorWithoutResponse(
                            "Error observed", method, id,
                            er -> InternalErrorException.with(er.message()),
                            actionParam)))
                    .doOnError(ex -> recordException(span, ex))
                    .doFinally(signal -> span.end())
                    .cast(Throwable.class);
        };
    }

    private <E extends NoStackTraceException> Mono<? extends Throwable> handleClientErrorWithTracing(
            final ClientResponse response,
            final String id,
            final String errorType,
            final Function<ErrorResponse, E> exceptionSupplier,
            final String... actionParam
    ) {
        final var span = startSpan("http.error", "resourceId", id, response);
        return handleClientError(response, id, errorType, exceptionSupplier, actionParam)
                .doOnError(ex -> recordException(span, ex))
                .doFinally(signal -> span.end());
    }

    private Span startSpan(String name, String key, String value, ClientResponse response) {
        final var span = tracer().spanBuilder(name).startSpan();
        try (Scope ignored = span.makeCurrent()) {
            span.setAttribute("http.namespace", namespace());
            span.setAttribute(key, value);
            if (response.request() != null && response.request().getMethod() != null) {
                span.setAttribute("http.method", response.request().getMethod().name());
            }
            if (response.statusCode() != null) {
                span.setAttribute("http.status", response.statusCode().value());
            }
        }
        return span;
    }

    private void recordException(Span span, Throwable ex) {
        span.recordException(ex);
        span.setStatus(StatusCode.ERROR, ex.getMessage() == null ? "error" : ex.getMessage());
    }

    private <E extends NoStackTraceException> Mono<? extends Throwable> handleClientError(
            final ClientResponse response,
            final String id,
            final String errorType,
            final Function<ErrorResponse, E> exceptionSupplier,
            final String... actionParam
    ) {
        final var method = response.request().getMethod().name();

        return response.bodyToMono(String.class)
                .flatMap(body -> {
                    final var parsed = Optional.ofNullable(convertToErrorResponse(body))
                            .orElse(new ErrorResponse("%s from %s"
                                    .formatted(errorType, namespace()), List.of(new Error(body))));

                    final var message = createBodyErrorMessage(errorType, id, method, parsed, actionParam);

                    logger().warn(message);
                    return Mono.error(exceptionSupplier.apply(parsed));
                })
                .switchIfEmpty(Mono.defer(() ->
                        handleErrorWithoutResponse(errorType, method, id, exceptionSupplier, actionParam)))
                .cast(Throwable.class);
    }

    private ErrorResponse convertToErrorResponse(final String body) {
        try {
            final JsonNode node = Json.mapper().readTree(body);

            // First format: { "nome": "chave_invalida", "mensagem": "..." }
            if (node.has("nome") && node.has("mensagem")) {
                final var error = new Error(node.get("nome").asText(), node.get("mensagem").asText());
                return new ErrorResponse(node.get("mensagem").asText(), List.of(error));
            }

            // Second format: { "message": "...", "errors": [{message, property}] }
            if (node.has("message") && node.has("errors")) {
                List<Error> errors = Json.mapper().convertValue(
                        node.get("errors"),
                        Json.mapper().getTypeFactory().constructCollectionType(List.class, Error.class)
                );
                return new ErrorResponse(node.get("message").asText(), errors);
            }

            return null;

        } catch (final Exception e) {
            return null;
        }
    }

    private String createBodyErrorMessage(
            final String errorType,
            final String id,
            final String method,
            final ErrorResponse response,
            final String... actionParam
    ) {
        final String action = (actionParam != null && actionParam.length > 0) ? actionParam[0] : null;

        return (action != null)
                ? "%s during %s from %s [method:%s] [resourceId:%s] [response:%s]"
                .formatted(errorType, action, namespace(), method, id, response)
                : "%s from %s [method:%s] [resourceId:%s] [response:%s]"
                .formatted(errorType, namespace(), method, id, response);
    }

    private <E extends NoStackTraceException> Mono<? extends Throwable> handleErrorWithoutResponse(
            final String errorType,
            final String method,
            final String id,
            final Function<ErrorResponse, E> exceptionSupplier,
            final String... actionParam
    ) {
        final String action = (actionParam != null && actionParam.length > 0) ? actionParam[0] : null;

        final String message = (action != null)
                ? "%s during %s from %s [method:%s] [resourceId:%s]"
                .formatted(errorType, action, namespace(), method, id)
                : "%s from %s [method:%s] [resourceId:%s]"
                .formatted(errorType, namespace(), method, id);

        logger().warn(message);
        return Mono.error(exceptionSupplier.apply(new ErrorResponse(message, List.of())));
    }

    record ErrorResponse(String message, List<Error> errors) {
        @Override
        public String toString() {
            return "ErrorResponse{message='%s', errors=%s}".formatted(message, errors);
        }
    }

    TextMapSetter<ClientRequest.Builder> setter =
            (carrier, key, value) -> {
                assert carrier != null;
                carrier.header(key, value);
            };

    default WebClient instrument(WebClient baseClient) {
        return baseClient.mutate()
                .filter((request, next) -> {
                    ClientRequest.Builder builder = ClientRequest.from(request);

                    GlobalOpenTelemetry.getPropagators()
                            .getTextMapPropagator()
                            .inject(Context.current(), builder, setter);

                    return next.exchange(builder.build());
                })
                .build();
    }
}
