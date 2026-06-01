
# 📦 TRANSCENDENCE MICROSERVICES CONTEXT

This single document contains the handwritten source code of the Transcendence Microservices Stack. It is optimized to be highly token-efficient for AI context ingestion.

## 🗂️ Project Structure Summary
```
|____auth-service
| |____Dockerfile
| |____.mvn
| |____mvnw
| |____mvnw.cmd
| | |____wrapper
| |____pom.xml
| |____src
| | |____main
| | |____test
|____config-repo
| |____application-dev.yaml
| |____application-docker.yaml
| |____application.yaml
| |____auth-service
| | |____auth-service-dev.yaml
| | |____auth-service-docker.yaml
| | |____auth-service.yaml
| |____eureka-server
| | |____eureka-server-dev.yaml
| | |____eureka-server-docker.yaml
| | |____eureka-server.yaml
| |____gateway
| | |____gateway-dev.yaml
| | |____gateway-docker.yaml
| | |____gateway.yaml
|____config-server
| |____Dockerfile
| |____.mvn
| |____mvnw
| |____mvnw.cmd
| | |____wrapper
| |____pom.xml
| |____src
| | |____main
| | |____test
|____dev-start.sh
|____docker-compose.apps.yml
|____docker-compose.yml
|____.env
|____env.example
|____eureka-server
| |____Dockerfile
| |____.mvn
| |____mvnw
| |____mvnw.cmd
| | |____wrapper
| |____pom.xml
| |____src
| | |____main
| | |____test
|____frontend
| |____eslint.config.js
| |____index.html
| |____package.json
| |____package-lock.json
| |____public
| | |____favicon.svg
| | |____icons.svg
| |____README.md
| |____src
| | |____App.css
| | |____App.jsx
| | |____assets
| | |____components
| | |____context
| | |____index.css
| | |____main.jsx
| | |____screens
| | |____services
| |____vite.config.js
|____gateway
| |____Dockerfile
| |____.mvn
| |____mvnw
| |____mvnw.cmd
| | |____wrapper
| |____pom.xml
| |____src
| | |____main
| | |____test
|____generate-ai-context.sh
|____mtls-setup.sh
|____project_codebase_context.md
```

## 📄 File: ./eureka-server/.mvn/wrapper/maven-wrapper.properties
```properties
wrapperVersion=3.3.4
distributionType=only-script
distributionUrl=https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.14/apache-maven-3.9.14-bin.zip

```

## 📄 File: ./eureka-server/src/main/java/com/ft_transcendence/eurekaserver/EurekaServerApplication.java
```java
package com.ft_transcendence.eurekaserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@EnableEurekaServer
@SpringBootApplication
public class EurekaServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }

}

```

## 📄 File: ./eureka-server/src/main/java/com/ft_transcendence/eurekaserver/config/SecurityConfig.java
```java
package com.ft_transcendence.eurekaserver.config;

import org.springframework.context.annotation.Bean;
import org.springframework.security.config.Customizer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;


@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {

        return http
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .csrf(AbstractHttpConfigurer::disable)

                .x509(Customizer.withDefaults())

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health/**").permitAll()
                        .requestMatchers("/eureka/**").hasRole("SYSTEM")
                        .anyRequest().authenticated()
                )

                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)

                .build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> User.withUsername(username)
                .password("")
                .roles("SYSTEM")
                .build();
    }

}

```

## 📄 File: ./eureka-server/src/main/resources/application-dev.yaml
```yaml
spring:
  config:
    import: "optional:configserver:https://localhost:${CONFIG_SERVER_PORT}"
  cloud:
    config:
      tls:
        enabled: true
        key-store-type: PKCS12
        trust-store-type: PKCS12
        key-store: "file:${CERT_DIR_PATH}/services/${spring.application.name}/${spring.application.name}.p12"
        key-store-password: "${CERT_PASSWORD}"
        key-password: "${CERT_PASSWORD}"
        trust-store: "file:${CERT_DIR_PATH}/truststore/truststore.p12"
        trust-store-password: "${CERT_PASSWORD}"



```

## 📄 File: ./eureka-server/src/main/resources/application-docker.yaml
```yaml
spring:
  config:
    import: "optional:configserver:https://config-server:${CONFIG_SERVER_PORT}"
  cloud:
    config:
      tls:
        enabled: true
        key-store-type: PKCS12
        trust-store-type: PKCS12
        key-store: "file:/app/certs/services/${spring.application.name}/${spring.application.name}.p12"
        key-store-password: "${CERT_PASSWORD}"
        key-password: "${CERT_PASSWORD}"
        trust-store: "file:/app/certs/truststore/truststore.p12"
        trust-store-password: "${CERT_PASSWORD}"

```

## 📄 File: ./eureka-server/src/main/resources/application.yaml
```yaml
spring:
  application:
    name: eureka-server

```

## 📄 File: ./eureka-server/src/test/java/com/ft_transcendence/eurekaserver/EurekaServerApplicationTests.java
```java
package com.ft_transcendence.eurekaserver;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class EurekaServerApplicationTests {

    @Test
    void contextLoads() {
    }

}

```

## 📄 File: ./eureka-server/pom.xml
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>4.0.6</version>
        <relativePath/> <!-- lookup parent from repository -->
    </parent>
    <groupId>com.ft_transcendence</groupId>
    <artifactId>eureka-server</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>eureka-server</name>
    <description>eureka-server</description>
    <url/>
    <licenses>
        <license/>
    </licenses>
    <developers>
        <developer/>
    </developers>
    <scm>
        <connection/>
        <developerConnection/>
        <tag/>
        <url/>
    </scm>
    <properties>
        <java.version>21</java.version>
        <spring-cloud.version>2025.1.1</spring-cloud.version>
        <maven.compiler.target>21</maven.compiler.target>
    </properties>
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-config</artifactId>
        </dependency>
    </dependencies>
    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-dependencies</artifactId>
                <version>${spring-cloud.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>

</project>

```

## 📄 File: ./eureka-server/Dockerfile
```dockerfile
# ==========================================
# BUILD STAGE
# ==========================================
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /app

# Cache Maven dependency layers for rapid rebuilds
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source and build
COPY src ./src
RUN mvn clean package -DskipTests

# ==========================================
# RUN STAGE
# ==========================================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Install curl for robust healthcheck support
RUN apk add --no-cache curl

# Copy the built jar from the builder stage
COPY --from=builder /app/target/*.jar app.jar

# Run the Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]

```

## 📄 File: ./gateway/.mvn/wrapper/maven-wrapper.properties
```properties
wrapperVersion=3.3.4
distributionType=only-script
distributionUrl=https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.14/apache-maven-3.9.14-bin.zip

```

## 📄 File: ./gateway/src/main/java/com/ft_transcendence/gateway/GatewayApplication.java
```java
package com.ft_transcendence.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }

}

```

## 📄 File: ./gateway/src/main/java/com/ft_transcendence/gateway/core/filter/ReactiveTraceIdFilter.java
```java
package com.ft_transcendence.gateway.core.filter;

import org.springframework.http.server.reactive.ServerHttpRequest;
import reactor.core.publisher.Mono;
import org.jspecify.annotations.NonNull;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.WebFilter;
import org.springframework.core.annotation.Order;
import org.springframework.web.server.WebFilterChain;
import org.springframework.web.server.ServerWebExchange;
import com.ft_transcendence.gateway.core.util.ReactiveTraceContext;

import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ReactiveTraceIdFilter implements WebFilter {

    @Override
    public @NonNull Mono<Void> filter(@NonNull ServerWebExchange exchange, @NonNull WebFilterChain chain) {
        String existingHeader = exchange.getRequest().getHeaders().getFirst(ReactiveTraceContext.TRACE_HEADER);
        String traceId = (existingHeader != null && !existingHeader.isBlank()) ? existingHeader : UUID.randomUUID().toString();

        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                .header(ReactiveTraceContext.TRACE_HEADER, traceId)
                .build();

        ServerWebExchange mutatedExchange = exchange.mutate().request(mutatedRequest).build();

        mutatedExchange.getAttributes().put(ReactiveTraceContext.TRACE_KEY, traceId);
        mutatedExchange.getResponse().getHeaders().set(ReactiveTraceContext.TRACE_HEADER, traceId);

        // Pass the mutated exchange down the pipeline
        return chain.filter(mutatedExchange)
                .contextWrite(context -> context.put(ReactiveTraceContext.TRACE_KEY, traceId));
    }
}
```

## 📄 File: ./gateway/src/main/java/com/ft_transcendence/gateway/core/filter/SessionToJwtGatewayFilterFactory.java
```java
package com.ft_transcendence.gateway.core.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import reactor.core.publisher.Mono;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Component;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.web.server.ResponseStatusException;
import com.ft_transcendence.gateway.domain.service.JwtService;
import org.springframework.http.server.reactive.ServerHttpRequest;
import com.ft_transcendence.gateway.core.util.ReactiveTraceContext;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;

import java.util.List;

@Slf4j
@Component
public class SessionToJwtGatewayFilterFactory extends AbstractGatewayFilterFactory<SessionToJwtGatewayFilterFactory.Config> {

    private final JwtService jwtService;

    public SessionToJwtGatewayFilterFactory(JwtService jwtService) {
        super(Config.class);
        this.jwtService = jwtService;
    }

    @Override
    @NullMarked
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String traceId = ReactiveTraceContext.getTraceId(exchange);

            // 1. Safe Cookie Evaluation: Only evaluates requests routed to protected pages
            List<HttpCookie> rawSessionCookies = request.getCookies().get("SESSION");
            if (rawSessionCookies == null || rawSessionCookies.isEmpty()) {
                log.warn("Missing session cookie context container on guarded route request.");
                return Mono.error(new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "An active cookie session is required to traverse this gateway proxy"
                ));
            }

            return exchange.getSession().flatMap(webSession -> {
                Object userIdAttr = webSession.getAttribute("userId");
                Object rolesAttr = webSession.getAttribute("roles");

                if (userIdAttr == null || rolesAttr == null) {
                    log.warn("Access intercept - Missing or expired active Redis context.");
                    return Mono.error(new ResponseStatusException(
                            HttpStatus.UNAUTHORIZED,
                            "An active cookie session is required to traverse this gateway proxy"
                    ));
                }

                String userId = userIdAttr.toString();
                String sessionId = webSession.getId();
                @SuppressWarnings("unchecked")
                List<String> roles = (List<String>) rolesAttr;


                String transitJwt = jwtService.mint(userId, roles, sessionId, traceId);

                ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                        .headers(headers -> {
                            headers.remove(HttpHeaders.COOKIE);
                            headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + transitJwt);
                        })
                        .build();

                return chain.filter(exchange.mutate().request(mutatedRequest).build());
            });
        };
    }

    public static class Config {
    }
}
```

## 📄 File: ./gateway/src/main/java/com/ft_transcendence/gateway/core/filter/TwoFactorCheckGatewayFilterFactory.java
```java
package com.ft_transcendence.gateway.core.filter;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import org.jspecify.annotations.NullMarked;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;

import java.util.List;

@Slf4j
@Component
public class TwoFactorCheckGatewayFilterFactory extends AbstractGatewayFilterFactory<TwoFactorCheckGatewayFilterFactory.Config> {

    public TwoFactorCheckGatewayFilterFactory() {
        super(Config.class);
    }

    @Override
    @NullMarked
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            // Eager Cookie Evaluation: Fast-fails requests routed to protected pages if cookie is absent
            List<HttpCookie> rawSessionCookies = exchange.getRequest().getCookies().get("SESSION");
            if (rawSessionCookies == null || rawSessionCookies.isEmpty()) {
                log.warn("Missing session cookie context container on 2FA guarded route request.");
                return Mono.error(new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "An active cookie session is required to access this resource"
                ));
            }

            return exchange.getSession().flatMap(webSession -> {
                Object isFullyAuthenticatedAttr = webSession.getAttribute("isFullyAuthenticated");

                if (isFullyAuthenticatedAttr == null || !Boolean.parseBoolean(isFullyAuthenticatedAttr.toString())) {
                    log.warn("Access Intercept - User session requires completed 2FA verification challenge.");
                    return Mono.error(new ResponseStatusException(
                            HttpStatus.FORBIDDEN,
                            "Full multi-factor authentication validation is required to access this resource"
                    ));
                }

                return chain.filter(exchange);
            });
        };
    }

    public static class Config {
    }
}
```

## 📄 File: ./gateway/src/main/java/com/ft_transcendence/gateway/core/config/RedisSessionSerializationConfig.java
```java
package com.ft_transcendence.gateway.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;

@Configuration
public class RedisSessionSerializationConfig {

    @Bean
    public RedisSerializer<Object> springSessionDefaultRedisSerializer() {
        return GenericJacksonJsonRedisSerializer.builder().build();
    }

}

```

## 📄 File: ./gateway/src/main/java/com/ft_transcendence/gateway/core/config/MdcPropagationConfig.java
```java
package com.ft_transcendence.gateway.core.config;

import org.slf4j.MDC;
import reactor.core.publisher.Hooks;
import jakarta.annotation.PostConstruct;
import io.micrometer.context.ContextRegistry;
import org.springframework.context.annotation.Configuration;
import com.ft_transcendence.gateway.core.util.ReactiveTraceContext;

@Configuration
public class MdcPropagationConfig {

    @PostConstruct
    public void initializeReactiveMdcPropagation() {
        // 1. Core Reactive Hook: Enables global, automated context copying across threads
        Hooks.enableAutomaticContextPropagation();

        // 2. Map the tracking key between Reactor Context and SLF4J MDC ThreadLocals
        ContextRegistry.getInstance().registerThreadLocalAccessor(
                ReactiveTraceContext.TRACE_KEY,
                () -> MDC.get(ReactiveTraceContext.TRACE_KEY),
                traceId -> MDC.put(ReactiveTraceContext.TRACE_KEY, traceId),
                () -> MDC.remove(ReactiveTraceContext.TRACE_KEY)
        );
    }
}
```

## 📄 File: ./gateway/src/main/java/com/ft_transcendence/gateway/core/config/WebClientConfig.java
```java
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
```

## 📄 File: ./gateway/src/main/java/com/ft_transcendence/gateway/core/util/RSAKeyUtils.java
```java
package com.ft_transcendence.gateway.core.util;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.security.KeyFactory;
import java.nio.charset.StandardCharsets;
import java.security.spec.X509EncodedKeySpec;
import java.security.interfaces.RSAPublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;

@Component
public class RSAKeyUtils {

    public RSAPrivateKey loadPrivateKey(Resource resource) throws Exception {
        String pem = readAndClean(resource, "PRIVATE KEY");
        byte[] encoded = Base64.getDecoder().decode(pem);
        return (RSAPrivateKey) KeyFactory.getInstance("RSA")
                .generatePrivate(new PKCS8EncodedKeySpec(encoded));
    }

    public RSAPublicKey loadPublicKey(Resource resource) throws Exception {
        String pem = readAndClean(resource, "PUBLIC KEY");
        byte[] encoded = Base64.getDecoder().decode(pem);
        return (RSAPublicKey) KeyFactory.getInstance("RSA")
                .generatePublic(new X509EncodedKeySpec(encoded));
    }

    private String readAndClean(Resource resource, String label) throws Exception {
        try (var inputStream = resource.getInputStream()) {
            String content = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            return content
                    .replace("-----BEGIN " + label + "-----", "")
                    .replace("-----END " + label + "-----", "")
                    .replaceAll("\\s", "");
        }
    }

}

```

## 📄 File: ./gateway/src/main/java/com/ft_transcendence/gateway/core/util/ReactiveTraceContext.java
```java
package com.ft_transcendence.gateway.core.util;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import java.util.UUID;

public final class ReactiveTraceContext {

    public static final String TRACE_KEY = "trace_id";
    public static final String TRACE_HEADER = "X-Trace-Id";

    private ReactiveTraceContext() {}

    public static String getTraceId(ServerWebExchange exchange) {
        if (exchange == null) return UUID.randomUUID().toString();

        // 1. Try to read from exchange attributes first
        String traceId = exchange.getAttribute(TRACE_KEY);

        // 2. Fallback: Parse from the raw incoming HTTP headers
        if (traceId == null || traceId.isBlank()) {
            ServerHttpRequest request = exchange.getRequest();
            traceId = request.getHeaders().getFirst(TRACE_HEADER);
        }

        // 3. Emergency: Fallback to a fresh generation if missing
        if (traceId == null || traceId.isBlank()) {
            traceId = UUID.randomUUID().toString();
        }

        return traceId;
    }
}
```

## 📄 File: ./gateway/src/main/java/com/ft_transcendence/gateway/security/config/SecurityConfig.java
```java
package com.ft_transcendence.gateway.security.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import com.ft_transcendence.gateway.security.oauth2.CustomOAuth2SuccessHandler;
import com.ft_transcendence.gateway.security.oauth2.RedisServerOAuth2AuthorizationRequestRepository;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import org.springframework.security.web.server.savedrequest.NoOpServerRequestCache;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.CorsConfiguration;

@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2SuccessHandler customOAuth2SuccessHandler;
    private final RedisServerOAuth2AuthorizationRequestRepository redisAuthorizationRepository;

    @org.springframework.beans.factory.annotation.Value("${app.cors.allowed-origins:http://localhost:5173}")
    private java.util.List<String> allowedOrigins;

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http)
    {
        return http

                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/login/**", "/oauth2/**").permitAll()
                        .anyExchange().permitAll()
                )

                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())

                .requestCache(cache -> cache
                        .requestCache(NoOpServerRequestCache.getInstance()))

                .oauth2Login(oauth2 -> oauth2
                        .authorizationRequestRepository(redisAuthorizationRepository)
                        .authenticationSuccessHandler(customOAuth2SuccessHandler)
                )

                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout")
                        .logoutSuccessHandler((exchange, authentication) -> {
                            exchange.getExchange().getResponse().setStatusCode(org.springframework.http.HttpStatus.OK);
                            return exchange.getExchange().getSession().flatMap(org.springframework.web.server.WebSession::invalidate);
                        })
                )
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)


                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOrigins(allowedOrigins);
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }


}

```

## 📄 File: ./gateway/src/main/java/com/ft_transcendence/gateway/security/config/JwtProperties.java
```java
package com.ft_transcendence.gateway.security.config;

import org.springframework.core.io.Resource;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "custom.jwt")
public record JwtProperties(
        Resource privateKeyLocation,
        Resource publicKeyLocation
) {}
```

## 📄 File: ./gateway/src/main/java/com/ft_transcendence/gateway/security/config/CryptoConfig.java
```java
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
```

## 📄 File: ./gateway/src/main/java/com/ft_transcendence/gateway/security/oauth2/CustomOAuth2SuccessHandler.java
```java
package com.ft_transcendence.gateway.security.oauth2;

import com.ft_transcendence.gateway.security.oauth2.OAuth2UserInfoCompositeExtractor.OAuth2SyncPayload;
import com.ft_transcendence.gateway.domain.service.JwtService;
import com.ft_transcendence.gateway.core.util.ReactiveTraceContext;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.server.DefaultServerRedirectStrategy;
import org.springframework.security.web.server.ServerRedirectStrategy;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.WebSession;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomOAuth2SuccessHandler implements ServerAuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final WebClient.Builder webClientBuilder;
    private final OAuth2UserInfoCompositeExtractor extractorFactory;

    @Value("${app.frontend.base-url:http://localhost:5173}")
    private String frontendBaseUrl;

    private final ServerRedirectStrategy redirectStrategy = new DefaultServerRedirectStrategy();

    @Override
    @NullMarked
    public Mono<Void> onAuthenticationSuccess(WebFilterExchange webFilterExchange, Authentication authentication) {
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        ServerWebExchange exchange = webFilterExchange.getExchange();

        OAuth2SyncPayload syncPayload = extractorFactory.extract(
                oauthToken.getAuthorizedClientRegistrationId(),
                oauthToken.getPrincipal()
        );

        return exchange.getSession().flatMap(session -> {
            String existingUserId = session.getAttribute("userId");
            Boolean isLinkingInProgress = session.getAttribute("oauth2_linking_in_progress");

            // Evict handshake flag immediately to prevent stale reuse
            session.getAttributes().remove("oauth2_linking_in_progress");

            if (existingUserId != null && Boolean.TRUE.equals(isLinkingInProgress)) {
                return executeAccountLink(exchange, session, syncPayload, existingUserId);
            }

            return executeIdentitySync(exchange, session, syncPayload);
        });
    }

    // ── PRIVATE ORCHESTRATION EXTRACTIONS ───────────────────────────────────

    private Mono<Void> executeAccountLink(ServerWebExchange exchange, WebSession session,
                                          OAuth2SyncPayload payload, String userId) {
        log.info("Active user [{}] is linking external identity provider [{}]...", userId, payload.provider());

        String transitJwt = mintTransitToken(exchange, session, userId);
        OAuth2LinkRequest linkRequest = new OAuth2LinkRequest(payload.provider(), payload.providerId());

        return webClientBuilder.build()
                .post()
                .uri("https://auth-service/oauth2/link")
                .header("Authorization", "Bearer " + transitJwt)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(linkRequest)
                .retrieve()
                .toBodilessEntity()
                .then(redirectStrategy.sendRedirect(exchange, URI.create(frontendBaseUrl + "/dashboard?link=success")))
                .onErrorResume(ex -> {
                    log.error("Failed to link social identity record", ex);
                    return redirectStrategy.sendRedirect(exchange, URI.create(frontendBaseUrl + "/dashboard?link=error"));
                });
    }

    private Mono<Void> executeIdentitySync(ServerWebExchange exchange, WebSession session, OAuth2SyncPayload payload) {
        log.info("OAuth2 login completed via [{}]. Executing downstream identity sync...", payload.provider());

        return webClientBuilder.build()
                .post()
                .uri("https://auth-service/oauth2/sync")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(UserSummaryResponse.class)
                .flatMap(userSummary -> {
                    session.getAttributes().put("userId", userSummary.userId());
                    session.getAttributes().put("roles", userSummary.roles());
                    session.getAttributes().put("isFullyAuthenticated", true);

                    log.info("OAuth Session registration completed for User ID [{}]", userSummary.userId());
                    return redirectStrategy.sendRedirect(exchange, URI.create(frontendBaseUrl + "/dashboard"));
                })
                .onErrorResume(ex -> {
                    log.error("OAuth2 SSO login synchronization failed", ex);
                    String errorParam = resolveErrorParam(ex);
                    return redirectStrategy.sendRedirect(exchange, URI.create(frontendBaseUrl + "/login?error=" + errorParam));
                });
    }

    // ── PRIVATE UTILITY SCOPES ──────────────────────────────────────────────

    private String mintTransitToken(ServerWebExchange exchange, WebSession session, String userId) {
        String traceId = ReactiveTraceContext.getTraceId(exchange);

        List<String> roles = session.getAttribute("roles");
        if (roles == null) {
            roles = List.of("ROLE_USER");
        }

        return jwtService.mint(userId, roles, session.getId(), traceId);
    }

    private String resolveErrorParam(Throwable ex) {
        if (ex instanceof WebClientResponseException.Conflict) {
            return "email_taken";
        }
        return "auth_error";
    }

    private record UserSummaryResponse(String userId, List<String> roles) {}
}
```

## 📄 File: ./gateway/src/main/java/com/ft_transcendence/gateway/security/oauth2/OAuth2UserInfoExtractor.java
```java
package com.ft_transcendence.gateway.security.oauth2;

import org.springframework.security.oauth2.core.user.OAuth2User;

public interface OAuth2UserInfoExtractor {
    String getRegistrationId();
    String getName(OAuth2User oAuth2User);
    String getEmail(OAuth2User oAuth2User);
    String getProviderId(OAuth2User oAuth2User);
}
```

## 📄 File: ./gateway/src/main/java/com/ft_transcendence/gateway/security/oauth2/GoogleUserInfoExtractor.java
```java
package com.ft_transcendence.gateway.security.oauth2;

import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

@Component
public class GoogleUserInfoExtractor implements OAuth2UserInfoExtractor {
    @Override
    public String getRegistrationId() {
        return "google";
    }

    @Override
    public String getProviderId(OAuth2User oAuth2User) {
        return oAuth2User.getAttribute("sub");
    }

    @Override
    public String getEmail(OAuth2User oAuth2User) {
        return oAuth2User.getAttribute("email");
    }

    @Override
    public String getName(OAuth2User oAuth2User) {
        return oAuth2User.getAttribute("name");
    }
}
```

## 📄 File: ./gateway/src/main/java/com/ft_transcendence/gateway/security/oauth2/FortyTwoUserInfoExtractor.java
```java
package com.ft_transcendence.gateway.security.oauth2;

import org.springframework.stereotype.Component;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Objects;

@Component
public class FortyTwoUserInfoExtractor implements OAuth2UserInfoExtractor {
    @Override
    public String getRegistrationId() {
        return "fortytwo";
    }

    @Override
    public String getProviderId(OAuth2User oAuth2User) {
        return String.valueOf(Objects.requireNonNull(oAuth2User.getAttribute("id")));
    }

    @Override
    public String getEmail(OAuth2User oAuth2User) {
        return oAuth2User.getAttribute("email");
    }

    @Override
    public String getName(OAuth2User oAuth2User) {
        return oAuth2User.getAttribute("login");
    }
}
```

## 📄 File: ./gateway/src/main/java/com/ft_transcendence/gateway/security/oauth2/OAuth2UserInfoCompositeExtractor.java
```java
package com.ft_transcendence.gateway.security.oauth2;

import org.springframework.stereotype.Component;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.function.Function;

@Component
public class OAuth2UserInfoCompositeExtractor {

    private final Map<String, OAuth2UserInfoExtractor> extractors;

    public OAuth2UserInfoCompositeExtractor(List<OAuth2UserInfoExtractor> extractorList) {
        this.extractors = extractorList.stream().collect(Collectors.toMap(
                extractor -> extractor.getRegistrationId().toLowerCase(),
                Function.identity()
        ));
    }

    public OAuth2SyncPayload extract(String registrationId, OAuth2User oAuth2User) {
        OAuth2UserInfoExtractor extractor = extractors.get(registrationId.toLowerCase());
        if (extractor == null) {
            throw new IllegalArgumentException("Unsupported OAuth2 provider: " + registrationId);
        }
        return new OAuth2SyncPayload(
                registrationId.toUpperCase(),
                extractor.getProviderId(oAuth2User),
                extractor.getEmail(oAuth2User),
                extractor.getName(oAuth2User)
        );
    }

    public record OAuth2SyncPayload(String provider, String providerId, String email, String name) {}
}
```

## 📄 File: ./gateway/src/main/java/com/ft_transcendence/gateway/security/oauth2/OAuth2LinkRequest.java
```java
package com.ft_transcendence.gateway.security.oauth2;

import lombok.Builder;

@Builder
public record OAuth2LinkRequest(
        String provider,
        String providerId
) {}

```

## 📄 File: ./gateway/src/main/java/com/ft_transcendence/gateway/security/oauth2/RedisServerOAuth2AuthorizationRequestRepository.java
```java
package com.ft_transcendence.gateway.security.oauth2;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.security.oauth2.client.web.server.ServerAuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.io.*;
import java.time.Duration;
import java.util.Base64;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisServerOAuth2AuthorizationRequestRepository 
        implements ServerAuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    private static final String REDIS_KEY_PREFIX = "oauth2_auth_request:";
    private static final Duration STATE_TTL = Duration.ofMinutes(10);

    private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

    @Override
    @NullMarked
    public Mono<OAuth2AuthorizationRequest> loadAuthorizationRequest(ServerWebExchange exchange) {
        String state = exchange.getRequest().getQueryParams().getFirst("state");
        if (state == null) {
            return Mono.empty();
        }
        return reactiveRedisTemplate.opsForValue()
                .get(REDIS_KEY_PREFIX + state)
                .map(this::deserialize)
                .onErrorResume(ex -> {
                    log.error("Failed to load authorization request from Redis", ex);
                    return Mono.empty();
                });
    }

    @Override
    @NullMarked
    public Mono<Void> saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest, ServerWebExchange exchange) {
        if (authorizationRequest == null || authorizationRequest.getState() == null) {
            return Mono.empty();
        }

        String state = authorizationRequest.getState();
        String serialized = serialize(authorizationRequest);
        if (serialized == null) {
            return Mono.empty();
        }

        // 1. Prepare our isolation string persistent task
        Mono<Boolean> saveStateMono = reactiveRedisTemplate.opsForValue()
                .set(REDIS_KEY_PREFIX + state, serialized, STATE_TTL);

        // 2. Safely mutate the attributes map and force a state persistence save flush
        boolean isLink = exchange.getRequest().getQueryParams().containsKey("link");
        Mono<Void> updateSessionMono = exchange.getSession().flatMap(session -> {
            if (isLink) {
                session.getAttributes().put("oauth2_linking_in_progress", true);
                log.debug("Marked active session [{}] as in-flight account linking state", session.getId());
            } else {
                session.getAttributes().remove("oauth2_linking_in_progress");
            }
            // CRITICAL FIX: Explicitly invoke the session saver downstream flush!
            return session.save();
        });

        // Chain them synchronously to confirm both state writes land before redirection
        return saveStateMono.then(updateSessionMono);
    }

    @Override
    @NullMarked
    public Mono<OAuth2AuthorizationRequest> removeAuthorizationRequest(ServerWebExchange exchange) {
        String state = exchange.getRequest().getQueryParams().getFirst("state");
        if (state == null) {
            return Mono.empty();
        }
        String key = REDIS_KEY_PREFIX + state;
        return reactiveRedisTemplate.opsForValue()
                .get(key)
                .flatMap(serialized -> reactiveRedisTemplate.delete(key)
                        .thenReturn(deserialize(serialized)))
                .onErrorResume(ex -> {
                    log.error("Failed to remove authorization request from Redis", ex);
                    return Mono.empty();
                });
    }

    // ── BASE64 OBJECT SERIALIZATION HELPERS ─────────────────────────────────

    private String serialize(OAuth2AuthorizationRequest request) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(request);
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (IOException e) {
            log.error("Serialization of OAuth2AuthorizationRequest failed", e);
            return null;
        }
    }

    private OAuth2AuthorizationRequest deserialize(String base64) {
        byte[] bytes = Base64.getDecoder().decode(base64);
        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
             ObjectInputStream ois = new ObjectInputStream(bais)) {
            return (OAuth2AuthorizationRequest) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            log.error("Deserialization of OAuth2AuthorizationRequest failed", e);
            return null;
        }
    }
}

```

## 📄 File: ./gateway/src/main/java/com/ft_transcendence/gateway/domain/service/JwtService.java
```java
package com.ft_transcendence.gateway.domain.service;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.stereotype.Service;

import java.security.interfaces.RSAPublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@Service
public class JwtService {

    private final JWSSigner jwsSigner;
    private final String activeKid;

    public JwtService(RSAPrivateKey privateKey, RSAPublicKey publicKey) throws JOSEException {

        this.jwsSigner = new RSASSASigner(privateKey);

        RSAKey jwk = new RSAKey.Builder(publicKey)
                .keyUse(KeyUse.SIGNATURE)
                .algorithm(JWSAlgorithm.RS256)
                .build();

        this.activeKid = jwk.computeThumbprint().toString();
    }

    /**
     * Mints a cryptographically signed Transit JWT payload.
     * Includes the critical 'sid' pointer claim to enable downstream programmatic 2FA verification updates.
     */
    public String mint(String publicId, List<String> roles, String sessionId, String traceId) { // Added sessionId parameter
        Instant now = Instant.now();

        try {
            // Assemble the protected envelope header containing your dynamic thumbprint ID
            JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256)
                    .keyID(this.activeKid)
                    .build();

            // Fill out the token contents safely
            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                    .subject(publicId)
                    .issuer("transcendence-gateway")
                    .issueTime(Date.from(now))
                    .expirationTime(Date.from(now.plusSeconds(60))) // Short-lived transit window
                    .claim("roles", roles)
                    .claim("sid", sessionId)
                    .claim("tid", traceId)
                    .build();

            // Seal, cryptographically stamp, and compile into a string text block
            SignedJWT signedJWT = new SignedJWT(header, claimsSet);
            signedJWT.sign(this.jwsSigner);

            return signedJWT.serialize();

        } catch (Exception e) {
            throw new RuntimeException("Cryptographic Error: Failed to mint internal transit token payload", e);
        }
    }

}

```

## 📄 File: ./gateway/src/main/java/com/ft_transcendence/gateway/domain/controller/JwksController.java
```java
package com.ft_transcendence.gateway.domain.controller;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.JWSAlgorithm;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.security.interfaces.RSAPublicKey;
import java.util.concurrent.TimeUnit;

@RestController
public class JwksController {

    private final Map<String, Object> cachedJwkSet;

    public JwksController(RSAPublicKey publicKey) throws JOSEException {

        RSAKey jwk = new RSAKey.Builder(publicKey)
                .keyUse(KeyUse.SIGNATURE)
                .algorithm(JWSAlgorithm.RS256)
                .build();

        String dynamicKid = jwk.computeThumbprint().toString();
        jwk = new RSAKey.Builder(jwk).keyID(dynamicKid).build();

        this.cachedJwkSet = new JWKSet(jwk).toJSONObject();

    }

    @GetMapping(value = "/.well-known/jwks.json", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getJwks() {

        // Return the body alongside professional HTTP Cache-Control directives.
        // This tells downstream services: "Cache this safely in your RAM for 24 hours!"
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(24, TimeUnit.HOURS).cachePublic())
                .body(this.cachedJwkSet);
    }

}

```

## 📄 File: ./gateway/src/main/resources/application-dev.yaml
```yaml
spring:
  config:
    import: "optional:configserver:https://localhost:${CONFIG_SERVER_PORT}"
  cloud:
    config:
      tls:
        enabled: true
        key-store-type: PKCS12
        trust-store-type: PKCS12
        key-store: "file:${CERT_DIR_PATH}/services/${spring.application.name}/${spring.application.name}.p12"
        key-store-password: "${CERT_PASSWORD}"
        key-password: "${CERT_PASSWORD}"
        trust-store: "file:${CERT_DIR_PATH}/truststore/truststore.p12"
        trust-store-password: "${CERT_PASSWORD}"

```

## 📄 File: ./gateway/src/main/resources/application-docker.yaml
```yaml
spring:
  config:
    import: "optional:configserver:https://config-server:${CONFIG_SERVER_PORT}"
  cloud:
    config:
      tls:
        enabled: true
        key-store-type: PKCS12
        trust-store-type: PKCS12
        key-store: "file:/app/certs/services/${spring.application.name}/${spring.application.name}.p12"
        key-store-password: "${CERT_PASSWORD}"
        key-password: "${CERT_PASSWORD}"
        trust-store: "file:/app/certs/truststore/truststore.p12"
        trust-store-password: "${CERT_PASSWORD}"
```

## 📄 File: ./gateway/src/main/resources/application.yaml
```yaml
spring:
  application:
    name: gateway
```

## 📄 File: ./gateway/src/test/java/com/ft_transcendence/gateway/GatewayApplicationTests.java
```java
package com.ft_transcendence.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class GatewayApplicationTests {

    @Test
    void contextLoads() {
    }

}

```

## 📄 File: ./gateway/pom.xml
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>4.0.6</version>
        <relativePath/> </parent>
    <groupId>com.ft_transcendence</groupId>
    <artifactId>gateway</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>gateway</name>
    <description>gateway</description>

    <properties>
        <java.version>21</java.version>
        <spring-cloud.version>2025.1.1</spring-cloud.version>
        <maven.compiler.source>21</maven.compiler.source>
        <maven.compiler.target>21</maven.compiler.target>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis-reactive</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security-oauth2-resource-server</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-session-data-redis</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-gateway-server-webflux</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-config</artifactId>
        </dependency>
        <dependency>
            <groupId>io.micrometer</groupId>
            <artifactId>context-propagation</artifactId>
            <version>1.2.1</version>
        </dependency>
        <dependency>
            <groupId>org.springdoc</groupId>
            <artifactId>springdoc-openapi-starter-webflux-ui</artifactId>
            <version>3.0.3</version>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-configuration-processor</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis-reactive-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security-oauth2-resource-server-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-session-data-redis-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>io.projectreactor</groupId>
            <artifactId>reactor-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security-oauth2-client</artifactId>
        </dependency>
        <dependency>
            <groupId>com.github.ben-manes.caffeine</groupId>
            <artifactId>caffeine</artifactId>
        </dependency>
    </dependencies>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-dependencies</artifactId>
                <version>${spring-cloud.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>

            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.14.1</version>
                <configuration>
                    <source>21</source>
                    <target>21</target>
                    <annotationProcessorPaths>
                        <path>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                            <version>${lombok.version}</version>
                        </path>
                        <path>
                            <groupId>org.springframework.boot</groupId>
                            <artifactId>spring-boot-configuration-processor</artifactId>
                            <version>${project.parent.version}</version>
                        </path>
                    </annotationProcessorPaths>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

## 📄 File: ./gateway/Dockerfile
```dockerfile
# ==========================================
# BUILD STAGE
# ==========================================
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /app

# Cache Maven dependency layers for rapid rebuilds
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source and build
COPY src ./src
RUN mvn clean package -DskipTests

# ==========================================
# RUN STAGE
# ==========================================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Install curl for robust healthcheck support
RUN apk add --no-cache curl

# Copy the built jar from the builder stage
COPY --from=builder /app/target/*.jar app.jar

# Run the Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]

```

## 📄 File: ./auth-service/.mvn/wrapper/maven-wrapper.properties
```properties
wrapperVersion=3.3.4
distributionType=only-script
distributionUrl=https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.14/apache-maven-3.9.14-bin.zip

```

## 📄 File: ./auth-service/src/main/java/com/ft_transcendence/auth/AuthServiceApplication.java
```java
package com.ft_transcendence.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }

}

```

## 📄 File: ./auth-service/src/main/java/com/ft_transcendence/auth/core/config/PersistenceConfig.java
```java
package com.ft_transcendence.auth.core.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing
public class PersistenceConfig {
}
```

## 📄 File: ./auth-service/src/main/java/com/ft_transcendence/auth/core/config/SessionCookieConfig.java
```java
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
```

## 📄 File: ./auth-service/src/main/java/com/ft_transcendence/auth/core/config/OpenApiConfig.java
```java
package com.ft_transcendence.auth.core.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${springdoc.server-url:https://localhost:8080/api/auth}")
    private String serverUrl;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .servers(List.of(
                        new Server().url(serverUrl).description("Gateway Edge BFF Proxy")
                ));
    }
}

```

## 📄 File: ./auth-service/src/main/java/com/ft_transcendence/auth/core/exception/BaseException.java
```java
package com.ft_transcendence.auth.core.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class BaseException extends RuntimeException {

    private final String errorCode;
    private final HttpStatus httpStatus;

    protected BaseException(HttpStatus httpStatus, String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

}

```

## 📄 File: ./auth-service/src/main/java/com/ft_transcendence/auth/core/exception/DuplicateResourceException.java
```java
package com.ft_transcendence.auth.core.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateResourceException extends BaseException {
    public DuplicateResourceException(String resource) {
        super(HttpStatus.CONFLICT, "duplicate-resource",
                resource + " already exists");
    }
}

```

## 📄 File: ./auth-service/src/main/java/com/ft_transcendence/auth/core/exception/GlobalExceptionHandler.java
```java
package com.ft_transcendence.auth.core.exception;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import jakarta.servlet.http.HttpServletRequest;
import com.ft_transcendence.auth.core.util.TraceContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.*;
import java.net.URI;
import java.time.Instant;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @Value("${app.error-docs-url}")
    private String docBaseUrl;

    @Value("${spring.application.name}")
    private String serviceId;

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            @NonNull MethodArgumentNotValidException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull WebRequest request) {

        // Safely extract the raw HttpServletRequest to get the URI string
        var servletRequest = ((ServletWebRequest) request).getRequest();

        Map<String, String> fieldErrors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(fe ->
                fieldErrors.put(fe.getField(), fe.getDefaultMessage()));

        ProblemDetail problem = ProblemDetail
                .forStatusAndDetail(HttpStatus.BAD_REQUEST, "Input validation failed");

        problem.setType(URI.create(docBaseUrl + "validation-failed"));
        problem.setTitle("Constraint Violation");
        problem.setInstance(URI.create(servletRequest.getRequestURI()));
        problem.setProperty("invalid_params", fieldErrors);

        // Cast to Object to match the parent framework signature requirement
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(addMetadata(problem));
    }

    // ── Spring Security Authentication Exceptions ─────────────────────────
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ProblemDetail> handleAuthenticationException(
            AuthenticationException ex, HttpServletRequest request) {

        // Default to a generic unauthorized message
        String detailMessage = "Authentication failed";
        String errorCode = "unauthorized";

        // You can check the specific subclass to give better error messages
        if (ex instanceof BadCredentialsException) {
            detailMessage = "Email or password is incorrect";
            errorCode = "invalid-credentials";
        }

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED, detailMessage);

        problem.setType(URI.create(docBaseUrl + errorCode));
        problem.setTitle(toTitle(errorCode));
        problem.setInstance(URI.create(request.getRequestURI()));

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(addMetadata(problem));
    }

    // ── All domain exceptions via single handler ──────────────────────────
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ProblemDetail> handleBaseException(BaseException ex, HttpServletRequest request) {

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(ex.getHttpStatus(), ex.getMessage());

        problemDetail.setType(URI.create(docBaseUrl + ex.getErrorCode()));
        problemDetail.setTitle(toTitle(ex.getErrorCode()));
        problemDetail.setInstance(URI.create(request.getRequestURI()));

        return ResponseEntity.status(ex.getHttpStatus()).body(addMetadata(problemDetail));
    }

    // ── Catch-all — unexpected, always log with stack trace ───────────────
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleUnexpected(
            Exception ex, HttpServletRequest request) {

        log.error("Unhandled exception on {} {}",
                request.getMethod(), request.getRequestURI(), ex);

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred");

        problem.setType(URI.create(docBaseUrl + "internal-error"));
        problem.setTitle("Internal Server Error");
        problem.setInstance(URI.create(request.getRequestURI()));

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(addMetadata(problem));
    }


    /**
     * Universal Interceptor Hook: Intercepts every built-in Spring MVC framework exception
     * handled by ResponseEntityExceptionHandler (like HttpMessageNotReadableException)
     * and guarantees it gets enriched with our trace, timestamp, and origin tracking data.
     */
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            @NonNull Exception ex,
            Object body,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode statusCode,
            @NonNull WebRequest request) {

        // If Spring Framework has initialized an RFC 7807 ProblemDetail body for this error, enrich it!
        if (body instanceof ProblemDetail problem) {
            addMetadata(problem);
        }

        // Forward to the parent execution chain to finalize network transmission
        return super.handleExceptionInternal(ex, body, headers, statusCode, request);
    }

    // ── Metadata ──────────────────────────────────────────────────────────
    private ProblemDetail addMetadata(ProblemDetail problem) {
        problem.setProperty("service_origin", serviceId);
        problem.setProperty("timestamp", Instant.now().toString());
        problem.setProperty("trace_id", TraceContext.getTraceId(null));
        return problem;
    }

    private String toTitle(String errorCode) {
        if (errorCode == null || errorCode.isBlank()) return "Error";
        return Arrays.stream(errorCode.split("-"))
                .filter(w -> !w.isBlank())
                .map(w -> Character.toUpperCase(w.charAt(0)) + w.substring(1))
                .collect(Collectors.joining(" "));
    }
}

```

## 📄 File: ./auth-service/src/main/java/com/ft_transcendence/auth/core/exception/InvalidCredentialsException.java
```java
package com.ft_transcendence.auth.core.exception;

import org.springframework.http.HttpStatus;

public class InvalidCredentialsException extends BaseException {

    /**
     * Default constructor used by standard password login failures.
     */
    public InvalidCredentialsException() {
        super(HttpStatus.UNAUTHORIZED, "invalid-credentials", "Email or password is incorrect");
    }

    /**
     * Overloaded constructor allowing custom contextual messages
     * (like your specific MFA challenge failures).
     */
    public InvalidCredentialsException(String message) {
        super(HttpStatus.UNAUTHORIZED, "invalid-credentials", message);
    }
}
```

## 📄 File: ./auth-service/src/main/java/com/ft_transcendence/auth/core/exception/ResourceNotFoundException.java
```java
package com.ft_transcendence.auth.core.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends BaseException {
    public ResourceNotFoundException(String resource) {
        super(HttpStatus.NOT_FOUND, "resource-not-found",
                resource + " was not found");
    }
}

```

## 📄 File: ./auth-service/src/main/java/com/ft_transcendence/auth/core/exception/MfaException.java
```java
package com.ft_transcendence.auth.core.exception;

import org.springframework.http.HttpStatus;

public class MfaException extends BaseException {

    /**
     * Constructs a custom multi-factor authentication lifecycle exception.
     * Maps automatically to an HTTP 400 Bad Request and provides a clean error code.
     */
    public MfaException(String message) {
        super(HttpStatus.BAD_REQUEST, "mfa-validation-failed", message);
    }
}
```

## 📄 File: ./auth-service/src/main/java/com/ft_transcendence/auth/core/exception/BadRequestException.java
```java
package com.ft_transcendence.auth.core.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BadRequestException extends BaseException {
    public BadRequestException(String message) {
        super(HttpStatus.BAD_REQUEST, "bad-request", message);
    }
}

```

## 📄 File: ./auth-service/src/main/java/com/ft_transcendence/auth/core/filter/TraceIdFilter.java
```java
package com.ft_transcendence.auth.core.filter;

import org.slf4j.MDC;
import jakarta.servlet.*;
import org.springframework.core.Ordered;
import org.jspecify.annotations.NonNull;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import com.ft_transcendence.auth.core.util.TraceContext;
import org.springframework.web.filter.OncePerRequestFilter;

import java.util.UUID;
import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TraceIdFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain) throws ServletException, IOException {

        String traceId = request.getHeader(TraceContext.TRACE_HEADER);
        if (traceId == null || traceId.isBlank()) {
            traceId = UUID.randomUUID().toString();
        }

        MDC.put(TraceContext.TRACE_KEY, traceId);
        request.setAttribute(TraceContext.TRACE_KEY, traceId);

        try {
            chain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}
```

## 📄 File: ./auth-service/src/main/java/com/ft_transcendence/auth/core/util/TraceContext.java
```java
package com.ft_transcendence.auth.core.util;

import org.slf4j.MDC;
import java.util.UUID;
import jakarta.servlet.http.HttpServletRequest;

public final class TraceContext {

    public static final String TRACE_KEY = "trace_id";
    public static final String TRACE_HEADER = "X-Trace-Id";

    private TraceContext() {} // Suppress instantiation

    /**
     * Resolves the active trace ID across all compilation targets uniformly.
     * Checks MDC first, falls back to request attributes, and isolates a safe fallback if entirely decoupled.
     */
    public static String getTraceId(HttpServletRequest request) {
        // 1. Primary Source: Extract from the SLF4J Thread-Local Logging Context
        String traceId = MDC.get(TRACE_KEY);

        // 2. Secondary Fallback: Extract from the HttpServletRequest attribute space
        if ((traceId == null || traceId.isBlank()) && request != null) {
            traceId = (String) request.getAttribute(TRACE_KEY);
        }

        // 3. Tertiary Fallback: Read straight from incoming client headers if attributes were cleared
        if ((traceId == null || traceId.isBlank()) && request != null) {
            traceId = request.getHeader(TRACE_HEADER);
        }

        // 4. Emergency Fallback: Generate a fresh one if called outside an active HTTP servlet lifecycle thread
        if (traceId == null || traceId.isBlank()) {
            traceId = UUID.randomUUID().toString();
        }

        return traceId;
    }
}
```

## 📄 File: ./auth-service/src/main/java/com/ft_transcendence/auth/security/config/SecurityConfig.java
```java
package com.ft_transcendence.auth.security.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import com.ft_transcendence.auth.core.filter.TraceIdFilter;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.savedrequest.NullRequestCache;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final TraceIdFilter traceIdFilter;
    private final JwtAuthenticationConverter jwtAuthenticationConverter;
    private final ProblemDetailAuthenticationEntryPoint authenticationEntryPoint;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        return http
                .csrf(AbstractHttpConfigurer::disable)

                .authorizeHttpRequests(auth -> auth

                        .requestMatchers("/login", "/register", "/actuator/health/**", "/v3/api-docs/**", "/oauth2/sync", "/oauth2/link").permitAll()

                        // The "Closed" Doors (Requires the Gateway's JWT)
                        .anyRequest().authenticated()
                )

                .requestCache(requestCache -> requestCache
                        .requestCache(new NullRequestCache()))

                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .oauth2ResourceServer(oauth2 -> oauth2
                        // Instruct the resource server filter to use our custom converter configuration
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter))
                        // Links filter boundary handshake exceptions to our unified tracing format
                        .authenticationEntryPoint(authenticationEntryPoint)
                )

                .addFilterBefore(traceIdFilter, UsernamePasswordAuthenticationFilter.class)

                .build();
    }

}

```

## 📄 File: ./auth-service/src/main/java/com/ft_transcendence/auth/security/config/ProblemDetailAuthenticationEntryPoint.java
```java
package com.ft_transcendence.auth.security.config;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ft_transcendence.auth.core.util.TraceContext;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.net.URI;
import java.time.Instant;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class ProblemDetailAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Value("${app.error-docs-url}")
    private String docBaseUrl;

    @Value("${spring.application.name}")
    private String serviceId;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         @NonNull AuthenticationException authException) throws IOException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);

        // Standard dynamic code lookup tag
        String errorCode = "unauthorized";

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED,
                "Full authentication is required to access this resource"
        );

        // UNIFIED: Replaced the static string literal path with a clean dynamic link
        problem.setType(URI.create(docBaseUrl + errorCode));
        problem.setTitle("Unauthorized Access");
        problem.setInstance(URI.create(request.getRequestURI()));

        // UNIFIED METADATA: Stripped out locally duplicated logic blocks to match the Global Handler
        problem.setProperty("service_origin", serviceId);
        problem.setProperty("timestamp", Instant.now().toString());
        problem.setProperty("trace_id", TraceContext.getTraceId(request));

        response.getWriter().write(objectMapper.writeValueAsString(problem));
    }
}
```

## 📄 File: ./auth-service/src/main/java/com/ft_transcendence/auth/security/config/PasswordConfig.java
```java
package com.ft_transcendence.auth.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class PasswordConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

## 📄 File: ./auth-service/src/main/java/com/ft_transcendence/auth/security/config/AuthConfig.java
```java
package com.ft_transcendence.auth.security.config;

import com.ft_transcendence.auth.security.context.MyUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class AuthConfig {

    private final PasswordEncoder passwordEncoder;
    private final MyUserDetailsService userDetailsService;

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config){
        return config.getAuthenticationManager();
    }
}
```

## 📄 File: ./auth-service/src/main/java/com/ft_transcendence/auth/security/config/JwtConfig.java
```java
package com.ft_transcendence.auth.security.config;

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
```

## 📄 File: ./auth-service/src/main/java/com/ft_transcendence/auth/security/context/MyUserDetailsService.java
```java
package com.ft_transcendence.auth.security.context;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.core.userdetails.UserDetails;
import com.ft_transcendence.auth.domain.repository.UserAuthRepository;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;

@Service

@RequiredArgsConstructor
public class MyUserDetailsService implements UserDetailsService {

    private final UserAuthRepository userAuthRepository;

    @Override
    @Transactional(readOnly = true)
    public @NonNull UserDetails loadUserByUsername(@NonNull String loginIdentifier) throws UsernameNotFoundException {
        return userAuthRepository.findByEmailOrUsername(loginIdentifier, loginIdentifier)
                .map(SecurityUser::new)
                .orElseThrow(() -> new UsernameNotFoundException(
                        String.format("No identity credentials found matching identifier: %s", loginIdentifier)
                ));
    }
}

```

## 📄 File: ./auth-service/src/main/java/com/ft_transcendence/auth/security/context/SecurityUser.java
```java
package com.ft_transcendence.auth.security.context;

import lombok.NonNull;
import com.ft_transcendence.auth.domain.model.UserAuth;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;

public record SecurityUser(UserAuth userAuth) implements UserDetails {

    @Override
    public @NonNull String getUsername() {
        // Essential: Spring Security uses this string token inside the principal identity context.
        // Mapping your raw database ID or business UUID ensures it remains immutable.
        return userAuth.getUserId().toString();
    }

    @Override
    public String getPassword() {
        return userAuth.getPassword();
    }

    @Override
    public @NonNull Collection<? extends GrantedAuthority> getAuthorities() {
        // Optimization: Swapped .collect(Collectors.toList()) to an unmodifiable List.
        // Context granted authorities should remain strictly immutable during a request runtime lifecycle.
        return userAuth.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                .toList();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // Set default safe fallback explicitly to bypass default interface rejection loops
    }

    @Override
    public boolean isAccountNonLocked() {
        return !userAuth.isAccountLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Set default safe fallback explicitly to bypass default interface rejection loops
    }

    @Override
    public boolean isEnabled() {
        return userAuth.isEnabled() && !userAuth.isDeleted();
    }

}
```

## 📄 File: ./auth-service/src/main/java/com/ft_transcendence/auth/security/session/RedisSessionConfig.java
```java
package com.ft_transcendence.auth.security.session;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;

@Configuration
public class RedisSessionConfig {

    @Bean
    public RedisSerializer<Object> springSessionDefaultRedisSerializer() {
        return GenericJacksonJsonRedisSerializer.builder().build();
    }

}

```

## 📄 File: ./auth-service/src/main/java/com/ft_transcendence/auth/domain/controller/AuthController.java
```java
package com.ft_transcendence.auth.domain.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.oauth2.jwt.Jwt;
import com.ft_transcendence.auth.domain.model.UserAuth;
import com.ft_transcendence.auth.domain.mapper.UserMapper;
import com.ft_transcendence.auth.domain.service.AuthService;
import com.ft_transcendence.auth.domain.dto.AuthStateResult;
import org.springframework.security.access.prepost.PreAuthorize;
import com.ft_transcendence.auth.domain.dto.request.LoginRequest;
import com.ft_transcendence.auth.domain.dto.response.AuthResponse;
import com.ft_transcendence.auth.domain.dto.request.RegisterRequest;
import com.ft_transcendence.auth.domain.dto.response.UserProfileResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final UserMapper userMapper;
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request, 
            HttpServletRequest servletRequest) {

        UserAuth savedUser = authService.register(request);
        
        // Populate the active session properties directly inside the controller tier boundary
        HttpSession session = servletRequest.getSession(true);
        syncSessionAttributes(savedUser, session);

        return ResponseEntity.status(HttpStatus.CREATED).body(userMapper.toRegisterResponse(savedUser));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request, 
            HttpServletRequest servletRequest) {
        
        AuthStateResult stateResult = authService.login(request);

        HttpSession session = servletRequest.getSession(true);
        syncSessionAttributes(stateResult.user(), session);

        if ("AWAITING_MFA".equals(stateResult.status())) {
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(userMapper.toLoginResponse(stateResult));
        }

        return ResponseEntity.ok(userMapper.toLoginResponse(stateResult));
    }

    @GetMapping("/users")
    @PreAuthorize("hasAuthority('SCOPE_USER') or hasRole('USER')")
    public ResponseEntity<UserProfileResponse> getProfile(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        UserAuth userDetails = authService.getUserDetails(userId);
        
        return ResponseEntity.ok(userMapper.toProfileResponse(userDetails));
    }

    // ── WEB SESSION HANDSHAKE SYNC ──────────────────────────────────────────

    private void syncSessionAttributes(UserAuth user, HttpSession session) {
        List<String> roleStrings = user.getRoles().stream()
                .map(Enum::name)
                .toList();

        session.setAttribute("roles", roleStrings);
        session.setAttribute("userId", user.getUserId());
        session.setAttribute("isFullyAuthenticated", !user.is2faEnabled());
    }
}

```

## 📄 File: ./auth-service/src/main/java/com/ft_transcendence/auth/domain/controller/TwoFactorController.java
```java
package com.ft_transcendence.auth.domain.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.oauth2.jwt.Jwt;
import com.ft_transcendence.auth.domain.dto.response.MfaResponse;
import com.ft_transcendence.auth.domain.dto.request.MfaVerificationRequest;
import com.ft_transcendence.auth.domain.service.twofactor.TwoFactorService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.UUID;

@RestController
@RequestMapping("/2fa")
@RequiredArgsConstructor
public class TwoFactorController {

    private final TwoFactorService twoFactorService;

    @PostMapping("/setup")
    public ResponseEntity<MfaResponse> initiateMfaSetup(
            @RequestBody MfaVerificationRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        UUID userId = UUID.fromString(jwt.getSubject());
        MfaResponse response = twoFactorService.initiateSetup(request, userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/enable")
    public ResponseEntity<MfaResponse> finalizeMfaSetup(
            @RequestBody MfaVerificationRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        UUID userId = UUID.fromString(jwt.getSubject());
        MfaResponse response = twoFactorService.finalizeSetup(request, userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify")
    public ResponseEntity<MfaResponse> verifyLoginChallenge(
            @RequestBody MfaVerificationRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        String sessionId = jwt.getClaimAsString("sid");
        UUID userId = UUID.fromString(jwt.getSubject());

        MfaResponse response = twoFactorService.verifyLoginChallenge(request, userId, sessionId);
        return ResponseEntity.ok(response);
    }
}
```

## 📄 File: ./auth-service/src/main/java/com/ft_transcendence/auth/domain/controller/OAuth2Controller.java
```java
package com.ft_transcendence.auth.domain.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.oauth2.jwt.Jwt;
import com.ft_transcendence.auth.domain.service.OAuth2Service;
import org.springframework.security.access.prepost.PreAuthorize;
import com.ft_transcendence.auth.domain.dto.request.OAuth2SyncRequest;
import com.ft_transcendence.auth.domain.dto.request.OAuth2LinkRequest;
import com.ft_transcendence.auth.domain.dto.response.OAuth2UserSummary;
import com.ft_transcendence.auth.domain.dto.request.OAuth2UnlinkRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/oauth2")
public class OAuth2Controller {

    private final OAuth2Service oauth2Service;

    /**
     * Internal Back channel Synchronization.
     * Invoked stateless by the Gateway to find-or-create anonymous SSO sign-ins.
     */
    @PostMapping("/sync")
    public ResponseEntity<OAuth2UserSummary> syncOAuth2Users(@Valid @RequestBody OAuth2SyncRequest request) {

        OAuth2UserSummary summary = oauth2Service.syncUser(request);
        return ResponseEntity.ok(summary);
    }

    /**
     * Account Linking Endpoint.
     * Leverages the active authenticated principal context to link an identity safely.
     */
    @PostMapping("/link")
    @PreAuthorize("hasAuthority('SCOPE_USER') or hasRole('USER')")
    public ResponseEntity<Void> linkSocialAccount(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody OAuth2LinkRequest request) {

        UUID userId = UUID.fromString(jwt.getSubject());
        oauth2Service.linkAccount(userId, request.provider(), request.providerId());

        return ResponseEntity.ok().build();
    }

    /**
     * Account Unlinking Endpoint.
     * Severs a social identity link safely using the token context.
     */
    @PostMapping("/unlink")
    @PreAuthorize("hasAuthority('SCOPE_USER') or hasRole('USER')")
    public ResponseEntity<Void> unlinkSocialAccount(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody OAuth2UnlinkRequest request) {

        UUID userId = UUID.fromString(jwt.getSubject());
        oauth2Service.unlinkAccount(userId, request.provider());

        return ResponseEntity.ok().build();
    }
}
```

## 📄 File: ./auth-service/src/main/java/com/ft_transcendence/auth/domain/repository/UserAuthRepository.java
```java
package com.ft_transcendence.auth.domain.repository;

import java.util.UUID;
import java.util.Optional;

import com.ft_transcendence.auth.domain.model.UserAuth;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAuthRepository extends JpaRepository<UserAuth, Long> {

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    Optional<UserAuth> findByEmail(String email);

    Optional<UserAuth> findByUserId(UUID userId);

    Optional<UserAuth> findByUsername(String username);

    Optional<UserAuth> findByEmailOrUsername(String email, String username);


}
```

## 📄 File: ./auth-service/src/main/java/com/ft_transcendence/auth/domain/repository/UserIdentityRepository.java
```java
package com.ft_transcendence.auth.domain.repository;

import java.util.Optional;
import com.ft_transcendence.auth.domain.model.AuthProvider;
import com.ft_transcendence.auth.domain.model.UserIdentity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserIdentityRepository extends JpaRepository<UserIdentity, Long> {

    Optional<UserIdentity> findByProviderAndProviderId(AuthProvider provider, String providerId);
    
}
```

## 📄 File: ./auth-service/src/main/java/com/ft_transcendence/auth/domain/service/AuthService.java
```java
package com.ft_transcendence.auth.domain.service;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.ft_transcendence.auth.domain.model.UserAuth;
import org.springframework.security.core.Authentication;
import com.ft_transcendence.auth.domain.model.UserIdentity;
import com.ft_transcendence.auth.domain.model.AuthProvider;
import com.ft_transcendence.auth.domain.dto.AuthStateResult;
import com.ft_transcendence.auth.security.context.SecurityUser;
import org.springframework.transaction.annotation.Transactional;
import com.ft_transcendence.auth.domain.dto.request.LoginRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.ft_transcendence.auth.domain.dto.request.RegisterRequest;
import com.ft_transcendence.auth.domain.repository.UserAuthRepository;
import org.springframework.security.authentication.AuthenticationManager;
import com.ft_transcendence.auth.core.exception.ResourceNotFoundException;
import com.ft_transcendence.auth.core.exception.DuplicateResourceException;
import com.ft_transcendence.auth.domain.model.twofactor.TwoFactorMethodType;
import com.ft_transcendence.auth.domain.model.twofactor.UserTwoFactorMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.List;
import java.util.UUID;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final PasswordEncoder passwordEncoder;
    private final UserAuthRepository userAuthRepository;
    private final AuthenticationManager authenticationManager;

    /**
     * Registers a new account shell along with its initial LOCAL credentials profile.
     */
    @Transactional
    public UserAuth register(RegisterRequest request) {
        validateRegistrationUnique(request.username(), request.email());

        UserAuth userAuth = UserAuth.builder()
                .username(request.username())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .build();

        UserIdentity localIdentity = UserIdentity.builder()
                .user(userAuth)
                .provider(AuthProvider.LOCAL)
                .providerId(request.email())
                .lastLoginAt(LocalDateTime.now())
                .build();

        userAuth.addIdentity(localIdentity);
        
        return userAuthRepository.save(userAuth);
    }

    /**
     * Executes the primary credentials verification challenge sequence against the security manager.
     */
    @Transactional(readOnly = true)
    public AuthStateResult login(LoginRequest request) {
        Authentication authenticationToken = new UsernamePasswordAuthenticationToken(request.login(), request.password());
        Authentication authResult = authenticationManager.authenticate(authenticationToken);

        SecurityUser securityUser = (SecurityUser) authResult.getPrincipal();
        UserAuth userAuth = securityUser.userAuth();

        if (userAuth.is2faEnabled()) {
            List<TwoFactorMethodType> verifiedMethods = userAuth.getTwoFactorMethods().stream()
                    .filter(UserTwoFactorMethod::isVerified)
                    .map(UserTwoFactorMethod::getMethodType)
                    .toList();

            return new AuthStateResult("AWAITING_MFA", userAuth, verifiedMethods);
        }

        return new AuthStateResult("AUTHENTICATED", userAuth, List.of());
    }

    /**
     * Fetches details for a unique user ID.
     */
    @Transactional(readOnly = true)
    public UserAuth getUserDetails(UUID userId) {
        return userAuthRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
    }

    // ── PRIVATE GUARD BLOCKS ────────────────────────────────────────────────

    private void validateRegistrationUnique(String username, String email) {
        if (userAuthRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("Email already exists");
        }
        if (userAuthRepository.existsByUsername(username)) {
            throw new DuplicateResourceException("Username already exists");
        }
    }
}

```

## 📄 File: ./auth-service/src/main/java/com/ft_transcendence/auth/domain/service/twofactor/TwoFactorStrategy.java
```java
package com.ft_transcendence.auth.domain.service.twofactor;

import com.ft_transcendence.auth.domain.dto.response.MfaResponse;
import com.ft_transcendence.auth.domain.model.UserAuth;
import com.ft_transcendence.auth.domain.model.twofactor.UserTwoFactorMethod;
import com.ft_transcendence.auth.domain.model.twofactor.TwoFactorMethodType;

public interface TwoFactorStrategy {

    /**
     * Orchestrates the initialization channel requirements for this specific 2FA method type.
     * Stuffs its unique tracking criteria directly into the relational method entity row.
     * Returns ONLY the custom setup payload configuration details (e.g. backup arrays, keys, etc.).
     */
    MfaResponse.SetupDetails initiate(UserAuth user, UserTwoFactorMethod method);

    /**
     * Verifies the user-supplied challenge code.
     */
    boolean verify(UserTwoFactorMethod method, String code);

    /**
     * Returns the strongly typed Enum variant handled by this strategy.
     */
    TwoFactorMethodType getType();
}
```

## 📄 File: ./auth-service/src/main/java/com/ft_transcendence/auth/domain/service/twofactor/TwoFactorStrategyFactory.java
```java
package com.ft_transcendence.auth.domain.service.twofactor;

import com.ft_transcendence.auth.domain.model.twofactor.TwoFactorMethodType;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class TwoFactorStrategyFactory {

    private final Map<TwoFactorMethodType, TwoFactorStrategy> strategies;

    public TwoFactorStrategyFactory(List<TwoFactorStrategy> strategyList) {
        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(TwoFactorStrategy::getType, strategy -> strategy));
    }

    public TwoFactorStrategy getStrategy(TwoFactorMethodType type) {
        TwoFactorStrategy strategy = strategies.get(type);
        if (strategy == null) {
            throw new IllegalArgumentException("Unsupported 2FA strategy handler for type: " + type);
        }
        return strategy;
    }
}
```

## 📄 File: ./auth-service/src/main/java/com/ft_transcendence/auth/domain/service/twofactor/TwoFactorService.java
```java
package com.ft_transcendence.auth.domain.service.twofactor;

import com.ft_transcendence.auth.core.exception.InvalidCredentialsException;
import com.ft_transcendence.auth.core.exception.MfaException;
import com.ft_transcendence.auth.core.exception.ResourceNotFoundException;
import com.ft_transcendence.auth.domain.dto.request.MfaVerificationRequest;
import com.ft_transcendence.auth.domain.dto.response.MfaResponse;
import com.ft_transcendence.auth.domain.model.UserAuth;
import com.ft_transcendence.auth.domain.model.twofactor.TwoFactorMethodType;
import com.ft_transcendence.auth.domain.model.twofactor.UserTwoFactorMethod;
import com.ft_transcendence.auth.domain.repository.UserAuthRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.session.data.redis.RedisIndexedSessionRepository;
import org.springframework.session.data.redis.RedisIndexedSessionRepository.RedisSession;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TwoFactorService {

    private final UserAuthRepository userAuthRepository;
    private final TwoFactorStrategyFactory strategyFactory;
    private final RedisIndexedSessionRepository sessionRepository;

    /**
     * Phase 1: Initiates a new pending multi-factor registration context flow.
     */
    @Transactional
    public MfaResponse initiateSetup(MfaVerificationRequest request, UUID userId) {
        UserAuth user = fetchUserAuth(userId);
        TwoFactorMethodType type = request.methodType();

        validateSetupEligible(user, type);

        // Clean out any historical stale, unverified challenge configurations of this type
        user.getTwoFactorMethods().removeIf(method -> method.getMethodType() == type && !method.isVerified());

        UserTwoFactorMethod pendingMethod = UserTwoFactorMethod.builder()
                .methodType(type)
                .isVerified(false)
                .build();

        MfaResponse.SetupDetails dynamicDetails = strategyFactory.getStrategy(type).initiate(user, pendingMethod);

        user.addTwoFactorMethod(pendingMethod);
        userAuthRepository.save(user);

        return MfaResponse.builder()
                .status("SETUP_INITIATED")
                .message("Multi-factor setup verification challenge initiated successfully. Complete the verification handshake sequence to protect your account.")
                .setupDetails(dynamicDetails)
                .build();
    }

    /**
     * Phase 2: Finalizes linking a new 2FA channel after verifying the initial code challenge.
     */
    @Transactional
    public MfaResponse finalizeSetup(MfaVerificationRequest request, UUID userId) {
        UserAuth user = fetchUserAuth(userId);
        TwoFactorMethodType type = request.methodType();

        UserTwoFactorMethod methodContext = fetchPendingMethod(user, type);
        verifyChallengeToken(methodContext, type, request.code());

        methodContext.setVerified(true);
        methodContext.setLastUsedAt(LocalDateTime.now());
        user.set2faEnabled(true);

        userAuthRepository.save(user);
        log.info("User [{}] successfully enabled multi-factor channel: {}", user.getUsername(), type);

        return MfaResponse.builder()
                .status("ENABLED")
                .message("Multi-factor authentication option verified and permanently locked to your identity profile successfully.")
                .build();
    }

    /**
     * Phase 3: Validates a user's step-up login verification challenge code and upgrades the active Redis session.
     */
    @Transactional
    public MfaResponse verifyLoginChallenge(MfaVerificationRequest request, UUID userId, String sessionId) {
        validateSessionIdPresent(sessionId);

        UserAuth user = fetchUserAuth(userId);
        TwoFactorMethodType type = request.methodType();

        UserTwoFactorMethod methodContext = fetchVerifiedMethod(user, type);
        verifyChallengeToken(methodContext, type, request.code());

        methodContext.setLastUsedAt(LocalDateTime.now());
        userAuthRepository.save(user);

        upgradeSharedRedisSession(sessionId);
        log.info("User [{}] successfully verified step-up login challenge via: {}", user.getUsername(), type);

        return MfaResponse.builder()
                .status("VERIFIED")
                .message("MFA verification challenge cleared successfully. Edge gateway core microservice network proxy unblocked.")
                .build();
    }

    // ── PRIVATE DOMAIN VALIDATIONS & LOOKUPS ─────────────────────────────────

    private UserAuth fetchUserAuth(UUID userId) {
        return userAuthRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found for ID: " + userId));
    }

    private UserTwoFactorMethod fetchPendingMethod(UserAuth user, TwoFactorMethodType type) {
        return user.getTwoFactorMethods().stream()
                .filter(method -> method.getMethodType() == type && !method.isVerified())
                .findFirst()
                .orElseThrow(() -> new MfaException("No pending configuration initialization sequence found for type: " + type + ". Run the /setup endpoint first."));
    }

    private UserTwoFactorMethod fetchVerifiedMethod(UserAuth user, TwoFactorMethodType type) {
        return user.getTwoFactorMethods().stream()
                .filter(method -> method.getMethodType() == type && method.isVerified())
                .findFirst()
                .orElseThrow(() -> new MfaException("No active verified " + type + " security mechanism is configured for this account."));
    }

    private void verifyChallengeToken(UserTwoFactorMethod methodContext, TwoFactorMethodType type, String code) {
        boolean isValid = strategyFactory.getStrategy(type).verify(methodContext, code);
        if (!isValid) {
            throw new InvalidCredentialsException("The verification challenge code supplied is incorrect or has expired.");
        }
    }

    private void upgradeSharedRedisSession(String sessionId) {
        RedisSession originalSession = sessionRepository.findById(sessionId);
        if (originalSession == null) {
            throw new ResourceNotFoundException("Original authentication session tracking context");
        }

        originalSession.setAttribute("isFullyAuthenticated", true);
        sessionRepository.save(originalSession);
    }

    private void validateSetupEligible(UserAuth user, TwoFactorMethodType type) {
        boolean alreadyExists = user.getTwoFactorMethods().stream()
                .anyMatch(method -> method.getMethodType() == type && method.isVerified());
        if (alreadyExists) {
            throw new MfaException("The multi-factor configuration option " + type + " is already verified active on your account.");
        }
    }

    private void validateSessionIdPresent(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            throw new MfaException("Session reference context pointer missing from structural transaction payload parameters.");
        }
    }
}

```

## 📄 File: ./auth-service/src/main/java/com/ft_transcendence/auth/domain/service/twofactor/TotpTwoFactorStrategy.java
```java
package com.ft_transcendence.auth.domain.service.twofactor;

import com.ft_transcendence.auth.domain.dto.response.MfaResponse;
import com.ft_transcendence.auth.domain.model.twofactor.TwoFactorMethodType;
import com.ft_transcendence.auth.domain.model.UserAuth;
import com.ft_transcendence.auth.domain.model.twofactor.UserTwoFactorMethod;

import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import dev.samstevens.totp.code.CodeGenerator;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.code.DefaultCodeVerifier;

import org.springframework.stereotype.Component;

@Component
public class TotpTwoFactorStrategy implements TwoFactorStrategy {

    // Initialize the highly modular components provided by dev.samstevens.totp
    private final SecretGenerator secretGenerator = new DefaultSecretGenerator();
    private final TimeProvider timeProvider = new SystemTimeProvider();
    private final CodeGenerator codeGenerator = new DefaultCodeGenerator();

    // Sets up a default 30-second window with a ±1 step discrepancy tolerance cushion
    private final CodeVerifier codeVerifier = new DefaultCodeVerifier(codeGenerator, timeProvider);

    @Override
    public MfaResponse.SetupDetails initiate(UserAuth user, UserTwoFactorMethod method) {
        // 1. Generate a modern, cryptographically secure 32-character Base32 secret string
        String secureSecret = secretGenerator.generate();
        method.setSecretKey(secureSecret);

        // 2. Build the standard multi-factor parameters data container mapping
        QrData qrData = new QrData.Builder()
                .label(user.getEmail())
                .secret(secureSecret)
                .issuer("Ft_chess")
                .digits(6)
                .period(30)
                .build();

        String qrCodeUrl = qrData.getUri();

        // 4. Return the inner polymorphic data payload seamlessly
        return MfaResponse.SetupDetails.builder()
                .methodType(getType())
                .secretKey(secureSecret)
                .qrCodeUrl(qrCodeUrl)
                .build();
    }

    @Override
    public boolean verify(UserTwoFactorMethod method, String code) {
        // Validation check: reject empty strings or inputs that aren't numeric blocks
        if (code == null || !code.matches("\\d{6}")) {
            return false;
        }

        // Executes code check directly on the record's secret row parameters data string
        return codeVerifier.isValidCode(method.getSecretKey(), code);
    }

    @Override
    public TwoFactorMethodType getType() {
        return TwoFactorMethodType.TOTP;
    }
}
```

## 📄 File: ./auth-service/src/main/java/com/ft_transcendence/auth/domain/service/OAuth2Service.java
```java
package com.ft_transcendence.auth.domain.service;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.ft_transcendence.auth.domain.model.UserAuth;
import com.ft_transcendence.auth.domain.model.AuthProvider;
import com.ft_transcendence.auth.domain.model.UserIdentity;
import org.springframework.transaction.annotation.Transactional;
import com.ft_transcendence.auth.core.exception.BadRequestException;
import com.ft_transcendence.auth.domain.repository.UserAuthRepository;
import com.ft_transcendence.auth.domain.dto.request.OAuth2SyncRequest;
import com.ft_transcendence.auth.domain.dto.response.OAuth2UserSummary;
import com.ft_transcendence.auth.core.exception.ResourceNotFoundException;
import com.ft_transcendence.auth.domain.repository.UserIdentityRepository;
import com.ft_transcendence.auth.core.exception.DuplicateResourceException;

import java.util.UUID;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuth2Service {

    private final UserAuthRepository userAuthRepository;
    private final UserIdentityRepository userIdentityRepository;


    /**
     * Authenticates an existing social user or auto-provisions a new shell account.
     */
    @Transactional
    public OAuth2UserSummary syncUser(OAuth2SyncRequest request) {
        return userIdentityRepository.findByProviderAndProviderId(request.provider(), request.providerId())

                .map(identity -> {

                    identity.setLastLoginAt(LocalDateTime.now());

                    return OAuth2UserSummary.fromEntity(identity.getUser());
                })

                .orElseGet(() -> autoRegisterUser(request));
    }

    /**
     * Links a new social provider identity to an already authenticated local account context.
     */
    @Transactional
    public void linkAccount(UUID userId, AuthProvider provider, String providerId) {
        UserAuth user = fetchUserAuth(userId);
        validateNewLinkEligible(user, provider, providerId);

        UserIdentity newIdentity = UserIdentity.builder()
                .user(user)
                .provider(provider)
                .providerId(providerId)
                .lastLoginAt(LocalDateTime.now())
                .build();

        user.addIdentity(newIdentity);
        userAuthRepository.save(user);
        log.info("Successfully linked external provider [{}] to user UUID [{}]", provider, userId);
    }

    /**
     * severs an external identity association from an account while preventing lockouts.
     */
    @Transactional
    public void unlinkAccount(UUID userId, AuthProvider provider) {
        UserAuth user = fetchUserAuth(userId);
        validateUnlinkSafe(user, provider);

        UserIdentity targetIdentity = user.getIdentities().stream()
                .filter(id -> id.getProvider() == provider)
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Linked provider identity"));

        user.getIdentities().remove(targetIdentity);
        userIdentityRepository.delete(targetIdentity);
        userAuthRepository.save(user);
        log.info("Successfully unlinked external provider [{}] from user UUID [{}]", provider, userId);
    }

    // ── PRIVATE DOMAIN LOGIC EXTRACTIONS ────────────────────────────────────

    private UserAuth fetchUserAuth(UUID userId) {
        return userAuthRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User"));
    }

    private OAuth2UserSummary autoRegisterUser(OAuth2SyncRequest request) {
        if (userAuthRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("An account with this email address already exists via local credentials.");
        }

        UserAuth newAccount = UserAuth.builder()
                .username(generateUniqueUsername(request.name()))
                .email(request.email())
                .build();

        UserIdentity socialIdentity = UserIdentity.builder()
                .user(newAccount)
                .provider(request.provider())
                .providerId(request.providerId())
                .lastLoginAt(LocalDateTime.now())
                .build();

        newAccount.addIdentity(socialIdentity);
        return OAuth2UserSummary.fromEntity(userAuthRepository.save(newAccount));
    }

    private String generateUniqueUsername(String rawName) {
        String baseUsername = rawName.replaceAll("\\s+", "_").toLowerCase();
        if (!userAuthRepository.existsByUsername(baseUsername)) {
            return baseUsername;
        }
        return baseUsername + "_" + UUID.randomUUID().toString().substring(0, 5);
    }

    private void validateNewLinkEligible(UserAuth user, AuthProvider provider, String providerId) {
        if (provider == AuthProvider.LOCAL) {
            throw new BadRequestException("Cannot manually link LOCAL credentials.");
        }

        boolean identityTaken = userIdentityRepository.findByProviderAndProviderId(provider, providerId).isPresent();
        if (identityTaken) {
            throw new DuplicateResourceException("This " + provider + " account is already linked to another user.");
        }

        boolean alreadyLinked = user.getIdentities().stream().anyMatch(id -> id.getProvider() == provider);
        if (alreadyLinked) {
            throw new BadRequestException("You have already linked a " + provider + " account.");
        }
    }

    private void validateUnlinkSafe(UserAuth user, AuthProvider provider) {
        if (provider == AuthProvider.LOCAL) {
            throw new BadRequestException("Cannot unlink LOCAL credentials.");
        }

        boolean hasPassword = user.getPassword() != null && !user.getPassword().isBlank();
        if (!hasPassword && user.getIdentities().size() <= 1) {
            throw new BadRequestException("Cannot unlink this provider. You must first set up a local password or link another social login to prevent account lockout.");
        }
    }
}

```

## 📄 File: ./auth-service/src/main/java/com/ft_transcendence/auth/domain/model/UserAuth.java
```java
package com.ft_transcendence.auth.domain.model;

import com.ft_transcendence.auth.domain.model.twofactor.UserTwoFactorMethod;
import lombok.*;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_auth")
@EntityListeners(AuditingEntityListener.class)
public class UserAuth {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Builder.Default
    @Column(unique = true, nullable = false, updatable = false, name = "user_id")
    private UUID userId = UUID.randomUUID();

    @Column(unique = true, nullable = false)
    private String email;

    @Column(unique = true, nullable = false)
    private String username;

    private String password;

    @Builder.Default
    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_auth_id"))
    private Set<UserRole> roles = new HashSet<>(Set.of(UserRole.USER));

    // === Account Lifecycle ===
    @Builder.Default
    private boolean enabled = true;

    @Builder.Default
    @Column(name = "account_locked")
    private boolean accountLocked = false;

    @Builder.Default
    private boolean deleted = false;

    // === Security Hardening ===
    @Builder.Default
    @Column(name = "auth_version")
    private Integer authVersion = 0;

    // === 2FA ===
    @Builder.Default
    @Column(name = "is_2fa_enabled")
    private boolean is2faEnabled = false;

    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserTwoFactorMethod> twoFactorMethods = new ArrayList<>();

    public void addTwoFactorMethod(UserTwoFactorMethod method) {
        twoFactorMethods.add(method);
        method.setUser(this);
    }

    public void removeTwoFactorMethod(UserTwoFactorMethod method) {
        twoFactorMethods.remove(method);
        method.setUser(null);
    }

    // === OAuth ===
    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserIdentity> identities = new ArrayList<>();

    public void addIdentity(UserIdentity identity) {
        identities.add(identity);
        identity.setUser(this);
    }

    // === Auditing ===
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    private Integer version;

}

```

## 📄 File: ./auth-service/src/main/java/com/ft_transcendence/auth/domain/model/UserIdentity.java
```java
package com.ft_transcendence.auth.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(
    name = "user_identity",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_provider_provider_id",
        columnNames = {"provider", "provider_id"}
    )
)
public class UserIdentity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAuth user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuthProvider provider;

    @Column(name = "provider_id", nullable = false)
    private String providerId;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    // --- Auditing ---
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

```

## 📄 File: ./auth-service/src/main/java/com/ft_transcendence/auth/domain/model/UserRole.java
```java
package com.ft_transcendence.auth.domain.model;

public enum UserRole {
    USER,
    ADMIN
}

```

## 📄 File: ./auth-service/src/main/java/com/ft_transcendence/auth/domain/model/AuthProvider.java
```java
package com.ft_transcendence.auth.domain.model;

public enum AuthProvider {
    LOCAL,
    GOOGLE,
    FORTYTWO
}
```

## 📄 File: ./auth-service/src/main/java/com/ft_transcendence/auth/domain/model/twofactor/TwoFactorMethodType.java
```java
package com.ft_transcendence.auth.domain.model.twofactor;

public enum TwoFactorMethodType {
    TOTP,
    BACKUP_CODES
}
```

## 📄 File: ./auth-service/src/main/java/com/ft_transcendence/auth/domain/model/twofactor/UserTwoFactorMethod.java
```java
package com.ft_transcendence.auth.domain.model.twofactor;

import com.ft_transcendence.auth.domain.model.UserAuth;
import lombok.*;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_two_factor_methods")
@EntityListeners(AuditingEntityListener.class)
public class UserTwoFactorMethod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAuth user;

    @Enumerated(EnumType.STRING)
    @Column(name = "method_type", nullable = false)
    private TwoFactorMethodType methodType;

    @Column(name = "secret_key", nullable = false)
    private String secretKey; // Holds the encrypted secret seed configuration payload

    @Builder.Default
    @Column(name = "is_verified", nullable = false)
    private boolean isVerified = false; // Set to true once the user successfully satisfies the registration challenge

    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt; // Track usage statistics for audit trails

    // --- Auditing ---
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
```

## 📄 File: ./auth-service/src/main/java/com/ft_transcendence/auth/domain/dto/request/LoginRequest.java
```java
package com.ft_transcendence.auth.domain.dto.request;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

        @NotBlank(message = "Login (Email or Username) is required") String login,

        @NotBlank(message = "Password is required") String password) {
}
```

## 📄 File: ./auth-service/src/main/java/com/ft_transcendence/auth/domain/dto/request/RegisterRequest.java
```java
package com.ft_transcendence.auth.domain.dto.request;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(
        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
        @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, numbers and underscores")
        String username,

        @NotBlank(message = "Email is required")
        @Email(message = "Please provide a valid email address")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 100, message = "Password must be at least 8 characters long and under 100 characters")
        @Pattern(
                regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).*$",
                message = "Password must contain at least one digit, one lowercase, one uppercase, and one special character"
        )
        String password
) {}
```

## 📄 File: ./auth-service/src/main/java/com/ft_transcendence/auth/domain/dto/request/MfaVerificationRequest.java
```java
package com.ft_transcendence.auth.domain.dto.request;

import com.ft_transcendence.auth.domain.model.twofactor.TwoFactorMethodType;

public record MfaVerificationRequest(
        TwoFactorMethodType methodType,
        String code
) {}

```

## 📄 File: ./auth-service/src/main/java/com/ft_transcendence/auth/domain/dto/request/OAuth2SyncRequest.java
```java
package com.ft_transcendence.auth.domain.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import com.ft_transcendence.auth.domain.model.AuthProvider;

public record OAuth2SyncRequest(
        @NotNull(message = "Authentication provider type is required")
        AuthProvider provider,

        @NotBlank(message = "Provider unique identifier (sub ID) cannot be blank")
        String providerId,

        @NotBlank(message = "Email address cannot be blank")
        @Email(message = "Invalid email address format supplied")
        String email,

        @NotBlank(message = "User full name cannot be blank")
        String name
) {}
```

## 📄 File: ./auth-service/src/main/java/com/ft_transcendence/auth/domain/dto/request/OAuth2LinkRequest.java
```java
package com.ft_transcendence.auth.domain.dto.request;

import lombok.Builder;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import com.ft_transcendence.auth.domain.model.AuthProvider;

@Builder
public record OAuth2LinkRequest(
        @NotNull(message = "Authentication provider type is required")
        AuthProvider provider,

        @NotBlank(message = "Provider unique identifier (sub ID) cannot be blank")
        String providerId
) {}

```

## 📄 File: ./auth-service/src/main/java/com/ft_transcendence/auth/domain/dto/request/OAuth2UnlinkRequest.java
```java
package com.ft_transcendence.auth.domain.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import com.ft_transcendence.auth.domain.model.AuthProvider;

@Builder
public record OAuth2UnlinkRequest(
        @NotNull(message = "Authentication provider type is required")
        AuthProvider provider
) {}

```

## 📄 File: ./auth-service/src/main/java/com/ft_transcendence/auth/domain/dto/response/AuthResponse.java
```java
package com.ft_transcendence.auth.domain.dto.response;

import java.util.List;
import java.util.UUID;
import lombok.Builder;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.ft_transcendence.auth.domain.model.twofactor.TwoFactorMethodType;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AuthResponse(
        String status,                      // "AUTHENTICATED" or "AWAITING_MFA"
        UserSummary user,                   // Populated ONLY when status is "AUTHENTICATED"
        MfaDetails mfaDetails               // Populated ONLY when status is "AWAITING_MFA"
) {

    @Builder
    public record UserSummary(
            UUID userId,
            String username,
            String email,
            List<String> roles
    ) {}

    @Builder
    public record MfaDetails(
            List<TwoFactorMethodType> availableMethods
    ) {}
}
```

## 📄 File: ./auth-service/src/main/java/com/ft_transcendence/auth/domain/dto/response/MfaResponse.java
```java
package com.ft_transcendence.auth.domain.dto.response;

import lombok.Builder;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.ft_transcendence.auth.domain.model.twofactor.TwoFactorMethodType;

import java.util.List;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL) // Dynamically strips unpopulated null blocks out of the outbound TCP stream
public record MfaResponse(
        String status,                // "SETUP_INITIATED", "ENABLED", or "VERIFIED"
        String message,               // Human-readable localized transaction confirmation text
        SetupDetails setupDetails     // Multi-method setup configuration block wrapper
) {

    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record SetupDetails(
            TwoFactorMethodType methodType,

            // === TOTP Authenticator Apps Channel Specifics ===
            String secretKey,
            String qrCodeUrl,

            // === Out-of-Band Delivery Channels (SMS / Email) Specifics ===
            String targetDestination,     // Holds a masked destination string (e.g., "+*******12" or "l******d@gmail.com")

            // === Backup Recovery Codes Channel Specifics ===
            List<String> backupCodes      // Array containing generated recovery hashes
    ) {}
}
```

## 📄 File: ./auth-service/src/main/java/com/ft_transcendence/auth/domain/dto/response/OAuth2UserSummary.java
```java
package com.ft_transcendence.auth.domain.dto.response;

import lombok.Builder;
import com.ft_transcendence.auth.domain.model.UserAuth;

import java.util.List;
import java.util.UUID;

@Builder
public record OAuth2UserSummary(
        UUID userId,
        List<String> roles
) {
    public static OAuth2UserSummary fromEntity(UserAuth user) {
        return OAuth2UserSummary.builder()
                .userId(user.getUserId())
                .roles(user.getRoles().stream().map(Enum::name).toList())
                .build();
    }

}
```

## 📄 File: ./auth-service/src/main/java/com/ft_transcendence/auth/domain/dto/response/UserProfileResponse.java
```java
package com.ft_transcendence.auth.domain.dto.response;

import lombok.Builder;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.ft_transcendence.auth.domain.model.twofactor.TwoFactorMethodType;
import com.ft_transcendence.auth.domain.model.AuthProvider;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserProfileResponse(
        UUID userId,
        String username,
        String email,
        List<String> roles,
        boolean enabled,
        boolean is2faEnabled,
        List<MfaMethodSummary> twoFactorMethods,
        List<IdentitySummary> identities,
        LocalDateTime createdAt
) {

    @Builder
    public record MfaMethodSummary(
            TwoFactorMethodType methodType,
            boolean isVerified,
            LocalDateTime lastUsedAt
    ) {}

    @Builder
    public record IdentitySummary(
            AuthProvider provider,
            String providerId,
            LocalDateTime lastLoginAt
    ) {}
}

```

## 📄 File: ./auth-service/src/main/java/com/ft_transcendence/auth/domain/dto/AuthStateResult.java
```java
package com.ft_transcendence.auth.domain.dto;

import com.ft_transcendence.auth.domain.model.UserAuth;
import com.ft_transcendence.auth.domain.model.twofactor.TwoFactorMethodType;

import java.util.List;

public record AuthStateResult(
        String status,
        UserAuth user,
        List<TwoFactorMethodType> availableMethods
) {}
```

## 📄 File: ./auth-service/src/main/java/com/ft_transcendence/auth/domain/mapper/UserMapper.java
```java
package com.ft_transcendence.auth.domain.mapper;

import com.ft_transcendence.auth.domain.dto.AuthStateResult;
import org.springframework.stereotype.Component;
import com.ft_transcendence.auth.domain.model.UserAuth;
import com.ft_transcendence.auth.domain.dto.response.AuthResponse;
import com.ft_transcendence.auth.domain.dto.response.UserProfileResponse;

import java.util.List;

@Component
public class UserMapper {

    public AuthResponse toRegisterResponse(UserAuth user) {
        return AuthResponse.builder()
                .status("AUTHENTICATED")
                .user(mapToSummary(user))
                .build();
    }

    public AuthResponse toLoginResponse(AuthStateResult result) {
        if ("AWAITING_MFA".equals(result.status())) {
            return AuthResponse.builder()
                    .status("AWAITING_MFA")
                    .mfaDetails(AuthResponse.MfaDetails.builder()
                            .availableMethods(result.availableMethods())
                            .build())
                    .build();
        }

        return AuthResponse.builder()
                .status("AUTHENTICATED")
                .user(mapToSummary(result.user()))
                .build();
    }

    public UserProfileResponse toProfileResponse(UserAuth user) {
        List<String> userRoles = user.getRoles().stream()
                .map(Enum::name)
                .toList();

        List<UserProfileResponse.MfaMethodSummary> mfaSummaries = user.getTwoFactorMethods().stream()
                .map(m -> UserProfileResponse.MfaMethodSummary.builder()
                        .methodType(m.getMethodType())
                        .isVerified(m.isVerified())
                        .lastUsedAt(m.getLastUsedAt())
                        .build())
                .toList();

        List<UserProfileResponse.IdentitySummary> identitySummaries = user.getIdentities().stream()
                .map(i -> UserProfileResponse.IdentitySummary.builder()
                        .provider(i.getProvider())
                        .providerId(i.getProviderId())
                        .lastLoginAt(i.getLastLoginAt())
                        .build())
                .toList();

        return UserProfileResponse.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(userRoles)
                .enabled(user.isEnabled())
                .is2faEnabled(user.is2faEnabled())
                .twoFactorMethods(mfaSummaries)
                .identities(identitySummaries)
                .createdAt(user.getCreatedAt())
                .build();
    }

    private AuthResponse.UserSummary mapToSummary(UserAuth user) {
        List<String> userRoles = user.getRoles().stream()
                .map(Enum::name)
                .toList();

        return AuthResponse.UserSummary.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(userRoles)
                .build();
    }
}
```

## 📄 File: ./auth-service/src/main/resources/application-docker.yaml
```yaml
spring:
  config:
    import: "optional:configserver:https://config-server:${CONFIG_SERVER_PORT}"
  cloud:
    config:
      tls:
        enabled: true
        key-store-type: PKCS12
        trust-store-type: PKCS12
        key-store: "file:/app/certs/services/${spring.application.name}/${spring.application.name}.p12"
        key-store-password: "${CERT_PASSWORD}"
        key-password: "${CERT_PASSWORD}"
        trust-store: "file:/app/certs/truststore/truststore.p12"
        trust-store-password: "${CERT_PASSWORD}"
```

## 📄 File: ./auth-service/src/main/resources/application-dev.yaml
```yaml
spring:
  config:
    import: "optional:configserver:https://localhost:${CONFIG_SERVER_PORT}"
  cloud:
    config:
      tls:
        enabled: true
        key-store-type: PKCS12
        trust-store-type: PKCS12
        key-store: "file:${CERT_DIR_PATH}/services/${spring.application.name}/${spring.application.name}.p12"
        key-store-password: "${CERT_PASSWORD}"
        key-password: "${CERT_PASSWORD}"
        trust-store: "file:${CERT_DIR_PATH}/truststore/truststore.p12"
        trust-store-password: "${CERT_PASSWORD}"



```

## 📄 File: ./auth-service/src/main/resources/application.yaml
```yaml
spring:
  application:
    name: auth-service

```

## 📄 File: ./auth-service/src/test/java/com/ft_transcendence/auth/AuthServiceApplicationTests.java
```java
package com.ft_transcendence.auth;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AuthServiceApplicationTests {

    @Test
    void contextLoads() {
    }

}

```

## 📄 File: ./auth-service/pom.xml
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>4.0.6</version>
        <relativePath/> <!-- lookup parent from repository -->
    </parent>
    <groupId>com.ft_transcendence</groupId>
    <artifactId>auth-service</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>auth-service</name>
    <description>auth-service</description>
    <url/>
    <licenses>
        <license/>
    </licenses>
    <developers>
        <developer/>
    </developers>
    <scm>
        <connection/>
        <developerConnection/>
        <tag/>
        <url/>
    </scm>
    <properties>
        <java.version>21</java.version>
        <spring-cloud.version>2025.1.1</spring-cloud.version>
        <maven.compiler.target>21</maven.compiler.target>
    </properties>
    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-dependencies</artifactId>
                <version>${spring-cloud.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-webmvc</artifactId>
        </dependency>

        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-webmvc-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security-oauth2-client</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security-oauth2-resource-server</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.security</groupId>
            <artifactId>spring-security-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-config</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-session-data-redis</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis</artifactId>
        </dependency>
        <dependency>
            <groupId>dev.samstevens.totp</groupId>
            <artifactId>totp</artifactId>
            <version>1.7.1</version>
            <exclusions>
                <exclusion>
                    <groupId>com.google.zxing</groupId>
                    <artifactId>javase</artifactId>
                </exclusion>
            </exclusions>
        </dependency>
        <dependency>
            <groupId>org.springdoc</groupId>
            <artifactId>springdoc-openapi-starter-webmvc-api</artifactId>
            <version>3.0.3</version>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <executions>
                    <execution>
                        <id>default-compile</id>
                        <phase>compile</phase>
                        <goals>
                            <goal>compile</goal>
                        </goals>
                        <configuration>
                            <annotationProcessorPaths>
                                <path>
                                    <groupId>org.springframework.boot</groupId>
                                    <artifactId>spring-boot-configuration-processor</artifactId>
                                </path>
                                <path>
                                    <groupId>org.projectlombok</groupId>
                                    <artifactId>lombok</artifactId>
                                </path>
                            </annotationProcessorPaths>
                        </configuration>
                    </execution>
                    <execution>
                        <id>default-testCompile</id>
                        <phase>test-compile</phase>
                        <goals>
                            <goal>testCompile</goal>
                        </goals>
                        <configuration>
                            <annotationProcessorPaths>
                                <path>
                                    <groupId>org.projectlombok</groupId>
                                    <artifactId>lombok</artifactId>
                                </path>
                            </annotationProcessorPaths>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>

</project>

```

## 📄 File: ./auth-service/Dockerfile
```dockerfile
# ==========================================
# BUILD STAGE
# ==========================================
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /app

# Cache Maven dependency layers for rapid rebuilds
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source and build
COPY src ./src
RUN mvn clean package -DskipTests

# ==========================================
# RUN STAGE
# ==========================================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Install curl for robust healthcheck support
RUN apk add --no-cache curl

# Copy the built jar from the builder stage
COPY --from=builder /app/target/*.jar app.jar

# Run the Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]

```

## 📄 File: ./config-repo/application-dev.yaml
```yaml
# ==========================================
#  Application Dev Configuration
# ==========================================

spring:
  ssl:
    bundle:
      jks:
        microservice-bundle:
          keystore:
            location: "file:${CERT_DIR_PATH}/services/${spring.application.name}/${spring.application.name}.p12"
          truststore:
            location: "file:${CERT_DIR_PATH}/truststore/truststore.p12"

eureka:
  instance:
    hostname: localhost
  client:
    service-url:
      defaultZone: https://localhost:${EUREKA_PORT}/eureka/
    tls:
      key-store: "file:${CERT_DIR_PATH}/services/${spring.application.name}/${spring.application.name}.p12"
      trust-store: "file:${CERT_DIR_PATH}/truststore/truststore.p12"
```

## 📄 File: ./config-repo/application-docker.yaml
```yaml
# ==========================================
#  Application Docker Configuration
# ==========================================

spring:
  ssl:
    bundle:
      jks:
        microservice-bundle:
          keystore:
            location: "file:/app/certs/services/${spring.application.name}/${spring.application.name}.p12"
          truststore:
            location: "file:/app/certs/truststore/truststore.p12"

eureka:
  instance:
    hostname: ${spring.application.name}
  client:
    service-url:
      defaultZone: https://eureka-server:${EUREKA_PORT}/eureka/
    tls:
      key-store: "file:/app/certs/services/${spring.application.name}/${spring.application.name}.p12"
      trust-store: "file:/app/certs/truststore/truststore.p12"
```

## 📄 File: ./config-repo/application.yaml
```yaml
# ==========================================
# GLOBAL MICROSERVICE CONFIGURATION
# ==========================================

# == 1. Centralized Modern SSL Bundles ==
spring:
  ssl:
    bundle:
      jks:
        microservice-bundle:
          key:
            alias: "${spring.application.name}"
            password: "${CERT_PASSWORD}"
          keystore:
            password: "${CERT_PASSWORD}"
            type: "PKCS12"
          truststore:
            password: "${CERT_PASSWORD}"
            type: "PKCS12"
  mvc:
    problemdetails:
      enabled: true
  webflux:
    problemdetails:
      enabled: true

# == 2. Inbound Server Configuration (Uses Modern Bundle) ==
server:
  ssl:
    enabled: true
    client-auth: need
    bundle: microservice-bundle # Tomcat/Netty use the bundle natively
  error:
    include-stacktrace: never
    include-message: always
    include-binding-errors: always
    include-exception: false

# == 3. Eureka Discovery Configuration (Hybrid Layer) ==
eureka:
  instance:
    prefer-ip-address: false
    secure-port-enabled: true
    non-secure-port-enabled: false
  client:
    fetch-registry: true
    register-with-eureka: true
    tls:
      enabled: true
      # Old-school properties provided explicitly for legacy client/server registration stability
      key-password: "${CERT_PASSWORD}"
      key-store-password: "${CERT_PASSWORD}"
      key-store-type: "PKCS12"
      trust-store-password: "${CERT_PASSWORD}"
      trust-store-type: "PKCS12"

# == 4. Observability & Logging ==
management:
  endpoints:
    web:
      exposure:
        include: health,gateway
  endpoint:
    health:
      show-details: always

logging:
  pattern:
    console: "%d{HH:mm:ss.SSS} %clr([%36X{trace_id}]){cyan} %clr(%-5level) %clr(%logger{36}){magenta} - %msg%n"
  level:
    # Prevent common, expected client input errors (like 400 Bad Request or 401 Unauthorized) 
    # from dumping massive multi-page warning stacks into your console logs.
    org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler: WARN
    org.springframework.web.server.handler.ResponseStatusExceptionHandler: WARN

# == 5. API Documentation ==
app:
  error-docs-url: "https://api.transcendence.com/errors/"
```

## 📄 File: ./config-repo/auth-service/auth-service-docker.yaml
```yaml
# ==========================================
# AUTH SERVICE DOCKER OVERRIDES
# ==========================================

spring:
  datasource:
    url: jdbc:postgresql://postgres-db:5432/${DB_NAME}
  
  data:
    redis:
      host: redis-container
  
  security:
    oauth2:
      resourceserver:
        jwt:
          jwk-set-uri: "https://gateway:${GATEWAY_PORT}/.well-known/jwks.json"

```

## 📄 File: ./config-repo/auth-service/auth-service-dev.yaml
```yaml
# ==========================================
# AUTH SERVICE DEVELOPMENT OVERRIDES
# ==========================================

spring:
  datasource:
    url: jdbc:postgresql://localhost:${POSTGRES_PORT}/${DB_NAME}
  
  data:
    redis:
      host: localhost
  
  security:
    oauth2:
      resourceserver:
        jwt:
          jwk-set-uri: "https://localhost:${GATEWAY_PORT}/.well-known/jwks.json"
```

## 📄 File: ./config-repo/auth-service/auth-service.yaml
```yaml
# ==========================================
# AUTH SERVICE CORE BASE CONFIGURATION
# ==========================================

server:
  port: ${AUTH_SERVICE_PORT}
  forward-headers-strategy: framework

spring:
  # == 1. Database Driver & Schema Config ==
  datasource:
    username: "${DB_USER}"
    password: "${DB_PASSWORD}"
    driver-class-name: org.postgresql.Driver
  
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect

  # == 2. Redis Connection & Session Management ==
  session:
    timeout: 7d
    data:
      redis:
        namespace: "transcendence"
        save-mode: on-set-attribute
        repository-type: indexed
  data:
    redis:
      port: ${REDIS_PORT}
      password: "${REDIS_PASSWORD}"

# ==========================================
# OPENAPI / SWAGGER DOCUMENTATION CONFIGURATION
# ==========================================
springdoc:
  server-url: "https://localhost:8080/api/auth"

```

## 📄 File: ./config-repo/eureka-server/eureka-server-dev.yaml
```yaml
# ==========================================
# EUREKA SERVER DEV OVERRIDES
# ==========================================

eureka:
  instance:
    hostname: localhost

```

## 📄 File: ./config-repo/eureka-server/eureka-server-docker.yaml
```yaml
# ==========================================
# EUREKA SERVER DOCKER OVERRIDES
# ==========================================

eureka:
  instance:
    hostname: eureka-server

```

## 📄 File: ./config-repo/eureka-server/eureka-server.yaml
```yaml
# ==========================================
# Global Eureka Server Configuration
# ==========================================

server:
  port: ${EUREKA_PORT}
  ssl:
    client-auth: want # Allow browsers to view the UI dashboard easily without requiring an mTLS certificate

eureka:
  client:
    fetch-registry: false
    register-with-eureka: false
```

## 📄 File: ./config-repo/gateway/gateway-docker.yaml
```yaml
# ==========================================
#  Gateway Docker Configuration
# ==========================================

spring:
  data:
    redis:
      host: redis-container
          
custom:
  jwt:
    public-key-location: file:/app/certs/jwt/jwt_public.pem
    private-key-location: file:/app/certs/jwt/jwt_private_pkcs8.pem
```

## 📄 File: ./config-repo/gateway/gateway-dev.yaml
```yaml
# ==========================================
#  Gateway Dev Configuration
# ==========================================

spring:
  data:
    redis:
      host: localhost
          
custom:
  jwt:
    public-key-location: file:${CERT_DIR_PATH}/jwt/jwt_public.pem
    private-key-location: file:${CERT_DIR_PATH}/jwt/jwt_private_pkcs8.pem
```

## 📄 File: ./config-repo/gateway/gateway.yaml
```yaml
# ==========================================
# GATEWAY ROUTING & EDGE CONFIGURATION
# ==========================================

server:
  port: ${GATEWAY_PORT}
  ssl:
    client-auth: none

spring:
  # == Redis Session Management ==
  session:
    timeout: 7d
    data:
      redis:
        namespace: "transcendence"
        save-mode: on-set-attribute
        repository-type: indexed
  data:
    redis:
      port: ${REDIS_PORT}
      password: ${REDIS_PASSWORD}

  # == Security & OAuth2 Configuration ==
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: ${GOOGLE_CLIENT_ID}
            client-secret: ${GOOGLE_CLIENT_SECRET}
            scope:
              - openid
              - profile
              - email
            redirect-uri: "{baseUrl}/login/oauth2/code/google"

  # == WebClient & Netty Routing Engine Client SSL Configuration ==
  cloud:
    gateway:
      server:
        webflux:
          httpclient:
            wiretap: true
            ssl:
              ssl-bundle: microservice-bundle
        # == Discovery Configuration ==
          discovery:
            locator:
              enabled: true
              lower-case-service-id: true

          routes:
            # OpenAPI Spec Bypass (High Priority)
            - id: auth-service-openapi-bypass
              uri: lb://auth-service
              order: 1
              predicates:
                - Path=/api/auth/v3/api-docs/**, /api/auth/v3/api-docs
              filters:
                - StripPrefix=2

            # Public paths
            - id: auth-service-public
              uri: lb://auth-service
              order: 2
              predicates:
                - Path=/api/auth/login, /api/auth/register
              filters:
                - StripPrefix=2

            # Semi-Protected (2FA challenge)
            - id: auth-service-2fa-verification
              uri: lb://auth-service
              order: 3
              predicates:
                - Path=/api/auth/2fa/verify
              filters:
                - StripPrefix=2
                - SessionToJwt

            # Secure Catch-All (Enforces Full Authentication)
            - id: auth-service-secure-catchall
              uri: lb://auth-service
              order: 4
              predicates:
                - Path=/api/auth/**
              filters:
                - StripPrefix=2
                - SessionToJwt
                - TwoFactorCheck

# ==========================================
# OPENAPI / SWAGGER AGGREGATION CONFIGURATION
# ==========================================
springdoc:
  swagger-ui:
    use-root-path: false
    urls:
      - name: "Authentication Service"
        url: "/api/auth/v3/api-docs"

# ==========================================
# APPLICATION CORE SECURITY & CORS CONFIG
# ==========================================
app:
  cors:
    allowed-origins: "${CORS_ALLOWED_ORIGINS:http://localhost:5173}"
  frontend:
    base-url: "${FRONTEND_BASE_URL:http://localhost:5173}"

logging:
  level:
    org.springframework.cloud.gateway: TRACE
    org.springframework.cloud.loadbalancer: TRACE


```

## 📄 File: ./config-server/.mvn/wrapper/maven-wrapper.properties
```properties
wrapperVersion=3.3.4
distributionType=only-script
distributionUrl=https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.15/apache-maven-3.9.15-bin.zip

```

## 📄 File: ./config-server/src/main/java/com/ft_transcendence/configserver/ConfigServerApplication.java
```java
package com.ft_transcendence.configserver;

import org.springframework.boot.SpringApplication;
import org.springframework.cloud.config.server.EnableConfigServer;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableConfigServer
@SpringBootApplication
public class ConfigServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConfigServerApplication.class, args);
    }

}

```

## 📄 File: ./config-server/src/main/java/com/ft_transcendence/configserver/config/SecurityConfig.java
```java
package com.ft_transcendence.configserver.config;

import org.springframework.context.annotation.Bean;
import org.springframework.security.config.Customizer;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;


@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {

        http
                // 1. Disable Sessions (Make it Stateless)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 2. Enable mTLS X.509 Extraction
                .x509(Customizer.withDefaults())

                // 3. Require Authentication for everything
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().authenticated()
                )

                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable);



        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> User.withUsername(username)
                .password("")
                .roles("USER")
                .build();
    }

}

```

## 📄 File: ./config-server/src/main/resources/application-dev.yaml
```yaml
server:
  ssl:
    trust-store: file:${CERT_DIR_PATH}/truststore/truststore.p12
    key-store: file:${CERT_DIR_PATH}/services/config-server/config-server.p12

spring:
  cloud:
    config:
      server:
        native:
          search-locations:
            - file:${CONFIG_REPO_PATH}
            - file:${CONFIG_REPO_PATH}/{application}
```

## 📄 File: ./config-server/src/main/resources/application-docker.yaml
```yaml
server:
  ssl:
    trust-store: file:/app/certs/truststore/truststore.p12
    key-store: file:/app/certs/services/config-server/config-server.p12

spring:
  cloud:
    config:
      server:
        native:
          search-locations:
            - file:/app/config-repo
            - file:/app/config-repo/{application}
```

## 📄 File: ./config-server/src/main/resources/application.yaml
```yaml
server:
  port: ${CONFIG_SERVER_PORT}
  ssl:
    enabled: true
    client-auth: need
    key-store-type: PKCS12
    trust-store-type: PKCS12
    key-alias: config-server
    key-store-password: ${CERT_PASSWORD}
    trust-store-password: ${CERT_PASSWORD}

spring:
  application:
    name: config-server
  profiles:
    active: native

```

## 📄 File: ./config-server/src/test/java/com/ft_transcendence/configserver/ConfigServerApplicationTests.java
```java
package com.ft_transcendence.configserver;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ConfigServerApplicationTests {

    @Test
    void contextLoads() {
    }

}

```

## 📄 File: ./config-server/pom.xml
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>4.0.6</version>
        <relativePath/> <!-- lookup parent from repository -->
    </parent>
    <groupId>com.ft_transcendence</groupId>
    <artifactId>config-server</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>config-server</name>
    <description>config-server</description>
    <url/>
    <licenses>
        <license/>
    </licenses>
    <developers>
        <developer/>
    </developers>
    <scm>
        <connection/>
        <developerConnection/>
        <tag/>
        <url/>
    </scm>
    <properties>
        <java.version>21</java.version>
        <spring-cloud.version>2025.1.1</spring-cloud.version>
        <maven.compiler.target>21</maven.compiler.target>
    </properties>
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-config-server</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.security</groupId>
            <artifactId>spring-security-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-dependencies</artifactId>
                <version>${spring-cloud.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>

</project>

```

## 📄 File: ./config-server/Dockerfile
```dockerfile
# ==========================================
# BUILD STAGE
# ==========================================
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /app

# Cache Maven dependency layers for rapid rebuilds
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source and build
COPY src ./src
RUN mvn clean package -DskipTests

# ==========================================
# RUN STAGE
# ==========================================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Install curl for robust healthcheck support
RUN apk add --no-cache curl

# Copy the built jar from the builder stage
COPY --from=builder /app/target/*.jar app.jar

# Run the Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]

```

## 📄 File: ./mtls-setup.sh
```bash
#!/usr/bin/env bash
set -euo pipefail

# ─────────────────────────────────────────────────────────────────────────────
#  Transcendence — mTLS Certificate Manager
#
#  Usage:
#    ./mtls-setup.sh                        # bootstrap + generate all services
#    ./mtls-setup.sh add <name> [name...]   # add one or more services
#    ./mtls-setup.sh remove <name> [name…] # remove one or more services
#    ./mtls-setup.sh renew <name> [name…]  # force-renew specific services
#    ./mtls-setup.sh renew --all            # force-renew every service
#    ./mtls-setup.sh status                 # show cert inventory + expiry
#    ./mtls-setup.sh list                   # list registered services
#
#  Layout produced (under CERT_DIR):
#    certs/
#    ├── rootCA/
#    │   ├── rootCA.key          private key  (600)
#    │   ├── rootCA.crt          certificate  (644)
#    │   └── rootCA.srl          serial file
#    ├── truststore/
#    │   └── truststore.p12      Java truststore (644)
#    ├── jwt/
#    │   ├── jwt_private_pkcs8.pem   Gateway only (600)
#    │   └── jwt_public.pem          Distribute everywhere (644)
#    └── services/
#        └── <service-name>/
#            ├── <name>.key      raw private key (600)
#            ├── <name>.crt      signed certificate (644)
#            ├── <name>.pem      fullchain PEM (cert + key) (600)
#            ├── <name>.p12      PKCS12 keystore (644)
#            ├── <name>-chain.pem  cert + CA chain (no key) (644)
#            └── README.txt      usage guide per service
# ─────────────────────────────────────────────────────────────────────────────

# ── Configuration ─────────────────────────────────────────────────────────────
CERT_DIR="${CERT_DIR:-certs}"
CERT_PASS="${CERT_PASSWORD:-password}"
DAYS_VALID="${DAYS_VALID:-365}"
DAYS_CA="${DAYS_CA:-3650}"
CA_CN="${CA_CN:-TranscendenceCA}"

# Default services generated on first run (edit freely)
DEFAULT_SERVICES=(
    "gateway"
    "auth-service"
    "config-server"
    "eureka-server"
)

# ── Colour helpers ─────────────────────────────────────────────────────────────
RED='\033[0;31m'; GRN='\033[0;32m'; YLW='\033[0;33m'
BLU='\033[0;34m'; CYN='\033[0;36m'; GRY='\033[0;90m'
BLD='\033[1m'; RST='\033[0m'

info()    { echo -e "${BLU}[INFO]${RST}  $*"; }
ok()      { echo -e "${GRN}[OK]${RST}    $*"; }
warn()    { echo -e "${YLW}[WARN]${RST}  $*"; }
error()   { echo -e "${RED}[ERROR]${RST} $*" >&2; }
step()    { echo -e "\n${BLD}${CYN}──── $* ────${RST}"; }
dim()     { echo -e "${GRY}$*${RST}"; }

banner() {
    echo -e "${BLD}"
    echo "╔══════════════════════════════════════════════════╗"
    echo "║   Transcendence  ·  mTLS Certificate Manager    ║"
    echo "╚══════════════════════════════════════════════════╝"
    echo -e "${RST}"
}

# ── Directory helpers ──────────────────────────────────────────────────────────
ca_dir()         { echo "$CERT_DIR/rootCA"; }
trust_dir()      { echo "$CERT_DIR/truststore"; }
jwt_dir()        { echo "$CERT_DIR/jwt"; }
svc_dir()        { echo "$CERT_DIR/services/$1"; }
services_root()  { echo "$CERT_DIR/services"; }

ensure_dirs() {
    mkdir -p "$(ca_dir)" "$(trust_dir)" "$(jwt_dir)" "$(services_root)"
}

# ── CA ─────────────────────────────────────────────────────────────────────────
generate_ca() {
    local ca_key="$(ca_dir)/rootCA.key"
    local ca_crt="$(ca_dir)/rootCA.crt"

    if [[ -f "$ca_key" && -f "$ca_crt" ]]; then
        ok "Root CA already exists — skipping"
        return
    fi

    step "Creating Root CA"
    openssl req -x509 -nodes -newkey rsa:4096 \
        -keyout "$ca_key" \
        -out    "$ca_crt" \
        -days   "$DAYS_CA" \
        -subj   "/CN=${CA_CN}/O=Transcendence/OU=Infrastructure"

    chmod 600 "$ca_key"
    chmod 644 "$ca_crt"
    ok "Root CA created → $(ca_dir)/"
}

# ── Truststore ─────────────────────────────────────────────────────────────────
generate_truststore() {
    local ts="$(trust_dir)/truststore.p12"
    local ca_crt="$(ca_dir)/rootCA.crt"

    # Rebuild whenever the CA cert is newer than the truststore
    if [[ -f "$ts" && "$ca_crt" -ot "$ts" ]]; then
        ok "Truststore is current — skipping"
        return
    fi

    step "Rebuilding Java Truststore"
    rm -f "$ts"
    keytool -import -trustcacerts \
        -alias   rootca \
        -file    "$ca_crt" \
        -keystore "$ts" \
        -storetype PKCS12 \
        -storepass "$CERT_PASS" \
        -noprompt

    chmod 644 "$ts"
    ok "Truststore → $(trust_dir)/truststore.p12"
}

# ── JWT keys ───────────────────────────────────────────────────────────────────
generate_jwt_keys() {
    local priv="$(jwt_dir)/jwt_private_pkcs8.pem"
    local pub="$(jwt_dir)/jwt_public.pem"

    if [[ -f "$priv" && -f "$pub" ]]; then
        ok "JWT key pair already exists — skipping"
        return
    fi

    step "Generating RSA Key Pair for JWT"
    local tmp
    tmp="$(mktemp)"
    openssl genrsa -out "$tmp" 2048

    # PKCS#8 private (Spring Security / KeyFactory compatible)
    openssl pkcs8 -topk8 -inform PEM -outform PEM -nocrypt \
        -in "$tmp" -out "$priv"

    # X.509 public key
    openssl rsa -in "$tmp" -pubout -out "$pub"

    rm -f "$tmp"
    chmod 600 "$priv"
    chmod 644 "$pub"
    ok "JWT keys → $(jwt_dir)/"
}

# ── Per-service certificate generation ────────────────────────────────────────
#
#  Outputs for <name>  (all inside services/<name>/):
#    <name>.key           RSA private key              (600 — keep private)
#    <name>.crt           Signed certificate           (644)
#    <name>.pem           Full-chain PEM  key+cert     (600 — nginx/HAProxy)
#    <name>-chain.pem     cert+CA bundle  (no key)     (644 — client trust)
#    <name>.p12           PKCS12 keystore              (644 — Spring Boot)
#    README.txt           Spring props + usage hints
#
generate_service_cert() {
    local name="$1"
    local force="${2:-no}"          # pass "force" to renew
    local dir
    dir="$(svc_dir "$name")"
    local p12="$dir/$name.p12"

    if [[ "$force" != "force" && -f "$p12" ]]; then
        # Check if it's actually expired
        local expiry
        expiry=$(openssl pkcs12 -in "$p12" -passin pass:"$CERT_PASS" -nokeys 2>/dev/null \
                  | openssl x509 -noout -enddate 2>/dev/null \
                  | cut -d= -f2 || echo "unknown")
        if [[ "$expiry" != "unknown" ]]; then
            local exp_epoch
            exp_epoch=$(date -d "$expiry" +%s 2>/dev/null || date -j -f "%b %d %T %Y %Z" "$expiry" +%s 2>/dev/null || echo 0)
            local now_epoch
            now_epoch=$(date +%s)
            if (( exp_epoch > now_epoch )); then
                ok "[$name] certificate valid until $(date -d @$exp_epoch '+%Y-%m-%d' 2>/dev/null || echo $expiry) — skipping"
                return
            else
                warn "[$name] certificate is EXPIRED — regenerating"
            fi
        else
            ok "[$name] certificate exists — skipping (run 'renew $name' to force)"
            return
        fi
    fi

    info "[$name] Generating certificate package..."
    mkdir -p "$dir"

    local ca_key="$(ca_dir)/rootCA.key"
    local ca_crt="$(ca_dir)/rootCA.crt"
    local key="$dir/$name.key"
    local csr="$dir/$name.csr"
    local crt="$dir/$name.crt"
    local pem="$dir/$name.pem"
    local chain="$dir/$name-chain.pem"
    local p12="$dir/$name.p12"
    local ext_file="$dir/$name.ext"

    # SAN extension — always includes localhost variants + the service name
    cat > "$ext_file" <<EOF
[req]
distinguished_name = req_distinguished_name
req_extensions     = v3_req
prompt             = no

[req_distinguished_name]
CN = $name

[v3_req]
keyUsage         = keyEncipherment, dataEncipherment, digitalSignature
extendedKeyUsage = serverAuth, clientAuth
subjectAltName   = @alt_names

[alt_names]
DNS.1 = $name
DNS.2 = localhost
DNS.3 = 127.0.0.1
IP.1  = 127.0.0.1
EOF

    # 1. Private key
    openssl genrsa -out "$key" 2048 2>/dev/null

    # 2. CSR
    openssl req -new \
        -key  "$key" \
        -out  "$csr" \
        -subj "/CN=$name/O=Transcendence/OU=Service" \
        -config "$ext_file"

    # 3. Sign with CA
    openssl x509 -req \
        -in      "$csr" \
        -CA      "$ca_crt" \
        -CAkey   "$ca_key" \
        -CAcreateserial \
        -out     "$crt" \
        -days    "$DAYS_VALID" \
        -extensions v3_req \
        -extfile "$ext_file"

    # 4. Full-chain PEM (key + cert) — for nginx, HAProxy, raw TLS
    cat "$key" "$crt" > "$pem"

    # 5. Chain PEM (cert + CA, no key) — share with clients for trust
    cat "$crt" "$ca_crt" > "$chain"

    # 6. PKCS12 keystore — for Spring Boot server.ssl.*
    openssl pkcs12 -export \
        -in      "$crt" \
        -inkey   "$key" \
        -CAfile  "$ca_crt" \
        -caname  rootca \
        -out     "$p12" \
        -name    "$name" \
        -password pass:"$CERT_PASS"

    # 7. Permissions
    chmod 600 "$key" "$pem"
    chmod 644 "$crt" "$chain" "$p12"

    # 8. Cleanup temporaries
    rm -f "$csr" "$ext_file"

    # 9. README for this service
    write_service_readme "$name" "$dir"

    ok "[$name] → $dir/"
    dim "         .key  private key   (600 — do not distribute)"
    dim "         .crt  certificate   (644)"
    dim "         .pem  key+cert      (600 — nginx/HAProxy)"
    dim "         -chain.pem cert+CA  (644 — client trust)"
    dim "         .p12  PKCS12        (644 — Spring Boot)"
}

# ── Per-service README ─────────────────────────────────────────────────────────
write_service_readme() {
    local name="$1"
    local dir="$2"
    cat > "$dir/README.txt" <<EOF
Certificate Package: $name
Generated: $(date -u '+%Y-%m-%d %H:%M UTC')
Valid for: $DAYS_VALID days
CA: $CA_CN

FILES
─────
$name.key          RSA private key      — DO NOT DISTRIBUTE (chmod 600)
$name.crt          Signed certificate   — safe to distribute
$name.pem          key + cert bundle    — nginx, HAProxy, raw TLS (chmod 600)
$name-chain.pem    cert + CA bundle     — give to clients for mutual TLS trust
$name.p12          PKCS12 keystore      — Spring Boot server.ssl.*

SPRING BOOT APPLICATION.YML (server-side TLS)
──────────────────────────────────────────────
server:
  ssl:
    enabled: true
    key-store:             classpath:$name.p12
    key-store-password:    \${CERT_PASSWORD:$CERT_PASS}
    key-store-type:        PKCS12
    key-alias:             $name
    trust-store:           classpath:truststore.p12
    trust-store-password:  \${CERT_PASSWORD:$CERT_PASS}
    trust-store-type:      PKCS12
    client-auth:           need              # mTLS: require client certs

SPRING CLOUD GATEWAY (reactive, for mTLS outbound to services)
───────────────────────────────────────────────────────────────
spring:
  cloud:
    gateway:
      httpclient:
        ssl:
          use-insecure-trust-manager: false
          trusted-x509-certificates:
            - classpath:rootCA.crt

FEIGN / REST TEMPLATE CLIENT mTLS
───────────────────────────────────
# Add the .p12 as key-store AND the truststore.p12 as trust-store
# on any outbound HTTP client that calls a service behind mTLS.

EUREKA CLIENT
─────────────
eureka:
  client:
    service-url:
      defaultZone: https://eureka-server:8761/eureka/
  instance:
    secure-port-enabled: true
    non-secure-port-enabled: false
EOF
}

# ── Status / inventory ─────────────────────────────────────────────────────────
cmd_status() {
    banner
    echo -e "${BLD}Certificate Inventory${RST}"
    echo "────────────────────────────────────────────────────"

    # Root CA
    local ca_crt="$(ca_dir)/rootCA.crt"
    if [[ -f "$ca_crt" ]]; then
        local exp
        exp=$(openssl x509 -noout -enddate -in "$ca_crt" 2>/dev/null | cut -d= -f2)
        echo -e "  ${BLD}Root CA${RST}          expires ${exp}"
    else
        warn "  Root CA not found"
    fi

    # Truststore
    local ts="$(trust_dir)/truststore.p12"
    [[ -f "$ts" ]] && echo -e "  ${BLD}Truststore${RST}       ✓ present" || warn "  Truststore missing"

    # JWT
    local jwt_priv="$(jwt_dir)/jwt_private_pkcs8.pem"
    local jwt_pub="$(jwt_dir)/jwt_public.pem"
    [[ -f "$jwt_priv" && -f "$jwt_pub" ]] \
        && echo -e "  ${BLD}JWT keys${RST}         ✓ present" \
        || warn "  JWT keys missing"

    echo ""
    echo -e "${BLD}Services${RST}"
    echo "────────────────────────────────────────────────────"

    local svc_root
    svc_root="$(services_root)"
    if [[ ! -d "$svc_root" ]] || [[ -z "$(ls -A "$svc_root" 2>/dev/null)" ]]; then
        warn "  No service certificates found"
        return
    fi

    local now_epoch
    now_epoch=$(date +%s)
    local warn_threshold=$(( now_epoch + 30*86400 ))   # warn if expiring in <30 days

    printf "  %-22s %-12s %-28s %s\n" "SERVICE" "STATUS" "EXPIRES" "FORMATS"
    echo "  $(printf '─%.0s' {1..70})"
    for svc_path in "$svc_root"/*/; do
        [[ -d "$svc_path" ]] || continue
        local svc
        svc=$(basename "$svc_path")
        local p12="$svc_path/$svc.p12"

        if [[ ! -f "$p12" ]]; then
            printf "  %-22s ${RED}%-12s${RST}\n" "$svc" "MISSING"
            continue
        fi

        local exp_str
        exp_str=$(openssl pkcs12 -in "$p12" -passin pass:"$CERT_PASS" -nokeys 2>/dev/null \
                  | openssl x509 -noout -enddate 2>/dev/null \
                  | cut -d= -f2 || echo "unknown")

        local status="${GRN}OK${RST}"
        if [[ "$exp_str" != "unknown" ]]; then
            local exp_epoch
            exp_epoch=$(date -d "$exp_str" +%s 2>/dev/null \
                     || date -j -f "%b %d %T %Y %Z" "$exp_str" +%s 2>/dev/null \
                     || echo 0)
            local human_exp
            human_exp=$(date -d @"$exp_epoch" '+%Y-%m-%d' 2>/dev/null || echo "$exp_str")
            if (( exp_epoch < now_epoch )); then
                status="${RED}EXPIRED${RST}"
            elif (( exp_epoch < warn_threshold )); then
                status="${YLW}EXPIRING${RST}"
            fi

            # Which formats present
            local formats=""
            [[ -f "$svc_path/$svc.key"       ]] && formats+="key "
            [[ -f "$svc_path/$svc.crt"       ]] && formats+="crt "
            [[ -f "$svc_path/$svc.pem"       ]] && formats+="pem "
            [[ -f "$svc_path/$svc-chain.pem" ]] && formats+="chain "
            [[ -f "$svc_path/$svc.p12"       ]] && formats+="p12"

            printf "  %-22s %-20b %-28s %s\n" "$svc" "$status" "$human_exp" "$formats"
        else
            printf "  %-22s %-20b %-28s\n" "$svc" "${YLW}UNKNOWN${RST}" "—"
        fi
    done
    echo ""
}

# ── List services ──────────────────────────────────────────────────────────────
cmd_list() {
    local svc_root
    svc_root="$(services_root)"
    if [[ ! -d "$svc_root" ]] || [[ -z "$(ls -A "$svc_root" 2>/dev/null)" ]]; then
        info "No services registered yet"
        return
    fi
    echo "Registered services:"
    for svc_path in "$svc_root"/*/; do
        [[ -d "$svc_path" ]] && echo "  • $(basename "$svc_path")"
    done
}

# ── Remove service ─────────────────────────────────────────────────────────────
cmd_remove() {
    if [[ $# -eq 0 ]]; then
        error "Usage: $0 remove <service> [service...]"
        exit 1
    fi
    for name in "$@"; do
        local dir
        dir="$(svc_dir "$name")"
        if [[ -d "$dir" ]]; then
            rm -rf "$dir"
            ok "Removed $name"
        else
            warn "$name — not found (nothing to remove)"
        fi
    done
}

# ── Renew service(s) ───────────────────────────────────────────────────────────
cmd_renew() {
    if [[ $# -eq 0 ]]; then
        error "Usage: $0 renew <service|--all> [service...]"
        exit 1
    fi

    if [[ "$1" == "--all" ]]; then
        local svc_root
        svc_root="$(services_root)"
        if [[ ! -d "$svc_root" ]]; then
            warn "No services directory found"
            return
        fi
        for svc_path in "$svc_root"/*/; do
            [[ -d "$svc_path" ]] || continue
            generate_service_cert "$(basename "$svc_path")" "force"
        done
    else
        for name in "$@"; do
            generate_service_cert "$name" "force"
        done
    fi
}

# ── Add service(s) ────────────────────────────────────────────────────────────
cmd_add() {
    if [[ $# -eq 0 ]]; then
        error "Usage: $0 add <service> [service...]"
        exit 1
    fi
    # Ensure CA exists before generating service certs
    ensure_dirs
    generate_ca
    generate_truststore

    for name in "$@"; do
        generate_service_cert "$name"
    done
}

# ── Bootstrap (default: run everything for DEFAULT_SERVICES) ──────────────────
cmd_bootstrap() {
    banner
    ensure_dirs
    generate_ca
    generate_truststore
    generate_jwt_keys

    step "Service Certificates"
    for svc in "${DEFAULT_SERVICES[@]}"; do
        generate_service_cert "$svc"
    done

    echo ""
    echo -e "${BLD}${GRN}╔══════════════════════════════════════════════════╗${RST}"
    echo -e "${BLD}${GRN}║            All certificates ready ✓              ║${RST}"
    echo -e "${BLD}${GRN}╚══════════════════════════════════════════════════╝${RST}"
    echo ""
    echo -e "  ${BLD}Keystore password:${RST}  ${CERT_PASS}"
    echo -e "  ${BLD}Certificate root:${RST}   ${CERT_DIR}/"
    echo ""
    echo "  Files to copy into each service's src/main/resources/:"
    echo "    • services/<name>/<name>.p12     (its own keystore)"
    echo "    • truststore/truststore.p12      (shared CA trust)"
    echo "    • rootCA/rootCA.crt              (raw CA cert)"
    echo ""
    echo "  JWT keys:"
    echo "    • jwt/jwt_private_pkcs8.pem      → gateway only"
    echo "    • jwt/jwt_public.pem             → every resource server"
    echo ""
    echo "  Run './mtls-setup.sh status' to verify the full inventory."
    echo ""
}

# ── Entry point ───────────────────────────────────────────────────────────────
CMD="${1:-bootstrap}"

case "$CMD" in
    bootstrap|"")    cmd_bootstrap ;;
    add)             shift; cmd_add "$@" ;;
    remove|rm)       shift; cmd_remove "$@" ;;
    renew)           shift; cmd_renew "$@" ;;
    status)          cmd_status ;;
    list|ls)         cmd_list ;;
    help|-h|--help)
        echo "Usage: $0 [command] [args]"
        echo ""
        echo "Commands:"
        echo "  (none)             Bootstrap: CA + truststore + JWT + default services"
        echo "  add <name...>      Add one or more service cert packages"
        echo "  remove <name...>   Remove service cert directories"
        echo "  renew <name...>    Force-renew specific service certs"
        echo "  renew --all        Force-renew all service certs"
        echo "  status             Show cert inventory with expiry dates"
        echo "  list               List registered services"
        echo ""
        echo "Environment:"
        echo "  CERT_DIR       Output directory        (default: certs)"
        echo "  CERT_PASSWORD  Keystore password        (default: changeit)"
        echo "  DAYS_VALID     Service cert lifetime    (default: 365)"
        echo "  DAYS_CA        Root CA lifetime         (default: 3650)"
        echo "  CA_CN          CA common name           (default: TranscendenceCA)"
        ;;
    *)
        error "Unknown command: $CMD"
        echo "Run '$0 help' for usage."
        exit 1
        ;;
esac
```

## 📄 File: ./dev-start.sh
```bash
#!/usr/bin/env bash
set -euo pipefail

# ── Colors & Logging ──────────────────────────────────────────────────────────
RED='\033[0;31m'; GRN='\033[0;32m'; YLW='\033[0;33m'
BLU='\033[0;34m'; CYN='\033[0;36m'; MAG='\033[0;35m'
BLD='\033[1m';    DIM='\033[2m';     RST='\033[0m'

ts()    { date '+%H:%M:%S'; }
info()  { echo -e "$(ts) ${BLU}[INFO]${RST}  $*"; }
ok()    { echo -e "$(ts) ${GRN}[OK]${RST}    $*"; }
warn()  { echo -e "$(ts) ${YLW}[WARN]${RST}  $*"; }
error() { echo -e "$(ts) ${RED}[ERROR]${RST} $*" >&2; }
step()  { echo -e "\n${BLD}${CYN}──── $* ────${RST}"; }
dim()   { echo -e "${DIM}$*${RST}"; }

banner() {
    echo -e "${BLD}"
    echo "╔══════════════════════════════════════════════════╗"
    echo "║   Transcendence  ·  Dev Environment Orchestrator ║"
    echo "╚══════════════════════════════════════════════════╝"
    echo -e "${RST}"
}

# ── Globals ───────────────────────────────────────────────────────────────────
LOG_DIR="logs"
PID_DIR=".pids"
MAX_LOG_BYTES=$(( 20 * 1024 * 1024 ))

SERVICES=()
declare -A SVC_PORT
declare -A SVC_SCHEME
declare -A SVC_PROFILES

# ── Shutdown state ─────────────────────────────────────────────────────────────
#
# SHUTTING_DOWN is the critical flag that breaks the restart loop.
# Without it: cleanup kills a service → watcher sees dead PID → watcher
# restarts it → cleanup kills it again → infinite loop.
#
# Written to a file so the watcher subshell can see it. An in-memory
# variable won't work because the watcher is a separate process.
#
SHUTDOWN_FLAG="${PID_DIR}/.shutting_down"
WATCHER_PID=""   # captured after watch_services &, so cleanup can kill it

is_shutting_down() { [[ -f "$SHUTDOWN_FLAG" ]]; }

# ── PID file helpers ──────────────────────────────────────────────────────────
pid_file()  { echo "$PID_DIR/$1.pid"; }
write_pid() { echo "$2" > "$(pid_file "$1")"; }
read_pid()  { local f; f="$(pid_file "$1")"; [[ -f "$f" ]] && cat "$f" || echo ""; }
clear_pid() { rm -f "$(pid_file "$1")"; }

is_alive() {
    local pid
    pid="$(read_pid "$1")"
    [[ -n "$pid" ]] && kill -0 "$pid" 2>/dev/null
}

# ── Log rotation ──────────────────────────────────────────────────────────────
maybe_rotate_log() {
    local log_file="$1"
    if [[ -f "$log_file" ]]; then
        local size
        size=$(wc -c < "$log_file" 2>/dev/null || echo 0)
        if (( size > MAX_LOG_BYTES )); then
            mv "$log_file" "${log_file}.1"
            info "Rotated ${log_file} (was $(( size / 1024 / 1024 ))MB)"
        fi
    fi
}

# ── Cleanup ───────────────────────────────────────────────────────────────────
#
# FIX: The original script trapped EXIT in addition to SIGINT/SIGTERM.
# That caused cleanup to fire twice on Ctrl+C (once for SIGINT, once for
# EXIT when the script exited), and also on any normal exit path.
#
# FIX: The shutdown flag is set *before* any kills so the watcher
# subshell stops its restart loop immediately, instead of racing.
#
# FIX: The watcher's PID is explicitly killed here so it doesn't outlive
# the main script and keep trying to restart dead services.
#
_CLEANED_UP=0
cleanup() {
    # Guard against double invocation (SIGINT can fire twice: once from the
    # terminal, once when 'wait' returns with 130 and the script exits).
    [[ "$_CLEANED_UP" -eq 1 ]] && return
    _CLEANED_UP=1

    echo ""
    warn "Shutting down all services..."

    # Signal the watcher to stop before it races to restart dying services
    touch "$SHUTDOWN_FLAG"

    # Kill the watcher first so it can't restart anything we're about to kill
    if [[ -n "$WATCHER_PID" ]] && kill -0 "$WATCHER_PID" 2>/dev/null; then
        kill -TERM "$WATCHER_PID" 2>/dev/null || true
    fi

    for name in "${SERVICES[@]}"; do
        stop_service "$name" "quiet"
    done

    # Fallback for any orphaned Maven/Spring processes from this project.
    # Using pkill's exit code is unreliable under set -e, so we suppress it.
    pkill -f "com.ft_transcendence"                         2>/dev/null || true
    pkill -f "spring-boot:run.*transcendence"               2>/dev/null || true
    pkill -f "classworlds.launcher.Launcher.*transcendence" 2>/dev/null || true

    rm -rf "$PID_DIR"
    ok "All services stopped."
}

# Trap only real termination signals, not EXIT.
# Trapping EXIT causes cleanup to run on every exit path (including
# successful ones and the 'exit 0' inside cleanup itself), which
# produces double-output and can re-kill already-dead processes.
trap 'cleanup; exit 0' SIGINT SIGTERM

# ── Infra readiness ───────────────────────────────────────────────────────────
check_infra() {
    step "Checking Infrastructure"

    local required_containers=("redis-container" "postgres-db")
    local optional_containers=("rabbitmq")
    local all_ok=true

    for container in "${required_containers[@]}"; do
        local status
        status=$(docker inspect -f '{{.State.Health.Status}}' "$container" 2>/dev/null || echo "missing")
        case "$status" in
            healthy)  ok "$container is healthy" ;;
            missing)
                error "$container is not running. Start infra first:"
                error "  docker compose up -d"
                all_ok=false
                ;;
            starting)
                warn "$container is still starting — waiting..."
                wait_for_container "$container" 60
                ;;
            *)
                error "$container status: $status"
                all_ok=false
                ;;
        esac
    done

    for container in "${optional_containers[@]}"; do
        local status
        status=$(docker inspect -f '{{.State.Health.Status}}' "$container" 2>/dev/null || echo "missing")
        if [[ "$status" == "missing" ]]; then
            warn "$container not running (optional — skipping)"
        elif [[ "$status" != "healthy" ]]; then
            warn "$container status: $status (optional — continuing anyway)"
        else
            ok "$container is healthy"
        fi
    done

    if [[ "$all_ok" == "false" ]]; then
        error "Required infrastructure is not ready. Aborting."
        exit 1
    fi
}

wait_for_container() {
    local container="$1"
    local timeout="${2:-60}"
    local elapsed=0

    while true; do
        local status
        status=$(docker inspect -f '{{.State.Health.Status}}' "$container" 2>/dev/null || echo "missing")
        [[ "$status" == "healthy" ]] && { ok "$container healthy"; return; }
        sleep 3
        elapsed=$(( elapsed + 3 ))
        printf "\r  ${YLW}waiting${RST} for %s %ds / %ds ..." "$container" "$elapsed" "$timeout"
        if (( elapsed >= timeout )); then
            echo ""
            error "Timeout waiting for $container"
            exit 1
        fi
    done
}

# ── Maven resolution ──────────────────────────────────────────────────────────
mvn_cmd() {
    local dir="$1"
    if   [[ -x "$dir/mvnw"     ]]; then echo "$PWD/$dir/mvnw"
    elif [[ -x "./mvnw"        ]]; then echo "$PWD/mvnw"
    elif command -v mvn &>/dev/null; then echo "mvn"
    else
        error "No Maven found. Install Maven or add an mvnw wrapper."
        exit 1
    fi
}

# ── Health check ──────────────────────────────────────────────────────────────
wait_for_health() {
    local name="$1"
    local port="${SVC_PORT[$name]}"
    local scheme="${SVC_SCHEME[$name]}"
    local timeout=180
    local interval=3
    local elapsed=0
    local url="${scheme}://localhost:${port}/actuator/health"

    local curl_args=(-sf -o /dev/null "$url")
    if [[ "$scheme" == "https" ]]; then
        local client_pem="certs/services/${name}/${name}.pem"
        local ca_crt="certs/rootCA/rootCA.crt"
        if [[ -f "$client_pem" && -f "$ca_crt" ]]; then
            curl_args=(--cert "$client_pem" --cacert "$ca_crt" "${curl_args[@]}")
        else
            warn "[$name] cert not found — using -k (insecure) for health check"
            curl_args=(-k "${curl_args[@]}")
        fi
    fi

    info "[$name] waiting for health at $url"

    while ! curl "${curl_args[@]}" 2>/dev/null; do
        sleep "$interval"
        elapsed=$(( elapsed + interval ))
        printf "\r  ${YLW}·${RST} %ds / %ds" "$elapsed" "$timeout"

        if (( elapsed >= timeout )); then
            echo ""
            error "[$name] health timeout after ${timeout}s"
            tail -n 30 "${LOG_DIR}/${name}.log" >&2 || true
            return 1
        fi

        if ! is_alive "$name"; then
            echo ""
            error "[$name] process died during startup"
            tail -n 30 "${LOG_DIR}/${name}.log" >&2 || true
            return 1
        fi
    done

    echo ""
    ok "[$name] healthy ✓"
}

# ── Start a single service ────────────────────────────────────────────────────
start_service() {
    local name="$1"
    local port="${SVC_PORT[$name]}"
    local scheme="${SVC_SCHEME[$name]}"
    local profiles="${SVC_PROFILES[$name]:-dev}"

    step "Starting $name"

    if [[ ! -d "$name" ]]; then
        error "Directory '$name' not found — skipping"
        return 1
    fi

    if is_alive "$name"; then
        warn "[$name] already running — stopping first"
        stop_service "$name"
        sleep 2
    fi

    local log_file="${LOG_DIR}/${name}.log"
    maybe_rotate_log "$log_file"

    local mvn
    mvn="$(mvn_cmd "$name")"

    dim "  Log → tail -f $log_file"

    # FIX: 'exec' inside the subshell replaces the subshell with Maven,
    # which means the subshell's PID *becomes* Maven's PID. We save
    # that PID before exec so we can track and kill Maven directly.
    # Previously, saving $! captured the subshell PID, but after exec
    # that PID belongs to Maven — so this was accidentally correct, but
    # only because exec replaces the process. Made explicit here for clarity.
    (
        cd "$name"
        export MAVEN_OPTS="-Dspring-boot.run.fork=false"
        exec "$mvn" spring-boot:run \
            -Dspring-boot.run.profiles="$profiles" \
            --no-transfer-progress \
            >> "../$log_file" 2>&1
    ) &

    local pid=$!
    write_pid "$name" "$pid"
    info "[$name] PID $pid"

    if ! wait_for_health "$name"; then
        error "[$name] failed to start. Aborting."
        cleanup
        exit 1
    fi
}

# ── Stop a single service ─────────────────────────────────────────────────────
stop_service() {
    local name="$1"
    local mode="${2:-verbose}"

    local pid
    pid="$(read_pid "$name")"

    # Clear the PID file immediately so the watcher can't race us
    clear_pid "$name"

    if [[ -z "$pid" ]]; then
        [[ "$mode" == "verbose" ]] && warn "[$name] no PID on record"
        return
    fi

    if kill -0 "$pid" 2>/dev/null; then
        [[ "$mode" == "verbose" ]] && info "[$name] sending SIGTERM to PID $pid"
        kill -TERM "$pid" 2>/dev/null || true

        local waited=0
        while kill -0 "$pid" 2>/dev/null; do
            sleep 1
            # FIX: '(( waited++ ))' exits with code 1 when waited=0 under
            # set -e because the expression evaluates to 0 (falsy).
            # Use 'waited=$(( waited + 1 ))' instead — always exits 0.
            waited=$(( waited + 1 ))
            [[ "$waited" -ge 15 ]] && break
        done

        if kill -0 "$pid" 2>/dev/null; then
            warn "[$name] still alive after 15s — sending SIGKILL"
            kill -KILL "$pid" 2>/dev/null || true
        fi

        [[ "$mode" == "verbose" ]] && ok "[$name] stopped"
    fi
}

# ── Selective restart ─────────────────────────────────────────────────────────
restart_service() {
    local name="$1"

    if [[ -z "${SVC_PORT[$name]+x}" ]]; then
        error "Unknown service: $name"
        echo "Known services: ${SERVICES[*]}"
        return 1
    fi

    warn "Restarting $name..."
    stop_service "$name" "verbose"
    sleep 1
    start_service "$name"
    ok "$name restarted"
}

# ── Watcher ───────────────────────────────────────────────────────────────────
#
# FIX: The watcher now checks the shutdown flag before acting on a dead PID.
# Without this check, the watcher races cleanup: as cleanup kills services,
# the watcher sees dead PIDs and tries to restart them, fighting cleanup.
#
watch_services() {
    info "Watcher started (checks every 5s)"
    while true; do
        sleep 5

        # Bail out entirely if shutdown has been requested
        is_shutting_down && return

        for name in "${SERVICES[@]}"; do
            # Check shutdown flag inside the loop too — cleanup could have
            # started mid-iteration
            is_shutting_down && return

            local pid
            pid="$(read_pid "$name")"
            [[ -z "$pid" ]] && continue

            if ! kill -0 "$pid" 2>/dev/null; then
                echo ""
                error "[$name] crashed (PID $pid was expected alive)"
                tail -n 30 "${LOG_DIR}/${name}.log" >&2 || true

                warn "[$name] attempting auto-restart..."
                if ! start_service "$name"; then
                    error "[$name] failed to restart — aborting cluster"
                    cleanup
                    exit 1
                fi
            fi
        done

        for name in "${SERVICES[@]}"; do
            maybe_rotate_log "${LOG_DIR}/${name}.log"
        done
    done
}

# ── Status display ────────────────────────────────────────────────────────────
show_status() {
    echo ""
    echo -e "${BLD}Service Status${RST}"
    echo "────────────────────────────────────────────────"
    printf "  %-20s %-8s %-10s %s\n" "SERVICE" "PID" "STATUS" "URL"
    echo "  $(printf '─%.0s' {1..60})"

    for name in "${SERVICES[@]}"; do
        local pid url status_str
        pid="$(read_pid "$name")"
        url="${SVC_SCHEME[$name]}://localhost:${SVC_PORT[$name]}"

        if [[ -z "$pid" ]]; then
            status_str="${RED}not started${RST}"
            pid="—"
        elif kill -0 "$pid" 2>/dev/null; then
            status_str="${GRN}running${RST}"
        else
            status_str="${RED}dead${RST}"
        fi

        printf "  %-20s %-8s %-20b %s\n" "$name" "$pid" "$status_str" "$url"
    done
    echo ""
}

# ── Log tail shortcut ─────────────────────────────────────────────────────────
print_log_hints() {
    echo ""
    info "Logs (${LOG_DIR}/):"
    for name in "${SERVICES[@]}"; do
        dim "  tail -f ${LOG_DIR}/${name}.log"
    done

    if command -v multitail &>/dev/null; then
        local args=()
        for name in "${SERVICES[@]}"; do
            args+=("-l" "tail -f ${LOG_DIR}/${name}.log")
        done
        echo ""
        dim "  All at once: multitail ${args[*]}"
    fi
}

# ── Environment ───────────────────────────────────────────────────────────────
load_env() {
    step "Loading Environment"

    if [[ ! -f ".env" ]]; then
        error ".env not found. Copy .env.example to .env and configure it."
        exit 1
    fi

    set -a; source .env; set +a
    ok "Loaded .env"

    export CONFIG_SERVER_PORT=${CONFIG_SERVER_PORT:-8888}
    export EUREKA_PORT=${EUREKA_PORT:-8761}
    export GATEWAY_PORT=${GATEWAY_PORT:-8080}
    export AUTH_SERVICE_PORT=${AUTH_SERVICE_PORT:-8081}

    export CONFIG_SERVER_SCHEME=${CONFIG_SERVER_SCHEME:-https}
    export EUREKA_SCHEME=${EUREKA_SCHEME:-https}
    export GATEWAY_SCHEME=${GATEWAY_SCHEME:-https}
    export AUTH_SERVICE_SCHEME=${AUTH_SERVICE_SCHEME:-https}

    if [[ ! -d "certs/rootCA" ]]; then
        warn "certs/rootCA not found — run ./mtls-setup.sh first"
    fi
}

# ── Service registry ──────────────────────────────────────────────────────────
register_services() {
    register_service "config-server" "$CONFIG_SERVER_PORT" "$CONFIG_SERVER_SCHEME" "native,dev"
    register_service "eureka-server" "$EUREKA_PORT"        "$EUREKA_SCHEME"        "dev"
    register_service "gateway"       "$GATEWAY_PORT"       "$GATEWAY_SCHEME"       "dev"
    register_service "auth-service"  "$AUTH_SERVICE_PORT"  "$AUTH_SERVICE_SCHEME"  "dev"
}

register_service() {
    local name="$1" port="$2" scheme="$3" profiles="${4:-dev}"
    SERVICES+=("$name")
    SVC_PORT[$name]="$port"
    SVC_SCHEME[$name]="$scheme"
    SVC_PROFILES[$name]="$profiles"
}

# ── Entry point ───────────────────────────────────────────────────────────────
usage() {
    echo "Usage: $0 [command] [service]"
    echo ""
    echo "Commands:"
    echo "  (none)              Start all services"
    echo "  restart <service>   Restart one service without touching others"
    echo "  stop <service>      Stop one service"
    echo "  status              Show PID and health of all services"
    echo "  logs [service]      Tail logs (all or one)"
    echo ""
    echo "Services: config-server, eureka-server, gateway, auth-service"
}

main() {
    local cmd="${1:-start}"
    local target="${2:-}"

    banner
    mkdir -p "$LOG_DIR" "$PID_DIR"
    load_env
    register_services

    case "$cmd" in
        start|"")
            check_infra

            for name in "${SERVICES[@]}"; do
                start_service "$name"
            done

            echo ""
            echo -e "${BLD}${GRN}╔══════════════════════════════════════════════════╗${RST}"
            echo -e "${BLD}${GRN}║         All services healthy ✓                   ║${RST}"
            echo -e "${BLD}${GRN}╚══════════════════════════════════════════════════╝${RST}"

            show_status
            print_log_hints

            echo ""
            info "Press Ctrl+C to stop all services."
            info "In another terminal: $0 restart <service>"
            echo ""

            # Launch watcher and capture its PID so cleanup can kill it
            watch_services &
            WATCHER_PID=$!

            # FIX: 'wait' returns 130 on SIGINT, causing the script to
            # exit with a non-zero code, which would trigger the EXIT trap
            # if we had one (and cause a double-cleanup). We suppress the
            # non-zero exit from wait with '|| true', and let the SIGINT
            # trap handle the actual cleanup.
            wait || true
            ;;

        restart)
            [[ -z "$target" ]] && { error "Usage: $0 restart <service>"; exit 1; }
            restart_service "$target"
            ;;

        stop)
            [[ -z "$target" ]] && { error "Usage: $0 stop <service>"; exit 1; }
            stop_service "$target" "verbose"
            ;;

        status)
            show_status
            ;;

        logs)
            if [[ -n "$target" ]]; then
                tail -f "${LOG_DIR}/${target}.log"
            else
                if command -v multitail &>/dev/null; then
                    local args=()
                    for name in "${SERVICES[@]}"; do
                        args+=("-l" "tail -f ${LOG_DIR}/${name}.log")
                    done
                    multitail "${args[@]}"
                else
                    tail -f "${LOG_DIR}"/*.log
                fi
            fi
            ;;

        help|-h|--help)
            usage
            ;;

        *)
            error "Unknown command: $cmd"
            usage
            exit 1
            ;;
    esac
}

main "$@"
```

## 📄 File: ./docker-compose.yml
```yaml
services:
  postgres-db:
    image: postgres:15-alpine
    container_name: postgres-db
    ports:
      - "${POSTGRES_PORT}:5432"
    environment:
      - POSTGRES_USER=${DB_USER}
      - POSTGRES_PASSWORD=${DB_PASSWORD}
      - POSTGRES_DB=${DB_NAME}
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U ${DB_USER} -d ${DB_NAME}"]
      interval: 10s
      timeout: 5s
      retries: 5
    volumes:
      - postgres_data:/var/lib/postgresql/data
    networks:
      - transcendence-net

  redis-container:
    image: redis:7-alpine
    container_name: redis-container
    ports:
      - "${REDIS_PORT}:6379"
    command: redis-server --requirepass ${REDIS_PASSWORD}
    healthcheck:
      test: ["CMD", "redis-cli", "-a", "${REDIS_PASSWORD}", "ping"]
      interval: 10s
      timeout: 5s
      retries: 5
    volumes:
      - redis_data:/data
    networks:
      - transcendence-net

  redis-commander:
    image: rediscommander/redis-commander:latest
    container_name: redis-commander
    environment:
      - REDIS_HOST=redis-container
      - REDIS_PORT=6379               # Always use the container's internal port here
      - REDIS_PASSWORD=${REDIS_PASSWORD}
    ports:
      - "6381:8081"                   # Binds your laptop's 6381 to container's 8081
    networks:
      - transcendence-net
    depends_on:
      - redis-container

  rabbitmq:
    image: rabbitmq:3-management-alpine
    container_name: rabbitmq
    ports:
      - "${RABBITMQ_PORT}:5672"    # AMQP — your services connect here
      - "${RABBITMQ_MANAGEMENT_PORT}:15672"  # management UI — http://localhost:15672
    environment:
      RABBITMQ_DEFAULT_USER: ${RABBITMQ_USER:-admin}
      RABBITMQ_DEFAULT_PASS: ${RABBITMQ_PASSWORD}
    healthcheck:
      test: ["CMD", "rabbitmq-diagnostics", "ping"]
      interval: 10s
      timeout: 5s
      retries: 5
    volumes:
      - rabbitmq_data:/var/lib/rabbitmq
    networks:
      - transcendence-net


volumes:
  redis_data:
  postgres_data:
  rabbitmq_data:

networks:
  transcendence-net:
    name: transcendence-net
    driver: bridge
```

## 📄 File: ./.env
```properties
# .env — never commit this
DB_PASSWORD=password
DB_USER=transcendence
DB_NAME=transcendence_db

REDIS_PASSWORD=password

RABBITMQ_USER=admin
RABBITMQ_PASSWORD=password

CERT_PASSWORD=password
CERT_DIR_PATH=/home/laxuard/1337/Microservices/certs

EUREKA_USERNAME=admin
EUREKA_PASSWORD=password

CONFIG_USERNAME=admin
CONFIG_PASSWORD=password
CONFIG_REPO_PATH=/home/laxuard/1337/Microservices/config-repo

REDIS_PORT=6379
EUREKA_PORT=8761
GATEWAY_PORT=8080
POSTGRES_PORT=5432
RABBITMQ_PORT=5672
AUTH_SERVICE_PORT=8081
CONFIG_SERVER_PORT=8888
RABBITMQ_MANAGEMENT_PORT=15672

GOOGLE_CLIENT_ID=895874970701-07mag5f1pojc2i97mh5bmsf6euvmq5hv.apps.googleusercontent.com
GOOGLE_CLIENT_SECRET=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

```

## 📄 File: ./frontend/eslint.config.js
```javascript
import js from '@eslint/js'
import globals from 'globals'
import reactHooks from 'eslint-plugin-react-hooks'
import reactRefresh from 'eslint-plugin-react-refresh'
import { defineConfig, globalIgnores } from 'eslint/config'

export default defineConfig([
  globalIgnores(['dist']),
  {
    files: ['**/*.{js,jsx}'],
    extends: [
      js.configs.recommended,
      reactHooks.configs.flat.recommended,
      reactRefresh.configs.vite,
    ],
    languageOptions: {
      globals: globals.browser,
      parserOptions: { ecmaFeatures: { jsx: true } },
    },
  },
])

```

## 📄 File: ./frontend/index.html
```html
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>NexusAuth — Secure Gateway</title>
  <link rel="preconnect" href="https://fonts.googleapis.com" />
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin />
  <link href="https://fonts.googleapis.com/css2?family=Space+Mono:ital,wght@0,400;0,700;1,400&family=Syne:wght@400;600;700;800&family=DM+Sans:ital,opsz,wght@0,9..40,300;0,9..40,400;0,9..40,500;1,9..40,300&display=swap" rel="stylesheet" />
</head>
<body>
  <div id="root"></div>
  <script type="module" src="/src/main.jsx"></script>
</body>
</html>

```

## 📄 File: ./frontend/src/App.css
```css
/* Cleared App.css for custom Global Index.css Design System */

```

## 📄 File: ./frontend/src/App.jsx
```javascript
import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import LoginScreen from './screens/LoginScreen';
import RegisterScreen from './screens/RegisterScreen';
import MfaChallengeScreen from './screens/MfaChallengeScreen';
import DashboardScreen from './screens/DashboardScreen';

export default function App() {
  return (
    <AuthProvider>
      <Routes>
        {/* Public Routes */}
        <Route path="/login" element={<LoginScreen />} />
        <Route path="/register" element={<RegisterScreen />} />
        <Route path="/mfa-challenge" element={<MfaChallengeScreen />} />

        {/* Secure Dashboard Route */}
        <Route path="/dashboard" element={<DashboardScreen />} />

        {/* Fallbacks */}
        <Route path="/" element={<Navigate to="/login" replace />} />
        <Route path="*" element={<Navigate to="/login" replace />} />
      </Routes>
    </AuthProvider>
  );
}

```

## 📄 File: ./frontend/src/index.css
```css
/* ============================================
   NEXUSAUTH — GLOBAL DESIGN SYSTEM
   Cyber-Industrial Dark Theme
   ============================================ */

:root {
  /* Color Palette */
  --bg-void: #050709;
  --bg-base: #090c10;
  --bg-surface: #0d1117;
  --bg-raised: #131920;
  --bg-overlay: #1a2332;
  --bg-glass: rgba(13, 17, 23, 0.85);

  --border-subtle: rgba(0, 240, 200, 0.08);
  --border-default: rgba(0, 240, 200, 0.18);
  --border-strong: rgba(0, 240, 200, 0.4);
  --border-accent: rgba(0, 240, 200, 0.7);

  --teal-glow: #00f0c8;
  --teal-mid: #00c4a3;
  --teal-deep: #007a65;
  --teal-dim: rgba(0, 240, 200, 0.12);

  --amber-warn: #f0a500;
  --red-danger: #ff4560;
  --blue-info: #4d94ff;

  --text-primary: #e8edf5;
  --text-secondary: #8899aa;
  --text-muted: #4a5568;
  --text-accent: #00f0c8;

  /* Typography */
  --font-display: 'Syne', sans-serif;
  --font-mono: 'Space Mono', monospace;
  --font-body: 'DM Sans', sans-serif;

  /* Spacing */
  --space-1: 4px;
  --space-2: 8px;
  --space-3: 12px;
  --space-4: 16px;
  --space-5: 20px;
  --space-6: 24px;
  --space-8: 32px;
  --space-10: 40px;
  --space-12: 48px;
  --space-16: 64px;

  /* Radii */
  --radius-sm: 4px;
  --radius-md: 8px;
  --radius-lg: 12px;
  --radius-xl: 20px;

  /* Shadows */
  --shadow-teal: 0 0 30px rgba(0, 240, 200, 0.15);
  --shadow-teal-intense: 0 0 60px rgba(0, 240, 200, 0.25);
  --shadow-panel: 0 8px 40px rgba(0, 0, 0, 0.6);
  --shadow-inset: inset 0 1px 0 rgba(255, 255, 255, 0.05);
}

*, *::before, *::after {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
}

html {
  height: 100%;
  font-size: 16px;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

body {
  height: 100%;
  background-color: var(--bg-void);
  color: var(--text-primary);
  font-family: var(--font-body);
  font-weight: 400;
  line-height: 1.6;
  overflow-x: hidden;
}

#root {
  height: 100%;
}

/* Scrollbar */
::-webkit-scrollbar { width: 6px; }
::-webkit-scrollbar-track { background: var(--bg-base); }
::-webkit-scrollbar-thumb { background: var(--teal-deep); border-radius: 3px; }
::-webkit-scrollbar-thumb:hover { background: var(--teal-mid); }

/* Selection */
::selection {
  background: rgba(0, 240, 200, 0.25);
  color: var(--text-primary);
}

/* Focus */
:focus-visible {
  outline: 2px solid var(--teal-glow);
  outline-offset: 2px;
  border-radius: var(--radius-sm);
}

/* ============================================
   LAYOUT UTILITIES
   ============================================ */

.auth-layout {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 1fr 1fr;
  position: relative;
  overflow: hidden;
}

@media (max-width: 900px) {
  .auth-layout {
    grid-template-columns: 1fr;
  }
  .auth-layout__brand {
    display: none;
  }
}

.auth-layout__brand {
  background: var(--bg-base);
  border-right: 1px solid var(--border-subtle);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: flex-start;
  padding: var(--space-16) var(--space-12);
  position: relative;
  overflow: hidden;
}

.auth-layout__form {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: var(--space-8);
  background: var(--bg-surface);
  min-height: 100vh;
}

/* ============================================
   BACKGROUND EFFECTS
   ============================================ */

.grid-bg {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(0, 240, 200, 0.03) 1px, transparent 1px),
    linear-gradient(90deg, rgba(0, 240, 200, 0.03) 1px, transparent 1px);
  background-size: 40px 40px;
  pointer-events: none;
}

.glow-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  pointer-events: none;
  animation: orb-drift 12s ease-in-out infinite alternate;
}

.glow-orb--teal {
  width: 400px;
  height: 400px;
  background: radial-gradient(circle, rgba(0, 240, 200, 0.12) 0%, transparent 70%);
}

.glow-orb--blue {
  width: 300px;
  height: 300px;
  background: radial-gradient(circle, rgba(77, 148, 255, 0.08) 0%, transparent 70%);
}

@keyframes orb-drift {
  from { transform: translate(0, 0) scale(1); }
  to { transform: translate(30px, -20px) scale(1.05); }
}

/* ============================================
   CARD / PANEL
   ============================================ */

.card {
  background: var(--bg-raised);
  border: 1px solid var(--border-default);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-panel), var(--shadow-inset);
  position: relative;
  overflow: hidden;
}

.card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, var(--teal-glow), transparent);
  opacity: 0.5;
}

.auth-card {
  width: 100%;
  max-width: 440px;
  padding: var(--space-10) var(--space-8);
  animation: card-in 0.4s cubic-bezier(0.16, 1, 0.3, 1) both;
}

@keyframes card-in {
  from {
    opacity: 0;
    transform: translateY(20px) scale(0.98);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

/* ============================================
   TYPOGRAPHY
   ============================================ */

.text-display {
  font-family: var(--font-display);
  font-weight: 800;
  line-height: 1.1;
  letter-spacing: -0.02em;
}

.text-mono {
  font-family: var(--font-mono);
  letter-spacing: 0.05em;
}

.text-label {
  font-family: var(--font-mono);
  font-size: 0.7rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.15em;
  color: var(--text-muted);
}

.text-accent { color: var(--text-accent); }
.text-secondary { color: var(--text-secondary); }
.text-danger { color: var(--red-danger); }
.text-warn { color: var(--amber-warn); }

/* ============================================
   LOGO / BRAND
   ============================================ */

.logo {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  text-decoration: none;
}

.logo__mark {
  width: 36px;
  height: 36px;
  position: relative;
}

.logo__name {
  font-family: var(--font-display);
  font-weight: 800;
  font-size: 1.3rem;
  color: var(--text-primary);
  letter-spacing: -0.02em;
}

.logo__name span {
  color: var(--teal-glow);
}

/* ============================================
   FORM ELEMENTS
   ============================================ */

.form-group {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.form-label {
  font-family: var(--font-mono);
  font-size: 0.68rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.12em;
  color: var(--text-muted);
}

.form-input {
  width: 100%;
  padding: var(--space-3) var(--space-4);
  background: var(--bg-overlay);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-md);
  color: var(--text-primary);
  font-family: var(--font-body);
  font-size: 0.95rem;
  font-weight: 400;
  line-height: 1.5;
  transition: border-color 0.2s ease, box-shadow 0.2s ease, background 0.2s ease;
  appearance: none;
}

.form-input::placeholder {
  color: var(--text-muted);
  font-family: var(--font-mono);
  font-size: 0.8rem;
}

.form-input:hover {
  border-color: var(--border-default);
  background: rgba(26, 35, 50, 0.8);
}

.form-input:focus {
  outline: none;
  border-color: var(--teal-mid);
  box-shadow: 0 0 0 3px rgba(0, 240, 200, 0.1), 0 0 20px rgba(0, 240, 200, 0.05);
  background: rgba(26, 35, 50, 0.9);
}

.form-input.error {
  border-color: var(--red-danger);
  box-shadow: 0 0 0 3px rgba(255, 69, 96, 0.1);
}

.form-error {
  font-size: 0.78rem;
  color: var(--red-danger);
  display: flex;
  align-items: center;
  gap: var(--space-1);
}

/* ============================================
   BUTTONS
   ============================================ */

.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-2);
  padding: var(--space-3) var(--space-5);
  border-radius: var(--radius-md);
  font-family: var(--font-display);
  font-size: 0.9rem;
  font-weight: 700;
  letter-spacing: 0.02em;
  cursor: pointer;
  transition: all 0.2s cubic-bezier(0.16, 1, 0.3, 1);
  border: 1px solid transparent;
  text-decoration: none;
  position: relative;
  overflow: hidden;
  white-space: nowrap;
}

.btn::after {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, rgba(255,255,255,0.08) 0%, transparent 60%);
  pointer-events: none;
}

.btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
  pointer-events: none;
}

/* Primary */
.btn-primary {
  background: linear-gradient(135deg, var(--teal-mid) 0%, var(--teal-glow) 100%);
  color: var(--bg-void);
  border-color: var(--teal-mid);
  box-shadow: 0 4px 20px rgba(0, 240, 200, 0.2);
}

.btn-primary:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 30px rgba(0, 240, 200, 0.35);
  filter: brightness(1.05);
}

.btn-primary:active {
  transform: translateY(0);
}

/* Ghost */
.btn-ghost {
  background: transparent;
  color: var(--text-secondary);
  border-color: var(--border-default);
}

.btn-ghost:hover {
  background: var(--bg-overlay);
  border-color: var(--border-strong);
  color: var(--text-primary);
}

/* Google */
.btn-google {
  background: var(--bg-overlay);
  color: var(--text-primary);
  border-color: var(--border-default);
  width: 100%;
  padding: var(--space-3) var(--space-4);
}

.btn-google:hover {
  background: var(--bg-glass);
  border-color: var(--border-strong);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
  transform: translateY(-1px);
}

/* Danger */
.btn-danger {
  background: rgba(255, 69, 96, 0.12);
  color: var(--red-danger);
  border-color: rgba(255, 69, 96, 0.3);
}

.btn-danger:hover {
  background: rgba(255, 69, 96, 0.2);
  border-color: rgba(255, 69, 96, 0.5);
  box-shadow: 0 0 20px rgba(255, 69, 96, 0.2);
}

/* Full width */
.btn-full { width: 100%; }

/* Sizes */
.btn-sm { padding: var(--space-2) var(--space-3); font-size: 0.8rem; }
.btn-lg { padding: var(--space-4) var(--space-6); font-size: 1rem; }

/* ============================================
   DIVIDER
   ============================================ */

.divider {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.divider__line {
  flex: 1;
  height: 1px;
  background: var(--border-subtle);
}

.divider__text {
  font-family: var(--font-mono);
  font-size: 0.65rem;
  text-transform: uppercase;
  letter-spacing: 0.15em;
  color: var(--text-muted);
}

/* ============================================
   ALERTS / STATUS
   ============================================ */

.alert {
  padding: var(--space-3) var(--space-4);
  border-radius: var(--radius-md);
  font-size: 0.85rem;
  display: flex;
  align-items: flex-start;
  gap: var(--space-3);
  animation: alert-in 0.25s ease both;
}

@keyframes alert-in {
  from { opacity: 0; transform: translateY(-8px); }
  to { opacity: 1; transform: translateY(0); }
}

.alert--error {
  background: rgba(255, 69, 96, 0.1);
  border: 1px solid rgba(255, 69, 96, 0.25);
  color: #ff8fa0;
}

.alert--success {
  background: rgba(0, 240, 200, 0.08);
  border: 1px solid rgba(0, 240, 200, 0.2);
  color: var(--teal-glow);
}

.alert--warn {
  background: rgba(240, 165, 0, 0.1);
  border: 1px solid rgba(240, 165, 0, 0.25);
  color: var(--amber-warn);
}

/* ============================================
   SPINNER
   ============================================ */

.spinner {
  width: 18px;
  height: 18px;
  border: 2px solid rgba(0,240,200,0.2);
  border-top-color: var(--teal-glow);
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
  flex-shrink: 0;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* ============================================
   BADGE
   ============================================ */

.badge {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 3px 8px;
  border-radius: 20px;
  font-family: var(--font-mono);
  font-size: 0.68rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.1em;
}

.badge--online {
  background: rgba(0, 240, 200, 0.12);
  color: var(--teal-glow);
  border: 1px solid rgba(0, 240, 200, 0.25);
}

.badge--offline {
  background: rgba(255, 69, 96, 0.12);
  color: var(--red-danger);
  border: 1px solid rgba(255, 69, 96, 0.25);
}

.badge--warn {
  background: rgba(240, 165, 0, 0.12);
  color: var(--amber-warn);
  border: 1px solid rgba(240, 165, 0, 0.25);
}

.badge__dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: currentColor;
  animation: pulse-dot 2s ease infinite;
}

@keyframes pulse-dot {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.5; transform: scale(0.8); }
}

/* ============================================
   STACK / FLOW
   ============================================ */

.stack { display: flex; flex-direction: column; }
.stack-2 { gap: var(--space-2); }
.stack-3 { gap: var(--space-3); }
.stack-4 { gap: var(--space-4); }
.stack-5 { gap: var(--space-5); }
.stack-6 { gap: var(--space-6); }
.stack-8 { gap: var(--space-8); }

.row { display: flex; align-items: center; }
.row-between { display: flex; align-items: center; justify-content: space-between; }
.gap-2 { gap: var(--space-2); }
.gap-3 { gap: var(--space-3); }
.gap-4 { gap: var(--space-4); }

/* ============================================
   LINK
   ============================================ */

.link {
  color: var(--teal-glow);
  text-decoration: none;
  font-size: 0.85rem;
  font-weight: 500;
  transition: opacity 0.15s;
}

.link:hover { opacity: 0.75; }

/* ============================================
   ANIMATED SCAN LINE
   ============================================ */

.scan-line {
  position: absolute;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(0,240,200,0.6), transparent);
  animation: scan 5s ease-in-out infinite;
  pointer-events: none;
}

@keyframes scan {
  0% { top: 0%; opacity: 0; }
  5% { opacity: 1; }
  95% { opacity: 1; }
  100% { top: 100%; opacity: 0; }
}

/* ============================================
   CORNER DECORATION
   ============================================ */

.corner-decor {
  position: absolute;
  width: 20px;
  height: 20px;
}

.corner-decor--tl { top: 12px; left: 12px; border-top: 2px solid var(--teal-glow); border-left: 2px solid var(--teal-glow); }
.corner-decor--tr { top: 12px; right: 12px; border-top: 2px solid var(--teal-glow); border-right: 2px solid var(--teal-glow); }
.corner-decor--bl { bottom: 12px; left: 12px; border-bottom: 2px solid var(--teal-glow); border-left: 2px solid var(--teal-glow); }
.corner-decor--br { bottom: 12px; right: 12px; border-bottom: 2px solid var(--teal-glow); border-right: 2px solid var(--teal-glow); }

```

## 📄 File: ./frontend/src/main.jsx
```javascript
import React from 'react';
import ReactDOM from 'react-dom/client';
import { BrowserRouter } from 'react-router-dom';
import App from './App';
import './index.css';

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <BrowserRouter>
      <App />
    </BrowserRouter>
  </React.StrictMode>
);

```

## 📄 File: ./frontend/src/services/api.js
```javascript
/**
 * NexusAuth — API Service Layer
 * All calls go through the BFF Gateway at https://localhost:8080
 * credentials: "include" is set on EVERY request for session cookie propagation
 */

const GATEWAY = '';

// ─── Core fetch wrapper ────────────────────────────────────────────────────

async function apiFetch(path, options = {}) {
  const url = `${GATEWAY}${path}`;

  const response = await fetch(url, {
    credentials: 'include',           // Always send/receive session cookies
    headers: {
      'Content-Type': 'application/json',
      'Accept': 'application/json',
      ...(options.headers || {}),
    },
    ...options,
  });

  return response;
}

// ─── Auth Endpoints ────────────────────────────────────────────────────────

/**
 * POST /api/auth/login
 * Returns: { status: "AUTHENTICATED" | "AWAITING_MFA", user?: {...} }
 */
export async function loginWithCredentials(username, password) {
  const response = await apiFetch('/api/auth/login', {
    method: 'POST',
    body: JSON.stringify({ login: username, password }), // Maps properly to Auth Service's LoginRequest (login & password)
  });

  const data = await response.json().catch(() => ({}));
  return { status: response.status, data };
}

/**
 * POST /api/auth/register
 * Body: { username, email, password }
 */
export async function register(username, email, password) {
  const response = await apiFetch('/api/auth/register', {
    method: 'POST',
    body: JSON.stringify({ username, email, password }),
  });

  const data = await response.json().catch(() => ({}));
  return { status: response.status, data };
}

/**
 * POST /api/auth/2fa/verify
 * Body: { methodType: "TOTP", code }
 * Returns: { status: "VERIFIED" }
 */
export async function verifyMfa(code) {
  const response = await apiFetch('/api/auth/2fa/verify', {
    method: 'POST',
    body: JSON.stringify({ methodType: 'TOTP', code }),
  });

  const data = await response.json().catch(() => ({}));
  return { status: response.status, data };
}

/**
 * POST /api/auth/2fa/setup
 * Body: { methodType: "TOTP" }
 * Returns: { status: "SETUP_INITIATED", setupDetails: { qrCodeUrl, secret } }
 */
export async function getMfaSetup() {
  const response = await apiFetch('/api/auth/2fa/setup', {
    method: 'POST',
    body: JSON.stringify({ methodType: 'TOTP' }),
  });
  const data = await response.json().catch(() => ({}));
  return { status: response.status, data };
}

/**
 * POST /api/auth/2fa/enable
 * Body: { methodType: "TOTP", code }
 * Returns: { status: "ENABLED" }
 */
export async function confirmMfaSetup(code) {
  const response = await apiFetch('/api/auth/2fa/enable', {
    method: 'POST',
    body: JSON.stringify({ methodType: 'TOTP', code }),
  });

  const data = await response.json().catch(() => ({}));
  return { status: response.status, data };
}

/**
 * GET /api/auth/users — guarded endpoint (tests auth status)
 */
export async function getUsers() {
  const response = await apiFetch('/api/auth/users');
  
  // Handles plain text responses since auth-service /users returns a raw text string
  const contentType = response.headers.get('content-type') || '';
  let data;
  if (contentType.includes('application/json')) {
    data = await response.json().catch(() => ({}));
  } else {
    data = await response.text().catch(() => '');
  }
  
  return { status: response.status, data };
}

// ─── OAuth2 ────────────────────────────────────────────────────────────────

/**
 * Initiates Google OAuth2 by doing a full browser redirect.
 * The BFF gateway will handle the OAuth dance and redirect back.
 */
export function redirectToGoogleOAuth(isLink = false) {
  const query = isLink ? '?link=true' : '';
  window.location.href = `${GATEWAY}/oauth2/authorization/google${query}`;
}

/**
 * Initiates FortyTwo OAuth2 by doing a full browser redirect.
 */
export function redirectToFortyTwoOAuth(isLink = false) {
  const query = isLink ? '?link=true' : '';
  window.location.href = `${GATEWAY}/oauth2/authorization/fortytwo${query}`;
}

/**
 * POST /api/auth/oauth2/unlink
 * Body: { provider }
 */
export async function unlinkOAuth2Provider(provider) {
  const response = await apiFetch('/api/auth/oauth2/unlink', {
    method: 'POST',
    body: JSON.stringify({ provider }),
  });

  const data = await response.json().catch(() => ({}));
  return { status: response.status, data };
}

/**
 * POST /api/auth/logout
 * Triggers full stateful session invalidation on the BFF Gateway.
 */
export async function logoutUser() {
  const response = await apiFetch('/api/auth/logout', {
    method: 'POST',
  });
  return response.status;
}

// ─── Session Interceptor ──────────────────────────────────────────────────

/**
 * Wraps an API call and handles session-based interceptor logic:
 * - 401 → callback for redirect to login
 * - 403 → callback for redirect to MFA challenge
 */
export async function withSessionIntercept(apiFn, { on401, on403 } = {}) {
  try {
    const result = await apiFn();

    if (result.status === 401) {
      on401?.();
      return null;
    }

    if (result.status === 403) {
      on403?.();
      return null;
    }

    return result;
  } catch (error) {
    console.error('[API] Network error:', error);
    throw error;
  }
}

```

## 📄 File: ./frontend/src/context/AuthContext.jsx
```javascript
import React, { createContext, useContext, useState, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { getUsers, logoutUser } from '../services/api';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const loadCurrentUser = useCallback(async () => {
    setLoading(true);
    try {
      const { status, data } = await getUsers();
      if (status === 200) {
        // Since we are validated, store full user profile state
        const profile = { authenticated: true, ...data };
        setUser(profile);
        return profile;
      } else if (status === 401) {
        setUser(null);
      } else if (status === 403) {
        navigate('/mfa-challenge');
      }
    } catch (_) { /* silent */ }
    finally {
      setLoading(false);
    }
    return null;
  }, [navigate]);

  const logout = useCallback(async () => {
    try {
      await logoutUser();
    } catch (_) { /* silent */ }
    setUser(null);
    navigate('/login');
  }, [navigate]);

  // Global session intercept handlers
  const handle401 = useCallback(() => {
    setUser(null);
    navigate('/login');
  }, [navigate]);

  const handle403 = useCallback(() => {
    navigate('/mfa-challenge');
  }, [navigate]);

  return (
    <AuthContext.Provider value={{
      user, setUser,
      loading,
      loadCurrentUser,
      logout,
      handle401,
      handle403,
    }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
}

```

## 📄 File: ./frontend/src/components/Logo.jsx
```javascript
import React from 'react';

export default function Logo({ size = 'sm' }) {
  const isLarge = size === 'lg';
  return (
    <div className="logo">
      <div className="logo__mark" style={{ width: isLarge ? '48px' : '36px', height: isLarge ? '48px' : '36px' }}>
        <svg viewBox="0 0 100 100" fill="none" xmlns="http://www.w3.org/2000/svg" style={{ width: '100%', height: '100%' }}>
          <polygon points="50,5 95,30 95,80 50,95 5,80 5,30" stroke="var(--teal-glow)" strokeWidth="8" fill="rgba(0, 240, 200, 0.05)" />
          <polygon points="50,20 80,38 80,72 50,82 20,72 20,38" stroke="var(--teal-mid)" strokeWidth="6" fill="rgba(0, 240, 200, 0.1)" />
          <circle cx="50" cy="50" r="10" fill="var(--teal-glow)" />
        </svg>
      </div>
      <span className="logo__name" style={{ fontSize: isLarge ? '1.8rem' : '1.3rem' }}>
        NEXUS<span>AUTH</span>
      </span>
    </div>
  );
}

```

## 📄 File: ./frontend/src/components/AuthLayout.jsx
```javascript
import React from 'react';
import Logo from './Logo';

const BRAND_FEATURES = [
  { icon: '⬡', label: 'Zero-Trust Architecture', desc: 'Every request verified at the gateway layer.' },
  { icon: '◈', label: 'OAuth2 + MFA', desc: 'Multi-factor enforcement with TOTP support.' },
  { icon: '◉', label: 'Session Intercept', desc: 'Automatic 401/403 challenge handling.' },
];

export default function AuthLayout({ children }) {
  return (
    <div className="auth-layout">
      {/* ─── Brand Panel ─────────────────────────── */}
      <div className="auth-layout__brand">
        <div className="grid-bg" />
        <div className="glow-orb glow-orb--teal" style={{ top: '20%', left: '-10%' }} />
        <div className="glow-orb glow-orb--blue" style={{ bottom: '15%', right: '10%' }} />
        <div className="scan-line" />

        <div className="corner-decor corner-decor--tl" />
        <div className="corner-decor corner-decor--br" />

        <div style={{ position: 'relative', zIndex: 1, maxWidth: '480px' }}>
          <Logo size="lg" />

          <div style={{ marginTop: '48px', marginBottom: '40px' }}>
            <h1 className="text-display" style={{ fontSize: 'clamp(2rem, 3.5vw, 2.8rem)', color: 'var(--text-primary)', marginBottom: '16px' }}>
              Secure by<br />
              <span style={{ color: 'var(--teal-glow)' }}>design.</span>
            </h1>
            <p style={{ color: 'var(--text-secondary)', fontSize: '1rem', lineHeight: 1.7, maxWidth: '380px' }}>
              Enterprise-grade authentication backed by a Spring microservices BFF gateway.
            </p>
          </div>

          <div className="stack stack-4">
            {BRAND_FEATURES.map((f, i) => (
              <div key={i} style={{
                display: 'flex',
                alignItems: 'flex-start',
                gap: '16px',
                padding: '16px',
                background: 'rgba(13,17,23,0.6)',
                border: '1px solid var(--border-subtle)',
                borderRadius: '8px',
                backdropFilter: 'blur(8px)',
              }}>
                <span style={{
                  fontSize: '1.1rem',
                  color: 'var(--teal-glow)',
                  lineHeight: 1,
                  marginTop: '2px',
                  flexShrink: 0,
                  fontFamily: 'var(--font-mono)',
                }}>{f.icon}</span>
                <div>
                  <div style={{ fontFamily: 'var(--font-display)', fontWeight: 700, fontSize: '0.9rem', marginBottom: '2px' }}>{f.label}</div>
                  <div style={{ color: 'var(--text-muted)', fontSize: '0.8rem' }}>{f.desc}</div>
                </div>
              </div>
            ))}
          </div>

          {/* Version tag */}
          <div style={{ marginTop: '40px', display: 'flex', alignItems: 'center', gap: '8px' }}>
            <span className="text-label">BFF Gateway</span>
            <span style={{ width: '4px', height: '4px', borderRadius: '50%', background: 'var(--text-muted)' }} />
            <span className="text-label" style={{ color: 'var(--teal-deep)' }}>v2.4.1</span>
            <span style={{ width: '4px', height: '4px', borderRadius: '50%', background: 'var(--text-muted)' }} />
            <span className="badge badge--online"><span className="badge__dot" />ONLINE</span>
          </div>
        </div>
      </div>

      {/* ─── Form Panel ──────────────────────────── */}
      <div className="auth-layout__form">
        {children}
      </div>
    </div>
  );
}

```

## 📄 File: ./frontend/src/screens/LoginScreen.jsx
```javascript
import React, { useState } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import AuthLayout from '../components/AuthLayout';
import Logo from '../components/Logo';
import { loginWithCredentials, redirectToGoogleOAuth } from '../services/api';
import { useAuth } from '../context/AuthContext';

const GoogleIcon = () => (
  <svg width="18" height="18" viewBox="0 0 18 18" fill="none">
    <path d="M17.64 9.2c0-.637-.057-1.251-.164-1.84H9v3.481h4.844a4.14 4.14 0 0 1-1.796 2.716v2.259h2.908c1.702-1.567 2.684-3.875 2.684-6.615Z" fill="#4285F4"/>
    <path d="M9 18c2.43 0 4.467-.806 5.956-2.18l-2.908-2.259c-.806.54-1.837.86-3.048.86-2.344 0-4.328-1.584-5.036-3.711H.957v2.332A8.997 8.997 0 0 0 9 18Z" fill="#34A853"/>
    <path d="M3.964 10.71A5.41 5.41 0 0 1 3.682 9c0-.593.102-1.17.282-1.71V4.958H.957A8.996 8.996 0 0 0 0 9c0 1.452.348 2.827.957 4.042l3.007-2.332Z" fill="#FBBC05"/>
    <path d="M9 3.58c1.321 0 2.508.454 3.44 1.345l2.582-2.58C13.463.891 11.426 0 9 0A8.997 8.997 0 0 0 .957 4.958L3.964 7.29C4.672 5.163 6.656 3.58 9 3.58Z" fill="#EA4335"/>
  </svg>
);

const EyeIcon = ({ open }) => open ? (
  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
    <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/>
  </svg>
) : (
  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
    <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/>
    <line x1="1" y1="1" x2="23" y2="23"/>
  </svg>
);

export default function LoginScreen() {
  const navigate = useNavigate();
  const location = useLocation();
  const { setUser, loadCurrentUser } = useAuth();

  const [form, setForm] = useState({ username: '', password: '' });
  const [showPw, setShowPw] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  React.useEffect(() => {
    const params = new URLSearchParams(location.search);
    const errParam = params.get('error');
    if (errParam === 'email_taken') {
      setError('This email address is already bound to a username and password account. Please log in with your credentials.');
    } else if (errParam === 'auth_error') {
      setError('Authentication failed. Please try again.');
    }
  }, [location]);

  const handleChange = (e) => {
    setForm(f => ({ ...f, [e.target.name]: e.target.value }));
    if (error) setError('');
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!form.username || !form.password) {
      setError('Please enter your credentials.');
      return;
    }

    setLoading(true);
    setError('');

    try {
      const { status, data } = await loginWithCredentials(form.username, form.password);

      if (status === 200 || status === 201) {
        if (data.status === 'AUTHENTICATED') {
          await loadCurrentUser();
          navigate('/dashboard');
        } else if (data.status === 'AWAITING_MFA') {
          navigate('/mfa-challenge');
        } else {
          setError('Unexpected response from server.');
        }
      } else if (status === 202) {
        if (data.status === 'AWAITING_MFA') {
          navigate('/mfa-challenge');
        }
      } else if (status === 401) {
        setError('Invalid username or password.');
      } else if (status === 403) {
        navigate('/mfa-challenge');
      } else {
        setError(data?.message || 'Login failed. Please try again.');
      }
    } catch (err) {
      setError('Cannot reach gateway. Check your connection.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <AuthLayout>
      <div className="auth-card card">
        <div className="corner-decor corner-decor--tl" />
        <div className="corner-decor corner-decor--br" />

        <div className="stack stack-6">
          {/* Header */}
          <div>
            <div style={{ marginBottom: '20px' }}>
              <Logo size="sm" />
            </div>
            <h2 className="text-display" style={{ fontSize: '1.75rem', marginBottom: '6px' }}>
              Welcome back
            </h2>
            <p style={{ color: 'var(--text-secondary)', fontSize: '0.875rem' }}>
              Sign in to your secure workspace
            </p>
          </div>

          {/* Google OAuth */}
          <button className="btn btn-google" onClick={redirectToGoogleOAuth} type="button">
            <GoogleIcon />
            <span>Continue with Google</span>
            <span style={{ marginLeft: 'auto', opacity: 0.4, fontSize: '0.7rem', fontFamily: 'var(--font-mono)' }}>OAUTH2</span>
          </button>

          {/* Divider */}
          <div className="divider">
            <span className="divider__line" />
            <span className="divider__text">or sign in with credentials</span>
            <span className="divider__line" />
          </div>

          {/* Error */}
          {error && (
            <div className="alert alert--error">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" style={{flexShrink:0,marginTop:'1px'}}>
                <circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/>
              </svg>
              {error}
            </div>
          )}

          {/* Form */}
          <form onSubmit={handleSubmit} className="stack stack-4" noValidate>
            <div className="form-group">
              <label className="form-label" htmlFor="username">Username</label>
              <input
                id="username"
                name="username"
                type="text"
                className={`form-input ${error ? 'error' : ''}`}
                placeholder="your_username"
                value={form.username}
                onChange={handleChange}
                autoComplete="username"
                autoFocus
              />
            </div>

            <div className="form-group">
              <div className="row-between" style={{ marginBottom: '6px' }}>
                <label className="form-label" htmlFor="password">Password</label>
                <button
                  type="button"
                  style={{ background: 'none', border: 'none', cursor: 'pointer', color: 'var(--text-muted)', display: 'flex', alignItems: 'center', gap: '4px', fontSize: '0.72rem', fontFamily: 'var(--font-mono)' }}
                  onClick={() => setShowPw(s => !s)}
                >
                  <EyeIcon open={showPw} />
                  {showPw ? 'HIDE' : 'SHOW'}
                </button>
              </div>
              <input
                id="password"
                name="password"
                type={showPw ? 'text' : 'password'}
                className={`form-input ${error ? 'error' : ''}`}
                placeholder="••••••••••••"
                value={form.password}
                onChange={handleChange}
                autoComplete="current-password"
              />
            </div>

            <button
              type="submit"
              className="btn btn-primary btn-full btn-lg"
              disabled={loading}
              style={{ marginTop: '4px' }}
            >
              {loading ? (
                <>
                  <span className="spinner" />
                  Authenticating...
                </>
              ) : (
                <>
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
                    <path d="M15 3h4a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2h-4"/><polyline points="10 17 15 12 10 7"/><line x1="15" y1="12" x2="3" y2="12"/>
                  </svg>
                  Sign In
                </>
              )}
            </button>
          </form>

          {/* Footer */}
          <p style={{ textAlign: 'center', color: 'var(--text-muted)', fontSize: '0.83rem' }}>
            No account?{' '}
            <Link to="/register" className="link">
              Create one free
            </Link>
          </p>
        </div>
      </div>
    </AuthLayout>
  );
}

```

## 📄 File: ./frontend/src/screens/RegisterScreen.jsx
```javascript
import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import AuthLayout from '../components/AuthLayout';
import Logo from '../components/Logo';
import { register } from '../services/api';
import { useAuth } from '../context/AuthContext';

export default function RegisterScreen() {
  const navigate = useNavigate();
  const { setUser } = useAuth();

  const [form, setForm] = useState({ username: '', email: '', password: '' });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState(false);

  const handleChange = (e) => {
    setForm(f => ({ ...f, [e.target.name]: e.target.value }));
    if (error) setError('');
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!form.username || !form.email || !form.password) {
      setError('Please fill in all registration fields.');
      return;
    }

    setLoading(true);
    setError('');

    try {
      const { status, data } = await register(form.username, form.email, form.password);

      if (status === 201) {
        setSuccess(true);
        // Autologin successfully occurred on registration success!
        setUser({ authenticated: true });
        setTimeout(() => {
          navigate('/dashboard');
        }, 1500);
      } else {
        setError(data?.detail || data?.message || 'Registration failed. Please try again.');
      }
    } catch (err) {
      setError('Cannot reach gateway. Check your connection.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <AuthLayout>
      <div className="auth-card card">
        <div className="corner-decor corner-decor--tl" />
        <div className="corner-decor corner-decor--br" />

        <div className="stack stack-6">
          {/* Header */}
          <div>
            <div style={{ marginBottom: '20px' }}>
              <Logo size="sm" />
            </div>
            <h2 className="text-display" style={{ fontSize: '1.75rem', marginBottom: '6px' }}>
              Create Account
            </h2>
            <p style={{ color: 'var(--text-secondary)', fontSize: '0.875rem' }}>
              Deploy your secure client profile identity
            </p>
          </div>

          {/* Success */}
          {success && (
            <div className="alert alert--success">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" style={{flexShrink:0,marginTop:'1px'}}>
                <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/>
              </svg>
              Registration successful! Initializing session...
            </div>
          )}

          {/* Error */}
          {error && (
            <div className="alert alert--error">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" style={{flexShrink:0,marginTop:'1px'}}>
                <circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/>
              </svg>
              {error}
            </div>
          )}

          {/* Form */}
          {!success && (
            <form onSubmit={handleSubmit} className="stack stack-4" noValidate>
              <div className="form-group">
                <label className="form-label" htmlFor="username">Username</label>
                <input
                  id="username"
                  name="username"
                  type="text"
                  className={`form-input ${error ? 'error' : ''}`}
                  placeholder="cyber_operator"
                  value={form.username}
                  onChange={handleChange}
                  autoComplete="username"
                  autoFocus
                />
              </div>

              <div className="form-group">
                <label className="form-label" htmlFor="email">Email address</label>
                <input
                  id="email"
                  name="email"
                  type="email"
                  className={`form-input ${error ? 'error' : ''}`}
                  placeholder="operator@nexus.net"
                  value={form.email}
                  onChange={handleChange}
                  autoComplete="email"
                />
              </div>

              <div className="form-group">
                <label className="form-label" htmlFor="password">Password</label>
                <input
                  id="password"
                  name="password"
                  type="password"
                  className={`form-input ${error ? 'error' : ''}`}
                  placeholder="••••••••••••"
                  value={form.password}
                  onChange={handleChange}
                  autoComplete="new-password"
                />
              </div>

              <button
                type="submit"
                className="btn btn-primary btn-full btn-lg"
                disabled={loading}
                style={{ marginTop: '8px' }}
              >
                {loading ? (
                  <>
                    <span className="spinner" />
                    Registering...
                  </>
                ) : (
                  <>
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
                      <path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="8.5" cy="7" r="4"/><line x1="20" y1="8" x2="20" y2="14"/><line x1="23" y1="11" x2="17" y2="11"/>
                    </svg>
                    Register Operator
                  </>
                )}
              </button>
            </form>
          )}

          {/* Footer */}
          <p style={{ textAlign: 'center', color: 'var(--text-muted)', fontSize: '0.83rem' }}>
            Already registered?{' '}
            <Link to="/login" className="link">
              Sign in operator
            </Link>
          </p>
        </div>
      </div>
    </AuthLayout>
  );
}

```

## 📄 File: ./frontend/src/screens/MfaChallengeScreen.jsx
```javascript
import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import AuthLayout from '../components/AuthLayout';
import Logo from '../components/Logo';
import { verifyMfa } from '../services/api';
import { useAuth } from '../context/AuthContext';

export default function MfaChallengeScreen() {
  const navigate = useNavigate();
  const { setUser, loadCurrentUser } = useAuth();

  const [code, setCode] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleChange = (e) => {
    setCode(e.target.value.replace(/\D/g, '').slice(0, 6));
    if (error) setError('');
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (code.length !== 6) {
      setError('Please enter a valid 6-digit code.');
      return;
    }

    setLoading(true);
    setError('');

    try {
      const { status, data } = await verifyMfa(code);

      if (status === 200) {
        await loadCurrentUser();
        navigate('/dashboard');
      } else {
        setError(data?.message || 'Verification failed. Code may be invalid or expired.');
      }
    } catch (err) {
      setError('Cannot reach gateway. Check your connection.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <AuthLayout>
      <div className="auth-card card">
        <div className="corner-decor corner-decor--tl" />
        <div className="corner-decor corner-decor--br" />

        <div className="stack stack-6">
          {/* Header */}
          <div>
            <div style={{ marginBottom: '20px' }}>
              <Logo size="sm" />
            </div>
            <span className="badge badge--warn" style={{ marginBottom: '12px' }}>
              <span className="badge__dot" />Step-Up Verification
            </span>
            <h2 className="text-display" style={{ fontSize: '1.75rem', marginBottom: '6px' }}>
              Enter MFA Code
            </h2>
            <p style={{ color: 'var(--text-secondary)', fontSize: '0.875rem' }}>
              An active Multi-Factor configuration is enabled on this profile. Verify your token.
            </p>
          </div>

          {/* Error */}
          {error && (
            <div className="alert alert--error">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" style={{flexShrink:0,marginTop:'1px'}}>
                <circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/>
              </svg>
              {error}
            </div>
          )}

          {/* Form */}
          <form onSubmit={handleSubmit} className="stack stack-4">
            <div className="form-group">
              <label className="form-label" htmlFor="mfaCode">6-Digit Verification Token</label>
              <input
                id="mfaCode"
                name="mfaCode"
                type="text"
                className={`form-input ${error ? 'error' : ''}`}
                style={{ textAlign: 'center', fontSize: '1.6rem', letterSpacing: '0.4em', fontFamily: 'var(--font-mono)' }}
                placeholder="000000"
                value={code}
                onChange={handleChange}
                autoComplete="one-time-code"
                autoFocus
              />
            </div>

            <button
              type="submit"
              className="btn btn-primary btn-full btn-lg"
              disabled={loading || code.length !== 6}
              style={{ marginTop: '8px' }}
            >
              {loading ? (
                <>
                  <span className="spinner" />
                  Verifying challenge...
                </>
              ) : (
                <>
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
                    <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/>
                  </svg>
                  Decrypt Gateway Session
                </>
              )}
            </button>
          </form>

          {/* Footer */}
          <p style={{ textAlign: 'center', color: 'var(--text-muted)', fontSize: '0.83rem' }}>
            Having issues?{' '}
            <button
              onClick={() => navigate('/login')}
              style={{ background: 'none', border: 'none', cursor: 'pointer', padding: 0 }}
              className="link"
            >
              Return to Authentication Portal
            </button>
          </p>
        </div>
      </div>
    </AuthLayout>
  );
}

```

## 📄 File: ./frontend/src/screens/DashboardScreen.jsx
```javascript
import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Logo from '../components/Logo';
import { useAuth } from '../context/AuthContext';
import { getUsers, getMfaSetup, confirmMfaSetup, unlinkOAuth2Provider, redirectToGoogleOAuth, redirectToFortyTwoOAuth } from '../services/api';
import { QRCodeSVG } from 'qrcode.react';

export default function DashboardScreen() {
  const navigate = useNavigate();
  const { logout } = useAuth();

  const [activeTab, setActiveTab] = useState('profile');
  const [loading, setLoading] = useState(false);
  const [usersResponse, setUsersResponse] = useState('');
  const [profile, setProfile] = useState(null);
  
  // 2FA Setup state
  const [mfaSecret, setMfaSecret] = useState('');
  const [mfaQrUrl, setMfaQrUrl] = useState('');
  const [confirmCode, setConfirmCode] = useState('');
  const [mfaSuccess, setMfaSuccess] = useState(false);
  const [mfaError, setMfaError] = useState('');

  // Link status notifications
  const [linkMessage, setLinkMessage] = useState('');
  const [linkError, setLinkError] = useState('');

  // Validate session on dashboard load and populate profile context
  useEffect(() => {
    async function verifyAccess() {
      const { status, data } = await getUsers();
      if (status === 401) {
        logout();
      } else if (status === 403) {
        navigate('/mfa-challenge');
      } else if (status === 200) {
        setProfile(data);
      }
    }
    verifyAccess();
  }, [logout, navigate]);

  // Handle OAuth linking query feedback on redirection back to dashboard
  useEffect(() => {
    const params = new URLSearchParams(window.location.search);
    if (params.get('link') === 'success') {
      setLinkMessage('Social identity provider linked successfully!');
      window.history.replaceState({}, document.title, window.location.pathname);
    } else if (params.get('link') === 'error') {
      setLinkError('Failed to link social identity. Provider might already be linked to another profile.');
      window.history.replaceState({}, document.title, window.location.pathname);
    }
  }, []);

  const isLinked = (provider) => {
    if (!profile || !profile.identities) return false;
    return profile.identities.some(id => id.provider === provider);
  };

  const getProviderId = (provider) => {
    if (!profile || !profile.identities) return '';
    const found = profile.identities.find(id => id.provider === provider);
    return found ? found.providerId : '';
  };

  const handleUnlink = async (provider) => {
    if (!window.confirm(`Are you sure you want to unlink your ${provider} account?`)) {
      return;
    }
    setLinkError('');
    setLinkMessage('');
    try {
      const { status, data } = await unlinkOAuth2Provider(provider);
      if (status === 200) {
        setLinkMessage(`${provider} disconnected successfully.`);
        const response = await getUsers();
        if (response.status === 200) {
          setProfile(response.data);
        }
      } else {
        setLinkError(data.message || `Failed to unlink ${provider}.`);
      }
    } catch (err) {
      setLinkError('Error contacting Gateway service.');
    }
  };

  const handleTestApi = async () => {
    setLoading(true);
    setUsersResponse('');
    try {
      const { status, data } = await getUsers();
      if (status === 200) {
        if (typeof data === 'string') {
          setUsersResponse(data);
        } else {
          setUsersResponse(JSON.stringify(data, null, 2));
        }
      } else {
        setUsersResponse(`[Error] Gateway rejected request. Status: ${status}`);
      }
    } catch (err) {
      setUsersResponse('[Error] Cannot contact BFF Gateway proxy.');
    } finally {
      setLoading(false);
    }
  };

  const handleInitiateMfa = async () => {
    setMfaError('');
    setMfaSuccess(false);
    try {
      const { status, data } = await getMfaSetup();
      if (status === 200) {
        setMfaSecret(data.setupDetails.secretKey);
        setMfaQrUrl(data.setupDetails.qrCodeUrl);
      } else {
        setMfaError('Failed to initiate Multi-Factor setup.');
      }
    } catch (err) {
      setMfaError('Error communicating with Auth-Service.');
    }
  };

  const handleConfirmMfa = async (e) => {
    e.preventDefault();
    if (confirmCode.length !== 6) {
      setMfaError('Enter a valid 6-digit confirmation code.');
      return;
    }

    setMfaError('');
    try {
      const { status, data } = await confirmMfaSetup(confirmCode);
      if (status === 200) {
        setMfaSuccess(true);
        setMfaSecret('');
        setMfaQrUrl('');
        setConfirmCode('');
      } else {
        setMfaError(data.message || 'MFA validation failed. Check your secret.');
      }
    } catch (err) {
      setMfaError('Network error confirming setup.');
    }
  };

  return (
    <div style={{ minHeight: '100vh', background: 'var(--bg-void)', color: 'var(--text-primary)', display: 'flex', flexDirection: 'column' }}>
      
      {/* ─── Global Navbar ──────────────────────── */}
      <header style={{ background: 'var(--bg-base)', borderBottom: '1px solid var(--border-subtle)', padding: '16px 32px', display: 'flex', justifyContent: 'space-between', alignItems: 'center', position: 'relative' }}>
        <Logo size="sm" />
        <div style={{ display: 'flex', gap: '16px' }}>
          <button className="btn btn-ghost btn-sm" onClick={logout}>
            Sign Out
          </button>
        </div>
      </header>

      {/* ─── Main Content ────────────────────────── */}
      <main style={{ flex: 1, padding: '40px 32px', maxWidth: '1200px', width: '100%', margin: '0 auto', display: 'grid', gridTemplateColumns: '240px 1fr', gap: '40px' }}>
        
        {/* Sidebar Nav */}
        <aside className="stack stack-3">
          <button 
            className={`btn btn-full ${activeTab === 'profile' ? 'btn-primary' : 'btn-ghost'}`} 
            onClick={() => setActiveTab('profile')}
            style={{ textAlign: 'left', justifyContent: 'flex-start' }}
          >
            ◉ Operator Profile
          </button>
          <button 
            className={`btn btn-full ${activeTab === 'mfa' ? 'btn-primary' : 'btn-ghost'}`} 
            onClick={() => setActiveTab('mfa')}
            style={{ textAlign: 'left', justifyContent: 'flex-start' }}
          >
            🔒 Multi-Factor Setup
          </button>
          <button 
            className={`btn btn-full ${activeTab === 'linking' ? 'btn-primary' : 'btn-ghost'}`} 
            onClick={() => setActiveTab('linking')}
            style={{ textAlign: 'left', justifyContent: 'flex-start' }}
          >
            🔗 Linked Accounts
          </button>
          <button 
            className={`btn btn-full ${activeTab === 'api' ? 'btn-primary' : 'btn-ghost'}`} 
            onClick={() => setActiveTab('api')}
            style={{ textAlign: 'left', justifyContent: 'flex-start' }}
          >
            ⚡ Test Transit JWTs
          </button>
        </aside>

        {/* Tab content panel */}
        <section className="card" style={{ padding: '32px', minHeight: '400px' }}>
            {/* Profile Tab */}
          {activeTab === 'profile' && (
            <div className="stack stack-6">
              <div>
                <span className="badge badge--online" style={{ marginBottom: '8px' }}>
                  <span className="badge__dot" />Edge Active
                </span>
                <h2 className="text-display" style={{ fontSize: '1.75rem', marginBottom: '8px' }}>
                  Identity Topology Map
                </h2>
                <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem' }}>
                  Real-time database visualization of your security context, credential roots, and active MFA gates.
                </p>
              </div>

              {!profile ? (
                <div style={{ display: 'flex', justifyContent: 'center', padding: '40px', color: 'var(--text-secondary)' }}>
                  <span className="text-mono">Synchronizing Identity Graph...</span>
                </div>
              ) : (
                <div className="stack stack-6">
                  {/* Visual Topology Diagram */}
                  <div style={{
                    display: 'flex',
                    flexDirection: 'column',
                    alignItems: 'center',
                    gap: '24px',
                    background: 'rgba(255,255,255,0.01)',
                    padding: '32px 16px',
                    borderRadius: '12px',
                    border: '1px solid var(--border-subtle)',
                    position: 'relative',
                    overflow: 'hidden'
                  }}>
                    {/* Glowing background decor */}
                    <div style={{
                      position: 'absolute',
                      top: '50%',
                      left: '50%',
                      transform: 'translate(-50%, -50%)',
                      width: '300px',
                      height: '300px',
                      background: 'radial-gradient(circle, rgba(0,240,200,0.03) 0%, transparent 70%)',
                      pointerEvents: 'none',
                      zIndex: 0
                    }} />

                    {/* Layer 1: Identity Providers (Linked Sources) */}
                    <div style={{ display: 'flex', gap: '20px', zIndex: 1, width: '100%', justifyContent: 'center' }}>
                      {profile.identities && profile.identities.length > 0 ? (
                        profile.identities.map((ident, idx) => (
                          <div key={idx} style={{
                            background: 'var(--bg-overlay)',
                            border: '1px solid var(--teal-glow)',
                            borderRadius: '8px',
                            padding: '16px 20px',
                            textAlign: 'center',
                            minWidth: '180px',
                            boxShadow: '0 0 15px rgba(0, 240, 200, 0.05)'
                          }}>
                            <span className="text-label" style={{ color: 'var(--teal-glow)', display: 'block', fontSize: '0.75rem', marginBottom: '8px' }}>
                              AUTHENTICATION SOURCE
                            </span>
                            <span className="text-display" style={{ fontSize: '1.2rem', display: 'block', marginBottom: '4px' }}>
                              {ident.provider}
                            </span>
                            <span className="text-mono" style={{ fontSize: '0.75rem', color: 'var(--text-secondary)' }}>
                              ID: {ident.providerId.length > 20 ? ident.providerId.slice(0, 20) + '...' : ident.providerId}
                            </span>
                          </div>
                        ))
                      ) : (
                        <div style={{
                          background: 'var(--bg-overlay)',
                          border: '1px solid var(--border-subtle)',
                          borderRadius: '8px',
                          padding: '16px 20px',
                          textAlign: 'center',
                          minWidth: '180px'
                        }}>
                          <span className="text-label" style={{ color: 'var(--text-secondary)', display: 'block', fontSize: '0.75rem', marginBottom: '8px' }}>
                            IDENTITY SOURCE
                          </span>
                          <span className="text-display" style={{ fontSize: '1.2rem', display: 'block', color: 'var(--text-secondary)' }}>
                            LOCAL CREDENTIALS
                          </span>
                        </div>
                      )}
                    </div>

                    {/* Connector Arrow */}
                    <div style={{ color: 'var(--teal-glow)', fontSize: '1.5rem', fontWeight: 'bold', zIndex: 1, userSelect: 'none' }}>
                      ↓
                    </div>

                    {/* Layer 2: Central Identity Core */}
                    <div style={{
                      background: 'var(--bg-raised)',
                      border: '1.5px solid var(--teal-glow)',
                      borderRadius: '10px',
                      padding: '24px',
                      width: '100%',
                      maxWidth: '480px',
                      zIndex: 1,
                      position: 'relative',
                      boxShadow: '0 0 25px rgba(0, 240, 200, 0.08)'
                    }}>
                      <div style={{ position: 'absolute', top: '12px', right: '12px' }}>
                        <span className="badge badge--online"><span className="badge__dot" />CORE</span>
                      </div>
                      <span className="text-label" style={{ color: 'var(--text-secondary)', fontSize: '0.7rem', display: 'block', marginBottom: '4px' }}>
                        CENTRAL USER IDENTITY
                      </span>
                      <h3 className="text-display" style={{ fontSize: '1.5rem', marginBottom: '4px', color: '#fff' }}>
                        {profile.username}
                      </h3>
                      <span className="text-mono" style={{ fontSize: '0.85rem', color: 'var(--teal-glow)', display: 'block', marginBottom: '16px' }}>
                        {profile.email}
                      </span>
                      
                      <div className="divider" style={{ margin: '12px 0' }}><span className="divider__line" /></div>
                      
                      <div style={{ display: 'grid', gridTemplateColumns: '80px 1fr', gap: '8px 16px', fontSize: '0.8rem' }}>
                        <span className="text-label">USER UUID</span>
                        <span className="text-mono" style={{ color: 'var(--text-secondary)', overflowWrap: 'anywhere' }}>{profile.userId}</span>
                        
                        <span className="text-label">ROLES</span>
                        <div style={{ display: 'flex', gap: '6px', flexWrap: 'wrap' }}>
                          {profile.roles && profile.roles.map((role, idx) => (
                            <span key={idx} className="badge badge--online" style={{ background: 'rgba(0, 240, 200, 0.1)', border: '1px solid var(--teal-glow)', padding: '2px 8px', fontSize: '0.7rem' }}>
                              {role}
                            </span>
                          ))}
                        </div>
                      </div>
                    </div>

                    {/* Connector Arrow */}
                    <div style={{ color: 'var(--teal-glow)', fontSize: '1.5rem', fontWeight: 'bold', zIndex: 1, userSelect: 'none' }}>
                      ↓
                    </div>

                    {/* Layer 3: Multi-Factor Authentication Shield */}
                    <div style={{
                      background: 'var(--bg-overlay)',
                      border: profile.is2faEnabled ? '1.5px solid var(--teal-glow)' : '1px solid var(--border-subtle)',
                      borderRadius: '8px',
                      padding: '16px 24px',
                      width: '100%',
                      maxWidth: '360px',
                      zIndex: 1,
                      display: 'flex',
                      alignItems: 'center',
                      gap: '16px',
                      boxShadow: profile.is2faEnabled ? '0 0 20px rgba(0, 240, 200, 0.06)' : 'none'
                    }}>
                      <div style={{
                        width: '44px',
                        height: '44px',
                        borderRadius: '50%',
                        background: profile.is2faEnabled ? 'rgba(0, 240, 200, 0.1)' : 'rgba(255, 255, 255, 0.02)',
                        border: profile.is2faEnabled ? '1.5px solid var(--teal-glow)' : '1px solid var(--border-subtle)',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        fontSize: '1.3rem'
                      }}>
                        {profile.is2faEnabled ? '🛡️' : '🔓'}
                      </div>
                      <div style={{ flex: 1 }}>
                        <span className="text-label" style={{ color: 'var(--text-secondary)', fontSize: '0.7rem', display: 'block', marginBottom: '2px' }}>
                          STEP-UP SECURITY SHIELD
                        </span>
                        <h4 className="text-display" style={{ fontSize: '1rem', color: profile.is2faEnabled ? 'var(--teal-glow)' : 'var(--text-secondary)' }}>
                          {profile.is2faEnabled ? 'MFA FULLY ENFORCED' : 'NO MFA SHIELD BIND'}
                        </h4>
                        {profile.twoFactorMethods && profile.twoFactorMethods.length > 0 && (
                          <span className="text-mono" style={{ fontSize: '0.7rem', color: 'var(--text-secondary)' }}>
                            Active Methods: {profile.twoFactorMethods.map(m => m.methodType).join(', ')}
                          </span>
                        )}
                      </div>
                    </div>
                  </div>

                  {/* Network Infrastructure Meta */}
                  <div className="divider"><span className="divider__line" /></div>

                  <div className="stack stack-4">
                    <h3 className="text-display" style={{ fontSize: '1rem', color: 'var(--text-secondary)' }}>
                      EDGE NETWORK METADATA
                    </h3>
                    <div style={{ display: 'grid', gridTemplateColumns: '180px 1fr', gap: '12px 16px', background: 'rgba(255,255,255,0.02)', padding: '16px', borderRadius: '8px', border: '1px solid var(--border-subtle)' }}>
                      <span className="text-label">Active Cookie Domain</span>
                      <span className="text-mono" style={{ color: 'var(--teal-glow)', fontSize: '0.85rem' }}>https://localhost:8080</span>

                      <span className="text-label">Encryption Level</span>
                      <span className="text-mono" style={{ fontSize: '0.85rem' }}>Lightweight Transit State Representation (JWT)</span>

                      <span className="text-label">SameSite Cookie Policy</span>
                      <span className="text-mono" style={{ fontSize: '0.85rem' }}>Lax (Secure HttpOnly Session Propagation)</span>

                      <span className="text-label">Downstream Sync</span>
                      <span className="text-mono" style={{ color: 'var(--teal-glow)', fontSize: '0.85rem' }}>mTLS Signed RSA-256 Downstream Handshake</span>
                    </div>
                  </div>
                </div>
              )}
            </div>
          )}

          {/* MFA Tab */}
          {activeTab === 'mfa' && (
            <div className="stack stack-6">
              <div>
                <h2 className="text-display" style={{ fontSize: '1.75rem', marginBottom: '8px' }}>
                  Multi-Factor Authentication (TOTP)
                </h2>
                <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem' }}>
                  Add a second verification layer to secure access logs and block unauthorized proxy sessions.
                </p>
              </div>

              {mfaSuccess && (
                <div className="alert alert--success">
                  Multi-Factor Authentication enabled successfully!
                </div>
              )}

              {mfaError && (
                <div className="alert alert--error">
                  {mfaError}
                </div>
              )}

              {!mfaSecret ? (
                <button className="btn btn-primary" onClick={handleInitiateMfa}>
                  Initialize MFA Setup
                </button>
              ) : (
                <div className="stack stack-6" style={{ background: 'rgba(255,255,255,0.01)', padding: '24px', borderRadius: '8px', border: '1px solid var(--border-subtle)' }}>
                  <div>
                    <h3 className="text-display" style={{ fontSize: '1.1rem', marginBottom: '8px', color: 'var(--teal-glow)' }}>
                      Step 1: Scan Authenticator Token
                    </h3>
                    <p style={{ color: 'var(--text-secondary)', fontSize: '0.85rem', marginBottom: '16px' }}>
                      Scan the QR parameter link using Google Authenticator, or manually key in the secure secret.
                    </p>
                    <div style={{ display: 'flex', justifyContent: 'center', margin: '20px 0' }}>
                      <div style={{ padding: '16px', background: 'var(--bg-overlay)', borderRadius: '8px', border: '1px solid var(--border-subtle)', display: 'inline-block' }}>
                        <QRCodeSVG value={mfaQrUrl} size={160} bgColor="transparent" fgColor="#00f0c8" level="M" includeMargin={false} />
                      </div>
                    </div>
                    <div style={{ background: 'var(--bg-overlay)', padding: '12px', borderRadius: '6px', overflowX: 'auto', border: '1px solid var(--border-subtle)' }}>
                      <span className="text-label" style={{ display: 'block', marginBottom: '4px' }}>MANUAL KEY</span>
                      <span className="text-mono" style={{ fontSize: '1.2rem', color: 'var(--teal-glow)' }}>{mfaSecret}</span>
                    </div>
                    <div style={{ marginTop: '12px', background: 'var(--bg-overlay)', padding: '12px', borderRadius: '6px', overflowX: 'auto', border: '1px solid var(--border-subtle)' }}>
                      <span className="text-label" style={{ display: 'block', marginBottom: '4px' }}>QR URL</span>
                      <span className="text-mono" style={{ fontSize: '0.78rem', color: 'var(--text-secondary)' }}>{mfaQrUrl}</span>
                    </div>
                  </div>

                  <form onSubmit={handleConfirmMfa} className="stack stack-4">
                    <h3 className="text-display" style={{ fontSize: '1.1rem', color: 'var(--teal-glow)' }}>
                      Step 2: Confirm Verification Code
                    </h3>
                    <div className="form-group">
                      <label className="form-label" htmlFor="confirmCode">6-Digit Code</label>
                      <input
                        id="confirmCode"
                        type="text"
                        className="form-input"
                        placeholder="000000"
                        value={confirmCode}
                        onChange={(e) => setConfirmCode(e.target.value.replace(/\D/g, '').slice(0, 6))}
                      />
                    </div>
                    <div className="row gap-3">
                      <button type="submit" className="btn btn-primary">
                        Lock 2FA To Profile
                      </button>
                      <button type="button" className="btn btn-ghost" onClick={() => setMfaSecret('')}>
                        Cancel
                      </button>
                    </div>
                  </form>
                </div>
              )}
            </div>
          )}

          {/* Test Transit JWT Tab */}
          {activeTab === 'api' && (
            <div className="stack stack-6">
              <div>
                <h2 className="text-display" style={{ fontSize: '1.75rem', marginBottom: '8px' }}>
                  Transit JWT & Security Context Verification
                </h2>
                <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem' }}>
                  Test how the Gateway extracts your stateful session cookie, constructs a signed transit JWT, and passes it securely to downstream services.
                </p>
              </div>

              <div className="row gap-4">
                <button className="btn btn-primary" onClick={handleTestApi} disabled={loading}>
                  {loading ? 'Interrogating Gateway...' : 'Interrogate /api/auth/users'}
                </button>
              </div>

              {usersResponse && (
                <div style={{ background: 'var(--bg-overlay)', border: '1px solid var(--border-subtle)', borderRadius: '8px', padding: '20px', position: 'relative' }}>
                  <span className="text-label" style={{ position: 'absolute', top: '12px', right: '12px', color: 'var(--teal-glow)' }}>DECISION RESPONSE</span>
                  <pre style={{ margin: 0, fontFamily: 'var(--font-mono)', fontSize: '0.82rem', overflowX: 'auto', color: 'var(--text-primary)', whiteSpace: 'pre-wrap' }}>
                    {usersResponse}
                  </pre>
                </div>
              )}
            </div>
          )}

          {/* Linked Accounts Tab */}
          {activeTab === 'linking' && (
            <div className="stack stack-6">
              <div>
                <h2 className="text-display" style={{ fontSize: '1.75rem', marginBottom: '8px' }}>
                  Linked Identity Providers
                </h2>
                <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem' }}>
                  Connect or disconnect social single-sign-on (SSO) options to simplify access to your profile.
                </p>
              </div>

              {linkMessage && (
                <div className="alert alert--success">
                  {linkMessage}
                </div>
              )}

              {linkError && (
                <div className="alert alert--error">
                  {linkError}
                </div>
              )}

              <div className="stack stack-4">
                {/* Google Provider Card */}
                <div style={{
                  display: 'flex',
                  justifyContent: 'space-between',
                  alignItems: 'center',
                  background: 'rgba(255,255,255,0.01)',
                  padding: '20px 24px',
                  borderRadius: '8px',
                  border: '1px solid var(--border-subtle)',
                  transition: 'border-color 0.2s'
                }}>
                  <div>
                    <h4 className="text-display" style={{ fontSize: '1.1rem', marginBottom: '4px' }}>Google Access Node</h4>
                    <span className="text-mono" style={{ fontSize: '0.78rem', color: isLinked('GOOGLE') ? 'var(--teal-glow)' : 'var(--text-secondary)' }}>
                      {isLinked('GOOGLE') ? `Connected (Provider ID: ${getProviderId('GOOGLE')})` : 'Disconnected'}
                    </span>
                  </div>
                  <button
                    className={`btn ${isLinked('GOOGLE') ? 'btn-ghost' : 'btn-primary'}`}
                    style={{ minWidth: '120px' }}
                    onClick={() => isLinked('GOOGLE') ? handleUnlink('GOOGLE') : redirectToGoogleOAuth(true)}
                  >
                    {isLinked('GOOGLE') ? 'Disconnect' : 'Connect'}
                  </button>
                </div>

                {/* 42 Network Node Card */}
                <div style={{
                  display: 'flex',
                  justifyContent: 'space-between',
                  alignItems: 'center',
                  background: 'rgba(255,255,255,0.01)',
                  padding: '20px 24px',
                  borderRadius: '8px',
                  border: '1px solid var(--border-subtle)',
                  transition: 'border-color 0.2s'
                }}>
                  <div>
                    <h4 className="text-display" style={{ fontSize: '1.1rem', marginBottom: '4px' }}>42 Network Node</h4>
                    <span className="text-mono" style={{ fontSize: '0.78rem', color: isLinked('FORTYTWO') ? 'var(--teal-glow)' : 'var(--text-secondary)' }}>
                      {isLinked('FORTYTWO') ? `Connected (Provider ID: ${getProviderId('FORTYTWO')})` : 'Disconnected'}
                    </span>
                  </div>
                  <button
                    className={`btn ${isLinked('FORTYTWO') ? 'btn-ghost' : 'btn-primary'}`}
                    style={{ minWidth: '120px' }}
                    onClick={() => isLinked('FORTYTWO') ? handleUnlink('FORTYTWO') : redirectToFortyTwoOAuth(true)}
                  >
                    {isLinked('FORTYTWO') ? 'Disconnect' : 'Connect'}
                  </button>
                </div>
              </div>
            </div>
          )}
        </section>
      </main>
    </div>
  );
}

```

## 📄 File: ./frontend/vite.config.js
```javascript
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      // Proxies standard auth endpoints through local Node dev server
      '/api': {
        target: 'https://localhost:8080',
        changeOrigin: true,
        secure: false, // Ignore self-signed SSL errors during local development
      },
      // Proxies Google OAuth redirection initiation
      '/oauth2': {
        target: 'https://localhost:8080',
        changeOrigin: true,
        secure: false,
      },
      // Proxies Google OAuth redirect callbacks
      '/login/oauth2': {
        target: 'https://localhost:8080',
        changeOrigin: true,
        secure: false,
      }
    }
  }
})

```

## 📄 File: ./docker-compose.apps.yml
```yaml
# ==========================================
# TRANSCENDENCE MICROSERVICES APPLICATION STACK
# ==========================================
# Run after starting the infrastructure compose stack:
# docker compose -f docker-compose.yml up -d
# docker compose -f docker-compose.apps.yml up --build -d

services:
  # == 1. Spring Cloud Config Server (Internal Only) ==
  config-server:
    build:
      context: ./config-server
      dockerfile: Dockerfile
    image: transcendence-config-server:latest
    container_name: config-server
    environment:
      - SPRING_PROFILES_ACTIVE=native,docker
      - CONFIG_SERVER_PORT=${CONFIG_SERVER_PORT}
      - CERT_PASSWORD=${CERT_PASSWORD}
    volumes:
      - ./certs:/app/certs:ro             # Mounts secure mTLS keystores/truststores
      - ./config-repo:/app/config-repo:ro # Mounts local git configuration repository
    healthcheck:
      test: [ "CMD", "curl", "-k", "--cert", "/app/certs/services/config-server/config-server.p12:${CERT_PASSWORD}", "--cert-type", "P12", "-f", "https://localhost:${CONFIG_SERVER_PORT}/actuator/health" ]
      interval: 10s
      timeout: 5s
      retries: 5
      start_period: 15s
    networks:
      - transcendence-net

  # == 2. Eureka Service Discovery Registry (Internal Only) ==
  eureka-server:
    build:
      context: ./eureka-server
      dockerfile: Dockerfile
    image: transcendence-eureka-server:latest
    container_name: eureka-server
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - EUREKA_PORT=${EUREKA_PORT}
      - CONFIG_SERVER_PORT=${CONFIG_SERVER_PORT}
      - CERT_PASSWORD=${CERT_PASSWORD}
    volumes:
      - ./certs:/app/certs:ro
    healthcheck:
      test: [ "CMD", "curl", "-k", "--cert", "/app/certs/services/eureka-server/eureka-server.p12:${CERT_PASSWORD}", "--cert-type", "P12", "-f", "https://localhost:${EUREKA_PORT}/actuator/health" ]
      interval: 10s
      timeout: 5s
      retries: 5
      start_period: 15s
    depends_on:
      config-server:
        condition: service_healthy
    networks:
      - transcendence-net

  # == 3. Core Authentication Service (Internal Only) ==
  auth-service:
    build:
      context: ./auth-service
      dockerfile: Dockerfile
    image: transcendence-auth-service:latest
    container_name: auth-service
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - AUTH_SERVICE_PORT=${AUTH_SERVICE_PORT}
      - CONFIG_SERVER_PORT=${CONFIG_SERVER_PORT}
      - EUREKA_PORT=${EUREKA_PORT}
      - CERT_PASSWORD=${CERT_PASSWORD}
      - DB_NAME=${DB_NAME}
      - DB_USER=${DB_USER}
      - DB_PASSWORD=${DB_PASSWORD}
      - REDIS_PASSWORD=${REDIS_PASSWORD}
      - REDIS_PORT=${REDIS_PORT}
      - GATEWAY_PORT=${GATEWAY_PORT}
    volumes:
      - ./certs:/app/certs:ro
    healthcheck:
      test: [ "CMD", "curl", "-k", "--cert", "/app/certs/services/auth-service/auth-service.p12:${CERT_PASSWORD}", "--cert-type", "P12", "-f", "https://localhost:${AUTH_SERVICE_PORT}/actuator/health" ]
      interval: 10s
      timeout: 5s
      retries: 5
      start_period: 20s
    depends_on:
      config-server:
        condition: service_healthy
      eureka-server:
        condition: service_healthy
    networks:
      - transcendence-net

  # == 4. Backend-For-Frontend (BFF) Gateway (Public Entrypoint) ==
  gateway:
    build:
      context: ./gateway
      dockerfile: Dockerfile
    image: transcendence-gateway:latest
    container_name: gateway
    ports:
      - "${GATEWAY_PORT}:${GATEWAY_PORT}" # ONLY the Gateway is exposed to the host system!
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - GATEWAY_PORT=${GATEWAY_PORT}
      - CONFIG_SERVER_PORT=${CONFIG_SERVER_PORT}
      - EUREKA_PORT=${EUREKA_PORT}
      - CERT_PASSWORD=${CERT_PASSWORD}
      - REDIS_PASSWORD=${REDIS_PASSWORD}
      - REDIS_PORT=${REDIS_PORT}
      - GOOGLE_CLIENT_ID=${GOOGLE_CLIENT_ID}
      - GOOGLE_CLIENT_SECRET=${GOOGLE_CLIENT_SECRET}
      - CORS_ALLOWED_ORIGINS=${CORS_ALLOWED_ORIGINS:-http://localhost:5173}
      - FRONTEND_BASE_URL=${FRONTEND_BASE_URL:-http://localhost:5173}
    volumes:
      - ./certs:/app/certs:ro
    healthcheck:
      test: [ "CMD", "curl", "-k", "--cert", "/app/certs/services/gateway/gateway.p12:${CERT_PASSWORD}", "--cert-type", "P12", "-f", "https://localhost:${GATEWAY_PORT}/actuator/health" ]
      interval: 10s
      timeout: 5s
      retries: 5
      start_period: 20s
    depends_on:
      config-server:
        condition: service_healthy
      eureka-server:
        condition: service_healthy
      auth-service:
        condition: service_healthy
    networks:
      - transcendence-net

# == 5. Bridge Network Connectivity ==
networks:
  transcendence-net:
    external: true # Connects seamlessly to the pre-existing infrastructure network

```

## 📄 File: ./generate-ai-context.sh
```bash
#!/usr/bin/env bash

# ==============================================================================
# 🚀 AI CONTEXT GENERATOR - TOKEN OPTIMIZER
# ==============================================================================
# This script scans your microservices and frontend workspace, filters out
# generated code, external libraries, secrets, and binary assets, and combines
# your actual hand-written source code into a single, clean Markdown file.
#
# Usage:
#   chmod +x generate-ai-context.sh
#   ./generate-ai-context.sh
# ==============================================================================

OUTPUT_FILE="project_codebase_context.md"

# Clear previous output
echo "" > "$OUTPUT_FILE"

echo "===================================================="
echo "🔍 Scanning workspace for source files..."
echo "===================================================="

# Temporary file to store file list
TEMP_LIST=$(mktemp)

# Find relevant files, ignoring bulky dependencies, binary builds, and secrets
find . -type f \
  ! -path "*/node_modules/*" \
  ! -path "*/target/*" \
  ! -path "*/.git/*" \
  ! -path "*/.idea/*" \
  ! -path "*/.vscode/*" \
  ! -path "*/certs/*" \
  ! -path "*/dist/*" \
  ! -path "*/build/*" \
  ! -name "*.p12" \
  ! -name "*.pem" \
  ! -name "*.key" \
  ! -name "*.crt" \
  ! -name "*.jks" \
  ! -name "*.png" \
  ! -name "*.jpg" \
  ! -name "*.jpeg" \
  ! -name "*.gif" \
  ! -name "*.ico" \
  ! -name "*.woff*" \
  ! -name "*.ttf" \
  ! -name "package-lock.json" \
  ! -name "yarn.lock" \
  ! -name "pnpm-lock.yaml" \
  ! -name "$OUTPUT_FILE" \
  \( \
     -name "*.java" \
     -o -name "*.jsx" \
     -o -name "*.js" \
     -o -name "*.ts" \
     -o -name "*.tsx" \
     -o -name "*.css" \
     -o -name "*.html" \
     -o -name "*.xml" \
     -o -name "*.yaml" \
     -o -name "*.yml" \
     -o -name "*.properties" \
     -o -name "*.sh" \
     -o -name "*.env*" \
     -o -name "Dockerfile" \
     -o -name "docker-compose*" \
  \) > "$TEMP_LIST"

TOTAL_FILES=$(wc -l < "$TEMP_LIST" | xargs)
echo "Found $TOTAL_FILES relevant source code files!"
echo "Bundling files into $OUTPUT_FILE..."

# Write Markdown Header
cat << 'EOF' >> "$OUTPUT_FILE"
# 📦 TRANSCENDENCE MICROSERVICES CONTEXT

This single document contains the handwritten source code of the Transcendence Microservices Stack. It is optimized to be highly token-efficient for AI context ingestion.

## 🗂️ Project Structure Summary
EOF

# Append directory layout
echo "Generating directory summary..."
echo '```' >> "$OUTPUT_FILE"
find . -maxdepth 3 \
  ! -path "*/node_modules*" \
  ! -path "*/target*" \
  ! -path "*/.git*" \
  ! -path "*/.idea*" \
  ! -path "*/.vscode*" \
  ! -path "*/certs*" \
  ! -path "*/dist*" \
  ! -path "*/build*" \
  -not -name "." | sort | sed -e 's;[^/]*/;|____;g;s;____|; |;g' >> "$OUTPUT_FILE"
echo '```' >> "$OUTPUT_FILE"
echo "" >> "$OUTPUT_FILE"

# Process each file
CURRENT_COUNT=0
while IFS= read -r file; do
  ((CURRENT_COUNT++))
  
  # Determine markdown syntax highlighting language
  ext="${file##*.}"
  lang="text"
  case "$ext" in
    java) lang="java" ;;
    jsx|js) lang="javascript" ;;
    tsx|ts) lang="typescript" ;;
    xml) lang="xml" ;;
    yaml|yml) lang="yaml" ;;
    css) lang="css" ;;
    html) lang="html" ;;
    sh) lang="bash" ;;
    properties) lang="properties" ;;
  esac
  
  if [[ "$file" == *"Dockerfile"* ]]; then
    lang="dockerfile"
  elif [[ "$file" == *".env"* ]]; then
    lang="properties"
  fi

  # Append file context
  echo "📄 Adding [$CURRENT_COUNT/$TOTAL_FILES]: $file"
  
  echo "## 📄 File: $file" >> "$OUTPUT_FILE"
  echo '```'"$lang" >> "$OUTPUT_FILE"
  cat "$file" >> "$OUTPUT_FILE"
  echo "" >> "$OUTPUT_FILE"
  echo '```' >> "$OUTPUT_FILE"
  echo "" >> "$OUTPUT_FILE"

done < "$TEMP_LIST"

rm "$TEMP_LIST"

echo "===================================================="
echo "🎉 SUCCESS! Single context file generated:"
echo "📂 $OUTPUT_FILE"
echo "===================================================="

```

