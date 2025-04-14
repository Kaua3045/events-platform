package com.kaua.events.platform.domain.utils;

import com.kaua.events.platform.domain.UnitTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PKCEUtilsTest extends UnitTest {

    @Test
    void testGenerateCodeChallenge() {
        String codeVerifier = "code_verifier";
        String expectedCodeChallenge = "73oehA2tBul5grZPhXUGQwNAjxh69zNES8bu2bVD0EM"; // Base64 encoded value of "code_verifier"

        String codeChallenge = PKCEUtils.generateCodeChallenge(codeVerifier);

        Assertions.assertEquals(expectedCodeChallenge, codeChallenge);
    }

    @Test
    void testGenerateSha256Base64() {
        final var aToken = "token";
        final var expectedOutput = "PEaenWxYddN6Q/NT1PiOYfz4EsZu7jRXRlpAsNpBU+A=";

        final var sha256Base64 = PKCEUtils.generateSha256Base64(aToken);

        Assertions.assertEquals(expectedOutput, sha256Base64);
    }

    @Test
    void testGenerateCodeChallengeThrows() {
        String codeVerifier = null;

        Assertions.assertThrows(RuntimeException.class, () -> {
            PKCEUtils.generateCodeChallenge(codeVerifier);
        });
    }

    @Test
    void testGenerateSha256Base64Throws() {
        String token = null;

        Assertions.assertThrows(RuntimeException.class, () -> {
            PKCEUtils.generateSha256Base64(token);
        });
    }
}
