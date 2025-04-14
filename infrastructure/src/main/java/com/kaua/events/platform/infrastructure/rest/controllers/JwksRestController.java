package com.kaua.events.platform.infrastructure.rest.controllers;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKMatcher;
import com.nimbusds.jose.jwk.JWKSelector;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/.well-known")
public class JwksRestController {

    private final JWKSource<SecurityContext> jwkSource;

    public JwksRestController(final JWKSource<SecurityContext> jwkSource) {
        this.jwkSource = Objects.requireNonNull(jwkSource);
    }

    @GetMapping("/jwks.json")
    public Map<String, Object> keys() throws Exception {
        JWKSelector selector = new JWKSelector(new JWKMatcher.Builder().build());
        List<JWK> jwks = jwkSource.get(selector, null);
        return new JWKSet(jwks).toJSONObject();
    }
}
