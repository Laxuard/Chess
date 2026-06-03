import logging
import os
import ssl
import sys
from fastapi import FastAPI, Request
from eureka import EurekaClient
from jwt_validator import JwtValidator, TransitJwtMiddleware

# Configure logging to match our standard trace pattern
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(name)s - %(message)s",
    stream=sys.stdout
)
logger = logging.getLogger("main")

# Load configuration from environment variables (fallback to dev settings)
APP_NAME = os.getenv("SPRING_APPLICATION_NAME", "python-service")
PORT = int(os.getenv("PORT", "8082"))
EUREKA_URL = os.getenv("EUREKA_URL", "https://localhost:8761/eureka/")
CERT_DIR = os.getenv("CERT_DIR_PATH", "./certs")
JWKS_URI = os.getenv("JWKS_URI", "https://localhost:8080/.well-known/jwks.json")
JWT_PUBLIC_KEY_PATH = os.getenv("JWT_PUBLIC_KEY_PATH", os.path.join(CERT_DIR, "jwt", "jwt_public.pem"))

# Create FastAPI app
app = FastAPI(
    title="Python Microservice Template",
    description="FastAPI service integrated with mTLS, Eureka, and Transit JWT translation.",
    version="1.0.0",
    docs_url="/docs",
    openapi_url="/openapi.json"
)

# Initialize JWT Validator
# Using local public key by default if available; otherwise falls back to JWKS over mTLS
jwt_validator = JwtValidator(
    app_name=APP_NAME,
    cert_dir=CERT_DIR,
    jwks_uri=JWKS_URI,
    public_key_path=JWT_PUBLIC_KEY_PATH
)

# Attach JWT Validation Middleware
app.add_middleware(TransitJwtMiddleware, validator=jwt_validator)

# Initialize Eureka Client
eureka_client = EurekaClient(
    app_name=APP_NAME,
    host_name=APP_NAME, # Hostname matches the service name in docker network
    port=PORT,
    eureka_url=EUREKA_URL,
    cert_dir=CERT_DIR
)

@app.on_event("startup")
async def startup_event():
    logger.info("Starting up microservice...")
    # Start the Eureka background registration and heartbeat daemon
    eureka_client.start()

@app.on_event("shutdown")
async def shutdown_event():
    logger.info("Shutting down microservice...")
    # Clean up and deregister from Eureka
    eureka_client.stop()

# ── Health Endpoints ──────────────────────────────────────────────────────────
@app.get("/health", tags=["Monitoring"])
@app.get("/actuator/health", tags=["Monitoring"])
async def health_check():
    """
    Standard Actuator-like health endpoint used by Docker healthchecks and gateway.
    Exempt from JWT authorization in middleware.
    """
    return {"status": "UP", "details": {"database": "UP", "cache": "UP"}}

# ── Secure Business Endpoints ──────────────────────────────────────────────────
@app.get("/hello", tags=["Business Logic"])
async def secure_hello(request: Request):
    """
    Sample endpoint guarded by the TransitJwtMiddleware.
    Extracts the authenticated caller's identity injected into the request state.
    """
    user_id = request.state.user_id
    roles = request.state.roles
    session_id = request.state.session_id
    trace_id = request.state.trace_id
    
    logger.info(f"Trace [{trace_id}] - User {user_id} accessed hello endpoint.")
    
    return {
        "message": f"Hello from Python service! You are authenticated.",
        "principal": {
            "userId": user_id,
            "roles": roles,
            "sessionId": session_id,
            "traceId": trace_id
        }
    }

if __name__ == "__main__":
    import uvicorn
    
    # Path configurations for server-side mTLS
    ssl_cert = os.path.join(CERT_DIR, "services", APP_NAME, f"{APP_NAME}.crt")
    ssl_key = os.path.join(CERT_DIR, "services", APP_NAME, f"{APP_NAME}.key")
    ssl_ca = os.path.join(CERT_DIR, "rootCA", "rootCA.crt")
    
    if not os.path.exists(ssl_cert) or not os.path.exists(ssl_key):
        logger.error(f"Required mTLS certificates not found. Expected at:\n - Cert: {ssl_cert}\n - Key: {ssl_key}")
        logger.error("Please run './scripts/mtls-setup.sh add python-service' to generate them first.")
        sys.exit(1)

    logger.info(f"Launching secure Uvicorn server on port {PORT} with mTLS...")
    
    # Configure Server-side mTLS
    # Requiring clients (like the Gateway) to present certificates signed by our Root CA
    uvicorn.run(
        "app:app",
        host="0.0.0.0",
        port=PORT,
        ssl_keyfile=ssl_key,
        ssl_certfile=ssl_cert,
        ssl_ca_certs=ssl_ca,
        ssl_cert_reqs=ssl.CERT_REQUIRED
    )
