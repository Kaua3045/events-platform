package com.kaua.events.platform.infrastructure.oauth.code.req;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CreateAuthorizationCodeRequest(
        @JsonProperty("client_id") String clientId,
        @JsonProperty("client_secret") String clientSecret,
        @JsonProperty("code_challenge") String codeChallenge,
        @JsonProperty("code_challenge_method") String codeChallengeMethod,
        @JsonProperty("email") String email,
        @JsonProperty("password") String password
) {
}
