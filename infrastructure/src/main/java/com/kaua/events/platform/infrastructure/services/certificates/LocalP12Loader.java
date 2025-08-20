package com.kaua.events.platform.infrastructure.services.certificates;

import com.kaua.events.platform.domain.exceptions.InternalErrorException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public class LocalP12Loader implements P12Loader {

    private final String localPath;

    public LocalP12Loader(String localPath) {
        this.localPath = Objects.requireNonNull(localPath);
    }

    @Override
    public byte[] loadP12Bytes() {
        Resource resource = new ClassPathResource(localPath);
        try (InputStream is = resource.getInputStream()) {
            return is.readAllBytes();
        } catch (IOException e) {
            throw InternalErrorException.with("Error reading p12 from classpath: " + localPath, e);
        }
    }
}
