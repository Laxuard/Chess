package com.ft_transcendence.gateway.core.config;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import javax.net.ssl.SSLException;

@Configuration
public class WebClientConfig {

    @Bean
    @LoadBalanced // Enables "lb://auth-service" named routing resolutions!
    public WebClient.Builder webClientBuilder(SslBundles sslBundles) throws SSLException {
        var sslBundle = sslBundles.getBundle("microservice-bundle");

        // 1. Convert the Spring SslBundle managers into a Netty native SslContext instance
        SslContext nettySslContext = SslContextBuilder.forClient()
                .keyManager(sslBundle.getManagers().getKeyManagerFactory())
                .trustManager(sslBundle.getManagers().getTrustManagerFactory())
                .build();

        // 2. Feed the native Netty context cleanly into the secure pipeline spec
        HttpClient httpClient = HttpClient.create()
                .secure(sslContextSpec -> sslContextSpec.sslContext(nettySslContext));

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient));
    }
}