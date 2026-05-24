package com.ft_transcendence.gateway.config;

import com.nimbusds.jose.util.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import com.ft_transcendence.gateway.util.RSAKeyUtils;
import com.ft_transcendence.gateway.util.JwtProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.web.server.savedrequest.NoOpServerRequestCache;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@Configuration
@EnableWebFluxSecurity
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityConfig {

    private final RSAKeyUtils keyUtils;
    private final JwtProperties jwtProperties;
    
    public SecurityConfig(RSAKeyUtils keyUtils, JwtProperties jwtProperties) {
        this.keyUtils = keyUtils;
        this.jwtProperties = jwtProperties;
    }

    @Bean
    public RSAPrivateKey rsaPrivateKey() throws Exception {
        return keyUtils.loadPrivateKey(jwtProperties.privateKeyLocation());
    }

    @Bean
    public RSAPublicKey rsaPublicKey() throws Exception {
        return keyUtils.loadPublicKey(jwtProperties.publicKeyLocation());
    }

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http)
    {
        return http

                .authorizeExchange(exchanges -> exchanges
                        .anyExchange().permitAll()
                )

                .requestCache(cache -> cache
                        .requestCache(NoOpServerRequestCache.getInstance()))

                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .logout(ServerHttpSecurity.LogoutSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)


                .build();
    }


}
