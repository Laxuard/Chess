#!/usr/bin/env bash
set -euo pipefail

# Establish path to services/frontend relative to this script's directory
FRONTEND_DIR="$(dirname "$0")/../services/frontend"

# ── Colors & Logging ──────────────────────────────────────────────────────────
RED='\033[0;31m'; GRN='\033[0;32m'; YLW='\033[0;33m'
BLU='\033[0;34m'; CYN='\033[0;36m'; BLD='\033[1m'; RST='\033[0m'

info()  { echo -e "$(date '+%H:%M:%S') ${BLU}[INFO]${RST}  $*"; }
ok()    { echo -e "$(date '+%H:%M:%S') ${GRN}[OK]${RST}    $*"; }
warn()  { echo -e "$(date '+%H:%M:%S') ${YLW}[WARN]${RST}  $*"; }
error() { echo -e "$(date '+%H:%M:%S') ${RED}[ERROR]${RST} $*" >&2; }
step()  { echo -e "\n${BLD}${CYN}──── $* ────${RST}"; }

usage() {
    echo "Usage: $0 [command]"
    echo ""
    echo "Commands:"
    echo "  (none) / dev   Start Vite development server"
    echo "  build          Build production assets"
    echo "  install        Install dependencies (npm install)"
    echo "  clean          Force-reinstall dependencies (wipes node_modules & lockfile)"
}

cmd="${1:-dev}"

# Verify services/frontend folder exists
if [[ ! -d "$FRONTEND_DIR" ]]; then
    error "Frontend directory not found at: $FRONTEND_DIR"
    exit 1
fi

case "$cmd" in
    dev|start)
        step "Starting Frontend Dev Server (Vite)"
        cd "$FRONTEND_DIR"
        if [[ ! -d "node_modules" ]]; then
            info "node_modules not found, installing dependencies first..."
            npm install
        fi
        exec npm run dev
        ;;
        
    build)
        step "Compiling Frontend Production Assets"
        cd "$FRONTEND_DIR"
        exec npm run build
        ;;
        
    install)
        step "Installing Frontend Dependencies"
        cd "$FRONTEND_DIR"
        exec npm install
        ;;
        
    clean)
        step "Resetting & Reinstalling Frontend Dependencies"
        cd "$FRONTEND_DIR"
        info "Wiping node_modules, package-lock.json and dist..."
        rm -rf node_modules package-lock.json dist .vite
        ok "Cleaned existing assets."
        info "Running npm install..."
        exec npm install
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
