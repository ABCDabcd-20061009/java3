import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    // 端口被占用时直接报错，不要悄悄换到 5174 —— 否则别人用 http://本机IP:5173 就打不开了
    strictPort: true,
    open: false,
    // 监听 0.0.0.0，使同一局域网内的其他电脑/手机可用 http://本机IP:5173 访问
    host: true,
    // Vite 6 默认会校验 Host 头，用 IP、域名或内网穿透访问时可能被拦成 403。
    // 本机是局域网演示项目，这里放开（只影响开发服务器，不影响打包产物）。
    allowedHosts: true,
    proxy: {
      // 前端 /api 请求转发到本机 Spring Boot 后端。
      // 这里固定用 127.0.0.1：写死局域网 IP 的话，一旦换网络/IP 变了，接口就会 502（页面看着"前端出错"）。
      '/api': {
        target: 'http://127.0.0.1:8081',
        changeOrigin: true
      },
      // 广播剧媒体（视频/音频）同样由后端提供，避免依赖外网地址
      '/media': {
        target: 'http://127.0.0.1:8081',
        changeOrigin: true
      }
    }
  }
})
