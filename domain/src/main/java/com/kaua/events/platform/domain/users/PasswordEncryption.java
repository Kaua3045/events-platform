package com.kaua.events.platform.domain.users;

public interface PasswordEncryption {

    String encrypt(String rawPassword);

    boolean matches(String rawPassword, String encryptedPassword);
}
