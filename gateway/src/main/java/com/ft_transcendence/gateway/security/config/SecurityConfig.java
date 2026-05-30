package com.ft_transcendence.gateway.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.web.server.savedrequest.NoOpServerRequestCache;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

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
