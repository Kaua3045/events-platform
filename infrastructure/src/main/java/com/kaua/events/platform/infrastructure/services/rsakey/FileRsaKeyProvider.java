package com.kaua.events.platform.infrastructure.services.rsakey;

import com.kaua.events.platform.infrastructure.constants.Constants;
import com.kaua.events.platform.infrastructure.utils.PemFileUtils;
import com.kaua.events.platform.infrastructure.utils.RsaKeyPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class FileRsaKeyProvider implements RsaKeyProvider {

    private static final Logger log = LoggerFactory.getLogger(FileRsaKeyProvider.class);

    private final Path basePath;
    private final Map<String, RsaKeyPair> keyPairs = new ConcurrentHashMap<>();

    public FileRsaKeyProvider(final Path basePath) {
        this.basePath = Objects.requireNonNull(basePath);
        createBasePathIfNotExists();
    }

    /*
     * @Param keyName - The name of the key to load.
     * @Return - The private key.
     * @Example - If the key name is "mykey", the private key will be loaded from, does not send this "mykey-public.pem"
     * */
    @Override
    public RSAPrivateKey getPrivateKey(final String keyName) {
        log.debug("Loading private key for: {}", keyName);
        return keyPairs.computeIfAbsent(keyName, name -> {
            Path path = basePath.resolve(name + Constants.RSA_KEY_PRIVATE_FILE);
            log.debug("Loading private key from: {}", path);
            return PemFileUtils.readOrCreateKeyPair(path);
        }).privateKey();
    }

    /*
     * @Param keyName - The name of the key to load.
     * @Return - The public key.
     * @Example - If the key name is "mykey", the public key will be loaded from, does not send this "mykey-public.pem"
     * */
    @Override
    public RSAPublicKey getPublicKey(final String keyName) {
        log.debug("Loading public key for: {}", keyName);
        return keyPairs.computeIfAbsent(keyName, name -> {
            Path path = basePath.resolve(name + Constants.RSA_KEY_PUBLIC_FILE);
            log.debug("Loading public key from: {}", path);
            return PemFileUtils.readOrCreateKeyPair(path);
        }).publicKey();
    }

    private void createBasePathIfNotExists() {
        try {
            if (!Files.exists(basePath)) {
                Files.createDirectories(basePath);
                log.debug("Created missing base path directory: {}", basePath);
            }
        } catch (IOException e) {
            log.error("Could not create base path: {}", basePath, e);
            throw new RuntimeException("Could not create base path: " + basePath, e);
        }
    }
}
