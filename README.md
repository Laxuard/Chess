# 🌐 Transcendence Microservices

Welcome to the **Transcendence Microservices** repository. This project is a modular, zero-trust microservice ecosystem built using Spring Boot, Spring Cloud, React (Vite), and Docker.

All service-to-service communications are discovery-driven via Netflix Eureka, configured dynamically via Spring Cloud Config Server, and secured end-to-end using **Mutual TLS (mTLS)** and a Backend-For-Frontend (BFF) Gateway session translation pattern.

---

## 🚀 Quick-Start (Local Development)

Get the entire cluster and Vite development server running in three simple steps:

1. **Initialize Certificates & Trust Chain**:
   ```bash
   ./scripts/mtls-setup.sh
   ```

2. **Boot the Docker Container Cluster**:
   ```bash
   ./scripts/docker.sh up
   ```

3. **Start the Frontend Dev Server**:
   ```bash
   ./scripts/frontend.sh dev
   ```

Open [http://localhost:5173](http://localhost:5173) in your browser.

---

## 📂 Project Directory Structure

```
.
├── certs/                      # Secure keystores and truststores (gitignored)
├── config/                     # Shared environment configuration resources
│   └── config-repo/            # Local Git-managed properties repository
├── docker/                     # Compose files (infrastructure and microservices)
├── docs/                       # Architectural manuals and Mermaid diagrams
│   └── README.md               # Main Developer & Architectural Guide (Read this!)
├── scripts/                    # Automation and management shell utilities
└── services/                   # Source codes (gateway, auth, config, eureka, frontend)
```

---

## 🏛️ Comprehensive Documentation

For complete architectural details, sequence diagrams, and troubleshooting guidelines, please refer to our main documentation:

👉 **[Transcendence Developer & Architecture Guide (docs/README.md)](file:///home/yjazouli/1337/Microservices/docs/README.md)**

Inside the guide you will find:
- **[System Architecture Diagram](file:///home/yjazouli/1337/Microservices/docs/README.md#🏛️-system-architecture)**: Full network mapping of client, gateway, internal services, and databases.
- **[Port Allocation Map](file:///home/yjazouli/1337/Microservices/docs/README.md#🔌-port-allocation-map)**: Host port mappings and internal container routing maps.
- **[Authentication Translation Details](file:///home/yjazouli/1337/Microservices/docs/README.md#🔒-request-authentication--gateway-to-backend-translation)**: Sequences of how cookies translate into short-lived transit JWTs.
- **[OAuth2 Reconcile Synchronization Flow](file:///home/yjazouli/1337/Microservices/docs/README.md#🔑-oauth2-authentication--account-synchronization-flow)**: Direct Google/42 social login integration sequence diagram.
- **[Mutual TLS (mTLS) Trust Scheme](file:///home/yjazouli/1337/Microservices/docs/README.md#🔑-mutual-tls-mtls-security-scheme)**: How the custom PKCS12 keystore trust chains are structured.
- **[Database Entity Relationships (ERD)](file:///home/yjazouli/1337/Microservices/docs/README.md#🗄️-database-entity-relationship-diagram-erd)**: Visual entity map of authorization and identity tables.
- **[Detailed Script Command Catalog](file:///home/yjazouli/1337/Microservices/docs/README.md#📂-detailed-script-reference)**: Reference index for all commands in `clean.sh`, `build-all.sh`, `docker.sh`, and `frontend.sh`.
- **[Troubleshooting Guide](file:///home/yjazouli/1337/Microservices/docs/README.md#🔍-troubleshooting--diagnostic-guide)**: Common errors, SSL handshake failures, container naming conflicts, and how to fix them.
