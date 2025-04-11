package com.kaua.events.platform.infrastructure.services.encryption;

import com.kaua.events.platform.domain.users.PasswordEncryption;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class BcryptPasswordEncryption implements PasswordEncryption {

    private final PasswordEncoder passwordEncoder;

    public BcryptPasswordEncryption(final PasswordEncoder passwordEncoder) {
        this.passwordEncoder = Objects.requireNonNull(passwordEncoder);
    }

    @Override
    public String encrypt(final String rawPassword) {
        return this.passwordEncoder.encode(rawPassword);
    }

    @Override
    public boolean matches(final String rawPassword, final String encryptedPassword) {
        return this.passwordEncoder.matches(rawPassword, encryptedPassword);
    }
}
