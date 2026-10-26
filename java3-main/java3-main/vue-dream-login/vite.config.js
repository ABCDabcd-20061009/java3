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
      },
      // 广播剧媒体（视频/音频）同样由后端提供，避免依赖外网地址
      '/media': {
        target: 'http://localhost:8081',
        changeOrigin: true
      }
    }
  }
})
