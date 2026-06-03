import logging
import os
import requests
import jwt
from jwt.algorithms import RSAAlgorithm
from fastapi import Request, HTTPException, Security
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials
from fastapi.middleware.base import BaseHTTPMiddleware
from starlette.middleware.base import RequestResponseEndpoint
from starlette.responses import Response

logger = logging.getLogger("jwt_validator")

class JwtValidator:
    def __init__(self, app_name, cert_dir, jwks_uri=None, public_key_path=None):
        self.app_name = app_name
        self.cert_dir = cert_dir
        self.jwks_uri = jwks_uri
        self.public_key_path = public_key_path
        
        # Cache for parsed public keys from JWKS
        self.jwks_cache = {}
        
        # Setup mTLS credentials for fetching JWKS if needed
        self.cert_path = os.path.join(cert_dir, "services", app_name, f"{app_name}.crt")
        self.key_path = os.path.join(cert_dir, "services", app_name, f"{app_name}.key")
        self.ca_path = os.path.join(cert_dir, "rootCA", "rootCA.crt")
        
        # Local public key fallback
        self.local_public_key = None
        if self.public_key_path and os.path.exists(self.public_key_path):
            try:
                with open(self.public_key_path, "r") as f:
                    self.local_public_key = f.read()
                logger.info(f"Loaded local JWT public key from {self.public_key_path}")
            except Exception as e:
                logger.error(f"Failed to read local public key: {e}")

    def fetch_jwks_keys(self):
        if not self.jwks_uri:
            raise ValueError("No JWKS URI or local public key available for JWT validation.")
            
        try:
            logger.info(f"Fetching JWKS keys from Gateway: {self.jwks_uri} over mTLS...")
            response = requests.get(
                self.jwks_uri,
                cert=(self.cert_path, self.key_path),
                verify=self.ca_path,
                timeout=5
            )
            response.raise_for_status()
            jwks = response.json()
            
            new_cache = {}
            for jwk in jwks.get("keys", []):
                kid = jwk.get("kid")
                if kid:
                    # Convert JWK JSON object into cryptography public key PEM structure
                    public_key = RSAAlgorithm.from_jwk(jwk)
                    new_cache[kid] = public_key
            
            self.jwks_cache = new_cache
            logger.info(f"Successfully cached {len(self.jwks_cache)} keys from JWKS.")
        except Exception as e:
            logger.error(f"Error fetching JWKS keys: {e}")
            if not self.jwks_cache:
                raise RuntimeError("Could not retrieve JWKS and no cached keys available.") from e

    def get_public_key(self, token_header):
        # 1. Use local public key if available (fastest, no network calls)
        if self.local_public_key:
            return self.local_public_key
            
        # 2. Extract key ID from token header and fetch/load from JWKS cache
        kid = token_header.get("kid")
        if not kid:
            raise jwt.InvalidTokenError("Token is missing key ID (kid) in header.")
            
        if kid not in self.jwks_cache:
            # Cache miss - reload JWKS
            self.fetch_jwks_keys()
            
        if kid not in self.jwks_cache:
            raise jwt.InvalidTokenError(f"Unknown key ID (kid): {kid}")
            
        return self.jwks_cache[kid]

    def validate_token(self, token: str) -> dict:
        try:
            # Unverified parse to extract the kid from the JWT header
            unverified_header = jwt.get_unverified_header(token)
            public_key = self.get_public_key(unverified_header)
            
            # Verify signature and standard claims
            payload = jwt.decode(
                token,
                public_key,
                algorithms=["RS256"],
                audience=None, # Transit token does not specify audience
                issuer="transcendence-gateway", # Matches JwtService.java 'iss'
                options={"require": ["exp", "iss", "sub"]}
            )
            return payload
        except jwt.ExpiredSignatureError as e:
            logger.warning("Token signature has expired.")
            raise HTTPException(status_code=401, detail="Transit token has expired") from e
        except jwt.InvalidTokenError as e:
            logger.warning(f"Invalid token: {e}")
            raise HTTPException(status_code=401, detail=f"Invalid transit token: {str(e)}") from e


class TransitJwtMiddleware(BaseHTTPMiddleware):
    def __init__(self, app, validator: JwtValidator, exclude_paths=None):
        super().__init__(app)
        self.validator = validator
        self.exclude_paths = exclude_paths or ["/docs", "/openapi.json", "/redoc", "/health", "/actuator/health"]

    async def dispatch(self, request: Request, call_next: RequestResponseEndpoint) -> Response:
        # Bypass validation for public/health paths
        if any(request.url.path.startswith(path) for path in self.exclude_paths):
            return await call_next(request)

        auth_header = request.headers.get("Authorization")
        if not auth_header or not auth_header.startswith("Bearer "):
            return Response(
                content='{"detail": "Missing or invalid Authorization header. A bearer transit JWT is required."}',
                status_code=401,
                media_type="application/json"
            )

        token = auth_header.split(" ")[1]
        try:
            payload = self.validator.validate_token(token)
            # Inject validated claims into request state for route handlers to access
            request.state.user_id = payload.get("sub")
            request.state.roles = payload.get("roles", [])
            request.state.session_id = payload.get("sid")
            request.state.trace_id = payload.get("tid")
        except HTTPException as he:
            return Response(
                content=f'{{"detail": "{he.detail}"}}',
                status_code=he.status_code,
                media_type="application/json"
            )
        except Exception:
            return Response(
                content='{"detail": "Internal server error during authentication verification"}',
                status_code=500,
                media_type="application/json"
            )

        return await call_next(request)
