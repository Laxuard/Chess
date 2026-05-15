Certificate Package: gateway
Generated: 2026-05-11 13:53 UTC
Valid for: 365 days
CA: TranscendenceCA

FILES
─────
gateway.key          RSA private key      — DO NOT DISTRIBUTE (chmod 600)
gateway.crt          Signed certificate   — safe to distribute
gateway.pem          key + cert bundle    — nginx, HAProxy, raw TLS (chmod 600)
gateway-chain.pem    cert + CA bundle     — give to clients for mutual TLS trust
gateway.p12          PKCS12 keystore      — Spring Boot server.ssl.*

SPRING BOOT APPLICATION.YML (server-side TLS)
──────────────────────────────────────────────
server:
  ssl:
    enabled: true
    key-store:             classpath:gateway.p12
    key-store-password:    ${CERT_PASSWORD:changeit}
    key-store-type:        PKCS12
    key-alias:             gateway
    trust-store:           classpath:truststore.p12
    trust-store-password:  ${CERT_PASSWORD:changeit}
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
