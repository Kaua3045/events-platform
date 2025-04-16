package com.kaua.events.platform.infrastructure.configurations.authentication;

public sealed interface AuthenticatedPrincipal permits AuthenticatedService, AuthenticatedUser {

    String id();
}
