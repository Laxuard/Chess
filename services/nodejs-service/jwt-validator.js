const fs = require('fs');
const path = require('path');
const https = require('https');
const jwt = require('jsonwebtoken');
const jwksClient = require('jwks-rsa');

class JwtValidator {
    constructor({ appName, certDir, jwksUri, publicKeyPath }) {
        this.appName = appName;
        this.certDir = certDir;
        this.jwksUri = jwksUri;
        this.publicKeyPath = publicKeyPath;
        
        // Load certificates for fetching JWKS if needed
        const certPath = path.join(certDir, 'services', appName, `${appName}.crt`);
        const keyPath = path.join(certDir, 'services', appName, `${appName}.key`);
        const caPath = path.join(certDir, 'rootCA', 'rootCA.crt');
        
        // Local public key fallback
        this.localPublicKey = null;
        if (this.publicKeyPath && fs.existsSync(this.publicKeyPath)) {
            try {
                this.localPublicKey = fs.readFileSync(this.publicKeyPath, 'utf8');
                console.log(`[JWT] Loaded local public key from ${this.publicKeyPath}`);
            } catch (err) {
                console.error(`[JWT] Failed to read local public key: ${err.message}`);
            }
        }
        
        // Build mTLS Agent for JWKS client
        if (!this.localPublicKey && this.jwksUri) {
            const httpsAgent = new https.Agent({
                cert: fs.readFileSync(certPath),
                key: fs.readFileSync(keyPath),
                ca: fs.readFileSync(caPath),
                rejectUnauthorized: true
            });
            
            this.jwksClient = jwksClient({
                jwksUri: this.jwksUri,
                requestAgent: httpsAgent,
                cache: true,
                cacheMaxEntries: 5,
                cacheMaxAge: 24 * 60 * 60 * 1000 // 24 hours (matches JWKS Cache-Control)
            });
            console.log(`[JWT] Initialized JWKS client pointing to ${this.jwksUri}`);
        }
    }

    // Helper to get public key for a specific JWT header
    getSigningKey(header, callback) {
        if (this.localPublicKey) {
            return callback(null, this.localPublicKey);
        }
        
        if (!this.jwksClient) {
            return callback(new Error('No JWKS client or local public key available'));
        }
        
        this.jwksClient.getSigningKey(header.kid, (err, key) => {
            if (err) {
                return callback(err);
            }
            const signingKey = key.getPublicKey();
            callback(null, signingKey);
        });
    }

    // Express middleware function
    getMiddleware(excludePaths = []) {
        return (req, res, next) => {
            // Bypass validation for public/health paths
            const isExcluded = excludePaths.some(p => req.path.startsWith(p));
            if (isExcluded) {
                return next();
            }
            
            const authHeader = req.headers.authorization;
            if (!authHeader || !authHeader.startsWith('Bearer ')) {
                return res.status(401).json({
                    detail: 'Missing or invalid Authorization header. A bearer transit JWT is required.'
                });
            }
            
            const token = authHeader.split(' ')[1];
            
            // Verify signature using the dynamically retrieved signing key
            this.getSigningKey(jwt.decode(token, { complete: true })?.header || {}, (err, signingKey) => {
                if (err) {
                    console.error(`[JWT] Error fetching signing key: ${err.message}`);
                    return res.status(401).json({ detail: 'Invalid transit token signing key' });
                }
                
                jwt.verify(token, signingKey, {
                    algorithms: ['RS256'],
                    issuer: 'transcendence-gateway'
                }, (verifyErr, decoded) => {
                    if (verifyErr) {
                        console.warn(`[JWT] Token verification failed: ${verifyErr.message}`);
                        return res.status(401).json({ detail: `Invalid transit token: ${verifyErr.message}` });
                    }
                    
                    // Inject claims into request context for handlers to read
                    req.user = {
                        id: decoded.sub,
                        roles: decoded.roles || [],
                        sessionId: decoded.sid,
                        traceId: decoded.tid
                    };
                    
                    next();
                });
            });
        };
    }
}

module.exports = JwtValidator;
