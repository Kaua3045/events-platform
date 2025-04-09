package com.kaua.events.platform.infrastructure.utils;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

public record RsaKeyPair(RSAPrivateKey privateKey, RSAPublicKey publicKey) {
}
