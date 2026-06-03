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

step "Starting Project-Wide Compilation & Build"

# 1. Build all backend services
for svc_dir in services/*/; do
    [[ -d "$svc_dir" ]] || continue
    name=$(basename "$svc_dir")
    
    if [[ -f "${svc_dir}pom.xml" ]]; then
        step "Building Backend: $name"
        if [[ -x "${svc_dir}mvnw" ]]; then
            info "[$name] Running './mvnw clean package -DskipTests'..."
            "${svc_dir}mvnw" -f "${svc_dir}pom.xml" clean package -DskipTests --no-transfer-progress
        elif command -v mvn >/dev/null 2>&1; then
            info "[$name] Running 'mvn clean package -DskipTests'..."
            mvn -f "${svc_dir}pom.xml" clean package -DskipTests --no-transfer-progress
        else
            error "[$name] Neither ./mvnw nor global mvn command was found. Cannot build!"
            exit 1
        fi
        ok "[$name] Build compiled successfully!"
    fi
done

# 2. Build frontend
if [[ -d "services/frontend" ]]; then
    step "Building Frontend: React App"
    cd services/frontend
    
    if command -v npm >/dev/null 2>&1; then
        if [[ ! -d "node_modules" ]]; then
            info "[frontend] Installing node dependencies (npm install)..."
            npm install --no-audit --no-fund
        fi
        info "[frontend] Compiling production build (npm run build)..."
        npm run build
        ok "[frontend] Production build compiled successfully!"
    else
        warn "[frontend] Node/NPM not found — skipping frontend build compilation."
    fi
    cd ../..
fi

step "All Services Built Successfully"
echo -e "${GRN}Build validated. All microservices and frontend code compiled successfully.${RST}"
