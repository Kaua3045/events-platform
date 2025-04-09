package com.kaua.events.platform.infrastructure.utils;

import com.kaua.events.platform.domain.UnitTest;
import com.kaua.events.platform.infrastructure.constants.Constants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;

class PemFileUtilsTest extends UnitTest {

    @TempDir
    private Path tempDir;

    private Path keyPath;
    private Path privatePath;
    private Path publicPath;

    @BeforeEach
    void setUp() {
        keyPath = tempDir.resolve("testkey" + Constants.RSA_KEY_PRIVATE_FILE);
        privatePath = tempDir.resolve("testkey" + Constants.RSA_KEY_PRIVATE_FILE);
        publicPath = tempDir.resolve("testkey" + Constants.RSA_KEY_PUBLIC_FILE);
    }

    @Test
    void shouldGenerateKeysIfNotExists() throws Exception {
        Files.deleteIfExists(privatePath);
        Files.deleteIfExists(publicPath);

        RsaKeyPair pair = PemFileUtils.readOrCreateKeyPair(keyPath);

        Assertions.assertNotNull(pair);
        Assertions.assertTrue(Files.exists(privatePath));
        Assertions.assertTrue(Files.exists(publicPath));
    }

    @Test
    void shouldGenerateKeysIfBothFilesAreMissing() throws IOException {
        Files.deleteIfExists(privatePath);
        Files.deleteIfExists(publicPath);

        RsaKeyPair pair = PemFileUtils.readOrCreateKeyPair(keyPath);
        Assertions.assertNotNull(pair);
    }

    @Test
    void shouldGenerateKeysIfPrivateFileIsMissingOnly() throws Exception {
        Files.deleteIfExists(privatePath);
        Files.writeString(publicPath, generateValidPublicPem());

        RsaKeyPair pair = PemFileUtils.readOrCreateKeyPair(keyPath);
        Assertions.assertNotNull(pair);
    }

    @Test
    void shouldGenerateKeysIfPublicFileIsMissingOnly() throws Exception {
        Files.writeString(privatePath, generateValidPrivatePem());
        Files.deleteIfExists(publicPath);

        RsaKeyPair pair = PemFileUtils.readOrCreateKeyPair(keyPath);
        Assertions.assertNotNull(pair);
    }

    @Test
    void shouldReadKeysIfBothFilesExist() throws Exception {
        Files.writeString(privatePath, generateValidPrivatePem());
        Files.writeString(publicPath, generateValidPublicPem());

        RsaKeyPair pair = PemFileUtils.readOrCreateKeyPair(keyPath);
        Assertions.assertNotNull(pair);
    }

    @Test
    void shouldReadKeysIfExists() throws Exception {
        generateAndWriteKeyPair(privatePath, publicPath);

        RsaKeyPair pair = PemFileUtils.readOrCreateKeyPair(keyPath);

        Assertions.assertNotNull(pair);
        Assertions.assertTrue(Files.exists(privatePath));
        Assertions.assertTrue(Files.exists(publicPath));
    }

    @Test
    void shouldThrowExceptionWhenPrivateFileIsInvalid() throws Exception {
        Files.writeString(privatePath, "invalid private key");
        Files.writeString(publicPath, generateValidPublicPem());

        UncheckedIOException exception = Assertions.assertThrows(UncheckedIOException.class, () -> {
            PemFileUtils.readOrCreateKeyPair(keyPath);
        });

        Assertions.assertTrue(exception.getMessage().contains("Failed to load/create RSA key pair"));
    }

    @Test
    void shouldThrowExceptionWhenPublicFileIsInvalid() throws Exception {
        Files.writeString(privatePath, generateValidPrivatePem());
        Files.writeString(publicPath, "invalid public key");

        UncheckedIOException exception = Assertions.assertThrows(UncheckedIOException.class, () -> {
            PemFileUtils.readOrCreateKeyPair(keyPath);
        });

        Assertions.assertTrue(exception.getMessage().contains("Failed to load/create RSA key pair"));
    }

    @Test
    void shouldThrowExceptionIfWriteFails() {
        Path invalidPath = Paths.get("/invalid/testkey" + Constants.RSA_KEY_PRIVATE_FILE);

        UncheckedIOException exception = Assertions.assertThrows(UncheckedIOException.class, () -> {
            PemFileUtils.readOrCreateKeyPair(invalidPath);
        });

        Assertions.assertTrue(exception.getMessage().contains("Failed to load/create RSA key pair"));
    }

    // Helpers

    private static void generateAndWriteKeyPair(Path privatePath, Path publicPath) throws Exception {
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(2048);
        KeyPair pair = gen.generateKeyPair();

        writePem(privatePath, "PRIVATE KEY", pair.getPrivate().getEncoded());
        writePem(publicPath, "PUBLIC KEY", pair.getPublic().getEncoded());
    }

    private static void writePem(Path path, String type, byte[] encoded) throws IOException {
        String base64 = Base64.getEncoder().encodeToString(encoded);
        String pem = "-----BEGIN " + type + "-----\n" +
                base64 + "\n" +
                "-----END " + type + "-----\n";

        Files.writeString(path, pem, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    private static String generateValidPrivatePem() throws Exception {
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(2048);
        KeyPair pair = gen.generateKeyPair();

        return encodeToPem("PRIVATE KEY", pair.getPrivate().getEncoded());
    }

    private static String generateValidPublicPem() throws Exception {
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(2048);
        KeyPair pair = gen.generateKeyPair();

        return encodeToPem("PUBLIC KEY", pair.getPublic().getEncoded());
    }

    private static String encodeToPem(String type, byte[] encoded) {
        String base64 = Base64.getEncoder().encodeToString(encoded);
        return "-----BEGIN " + type + "-----\n" +
                base64 + "\n" +
                "-----END " + type + "-----\n";
    }
}
