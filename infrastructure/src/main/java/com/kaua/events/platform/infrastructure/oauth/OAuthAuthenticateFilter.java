package com.kaua.events.platform.infrastructure.oauth;

import com.kaua.events.platform.infrastructure.configurations.properties.OAuthClients;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.stream.Collectors;

public class OAuthAuthenticateFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(OAuthAuthenticateFilter.class);

    private final OAuthClients oAuthClients;

    public OAuthAuthenticateFilter(
            final OAuthClients oAuthClients
    ) {
        this.oAuthClients = Objects.requireNonNull(oAuthClients);
    }

    @Override
    protected void doFilterInternal(
            final HttpServletRequest request,
            @NonNull final HttpServletResponse response,
            @NonNull final FilterChain filterChain
    ) throws ServletException, IOException {
        log.debug("OAuthAuthenticateFilter [request:{}]", request.getRequestURI());
        String aAuthHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (aAuthHeader != null && aAuthHeader.startsWith("Basic ")) {
            String aBase64Credentials = aAuthHeader.substring("Basic ".length()).trim();

            try {
                byte[] aDecoded = java.util.Base64.getDecoder().decode(aBase64Credentials);
                String aCredentials = new String(aDecoded, StandardCharsets.UTF_8);

                String[] aParts = aCredentials.split(":", 2);
                if (aParts.length == 2) {
                    String aClientId = aParts[0];
                    String aClientSecret = aParts[1];

                    log.debug("OAuthAuthenticateFilter [clientId:{}] authentication in progress", aClientId);

                    var aOAuthClient = this.oAuthClients.getClients().get(aClientId);

                    if (aOAuthClient == null || !aClientId.equalsIgnoreCase(aOAuthClient.clientId())) {
                        log.debug("OAuthAuthenticateFilter [clientId:{}] not found", aClientId);
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid client credentials");
                        return;
                    }

                    if (!aClientSecret.equals(aOAuthClient.clientSecret())) {
                        log.debug("OAuthAuthenticateFilter [clientId:{}] invalid secret", aClientId);
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid client credentials");
                        return;
                    }

                    final var authentication = new OAuth2ClientAuthenticationToken(
                            aClientId,
                            ClientAuthenticationMethod.CLIENT_SECRET_BASIC,
                            aClientSecret,
                            aOAuthClient.authorities().stream()
                                    .map(SimpleGrantedAuthority::new)
                                    .collect(Collectors.toSet())
                    );
                    authentication.setAuthenticated(true);

                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    log.debug("OAuthAuthenticateFilter [clientId:{}] authenticated", aClientId);
                }
            } catch (Exception ex) {
                log.error("OAuthAuthenticateFilter error decoding credentials", ex);
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid client credentials");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
