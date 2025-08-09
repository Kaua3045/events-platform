package com.kaua.events.platform.infrastructure.configurations.interceptors;

import com.kaua.events.platform.infrastructure.configurations.authentication.AuthenticatedService;
import com.kaua.events.platform.infrastructure.configurations.authentication.AuthenticatedUser;
import com.kaua.events.platform.infrastructure.configurations.authentication.ServiceAuthentication;
import com.kaua.events.platform.infrastructure.configurations.authentication.UserAuthentication;
import com.kaua.events.platform.infrastructure.oauth.OAuth2ClientAuthenticationToken;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Context;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.boot.info.BuildProperties;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Objects;
import java.util.Optional;

@Component
public class RequestMdcInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(RequestMdcInterceptor.class);

    private final BuildProperties buildProperties;

    public RequestMdcInterceptor(final BuildProperties buildProperties) {
        this.buildProperties = Objects.requireNonNull(buildProperties);
    }

    @Override
    public boolean preHandle(
            final HttpServletRequest request,
            @NonNull final HttpServletResponse response,
            @NonNull final Object handler
    ) {
        final var aCurrentSpan = Span.fromContext(Context.current());

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        MDC.put("appName", buildProperties.getName());
        MDC.put("appVersion", buildProperties.getVersion());
        MDC.put("appBuildDate", buildProperties.getTime().toString());
        MDC.put("traceId", aCurrentSpan.getSpanContext().isValid() ? aCurrentSpan.getSpanContext().getTraceId() : " ");
        MDC.put("spanId", aCurrentSpan.getSpanContext().isValid() ? aCurrentSpan.getSpanContext().getSpanId() : " ");
        MDC.put("host", request.getHeader("Host"));
        MDC.put("requestMethod", request.getMethod());
        MDC.put("userAgent", request.getHeader("User-Agent"));
        MDC.put("requestUri", request.getRequestURI());
        MDC.put("clientIp", request.getRemoteAddr());

        final var aTraceparent = Optional.ofNullable(request.getHeader("traceparent"))
                .orElse(" ");
        final var aTracestate = Optional.ofNullable(request.getHeader("tracestate"))
                .orElse(" ");

        MDC.put("traceparent", aTraceparent);
        MDC.put("tracestate", aTracestate);

        if (authentication == null || !authentication.isAuthenticated()) {
            MDC.put("user", "anonymous");
        } else {
            String userId = switch (authentication) {
                case OAuth2ClientAuthenticationToken oauth2ClientAuthenticationToken ->
                        (String) oauth2ClientAuthenticationToken.getPrincipal();
                case ServiceAuthentication serviceAuthentication
                        when serviceAuthentication.getPrincipal() instanceof AuthenticatedService authenticatedService ->
                        authenticatedService.id();
                case UserAuthentication userAuthentication
                        when userAuthentication.getPrincipal() instanceof AuthenticatedUser aUser -> aUser.id();
                default -> "anonymous";
            };

            MDC.put("user", userId);
        }

        log.debug("Request: {}", MDC.getCopyOfContextMap());

        return true;
    }
}
