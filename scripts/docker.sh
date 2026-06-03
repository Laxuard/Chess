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

# Compose Files configurations
INFRA_COMPOSE="docker/docker-compose.yml"
APPS_COMPOSE="docker/docker-compose.apps.yml"

usage() {
    echo "Usage: $0 [command] [options]"
    echo ""
    echo "Commands:"
    echo "  up             Start the entire cluster (infra first, then apps)"
    echo "  down           Stop the entire cluster and remove volumes"
    echo "  infra          Start only the infrastructure stack (db, redis, rabbitmq)"
    echo "  apps           Start only the application microservices"
    echo "  restart <svc>  Restart and rebuild a specific microservice container"
    echo "  logs [svc]     Tail logs (for all containers or a specific one)"
    echo "  ps             Show the running status and health of all containers"
    echo ""
    echo "Examples:"
    echo "  $0 up"
    echo "  $0 restart auth-service"
    echo "  $0 logs gateway"
}

# Ensure .env symlink exists
check_env_symlink() {
    if [[ ! -f ".env" ]]; then
        error "Root .env file is missing. Please create it first."
        exit 1
    fi
    if [[ ! -f "docker/.env" ]]; then
        info "Creating missing symbolic link docker/.env -> ../.env"
        ln -sf ../.env docker/.env
    fi
}

remove_conflicts() {
    info "Purging potential container name conflicts..."
    docker rm -f postgres-db redis-container redis-commander rabbitmq config-server eureka-server auth-service gateway >/dev/null 2>&1 || true
}

wait_for_infra() {
    step "Waiting for Infrastructure Readiness"
    local required=("postgres-db" "redis-container")
    
    for container in "${required[@]}"; do
        local elapsed=0
        local timeout=45
        info "Checking health of $container..."
        while true; do
            local status
            status=$(docker inspect -f '{{.State.Health.Status}}' "$container" 2>/dev/null || echo "missing")
            if [[ "$status" == "healthy" ]]; then
                ok "$container is healthy."
                break
            fi
            sleep 2
            elapsed=$(( elapsed + 2 ))
            if (( elapsed >= timeout )); then
                warn "$container health check timed out. Continuing anyway..."
                break
            fi
        done
    done
}

case "${1:-}" in
    up)
        check_env_symlink
        remove_conflicts
        step "Starting Infrastructure Stack"
        docker compose -f "$INFRA_COMPOSE" up -d
        
        wait_for_infra
        
        step "Starting Applications Stack"
        docker compose -f "$APPS_COMPOSE" up -d --build
        ok "Entire cluster started."
        ;;
        
    down)
        check_env_symlink
        step "Tearing Down Cluster"
        docker compose -f "$APPS_COMPOSE" down -v --remove-orphans
        docker compose -f "$INFRA_COMPOSE" down -v --remove-orphans
        remove_conflicts
        ok "Cluster stopped."
        ;;
        
    infra)
        check_env_symlink
        remove_conflicts
        step "Starting Infrastructure Stack Only"
        docker compose -f "$INFRA_COMPOSE" up -d
        ;;
        
    apps)
        check_env_symlink
        docker rm -f config-server eureka-server auth-service gateway >/dev/null 2>&1 || true
        step "Starting Applications Stack Only"
        docker compose -f "$APPS_COMPOSE" up -d --build
        ;;
        
    restart)
        check_env_symlink
        shift
        if [[ $# -eq 0 ]]; then
            error "Please specify a service to restart (e.g. gateway, auth-service)"
            exit 1
        fi
        svc="$1"
        step "Restarting & Rebuilding Service: $svc"
        
        # Check if service belongs to infra or apps
        if docker compose -f "$APPS_COMPOSE" ps --format json | grep -q "\"Service\":\"$svc\""; then
            docker compose -f "$APPS_COMPOSE" up -d --build "$svc"
        elif docker compose -f "$INFRA_COMPOSE" ps --format json | grep -q "\"Service\":\"$svc\""; then
            docker compose -f "$INFRA_COMPOSE" restart "$svc"
        else
            error "Service '$svc' not found in compose configurations."
            exit 1
        fi
        ok "$svc restarted successfully."
        ;;
        
    logs)
        check_env_symlink
        shift
        if [[ $# -eq 0 ]]; then
            # Tail everything
            docker compose -f "$INFRA_COMPOSE" -f "$APPS_COMPOSE" logs -f --tail=100
        else
            svc="$1"
            if docker compose -f "$APPS_COMPOSE" ps --format json | grep -q "\"Service\":\"$svc\""; then
                docker compose -f "$APPS_COMPOSE" logs -f --tail=100 "$svc"
            elif docker compose -f "$INFRA_COMPOSE" ps --format json | grep -q "\"Service\":\"$svc\""; then
                docker compose -f "$INFRA_COMPOSE" logs -f --tail=100 "$svc"
            else
                error "Service '$svc' not found."
                exit 1
            fi
        fi
        ;;
        
    ps|status)
        check_env_symlink
        step "Current Cluster Status"
        docker compose -f "$INFRA_COMPOSE" -f "$APPS_COMPOSE" ps
        ;;
        
    *)
        usage
        exit 1
        ;;
esac
