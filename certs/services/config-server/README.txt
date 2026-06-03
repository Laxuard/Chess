Certificate Package: config-server
Generated: 2026-06-03 11:01 UTC
Valid for: 365 days
CA: TranscendenceCA

FILES
─────
config-server.key          RSA private key      — DO NOT DISTRIBUTE (chmod 600)
config-server.crt          Signed certificate   — safe to distribute
config-server.pem          key + cert bundle    — nginx, HAProxy, raw TLS (chmod 600)
config-server-chain.pem    cert + CA bundle     — give to clients for mutual TLS trust
config-server.p12          PKCS12 keystore      — Spring Boot server.ssl.*

SPRING BOOT APPLICATION.YML (server-side TLS)
──────────────────────────────────────────────
server:
  ssl:
    enabled: true
    key-store:             classpath:config-server.p12
    key-store-password:    ${CERT_PASSWORD:password}
    key-store-type:        PKCS12
    key-alias:             config-server
    trust-store:           classpath:truststore.p12
    trust-store-password:  ${CERT_PASSWORD:password}
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
