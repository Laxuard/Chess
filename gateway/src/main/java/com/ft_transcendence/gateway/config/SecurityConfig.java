package com.ft_transcendence.gateway.config;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import com.ft_transcendence.gateway.util.RSAKeyUtils;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import javax.net.ssl.SSLException;
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
        return keyUtils.loadPrivateKey(jwtProperties.privateKeyPath());
    }

    @Bean
    public RSAPublicKey rsaPublicKey() throws Exception {
        return keyUtils.loadPublicKey(jwtProperties.publicKeyPath());
    }

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http)
    {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)

                .authorizeExchange(exchanges -> exchanges
                        // Allow Eureka and Docker to ping the health of the Gateway
                        .pathMatchers("/actuator/health/**").permitAll()

                        // Example: Allow your login/auth endpoints to be public
                        .pathMatchers("/api/auth/**").permitAll()

                        .pathMatchers("/.well-known/**").permitAll()

                        // Every other request MUST have a valid Redis Session
                        .anyExchange().authenticated()
                )

                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)

                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)

                .build();
    }

    @Bean
    @LoadBalanced
    public WebClient.Builder loadBalancedWebClientBuilder() throws SSLException {

        // 1. Build an SSL Context that blindly trusts all certificates (Local Dev Only)
        SslContext sslContext = SslContextBuilder.forClient()
                .trustManager(InsecureTrustManagerFactory.INSTANCE)
                .build();

        // 2. Attach it to a Netty HttpClient
        HttpClient httpClient = HttpClient.create()
                .secure(sslContextSpec -> sslContextSpec.sslContext(sslContext));

        // 3. Inject it into the WebClient Builder
        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient));
    }

}
