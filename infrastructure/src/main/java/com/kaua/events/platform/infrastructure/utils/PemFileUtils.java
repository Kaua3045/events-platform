package com.kaua.events.platform.infrastructure.utils;

import com.kaua.events.platform.infrastructure.constants.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public final class PemFileUtils {

    private static final Logger log = LoggerFactory.getLogger(PemFileUtils.class);

    private static final int KEY_SIZE = 2048;
    private static final String RSA = "RSA";

    private PemFileUtils() {
    }

    public static RsaKeyPair readOrCreateKeyPair(Path keyPath) {
        try {
            String keyBaseName = getBaseKeyName(keyPath);
            Path directory = keyPath.getParent();
            Path privatePath = directory.resolve(keyBaseName + Constants.RSA_KEY_PRIVATE_FILE);
            Path publicPath = directory.resolve(keyBaseName + Constants.RSA_KEY_PUBLIC_FILE);

            if (Files.notExists(privatePath) || Files.notExists(publicPath)) {
                log.info("Generating new RSA key pair: {}", keyBaseName);
                generateAndWriteKeyPair(privatePath, publicPath);
            }

            log.debug("Loading RSA keys: {} / {}", privatePath.getFileName(), publicPath.getFileName());
            RSAPrivateKey privateKey = readPrivateKey(privatePath);
            RSAPublicKey publicKey = readPublicKey(publicPath);
            return new RsaKeyPair(privateKey, publicKey);

        } catch (Exception e) {
            throw new UncheckedIOException(new IOException("Failed to load/create RSA key pair from path: " + keyPath, e));
        }
    }

    private static RSAPrivateKey readPrivateKey(Path path) throws Exception {
        byte[] decoded = decodePem(Files.readString(path), "PRIVATE KEY");
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decoded);
        return (RSAPrivateKey) KeyFactory.getInstance(RSA).generatePrivate(keySpec);
    }

    private static RSAPublicKey readPublicKey(Path path) throws Exception {
        byte[] decoded = decodePem(Files.readString(path), "PUBLIC KEY");
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decoded);
        return (RSAPublicKey) KeyFactory.getInstance(RSA).generatePublic(keySpec);
    }

    private static byte[] decodePem(String pem, String type) {
        String clean = pem.replace("-----BEGIN " + type + "-----", "")
                .replace("-----END " + type + "-----", "")
                .replaceAll("\\s", "");
        return Base64.getDecoder().decode(clean);
    }

    private static void generateAndWriteKeyPair(Path privatePath, Path publicPath) throws Exception {
        KeyPairGenerator gen = KeyPairGenerator.getInstance(RSA);
        gen.initialize(KEY_SIZE);
        KeyPair pair = gen.generateKeyPair();

        writePem(privatePath, "PRIVATE KEY", pair.getPrivate().getEncoded());
        writePem(publicPath, "PUBLIC KEY", pair.getPublic().getEncoded());
    }

    private static void writePem(Path path, String type, byte[] encoded) throws IOException {
        String base64 = Base64.getEncoder().encodeToString(encoded);
        String pem = "-----BEGIN " + type + "-----\n" +
                chunk(base64, 64) +
                "-----END " + type + "-----\n";

        Files.createDirectories(path.getParent());
        Files.writeString(path, pem, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    private static String chunk(String data, int size) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < data.length(); i += size) {
            sb.append(data, i, Math.min(i + size, data.length())).append('\n');
        }
        return sb.toString();
    }

    private static String getBaseKeyName(Path path) {
        String name = path.getFileName().toString();
        return name.replace(Constants.RSA_KEY_PRIVATE_FILE, "").replace(Constants.RSA_KEY_PUBLIC_FILE, "").replace(".pem", "");
    }
}
