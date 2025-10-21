import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      // 將 /api 的請求代理到後端
      '/api': {
        target: 'http://localhost:8080', // 您的 Spring Boot 伺服器
        changeOrigin: true, // 改變請求來源，才能繞過 CORS
        // 如果您的後端路徑沒有 /api，可以用這個重寫
        // rewrite: (path) => path.replace(/^\/api/, ''), 
      }
    }
  }
})