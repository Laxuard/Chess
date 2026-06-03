#!/usr/bin/env bash
# ==============================================================================
# TRANSCENDENCE SERVICE REGISTRATION UTILITY
# ==============================================================================
# Automates new microservice instantiation, mTLS cert generation,
# routing mapping injection, docker-compose inclusion, and dynamic Gateway refresh.
# ==============================================================================

set -euo pipefail

# Directories
WORKSPACE_DIR="/home/laxuard/1337/Microservices"
SERVICES_DIR="$WORKSPACE_DIR/services"
TEMPLATES_DIR="$WORKSPACE_DIR/templates"
SCRIPTS_DIR="$WORKSPACE_DIR/scripts"
GATEWAY_CONFIG="$WORKSPACE_DIR/config/config-repo/gateway/gateway.yaml"
APPS_COMPOSE="$WORKSPACE_DIR/docker/docker-compose.apps.yml"

# Colors & Styles
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
BOLD='\033[1m'
NC='\033[0;37m'

info() { echo -e "${BLUE}${BOLD}[INFO]${NC}  $1"; }
ok() { echo -e "${GREEN}${BOLD}[OK]${NC}    $1"; }
warn() { echo -e "${YELLOW}${BOLD}[WARN]${NC}  $1"; }
error() { echo -e "${RED}${BOLD}[ERROR]${NC}  $1"; exit 1; }

usage() {
    echo -e "${BOLD}Usage:${NC} $0 <service-name> <template-type>"
    echo -e "  ${BOLD}service-name:${NC}   Name of the service (e.g., billing-service, chat-service)"
    echo -e "  ${BOLD}template-type:${NC}  Template type, either 'nodejs' or 'python'"
    echo -e "\n${BOLD}Example:${NC}"
    echo -e "  $0 billing-service python"
    exit 1;
}

# 1. Parse Arguments
if [ "$#" -ne 2 ]; then
    usage
fi

SVC_NAME=$(echo "$1" | tr '[:upper:]' '[:lower:]')
TEMPLATE_TYPE=$(echo "$2" | tr '[:upper:]' '[:lower:]')

# Validate Template Type
if [ "$TEMPLATE_TYPE" != "nodejs" ] && [ "$TEMPLATE_TYPE" != "python" ]; then
    error "Invalid template type: '$TEMPLATE_TYPE'. Must be 'nodejs' or 'python'."
fi

# Validate Service Name Format (alphanumeric and hyphens only)
if [[ ! "$SVC_NAME" =~ ^[a-z0-9-]+$ ]]; then
    error "Invalid service name: '$SVC_NAME'. Only lowercase letters, numbers, and hyphens are allowed."
fi

echo -e "${PURPLE}${BOLD}🚀 Initiating Transcendence Service Provisioning: $SVC_NAME ($TEMPLATE_TYPE)${NC}"
echo -e "----------------------------------------------------------------------"

# 2. Check Directories & Overwrites
TARGET_DIR="$SERVICES_DIR/$SVC_NAME"
if [ -d "$TARGET_DIR" ]; then
    error "Target directory '$TARGET_DIR' already exists. Aborting to prevent overwrite."
fi

# Define source template folder
if [ "$TEMPLATE_TYPE" == "nodejs" ]; then
    SRC_TEMPLATE="$TEMPLATES_DIR/nodejs-express"
    INTERNAL_PORT=8083
    HEALTHCHECK_CMD="[ \"CMD\", \"curl\", \"-k\", \"--cert\", \"/app/certs/services/$SVC_NAME/$SVC_NAME.p12:\${CERT_PASSWORD}\", \"--cert-type\", \"P12\", \"-f\", \"https://localhost:$INTERNAL_PORT/health\" ]"
else
    SRC_TEMPLATE="$TEMPLATES_DIR/python-fastapi"
    INTERNAL_PORT=8082
    HEALTHCHECK_CMD="[ \"CMD\", \"curl\", \"-k\", \"--cert\", \"/app/certs/services/$SVC_NAME/$SVC_NAME.crt\", \"--key\", \"/app/certs/services/$SVC_NAME/$SVC_NAME.key\", \"-f\", \"https://localhost:$INTERNAL_PORT/health\" ]"
fi

if [ ! -d "$SRC_TEMPLATE" ]; then
    error "Source template '$SRC_TEMPLATE' does not exist."
fi

# 3. Copy Template Folder
info "Copying '$TEMPLATE_TYPE' template to services/$SVC_NAME..."
cp -r "$SRC_TEMPLATE" "$TARGET_DIR"
ok "Service boilerplate provisioned at services/$SVC_NAME"

# 4. Generate mTLS Certificates
info "Generating mTLS credentials for $SVC_NAME..."
if [ -f "$SCRIPTS_DIR/mtls-setup.sh" ]; then
    "$SCRIPTS_DIR/mtls-setup.sh" add "$SVC_NAME"
    ok "mTLS credentials generated successfully"
else
    warn "mtls-setup.sh script not found. You will need to generate credentials manually."
fi

# 5. Inject Gateway Route
ROUTE_PREFIX=$(echo "$SVC_NAME" | sed 's/-service$//')
info "Injecting API Gateway route for /api/$ROUTE_PREFIX/**..."

# Verify if route already exists in gateway.yaml
if grep -q "id: ${SVC_NAME}-route" "$GATEWAY_CONFIG"; then
    warn "Route '${SVC_NAME}-route' already exists in gateway.yaml. Skipping route injection."
else
    # Create temp route definition block
    ROUTE_BLOCK=$(cat <<EOF

            # Auto-Generated Route for $SVC_NAME
            - id: ${SVC_NAME}-route
              uri: lb://${SVC_NAME}
              predicates:
                - Path=/api/${ROUTE_PREFIX}/**
              filters:
                - StripPrefix=2
                - SessionToJwt
EOF
)
    # Inject before the OPENAPI / SWAGGER comment line
    # Match "# ==========================================" followed by "# OPENAPI / SWAGGER AGGREGATION CONFIGURATION"
    awk -v route="$ROUTE_BLOCK" '
    BEGIN { injected = 0 }
    /# ==========================================/ {
        line = $0
        getline next_line
        if (next_line ~ /OPENAPI \/ SWAGGER AGGREGATION/) {
            if (!injected) {
                print route
                injected = 1
            }
        }
        print line
        print next_line
        next
    }
    { print }
    ' "$GATEWAY_CONFIG" > "$GATEWAY_CONFIG.tmp"
    mv "$GATEWAY_CONFIG.tmp" "$GATEWAY_CONFIG"
    ok "Gateway routing rule appended under prefix: /api/$ROUTE_PREFIX"
fi

# 6. Inject Docker Compose Service Block
info "Injecting docker-compose container configuration..."

# Verify if service already exists in compose configuration
if grep -q "^  ${SVC_NAME}:" "$APPS_COMPOSE"; then
    warn "Docker service '${SVC_NAME}' already exists in docker-compose.apps.yml. Skipping compose injection."
else
    # Create compose definition block
    COMPOSE_BLOCK=$(cat <<EOF
  # == Auto-Generated Service: $SVC_NAME ==
  $SVC_NAME:
    build:
      context: ../services/$SVC_NAME
      dockerfile: Dockerfile
    image: transcendence-$SVC_NAME:latest
    container_name: $SVC_NAME
    environment:
      - PORT=$INTERNAL_PORT
      - SPRING_APPLICATION_NAME=$SVC_NAME
      - EUREKA_URL=https://eureka-server:\${EUREKA_PORT:-8761}/eureka/
      - CERT_DIR_PATH=/app/certs
      - JWKS_URI=https://gateway:\${GATEWAY_PORT:-8080}/.well-known/jwks.json
      - JWT_PUBLIC_KEY_PATH=/app/certs/jwt/jwt_public.pem
      - CERT_PASSWORD=\${CERT_PASSWORD}
    volumes:
      - ../certs:/app/certs:ro
    healthcheck:
      test: $HEALTHCHECK_CMD
      interval: 10s
      timeout: 5s
      retries: 5
      start_period: 10s
    depends_on:
      eureka-server:
        condition: service_healthy
    networks:
      - transcendence-net

EOF
)
    # Inject before the Bridge Network comment line
    awk -v compose="$COMPOSE_BLOCK" '
    BEGIN { injected = 0 }
    /# == .* Bridge Network Connectivity ==/ {
        if (!injected) {
            print compose
            injected = 1
        }
    }
    { print }
    ' "$APPS_COMPOSE" > "$APPS_COMPOSE.tmp"
    mv "$APPS_COMPOSE.tmp" "$APPS_COMPOSE"
    ok "Container service block added to docker-compose.apps.yml"
fi

# 7. Hot Reload Gateway Routes
info "Refreshing Gateway routes dynamically..."
# 7a. Refresh Config properties from Config Server
info "Triggering configuration context refresh..."
CONFIG_REFRESH_STATUS=$(docker exec gateway curl -k -s -w "%{http_code}" -X POST https://localhost:8080/actuator/refresh -o /dev/null || echo "failed")

# 7b. Refresh Gateway routes cache
info "Triggering Gateway routes cache refresh..."
ROUTE_REFRESH_STATUS=$(docker exec gateway curl -k -s -w "%{http_code}" -X POST https://localhost:8080/actuator/gateway/refresh -o /dev/null || echo "failed")

if [ "$CONFIG_REFRESH_STATUS" == "200" ] && [ "$ROUTE_REFRESH_STATUS" == "200" ]; then
    ok "Gateway routes dynamically reloaded (zero-downtime hot refresh)!"
else
    warn "Dynamic Gateway refresh status - Config Refresh: $CONFIG_REFRESH_STATUS, Route Refresh: $ROUTE_REFRESH_STATUS."
fi

echo -e "----------------------------------------------------------------------"
echo -e "${GREEN}${BOLD}✔ Microservice '$SVC_NAME' is fully provisioned and integrated!${NC}"
echo -e "To compile and launch the new container run:"
echo -e "  ${BOLD}docker compose -f docker/docker-compose.apps.yml up -d --build $SVC_NAME${NC}"
echo -e "----------------------------------------------------------------------"
