import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

export default defineConfig({
  plugins: [react()],
  server: {
    port: 3001,
    proxy: {
      "/payment-api": {
        target: "http://localhost:8089",
        rewrite: (path) => path.replace(/^\/payment-api/, ""),
      },
    },
  },
  build: {
    outDir: "dist",
  },
});
