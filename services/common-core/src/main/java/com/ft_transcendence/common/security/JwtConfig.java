package com.ft_transcendence.common.security;

import org.springframework.boot.ssl.SslBundles;
import org.springframework.web.client.RestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

@Configuration
public class JwtConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
    private String jwkSetUri;

    @Bean
    public JwtDecoder jwtDecoder(RestTemplateBuilder restTemplateBuilder, SslBundles sslBundles) {
        RestTemplate restTemplate = restTemplateBuilder
                .sslBundle(sslBundles.getBundle("microservice-bundle"))
                .build();

        return NimbusJwtDecoder.withJwkSetUri(this.jwkSetUri)
                .restOperations(restTemplate)
                .build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();

        // Tell Spring to extract custom arrays from the "roles" claim payload
        authoritiesConverter.setAuthoritiesClaimName("roles");
        // Convert claims seamlessly to the spring standard "ROLE_*" prefix format
        authoritiesConverter.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter authenticationConverter = new JwtAuthenticationConverter();
        authenticationConverter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
        return authenticationConverter;
    }
}
