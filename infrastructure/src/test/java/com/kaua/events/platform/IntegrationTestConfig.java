package com.kaua.events.platform;

import com.kaua.events.platform.domain.utils.InstantUtils;
import com.kaua.events.platform.infrastructure.services.rsakey.RsaKeyProvider;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

@Configuration(proxyBeanMethods = false)
public class IntegrationTestConfig {

    @Bean
    public RsaKeyProvider rsaKeyProviderTest() {
        return new InMemoryRsaKeyProvider();
    }

    @Bean
    public BuildProperties buildProperties() {
        Properties properties = new Properties();
        properties.setProperty("name", "events-platform");
        properties.setProperty("version", "0.0.1");
        properties.setProperty("time", InstantUtils.now().toString());
        return new BuildProperties(properties);
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
