import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      // Proxies standard auth endpoints through local Node dev server
      '/api': {
        target: 'https://localhost:8080',
        changeOrigin: true,
        secure: false, // Ignore self-signed SSL errors during local development
      },
      // Proxies Google OAuth redirection initiation
      '/oauth2': {
        target: 'https://localhost:8080',
        changeOrigin: true,
        secure: false,
      },
      // Proxies Google OAuth redirect callbacks
      '/login/oauth2': {
        target: 'https://localhost:8080',
        changeOrigin: true,
        secure: false,
      }
    }
  }
})
