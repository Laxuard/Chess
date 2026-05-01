#!/bin/bash

# Configuration
mkdir -p certs
CERT_PASS=${CERT_PASSWORD:-password}
DAYS_VALID=365

echo "-------------------------------------------------------"
echo "Starting Transcendence Security Infrastructure Setup"
echo "-------------------------------------------------------"

# --- PART 1: SSL/TLS INFRASTRUCTURE (The 'Pipe') ---

# 1. Create the Root CA (The source of trust for your internal network)
if [ ! -f certs/rootCA.key ]; then
    echo "[1/4] Creating new Root CA..."
    openssl req -x509 -nodes -newkey rsa:4096 -keyout certs/rootCA.key -out certs/rootCA.crt -days 3650 -subj "/CN=TranscendenceCA"
    chmod 644 certs/rootCA.crt
fi

# 2. Build Java Truststore (So services trust the Root CA)
rm -f certs/truststore.p12
echo "[2/4] Building Java Truststore..."
keytool -import -trustcacerts -alias rootca -file certs/rootCA.crt -keystore certs/truststore.p12 -storetype PKCS12 -storepass $CERT_PASS -noprompt
chmod 644 certs/truststore.p12

# 3. Function to generate service-specific SSL certificates (.p12)
generate_service_cert() {
    local SERVICE_NAME=$1
    echo " -> Generating SSL certificate for: $SERVICE_NAME"

    # Generate Private Key and CSR
    openssl req -newkey rsa:2048 -nodes -keyout certs/$SERVICE_NAME.key -out certs/$SERVICE_NAME.csr -subj "/CN=$SERVICE_NAME" -addext "subjectAltName = DNS:localhost, DNS:127.0.0.1, DNS:$SERVICE_NAME"

    # Sign with Root CA
    openssl x509 -req -in certs/$SERVICE_NAME.csr -CA certs/rootCA.crt -CAkey certs/rootCA.key -CAcreateserial -out certs/$SERVICE_NAME.crt -days $DAYS_VALID -copy_extensions copy

    # Bundle into PKCS12 briefcase (Used by Spring Boot 'server.ssl')
    openssl pkcs12 -export -in certs/$SERVICE_NAME.crt -inkey certs/$SERVICE_NAME.key -out certs/$SERVICE_NAME.p12 -name $SERVICE_NAME -password pass:$CERT_PASS

    # Cleanup and permissions
    chmod 644 certs/$SERVICE_NAME.p12
    chmod 644 certs/$SERVICE_NAME.crt
    rm certs/$SERVICE_NAME.csr certs/$SERVICE_NAME.key
}

echo "[3/4] Generating Service SSL Briefcases..."
generate_service_cert "gateway"
generate_service_cert "auth-service"
generate_service_cert "eureka-server"
# Add Librarian or other services here if they need their own .p12 for mTLS
# generate_service_cert "librarian"


# --- PART 2: JWT ASYMMETRIC IDENTITY (The 'Signature') ---

echo "[4/4] Generating RSA Key Pair for JWT Signing & JWKS..."

# 1. Generate the raw private key
openssl genrsa -out certs/jwt_tmp.pem 2048

# 2. Convert Private Key to PKCS#8 (Java/Spring KeyFactory standard)
# This stays ONLY on the Gateway
openssl pkcs8 -topk8 -inform PEM -outform PEM -nocrypt -in certs/jwt_tmp.pem -out certs/jwt_private_pkcs8.pem

# 3. Extract the Public Key in X.509 format
# This is used by the Gateway to host the JWKS endpoint
openssl rsa -in certs/jwt_tmp.pem -pubout -out certs/jwt_public.pem

# Cleanup temporary files
rm certs/jwt_tmp.pem

# Secure the files
chmod 600 certs/jwt_private_pkcs8.pem
chmod 644 certs/jwt_public.pem

echo "-------------------------------------------------------"
echo "SUCCESS: All certificates and keys generated in /certs"
echo "-------------------------------------------------------"
ls -l certs