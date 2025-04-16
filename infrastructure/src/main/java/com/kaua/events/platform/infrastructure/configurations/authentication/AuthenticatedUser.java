package com.kaua.events.platform.infrastructure.configurations.authentication;

public record AuthenticatedUser(String id) implements AuthenticatedPrincipal {
}
