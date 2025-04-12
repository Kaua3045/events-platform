package com.kaua.events.platform.infrastructure.configurations.logback;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import com.kaua.events.platform.infrastructure.configurations.authentication.AuthenticatedUser;
import com.kaua.events.platform.infrastructure.configurations.authentication.UserAuthentication;
import com.kaua.events.platform.infrastructure.oauth.OAuth2ClientAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class UserLogConverter extends ClassicConverter {

    @Override
    public String convert(final ILoggingEvent event) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return "anonymous"; // anonymous user
        }

        if (authentication instanceof OAuth2ClientAuthenticationToken oauth2ClientAuthenticationToken) {
            return (String) oauth2ClientAuthenticationToken.getPrincipal();
        }

        if (!(authentication instanceof UserAuthentication aUser)) {
            return "anonymous"; // not authenticated
        }

        final var aAuthenticatedUser = (AuthenticatedUser) aUser.getPrincipal();
        return aAuthenticatedUser.id();
    }
}
