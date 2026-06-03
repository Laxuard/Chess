#!/usr/bin/env bash
set -euo pipefail

# Establish root directory execution path (run from scripts/ folder or root)
cd "$(dirname "$0")/.."

# ── Colors & Logging ──────────────────────────────────────────────────────────
RED='\033[0;31m'; GRN='\033[0;32m'; YLW='\033[0;33m'
BLU='\033[0;34m'; CYN='\033[0;36m'; BLD='\033[1m'; RST='\033[0m'

info()  { echo -e "$(date '+%H:%M:%S') ${BLU}[INFO]${RST}  $*"; }
ok()    { echo -e "$(date '+%H:%M:%S') ${GRN}[OK]${RST}    $*"; }
warn()  { echo -e "$(date '+%H:%M:%S') ${YLW}[WARN]${RST}  $*"; }
error() { echo -e "$(date '+%H:%M:%S') ${RED}[ERROR]${RST} $*" >&2; }
step()  { echo -e "\n${BLD}${CYN}──── $* ────${RST}"; }

step "Starting Project Cleanup & Reset"

# 1. Stop and tear down docker containers and volumes
if command -v docker >/dev/null 2>&1; then
    if [[ -f "docker/docker-compose.yml" ]]; then
        info "Shutting down infrastructure containers and deleting volumes..."
        docker compose -f docker/docker-compose.yml --env-file .env down -v --remove-orphans || warn "Failed to completely teardown infra containers."
    fi
    if [[ -f "docker/docker-compose.apps.yml" ]]; then
        info "Shutting down application containers..."
        docker compose -f docker/docker-compose.apps.yml --env-file .env down -v --remove-orphans || warn "Failed to completely teardown app containers."
    fi
    # Force remove any residual/conflicting containers by name to prevent project-rename conflicts
    info "Force removing conflicting containers by name..."
    docker rm -f postgres-db redis-container redis-commander rabbitmq config-server eureka-server auth-service gateway >/dev/null 2>&1 || true
else
    warn "Docker command not found — skipping container teardown."
fi

# 2. Clear certificates
if [[ -d "certs" ]]; then
    info "Removing generated SSL certificates and Root CA..."
    rm -rf certs
    ok "Deleted certs/ directory."
fi

# 3. Clear logs
if [[ -d "logs" ]]; then
    info "Clearing logs directory..."
    rm -rf logs/*
    ok "Logs directory cleared."
fi

# 4. Clean Maven build folders in services
for svc_dir in services/*/; do
    [[ -d "$svc_dir" ]] || continue
    name=$(basename "$svc_dir")
    
    if [[ -f "${svc_dir}pom.xml" ]]; then
        info "[$name] Running maven clean..."
        if [[ -x "${svc_dir}mvnw" ]]; then
            "${svc_dir}mvnw" -f "${svc_dir}pom.xml" clean --no-transfer-progress >/dev/null 2>&1 || warn "[$name] Maven clean failed."
        elif command -v mvn >/dev/null 2>&1; then
            mvn -f "${svc_dir}pom.xml" clean --no-transfer-progress >/dev/null 2>&1 || warn "[$name] Maven clean failed."
        else
            warn "[$name] Maven not found, removing target/ directory directly..."
            rm -rf "${svc_dir}target"
        fi
        ok "[$name] Cleaned build targets."
    fi
done

# 5. Clean Frontend build assets
if [[ -d "services/frontend" ]]; then
    info "[frontend] Removing build assets..."
    rm -rf services/frontend/dist
    rm -rf services/frontend/.vite
    ok "[frontend] Cleaned build assets."
fi

step "Teardown & Cleanup Successful"
echo -e "${GRN}Environment is fully reset. Run './scripts/mtls-setup.sh' to regenerate certificates.${RST}"
