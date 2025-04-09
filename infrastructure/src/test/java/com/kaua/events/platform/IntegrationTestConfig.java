package com.kaua.events.platform;

import com.kaua.events.platform.infrastructure.services.rsakey.RsaKeyProvider;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration(proxyBeanMethods = false)
public class IntegrationTestConfig {

//    @Profile("test")
    @Bean
    public RsaKeyProvider rsaKeyProviderTest() {
        return new InMemoryRsaKeyProvider();
    }

    public static class InMemoryRsaKeyProvider implements RsaKeyProvider {

        private final Map<String, KeyPair> keyPairs = new ConcurrentHashMap<>();

        @Override
        public RSAPrivateKey getPrivateKey(String keyName) {
            return (RSAPrivateKey) getOrCreateKeyPair(keyName).getPrivate();
        }

        @Override
        public RSAPublicKey getPublicKey(String keyName) {
            return (RSAPublicKey) getOrCreateKeyPair(keyName).getPublic();
        }

        private KeyPair getOrCreateKeyPair(String keyName) {
            return keyPairs.computeIfAbsent(keyName, __ -> {
                try {
                    KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
                    generator.initialize(2048);
                    return generator.generateKeyPair();
                } catch (Exception e) {
                    throw new RuntimeException("Failed to generate RSA key pair for test", e);
                }
            });
        }
    }
}
