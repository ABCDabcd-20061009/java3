import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    open: false,
    proxy: {
      // 前端 /api 请求转发到本地 Spring Boot 后端
      '/api': {
        target: 'http://localhost:8081',
        changeOrigin: true
      }
    }
  }
})
