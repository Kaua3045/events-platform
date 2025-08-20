package com.kaua.events.platform.infrastructure.services.certificates;

import com.kaua.events.platform.domain.UnitTest;
import com.kaua.events.platform.domain.exceptions.InternalErrorException;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

class LocalP12LoaderTest extends UnitTest {

    @Test
    void loadP12Bytes_shouldReturnBytes_whenFileExists() throws IOException {
        String testFilePath = "/certificates/test/test_cert.p12";
        LocalP12Loader loader = new LocalP12Loader(testFilePath);

        Resource resource = new ClassPathResource(testFilePath);
        byte[] expectedBytes;
        try (InputStream is = resource.getInputStream()) {
            expectedBytes = is.readAllBytes();
        }

        byte[] actualBytes = loader.loadP12Bytes();

        assertArrayEquals(expectedBytes, actualBytes);
    }

    @Test
    void loadP12Bytes_shouldThrowInternalErrorException_whenFileDoesNotExist() {
        String nonExistentFile = "non-existent.p12";
        LocalP12Loader loader = new LocalP12Loader(nonExistentFile);

        InternalErrorException exception = assertThrows(
                InternalErrorException.class,
                loader::loadP12Bytes
        );

        assertTrue(exception.getMessage().contains("Error reading p12 from classpath: " + nonExistentFile));
    }

    @Test
    void constructor_shouldThrowNullPointerException_whenPathIsNull() {
        assertThrows(NullPointerException.class, () -> new LocalP12Loader(null));
    }
}
