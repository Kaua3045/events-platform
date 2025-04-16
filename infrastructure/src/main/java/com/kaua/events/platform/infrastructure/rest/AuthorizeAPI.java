package com.kaua.events.platform.infrastructure.rest;

import com.kaua.events.platform.infrastructure.oauth.code.req.CreateAuthorizationCodeRequest;
import com.kaua.events.platform.infrastructure.oauth.code.res.CreateAuthorizationCodeResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping("/v1/authorize")
public interface AuthorizeAPI {

    @PostMapping(
            path = "/code",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<CreateAuthorizationCodeResponse> createAuthorizationCode(
            @RequestBody CreateAuthorizationCodeRequest request
    );

    @PostMapping(
            path = "/token",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<?> createToken(
            @RequestParam("grant_type") String grantType,
            @RequestParam("client_id") String clientId,
            @RequestParam(value = "client_secret", required = false) String clientSecret,
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "code_verifier", required = false) String codeVerifier,
            @RequestParam(value = "refresh_token", required = false) String refreshToken
    );
}
