const fs = require('fs');
const path = require('path');
const https = require('https');
const axios = require('axios');

class EurekaClient {
    constructor({ appName, hostName, port, eurekaUrl, certDir }) {
        this.appName = appName.toUpperCase();
        this.hostName = hostName;
        this.port = parseInt(port, 10);
        this.eurekaUrl = eurekaUrl.replace(/\/$/, ''); // strip trailing slash
        
        // Setup certificate paths
        const certPath = path.join(certDir, 'services', appName, `${appName}.crt`);
        const keyPath = path.join(certDir, 'services', appName, `${appName}.key`);
        const caPath = path.join(certDir, 'rootCA', 'rootCA.crt');
        
        this.instanceId = `${this.hostName}:${this.appName.toLowerCase()}:${this.port}`;
        
        // Build mTLS HTTPS Agent for Axios
        this.httpsAgent = new https.Agent({
            cert: fs.readFileSync(certPath),
            key: fs.readFileSync(keyPath),
            ca: fs.readFileSync(caPath),
            rejectUnauthorized: true
        });
        
        this.client = axios.create({
            httpsAgent: this.httpsAgent,
            headers: { 'Content-Type': 'application/json' }
        });
        
        this.heartbeatInterval = null;
    }

    async register() {
        const url = `${this.eurekaUrl}/apps/${this.appName}`;
        const payload = {
            instance: {
                instanceId: this.instanceId,
                hostName: this.hostName,
                app: this.appName,
                ipAddr: this.hostName,
                status: 'UP',
                port: {
                    '$': this.port,
                    '@enabled': 'false' // Disable unsecure port
                },
                securePort: {
                    '$': this.port,
                    '@enabled': 'true' // Route only via HTTPS securePort
                },
                vipAddress: this.appName.toLowerCase(),
                secureVipAddress: this.appName.toLowerCase(),
                dataCenterInfo: {
                    '@class': 'com.netflix.appinfo.InstanceInfo$DefaultDataCenterInfo',
                    name: 'MyOwn'
                }
            }
        };

        try {
            console.log(`[Eureka] Registering service at ${url}...`);
            const response = await this.client.post(url, payload);
            if (response.status === 204 || response.status === 200) {
                console.log('[Eureka] Successfully registered with Eureka!');
                return true;
            }
            console.error(`[Eureka] Unexpected registration status: ${response.status}`);
            return false;
        } catch (error) {
            console.error(`[Eureka] Registration failed: ${error.message}`);
            if (error.response) {
                console.error(`[Eureka] Details: ${JSON.stringify(error.response.data)}`);
            }
            return false;
        }
    }

    async sendHeartbeat() {
        const url = `${this.eurekaUrl}/apps/${this.appName}/${this.instanceId}`;
        try {
            const response = await this.client.put(url);
            if (response.status === 200) {
                // Heartbeat successful
                return true;
            } else {
                console.error(`[Eureka] Heartbeat failed with status: ${response.status}`);
                return false;
            }
        } catch (error) {
            if (error.response && error.response.status === 404) {
                console.warn('[Eureka] Server returned 404. Re-registering instance...');
                await this.register();
                return false;
            }
            console.error(`[Eureka] Heartbeat connection error: ${error.message}`);
            return false;
        }
    }

    async deregister() {
        const url = `${this.eurekaUrl}/apps/${this.appName}/${this.instanceId}`;
        try {
            console.log(`[Eureka] Deregistering service at ${url}...`);
            const response = await this.client.delete(url);
            if (response.status === 200) {
                console.log('[Eureka] Successfully deregistered from Eureka.');
                return true;
            }
            console.error(`[Eureka] Failed to deregister: Status ${response.status}`);
            return false;
        } catch (error) {
            console.error(`[Eureka] Deregistration error: ${error.message}`);
            return false;
        }
    }

    start() {
        this.register().then(registered => {
            if (registered) {
                // Maintain heartbeat every 30 seconds
                this.heartbeatInterval = setInterval(() => {
                    this.sendHeartbeat();
                }, 30000);
                console.log('[Eureka] Heartbeat scheduler started (30s interval).');
            }
        });
    }

    stop() {
        console.log('[Eureka] Stopping Eureka client...');
        if (this.heartbeatInterval) {
            clearInterval(this.heartbeatInterval);
        }
        return this.deregister();
    }
}

module.exports = EurekaClient;
