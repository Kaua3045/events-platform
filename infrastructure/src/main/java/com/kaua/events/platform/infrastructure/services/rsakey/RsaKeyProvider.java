package com.kaua.events.platform.infrastructure.services.rsakey;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

public interface RsaKeyProvider {

    RSAPrivateKey getPrivateKey(String keyName);
    RSAPublicKey getPublicKey(String keyName);
}
