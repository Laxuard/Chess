package com.ft_transcendence.auth.security.config;

import com.ft_transcendence.auth.security.context.MyUserDetailsService;
import com.ft_transcendence.auth.core.filter.TraceIdFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.savedrequest.NullRequestCache;
import org.springframework.web.client.RestTemplate;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
    private String jwkSetUri;

    private final TraceIdFilter traceIdFilter;
    private final MyUserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        return http
                .csrf(AbstractHttpConfigurer::disable)

                .authorizeHttpRequests(auth -> auth

                        .requestMatchers("/login", "/register", "/actuator/health/**").permitAll()

                        // The "Closed" Doors (Requires the Gateway's JWT)
                        .anyRequest().authenticated()
                )

                .requestCache(requestCache -> requestCache
                        .requestCache(new NullRequestCache()))

                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwtConfigurer -> jwtConfigurer.jwtAuthenticationConverter(jwtAuthenticationConverter())))

                .addFilterBefore(traceIdFilter, UsernamePasswordAuthenticationFilter.class)

                .build();
    }

    private JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
        // 1. Tell Spring to look inside your custom "roles" claim array
        authoritiesConverter.setAuthoritiesClaimName("roles");
        // 2. Map them with the clean "ROLE_" prefix standard
        authoritiesConverter.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter authenticationConverter = new JwtAuthenticationConverter();
        authenticationConverter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
        return authenticationConverter;
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config){
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    /**
     * Real, Compilation-Safe JwtDecoder Configuration.
     * Uses Spring Boot's RestTemplateBuilder to inject the SSL Bundle seamlessly.
     */
    @Bean
    public JwtDecoder jwtDecoder(RestTemplateBuilder restTemplateBuilder, SslBundles sslBundles) {
        // 1. Build a RestTemplate that is natively configured with your certificate bundle
        RestTemplate restTemplate = restTemplateBuilder
                .sslBundle(sslBundles.getBundle("microservice-bundle"))
                .build();

        // 2. Supply the secured RestTemplate straight to Nimbus via restOperations
        return NimbusJwtDecoder.withJwkSetUri(this.jwkSetUri)
                .restOperations(restTemplate)
                .build();
    }
}
