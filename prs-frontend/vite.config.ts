import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  server: {
    port: 3000,
    proxy: {
      '/api/v1/parking-facilities': 'http://localhost:8085',
      '/api/v1/reservations': 'http://localhost:8085',
      '/admin-api/api/v1/parking-facilities': {
        target: 'http://localhost:8081',
        rewrite: (path) => path.replace(/^\/admin-api/, ''),
      },
      '/admin-api/api/v1/reservations': {
        target: 'http://localhost:8081',
        rewrite: (path) => path.replace(/^\/admin-api/, ''),
      },
      '/admin-api/api/v1/pricing-rules': {
        target: 'http://localhost:8083',
        rewrite: (path) => path.replace(/^\/admin-api/, ''),
      },
      '/admin-api/api/v1/orders': {
        target: 'http://localhost:8082',
        rewrite: (path) => path.replace(/^\/admin-api/, ''),
      },
      '/api/v1/registration': 'http://localhost:8082',
    },
  },
  build: {
    outDir: 'dist',
  },
})
