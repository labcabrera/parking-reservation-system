import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  server: {
    port: 3000,
    proxy: {
      '/api/v1/catalog': 'http://localhost:8081',
      '/api/v1/parking-facilities': 'http://localhost:8081',
      '^/api/v1/reservations$': 'http://localhost:8081',
      '/api/v1/reservations': 'http://localhost:8082',
      '/api/v1/registration': 'http://localhost:8082',
    },
  },
  build: {
    outDir: 'dist',
  },
})
