package com.ft_transcendence.gateway.util;

import org.springframework.stereotype.Component;

import java.security.interfaces.RSAPrivateKey;

@Component
public class JwtGenerator {

    private final RSAPrivateKey privateKey;

    public JwtGenerator(RSAPrivateKey privateKey) {
        this.privateKey = privateKey;
    }

    public string createToken(String userId, )

}
