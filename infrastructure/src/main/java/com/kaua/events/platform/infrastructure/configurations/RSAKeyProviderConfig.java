package com.kaua.events.platform.infrastructure.configurations;

import com.kaua.events.platform.infrastructure.services.rsakey.FileRsaKeyProvider;
import com.kaua.events.platform.infrastructure.services.rsakey.RsaKeyProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.nio.file.Path;

@Configuration(proxyBeanMethods = false)
public class RSAKeyProviderConfig {

    @Profile("!test-integration")
    @Bean
    public RsaKeyProvider rsaKeyProvider(@Value("${application.rsa.key.path}") Path keyPath) {
        return new FileRsaKeyProvider(keyPath);
    }
}
