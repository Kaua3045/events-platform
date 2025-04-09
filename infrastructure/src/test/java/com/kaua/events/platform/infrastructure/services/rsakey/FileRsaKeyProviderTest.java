package com.kaua.events.platform.infrastructure.services.rsakey;

import com.kaua.events.platform.domain.UnitTest;
import com.kaua.events.platform.infrastructure.utils.PemFileUtils;
import com.kaua.events.platform.infrastructure.utils.RsaKeyPair;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

class FileRsaKeyProviderTest extends UnitTest {

    @TempDir
    private Path tempDir;

    private FileRsaKeyProvider rsaKeyProvider;

    private static MockedStatic<PemFileUtils> pemUtilsMock;

    private static RsaKeyPair rsaKeyPair;

    @BeforeAll
    static void setupClass() {
        pemUtilsMock = mockStatic(PemFileUtils.class);
        rsaKeyPair = new RsaKeyPair(
                mock(RSAPrivateKey.class),
                mock(RSAPublicKey.class)
        );
    }

    @AfterAll
    static void tearDownClass() {
        pemUtilsMock.close();
    }

    @BeforeEach
    void setUp() {
        rsaKeyProvider = new FileRsaKeyProvider(tempDir);
    }

    @Test
    void shouldLoadPrivateKey() {
        // given
        String keyName = "mykey";
        Path privatePath = tempDir.resolve(keyName + "-private.pem");

        pemUtilsMock.when(() -> PemFileUtils.readOrCreateKeyPair(privatePath))
                .thenReturn(rsaKeyPair);

        // when
        RSAPrivateKey result = rsaKeyProvider.getPrivateKey(keyName);

        // then
        Assertions.assertNotNull(result);
        Assertions.assertSame(rsaKeyPair.privateKey(), result);

        // make sure it was cached
        RSAPrivateKey cached = rsaKeyProvider.getPrivateKey(keyName);
        Assertions.assertSame(result, cached); // same instance, from cache
    }

    @Test
    void shouldLoadPublicKey() {
        // given
        String keyName = "mykey";
        Path publicPath = tempDir.resolve(keyName + "-public.pem");

        pemUtilsMock.when(() -> PemFileUtils.readOrCreateKeyPair(publicPath))
                .thenReturn(rsaKeyPair);

        // when
        RSAPublicKey result = rsaKeyProvider.getPublicKey(keyName);

        // then
        Assertions.assertNotNull(result);
        Assertions.assertSame(rsaKeyPair.publicKey(), result);

        // make sure it was cached
        RSAPublicKey cached = rsaKeyProvider.getPublicKey(keyName);
        Assertions.assertSame(result, cached);
    }

    @Test
    void shouldCreateBasePathWithoutMock() {
        // given
        Path newDir = tempDir.resolve("real-new-dir");

        Assertions.assertFalse(Files.exists(newDir));

        // when
        FileRsaKeyProvider provider = new FileRsaKeyProvider(newDir);

        // then
        Assertions.assertNotNull(provider);
        Assertions.assertTrue(Files.exists(newDir));
    }

    @Test
    void shouldThrowExceptionWhenCreateBasePathFails() {
        // given
        Path path = tempDir.resolve("fail-dir");

        try (MockedStatic<Files> filesMock = mockStatic(Files.class)) {
            filesMock.when(() -> Files.exists(path)).thenReturn(false);
            filesMock.when(() -> Files.createDirectories(path))
                    .thenThrow(new IOException("Simulated failure"));

            // when + then
            RuntimeException exception = Assertions.assertThrows(RuntimeException.class, () -> new FileRsaKeyProvider(path));

            Assertions.assertTrue(exception.getMessage().contains("Could not create base path"));
        }
    }
}
