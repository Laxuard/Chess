import logging
import threading
import time
import requests
import os
import signal

logger = logging.getLogger("eureka_client")

class EurekaClient:
    def __init__(self, app_name, host_name, port, eureka_url, cert_dir):
        """
        app_name: e.g. "python-service"
        host_name: e.g. "python-service" (resolvable container name or hostname)
        port: HTTPS port of the service
        eureka_url: Eureka endpoint, e.g. "https://eureka-server:8761/eureka/"
        cert_dir: Base directory containing certs, e.g. "/app/certs"
        """
        self.app_name = app_name.upper()
        self.host_name = host_name
        self.port = int(port)
        self.eureka_url = eureka_url.rstrip("/")
        
        # Paths to mTLS certificates
        self.cert_path = os.path.join(cert_dir, "services", app_name, f"{app_name}.crt")
        self.key_path = os.path.join(cert_dir, "services", app_name, f"{app_name}.key")
        self.ca_path = os.path.join(cert_dir, "rootCA", "rootCA.crt")
        
        # Instance configuration for Eureka
        self.instance_id = f"{self.host_name}:{self.app_name.lower()}:{self.port}"
        
        self.session = requests.Session()
        # Set up mTLS on requests session
        self.session.cert = (self.cert_path, self.key_path)
        self.session.verify = self.ca_path
        
        self._stop_event = threading.Event()
        self._heartbeat_thread = None

    def register(self):
        url = f"{self.eureka_url}/apps/{self.app_name}"
        payload = {
            "instance": {
                "instanceId": self.instance_id,
                "hostName": self.host_name,
                "app": self.app_name,
                "ipAddr": self.host_name, # In Docker, DNS hostname is often the IP/host
                "status": "UP",
                "port": {
                    "$": self.port,
                    "@enabled": "false" # Secure ports only
                },
                "securePort": {
                    "$": self.port,
                    "@enabled": "true"
                },
                "vipAddress": self.app_name.lower(),
                "secureVipAddress": self.app_name.lower(),
                "dataCenterInfo": {
                    "@class": "com.netflix.appinfo.InstanceInfo$DefaultDataCenterInfo",
                    "name": "MyOwn"
                }
            }
        }
        
        try:
            logger.info(f"Registering service with Eureka at {url}...")
            response = self.session.post(url, json=payload, headers={"Content-Type": "application/json"})
            if response.status_code in [204, 200]:
                logger.info("Successfully registered with Eureka!")
                return True
            else:
                logger.error(f"Failed to register with Eureka. Status: {response.status_code}, Response: {response.text}")
                return False
        except Exception as e:
            logger.error(f"Error registering with Eureka: {e}")
            return False

    def send_heartbeat(self):
        url = f"{self.eureka_url}/apps/{self.app_name}/{self.instance_id}"
        try:
            response = self.session.put(url)
            if response.status_code == 200:
                logger.debug("Eureka heartbeat sent successfully.")
                return True
            elif response.status_code == 404:
                logger.warning("Eureka returned 404. Service registry expired or server restarted. Re-registering...")
                self.register()
                return False
            else:
                logger.error(f"Failed to send heartbeat. Status: {response.status_code}, Response: {response.text}")
                return False
        except Exception as e:
            logger.error(f"Error sending heartbeat to Eureka: {e}")
            return False

    def deregister(self):
        url = f"{self.eureka_url}/apps/{self.app_name}/{self.instance_id}"
        try:
            logger.info(f"Deregistering service from Eureka at {url}...")
            response = self.session.delete(url)
            if response.status_code == 200:
                logger.info("Successfully deregistered from Eureka.")
                return True
            else:
                logger.error(f"Failed to deregister. Status: {response.status_code}")
                return False
        except Exception as e:
            logger.error(f"Error deregistering: {e}")
            return False

    def _heartbeat_loop(self):
        while not self._stop_event.is_set():
            self.send_heartbeat()
            # Sleep 30 seconds or until stopped
            self._stop_event.wait(30)

    def start(self):
        if self.register():
            self._stop_event.clear()
            self._heartbeat_thread = threading.Thread(target=self._heartbeat_loop, daemon=True)
            self._heartbeat_thread.start()
            logger.info("Eureka heartbeat daemon thread started.")

    def stop(self):
        logger.info("Stopping Eureka client...")
        self._stop_event.set()
        if self._heartbeat_thread:
            self._heartbeat_thread.join(timeout=5)
        self.deregister()
