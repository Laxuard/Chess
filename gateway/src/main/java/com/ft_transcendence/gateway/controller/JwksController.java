package com.ft_transcendence.gateway.controller;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.interfaces.RSAPublicKey;
import java.util.Map;

@RestController
public class JwksController {

    private final RSAPublicKey publicKey;

    public JwksController(RSAPublicKey publicKey) {
        this.publicKey = publicKey;
    }

    @GetMapping("/.well-known/jwks.json")
    public Map<String, Object> getJwks() {

        RSAKey jwk = new RSAKey.Builder(this.publicKey)
                .keyID("transcendence-internal-key")
                .build();

        return new JWKSet(jwk).toJSONObject();
    }

}
