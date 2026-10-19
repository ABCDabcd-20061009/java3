<template>
  <div class="dream-bg">
    <!-- 粉金光晕 -->
    <div class="glow a"></div>
    <div class="glow b"></div>
    <div class="glow c"></div>

    <!-- 星星层：一闪一闪发光 -->
    <div class="stars" ref="stars"></div>

    <!-- 气泡层：缓慢流动上升 -->
    <div class="bubbles" ref="bubbles"></div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const stars = ref(null)
const bubbles = ref(null)

function spawn(container, cls, count, opt) {
  for (let i = 0; i < count; i++) {
    const el = document.createElement('div')
    el.className = cls
    const s = opt.min + Math.random() * opt.range
    el.style.width = s + 'px'
    el.style.height = s + 'px'
    if (opt.top) el.style.top = Math.random() * 100 + 'vh'
    if (opt.left) el.style.left = Math.random() * 100 + 'vw'
    const dur = opt.durMin + Math.random() * opt.durRange
    el.style.animationDuration = dur + 's'
    el.style.animationDelay = (-Math.random() * dur) + 's'
    if (opt.opacity) el.style.opacity = (0.6 + Math.random() * 0.4).toFixed(2)
    container.appendChild(el)
  }
}

onMounted(() => {
  // 小星星：满天散布、随机节奏闪烁
  spawn(stars.value, 'star', 64, { min: 2, range: 3, top: true, left: true, durMin: 2, durRange: 3.5 })
  // 大四角星光：数量少一些、闪烁更慢
  spawn(stars.value, 'sparkle', 10, { min: 12, range: 9, top: true, left: true, durMin: 3.5, durRange: 3 })
  // 气泡：缓慢流动上升
  spawn(bubbles.value, 'bubble', 20, { min: 12, range: 48, left: true, durMin: 22, durRange: 18, opacity: true })
})
</script>

<style scoped>
.dream-bg { position: fixed; inset: 0; }

/* ===== 粉金光晕 ===== */
.glow {
  position: fixed;
  border-radius: 50%;
  filter: blur(60px);
  opacity: .55;
  z-index: 0;
  animation: drift 18s ease-in-out infinite alternate;
}
.glow.a { width: 480px; height: 480px; top: -120px; left: -80px; background: radial-gradient(circle, #ffc6dd, transparent 70%); }
.glow.b { width: 520px; height: 520px; bottom: -160px; right: -100px; background: radial-gradient(circle, #f7d98a, transparent 70%); animation-delay: -6s; }
.glow.c { width: 360px; height: 360px; top: 40%; left: 55%; background: radial-gradient(circle, #ffd9e8, transparent 70%); animation-delay: -11s; }

@keyframes drift {
  from { transform: translate(0, 0) scale(1); }
  to { transform: translate(40px, 30px) scale(1.12); }
}

/* ===== 星星：一闪一闪发光 ===== */
.stars { position: fixed; inset: 0; z-index: 1; pointer-events: none; overflow: hidden; }

/* 注意：.star / .sparkle / .bubble 均为 JS 动态创建，无 scoped 属性，
   必须用 :deep() 挂到模板容器上，否则样式不生效（元素不可见） */
.stars :deep(.star) {
  position: absolute;
  border-radius: 50%;
  opacity: .85;
  background: radial-gradient(circle, #ffffff 20%, rgba(247, 217, 138, .95) 55%, rgba(255, 198, 221, .5) 100%);
  box-shadow:
    0 0 6px 1px rgba(247, 217, 138, .9),
    0 0 14px 4px rgba(255, 198, 221, .5);
  animation: twinkle ease-in-out infinite;
}

/* 四角大星光 */
.stars :deep(.sparkle) {
  position: absolute;
  opacity: .85;
  background-image: url("data:image/svg+xml;utf8,<svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24'><path d='M12 1.5C13.2 7.4 16.6 10.8 22.5 12 16.6 13.2 13.2 16.6 12 22.5 10.8 16.6 7.4 13.2 1.5 12 7.4 10.8 10.8 7.4 12 1.5Z' fill='%23ffe9b0'/><path d='M12 1.5C12.7 7.2 15.3 10 19.5 11 15.3 12.3 12.7 15 12 22.5 11 15 8.4 12.3 4.5 11 8.4 10 11.2 7.2 12 1.5Z' fill='%23ffffff' opacity='.85'/></svg>");
  background-size: contain;
  background-repeat: no-repeat;
  filter: drop-shadow(0 0 7px rgba(247, 217, 138, .85)) drop-shadow(0 0 14px rgba(255, 198, 221, .45));
  animation: twinkle ease-in-out infinite;
}

@keyframes twinkle {
  0%, 100% { opacity: .12; transform: scale(.65) rotate(0deg); }
  50% { opacity: 1; transform: scale(1.15) rotate(12deg); }
}

/* ===== 气泡：缓慢流动 ===== */
.bubbles { position: fixed; inset: 0; z-index: 3; pointer-events: none; overflow: hidden; }
.bubbles :deep(.bubble) {
  position: absolute; bottom: -120px; border-radius: 50%;
  background: radial-gradient(circle at 32% 28%, rgba(255, 255, 255, .9), rgba(255, 255, 255, .15) 38%, rgba(255, 214, 232, .18) 60%, rgba(247, 217, 138, .12) 100%);
  border: 1px solid rgba(255, 255, 255, .45);
  box-shadow: inset 0 0 18px rgba(255, 255, 255, .5), 0 0 16px rgba(255, 233, 176, .35);
  backdrop-filter: blur(1px);
  animation: rise linear infinite;
}
@keyframes rise {
  0% { transform: translateY(0) translateX(0) scale(.9); opacity: 0; }
  10% { opacity: .85; }
  50% { transform: translateY(-52vh) translateX(24px) scale(1); }
  90% { opacity: .8; }
  100% { transform: translateY(-106vh) translateX(-16px) scale(1.04); opacity: 0; }
}

@media (prefers-reduced-motion: reduce) {
  .glow, .stars :deep(.star), .stars :deep(.sparkle), .bubbles :deep(.bubble) { animation: none; }
}
</style>
