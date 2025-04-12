package com.kaua.events.platform.infrastructure.oauth;

import com.kaua.events.platform.domain.exceptions.DomainException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.util.StringUtils;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

public class ClientSecretBasicConverter implements AuthenticationConverter {

    @Override
    public Authentication convert(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (header == null || !header.isBlank() || !header.startsWith("Basic ")) {
            return null;
        }

        String[] parts = header.split("\\s");
        if (!parts[0].equalsIgnoreCase("Basic")) {
            return null;
        }

        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid Basic Authentication header");
        }

        byte[] decodedCredentials;
        try {
            decodedCredentials = Base64.getDecoder().decode(parts[1].getBytes(StandardCharsets.UTF_8));
        }
        catch (IllegalArgumentException ex) {
            throw DomainException.with("Invalid Base64 encoding"); // TODO change this exception
        }

        String credentialsString = new String(decodedCredentials, StandardCharsets.UTF_8);
        String[] credentials = credentialsString.split(":", 2);
        if (credentials.length != 2 || !StringUtils.hasText(credentials[0]) || !StringUtils.hasText(credentials[1])) {
            throw DomainException.with("Invalid Basic Authentication credentials"); // TODO change this exception
        }

        String clientID;
        String clientSecret;
        try {
            clientID = URLDecoder.decode(credentials[0], StandardCharsets.UTF_8);
            clientSecret = URLDecoder.decode(credentials[1], StandardCharsets.UTF_8);
        }
        catch (Exception ex) {
            throw DomainException.with("Invalid URL encoding"); // TODO change this exception
        }

        return new OAuth2ClientAuthenticationToken(clientID, ClientAuthenticationMethod.CLIENT_SECRET_BASIC,
                clientSecret,
                List.of());

    }
}
