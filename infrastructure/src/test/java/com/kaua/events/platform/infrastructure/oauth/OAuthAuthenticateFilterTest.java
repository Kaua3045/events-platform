package com.kaua.events.platform.infrastructure.oauth;

import com.kaua.events.platform.IntegrationTest;
import com.kaua.events.platform.infrastructure.configurations.properties.OAuthClients;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

@IntegrationTest
class OAuthAuthenticateFilterTest {

    @Autowired
    private OAuthClients oAuthClients;

    @Test
    void givenAValidClientIdAndClientSecret_whenCallDoFilter_thenAuthenticate() throws ServletException, IOException {
        final var aRequest = mock(HttpServletRequest.class);
        final var aResponse = mock(HttpServletResponse.class);
        final var aFilterChain = mock(FilterChain.class);

        final var aHandlerExceptionResolver = Mockito.spy(HandlerExceptionResolver.class);

        final var aFilter = new OAuthAuthenticateFilter(oAuthClients, aHandlerExceptionResolver);

        Mockito.when(aRequest.getRequestURI())
                .thenReturn("/any/path");
        Mockito.when(aRequest.getHeader(HttpHeaders.AUTHORIZATION))
                .thenReturn("Basic ZGVmYXVsdDpkZWZhdWx0");

        aFilter.doFilterInternal(
                aRequest,
                aResponse,
                aFilterChain
        );

        Assertions.assertTrue(SecurityContextHolder.getContext().getAuthentication().isAuthenticated());
        Assertions.assertEquals(
                "default",
                SecurityContextHolder.getContext().getAuthentication().getPrincipal()
        );
        Assertions.assertEquals("default", SecurityContextHolder.getContext().getAuthentication().getCredentials());
        Assertions.assertEquals(ClientAuthenticationMethod.CLIENT_SECRET_BASIC,
                ((OAuth2ClientAuthenticationToken) SecurityContextHolder.getContext().getAuthentication()).getClientAuthenticationMethod()
        );
    }

    @Test
    void givenAnNonExistsAuthorizationHeaderInRequest_whenCallDoFilter_thenDoNothing() throws ServletException, IOException {
        final var aRequest = mock(HttpServletRequest.class);
        final var aResponse = mock(HttpServletResponse.class);
        final var aFilterChain = mock(FilterChain.class);

        final var aHandlerExceptionResolver = Mockito.spy(HandlerExceptionResolver.class);

        final var aFilter = new OAuthAuthenticateFilter(oAuthClients, aHandlerExceptionResolver);

        Mockito.when(aRequest.getRequestURI())
                .thenReturn("/any/path");
        Mockito.when(aRequest.getHeader(HttpHeaders.AUTHORIZATION))
                .thenReturn(null);

        aFilter.doFilterInternal(
                aRequest,
                aResponse,
                aFilterChain
        );

        Assertions.assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void givenAnInvalidAuthorizationHeaderInRequest_whenCallDoFilter_thenDoNothing() throws ServletException, IOException {
        final var aRequest = mock(HttpServletRequest.class);
        final var aResponse = mock(HttpServletResponse.class);
        final var aFilterChain = mock(FilterChain.class);

        final var aHandlerExceptionResolver = Mockito.spy(HandlerExceptionResolver.class);

        final var aFilter = new OAuthAuthenticateFilter(oAuthClients, aHandlerExceptionResolver);

        Mockito.when(aRequest.getRequestURI())
                .thenReturn("/any/path");
        Mockito.when(aRequest.getHeader(HttpHeaders.AUTHORIZATION))
                .thenReturn("ZGVmYXVsdDpkZWZhdWx0");

        aFilter.doFilterInternal(
                aRequest,
                aResponse,
                aFilterChain
        );

        Assertions.assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void givenAnInvalidNullClientIdInRequest_whenCallDoFilter_thenReturnError() throws IOException, ServletException {
        final var aRequest = mock(HttpServletRequest.class);
        final var aResponse = mock(HttpServletResponse.class);
        final var aFilterChain = mock(FilterChain.class);

        final var aHandlerExceptionResolver = Mockito.spy(HandlerExceptionResolver.class);

        final var aFilter = new OAuthAuthenticateFilter(oAuthClients, aHandlerExceptionResolver);

        Mockito.when(aRequest.getRequestURI())
                .thenReturn("/any/path");
        Mockito.when(aRequest.getHeader(HttpHeaders.AUTHORIZATION))
                .thenReturn("Basic Y2xpbmV0OmRlZmF1bHQ=");

        aFilter.doFilterInternal(
                aRequest,
                aResponse,
                aFilterChain
        );

        Mockito.verify(aHandlerExceptionResolver, times(1)).resolveException(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    void givenAnInvalidClientSecretDoestNotMatch_whenCallDoFilter_thenReturnError() throws ServletException, IOException {
        final var aRequest = mock(HttpServletRequest.class);
        final var aResponse = mock(HttpServletResponse.class);
        final var aFilterChain = mock(FilterChain.class);

        final var aHandlerExceptionResolver = Mockito.spy(HandlerExceptionResolver.class);

        final var aFilter = new OAuthAuthenticateFilter(oAuthClients, aHandlerExceptionResolver);

        Mockito.when(aRequest.getRequestURI())
                .thenReturn("/any/path");
        Mockito.when(aRequest.getHeader(HttpHeaders.AUTHORIZATION))
                .thenReturn("Basic ZGVmYXVsdDpzZWNyZXQ=");

        aFilter.doFilterInternal(
                aRequest,
                aResponse,
                aFilterChain
        );

        Mockito.verify(aHandlerExceptionResolver, times(1)).resolveException(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    void givenAnInvalidBase64_whenCallDoFilter_thenReturnError() throws Exception {
        final var aRequest = mock(HttpServletRequest.class);
        final var aResponse = mock(HttpServletResponse.class);
        final var aFilterChain = mock(FilterChain.class);

        final var aHandlerExceptionResolver = Mockito.spy(HandlerExceptionResolver.class);

        final var aFilter = new OAuthAuthenticateFilter(oAuthClients, aHandlerExceptionResolver);

        Mockito.when(aRequest.getRequestURI())
                .thenReturn("/any/path");
        Mockito.when(aRequest.getHeader(HttpHeaders.AUTHORIZATION))
                .thenReturn("Basic invalidbase64");

        aFilter.doFilterInternal(
                aRequest,
                aResponse,
                aFilterChain
        );

        Mockito.verify(aHandlerExceptionResolver, times(1)).resolveException(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
        Mockito.verify(aFilterChain, Mockito.never()).doFilter(Mockito.any(), Mockito.any());
    }

    @Test
    void givenInvalidBase64CredentialsWithoutColon_whenCallDoFilter_thenReturnError() throws ServletException, IOException {
        final var aRequest = mock(HttpServletRequest.class);
        final var aResponse = mock(HttpServletResponse.class);
        final var aFilterChain = mock(FilterChain.class);

        final var aHandlerExceptionResolver = Mockito.spy(HandlerExceptionResolver.class);

        final var aFilter = new OAuthAuthenticateFilter(oAuthClients, aHandlerExceptionResolver);

        Mockito.when(aRequest.getRequestURI())
                .thenReturn("/any/path");
        // "invalid" -> Base64 encoded = "aW52YWxpZA=="
        Mockito.when(aRequest.getHeader(HttpHeaders.AUTHORIZATION))
                .thenReturn("Basic aW52YWxpZA==");

        aFilter.doFilterInternal(
                aRequest,
                aResponse,
                aFilterChain
        );

        Assertions.assertNull(SecurityContextHolder.getContext().getAuthentication());

        Mockito.verify(aFilterChain, times(1)).doFilter(aRequest, aResponse);
    }
}
