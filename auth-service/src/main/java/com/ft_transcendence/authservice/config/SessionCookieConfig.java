package com.ft_transcendence.authservice.config;

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

        // 2. COOKIE PARAMETERS PORTED FROM YOUR YAML CONFIG
        serializer.setCookieName("SESSION");
        serializer.setCookiePath("/");

        // secure: true -> Enforces that cookies are only transmitted over encrypted HTTPS channels
        serializer.setUseSecureCookie(true);

        // http-only: true -> Blocks client-side scripts (like JavaScript) from stealing the cookie
        serializer.setUseHttpOnlyCookie(true);

        // same-site: lax -> Cross-site requests won't attach the cookie, protecting against CSRF attacks
        serializer.setSameSite("Lax");

        return serializer;
    }
}