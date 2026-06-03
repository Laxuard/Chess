const fs = require('fs');
const path = require('path');
const https = require('https');
const express = require('express');
const EurekaClient = require('./eureka');
const JwtValidator = require('./jwt-validator');

// Configuration loading from environment variables
const APP_NAME = process.env.SPRING_APPLICATION_NAME || 'nodejs-service';
const PORT = parseInt(process.env.PORT || '8083', 10);
const EUREKA_URL = process.env.EUREKA_URL || 'https://localhost:8761/eureka/';
const CERT_DIR = process.env.CERT_DIR_PATH || './certs';
const JWKS_URI = process.env.JWKS_URI || 'https://localhost:8080/.well-known/jwks.json';
const JWT_PUBLIC_KEY_PATH = process.env.JWT_PUBLIC_KEY_PATH || path.join(CERT_DIR, 'jwt', 'jwt_public.pem');

const app = express();
app.use(express.json());

// Initialize JWT validator
const jwtValidator = new JwtValidator({
    appName: APP_NAME,
    certDir: CERT_DIR,
    jwksUri: JWKS_URI,
    publicKeyPath: JWT_PUBLIC_KEY_PATH
});

// Exclude Swagger UI docs and health checks from JWT security
const excludePaths = [
    '/health',
    '/actuator/health',
    '/api-docs'
];

app.use(jwtValidator.getMiddleware(excludePaths));

// Initialize Eureka client
const eurekaClient = new EurekaClient({
    appName: APP_NAME,
    hostName: APP_NAME, // resolves inside docker bridge network
    port: PORT,
    eurekaUrl: EUREKA_URL,
    certDir: CERT_DIR
});

// ── Health Endpoints ──────────────────────────────────────────────────────────
app.get(['/health', '/actuator/health'], (req, res) => {
    res.json({
        status: 'UP',
        details: {
            database: 'UP',
            cache: 'UP'
        }
    });
});

// ── Secure Endpoint ────────────────────────────────────────────────────────────
app.get('/hello', (req, res) => {
    const user = req.user;
    console.log(`[Trace: ${user.traceId}] User ${user.id} accessed node hello endpoint.`);
    
    res.json({
        message: 'Hello from Node.js Express service! You are authenticated.',
        principal: {
            userId: user.id,
            roles: user.roles,
            sessionId: user.sessionId,
            traceId: user.traceId
        }
    });
});

// ── HTTPS Server-side mTLS Boot ────────────────────────────────────────────────
const sslCert = path.join(CERT_DIR, 'services', APP_NAME, `${APP_NAME}.crt`);
const sslKey = path.join(CERT_DIR, 'services', APP_NAME, `${APP_NAME}.key`);
const sslCa = path.join(CERT_DIR, 'rootCA', 'rootCA.crt');

if (!fs.existsSync(sslCert) || !fs.existsSync(sslKey)) {
    console.error(`[Error] Required certificates missing. Expected: \n - Cert: ${sslCert}\n - Key: ${sslKey}`);
    console.error(`Please run './scripts/mtls-setup.sh add nodejs-service' to generate them first.`);
    process.exit(1);
}

const httpsOptions = {
    key: fs.readFileSync(sslKey),
    cert: fs.readFileSync(sslCert),
    ca: fs.readFileSync(sslCa),
    requestCert: true,          // Require client certificate
    rejectUnauthorized: true   // Verify client certificate matches CA
};

const server = https.createServer(httpsOptions, app);

server.listen(PORT, () => {
    console.log(`[mTLS HTTPS] Node.js server running securely on port ${PORT}...`);
    // Start Eureka registration & heartbeats
    eurekaClient.start();
});

// Handle graceful shutdown
const gracefulShutdown = () => {
    console.log('Received shutdown signal. Stopping Eureka client and closing server...');
    eurekaClient.stop().finally(() => {
        server.close(() => {
            console.log('HTTPS Server closed. Exiting process.');
            process.exit(0);
        });
    });
};

process.on('SIGTERM', gracefulShutdown);
process.on('SIGINT', gracefulShutdown);
