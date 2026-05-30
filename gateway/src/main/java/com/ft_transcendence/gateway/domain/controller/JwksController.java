package com.ft_transcendence.gateway.domain.controller;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.JWSAlgorithm;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.security.interfaces.RSAPublicKey;
import java.util.concurrent.TimeUnit;

@RestController
public class JwksController {

    private final Map<String, Object> cachedJwkSet;

    public JwksController(RSAPublicKey publicKey) throws JOSEException {

        RSAKey jwk = new RSAKey.Builder(publicKey)
                .keyUse(KeyUse.SIGNATURE)
                .algorithm(JWSAlgorithm.RS256)
                .build();

        String dynamicKid = jwk.computeThumbprint().toString();
        jwk = new RSAKey.Builder(jwk).keyID(dynamicKid).build();

        this.cachedJwkSet = new JWKSet(jwk).toJSONObject();

    }

    @GetMapping(value = "/.well-known/jwks.json", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getJwks() {

        // Return the body alongside professional HTTP Cache-Control directives.
        // This tells downstream services: "Cache this safely in your RAM for 24 hours!"
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(24, TimeUnit.HOURS).cachePublic())
                .body(this.cachedJwkSet);
    }

}
