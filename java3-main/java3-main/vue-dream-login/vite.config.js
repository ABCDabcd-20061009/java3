import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    open: false,
    // 监听 0.0.0.0，使同一局域网内的其他电脑可用 http://本机IP:5173 访问
    host: true,
    proxy: {
      // 前端 /api 请求转发到本地 Spring Boot 后端
      '/api': {
        target: 'http://192.168.68.22:8081',
        changeOrigin: true
      },
      // 广播剧媒体（视频/音频）同样由后端提供，避免依赖外网地址
      '/media': {
        target: 'http://192.168.68.22:8081',
        changeOrigin: true
      }
    }
  }
})
