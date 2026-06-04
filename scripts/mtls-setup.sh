#!/usr/bin/env bash
set -euo pipefail

# Establish root directory execution path (run from scripts/ folder or root)
cd "$(dirname "$0")/.."

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

# Discover services dynamically by scanning the services/ folder (excluding frontend and common-core)
DEFAULT_SERVICES=()
for d in services/*/; do
    if [[ -d "$d" ]]; then
        name=$(basename "$d")
        if [[ "$name" != "frontend" && "$name" != "common-core" ]]; then
            DEFAULT_SERVICES+=("$name")
        fi
    fi
done

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