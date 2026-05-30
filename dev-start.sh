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