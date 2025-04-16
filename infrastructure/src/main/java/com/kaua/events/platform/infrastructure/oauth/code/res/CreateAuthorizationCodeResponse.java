package com.kaua.events.platform.infrastructure.oauth.code.res;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CreateAuthorizationCodeResponse(
        @JsonProperty("code") String code,
        @JsonProperty("redirect_uri") String redirectUri
) {
}
