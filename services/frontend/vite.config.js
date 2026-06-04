import { defineConfig, loadEnv } from 'vite'
import react from '@vitejs/plugin-react'
import path from 'path'

// https://vite.dev/config/
export default defineConfig(({ mode }) => {
  // Load environment variables from the project root (.env)
  const env = loadEnv(mode, path.resolve(__dirname, '../..'), '')
  const gatewayPort = env.GATEWAY_PORT || '8080'

  return {
    plugins: [react()],
    define: {
      __GATEWAY_PORT__: JSON.stringify(gatewayPort)
    },
    server: {
      proxy: {
        // Proxies standard auth endpoints through local Node dev server
        '/api': {
          target: `https://localhost:${gatewayPort}`,
          changeOrigin: true,
          secure: false, // Ignore self-signed SSL errors during local development
        },
        // Proxies Google OAuth redirection initiation
        '/oauth2': {
          target: `https://localhost:${gatewayPort}`,
          changeOrigin: true,
          secure: false,
        },
        // Proxies Google OAuth redirect callbacks
        '/login/oauth2': {
          target: `https://localhost:${gatewayPort}`,
          changeOrigin: true,
          secure: false,
        }
      }
    }
  }
})
