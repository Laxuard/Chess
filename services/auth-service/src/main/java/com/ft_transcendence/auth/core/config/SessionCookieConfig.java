package com.ft_transcendence.auth.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

@Configuration
public class SessionCookieConfig {

    @Bean
    public CookieSerializer cookieSerializer() {
        DefaultCookieSerializer serializer = new DefaultCookieSerializer();

        // 1. THE CRITICAL SYSTEM BRIDGE
        // Disable Base64 encoding completely so it matches the raw UUID formatting
        // that your reactive WebFlux Gateway expects naturally.
        serializer.setUseBase64Encoding(false);

        serializer.setSameSite("Lax");
        serializer.setCookiePath("/");
        serializer.setCookieName("SESSION");
        serializer.setUseSecureCookie(true);
        serializer.setUseHttpOnlyCookie(true);

        return serializer;
    }
}