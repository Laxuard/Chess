package com.ft_transcendence.gateway.security.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import com.ft_transcendence.gateway.core.util.RSAKeyUtils;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(JwtProperties.class)
public class CryptoConfig {

    private final RSAKeyUtils keyUtils;
    private final JwtProperties jwtProperties;

    @Bean
    public RSAPrivateKey rsaPrivateKey() throws Exception {
        return keyUtils.loadPrivateKey(jwtProperties.privateKeyLocation());
    }

    @Bean
    public RSAPublicKey rsaPublicKey() throws Exception {
        return keyUtils.loadPublicKey(jwtProperties.publicKeyLocation());
    }
}