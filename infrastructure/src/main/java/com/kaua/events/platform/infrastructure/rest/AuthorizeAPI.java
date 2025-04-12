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
            @RequestParam("code") String code,
            @RequestParam("client_id") String clientId,
            @RequestParam("client_secret") String clientSecret,
            @RequestParam("code_verifier") String codeVerifier
    );

    @PostMapping(
            path = "/refresh",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<?> refreshToken(
            @RequestParam("refresh_token") String refreshToken,
            @RequestParam("client_id") String clientId
    );
}
