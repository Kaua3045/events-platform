package com.kaua.events.platform.infrastructure.configurations.efi;

import com.kaua.events.platform.domain.exceptions.InternalErrorException;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

import javax.net.ssl.KeyManagerFactory;
import java.io.ByteArrayInputStream;
import java.security.KeyStore;

public class PixSslContextFactory {

    public static SslContext buildClientSslContext(final byte[] p12Bytes, final String p12Password) {
        try {
            KeyStore ks = KeyStore.getInstance("PKCS12");
            ks.load(new ByteArrayInputStream(p12Bytes), p12Password.toCharArray());

            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(ks, p12Password.toCharArray());

            return SslContextBuilder.forClient()
                    .keyManager(kmf)
                    .trustManager(InsecureTrustManagerFactory.INSTANCE) // TODO for production change this
                    .build();
        } catch (Exception e) {
            throw InternalErrorException.with("Error on build SslContext for mTLS", e);
        }
    }
}
