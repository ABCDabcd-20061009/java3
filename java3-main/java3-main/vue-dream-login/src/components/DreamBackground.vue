<template>
  <div class="dream-bg" :class="'mode-' + mode">
    <!-- 背景图片（按页面切换） -->
    <div class="bg-photo" :style="bgStyle"></div>
    <!-- 风格化遮罩（让文字/卡片可读，并强化图片氛围） -->
    <div class="bg-overlay"></div>

    <!-- 光晕层：颜色随风格切换 -->
    <div class="glow a"></div>
    <div class="glow b"></div>
    <div class="glow c"></div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  // 'login' → 黑白质感年轻男子；'home' → 梦幻婚礼插画
  mode: { type: String, default: 'login' }
})

const bgStyle = computed(() => {
  const url = props.mode === 'home'
    ? '/backgrounds/home-bg.jpg'
    : '/backgrounds/login-bg.jpg'
  return { backgroundImage: `url("${url}")` }
})
</script>

<style scoped>
.dream-bg { position: fixed; inset: 0; z-index: 0; overflow: hidden; }

/* ===== 背景图片 ===== */
.bg-photo {
  position: fixed; inset: 0; z-index: 0;
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  transition: background-image .6s ease, filter .6s ease, opacity .6s ease;
}
/* 遮罩：文字/卡片可读 + 强化氛围 */
.bg-overlay {
  position: fixed; inset: 0; z-index: 1;
  transition: background .6s ease;
  pointer-events: none;
}

/* ===== 登录页：黑白冷调、沉稳高级 ===== */
.mode-login .bg-photo {
  /* 全屏铺满；原图竖构图、人物脸在右上方，
     靠右 + 顶部对齐，既铺满整页又保留右上方脸与顺着右侧向下的身体 */
  background-size: cover;
  background-position: right top;
  filter: grayscale(.18) contrast(1.05) brightness(.9);
}
.mode-login .bg-overlay {
  /* 左侧（卡片区）压暗保证文字可读；右侧（人物）保留清晰 */
  background:
    radial-gradient(900px 600px at 80% 14%, rgba(255,255,255,.12), transparent 55%),
    linear-gradient(100deg,
      rgba(6,6,9,.80) 0%,
      rgba(8,8,11,.52) 38%,
      rgba(10,10,14,.34) 66%,
      rgba(12,12,16,.50) 100%);
}

/* ===== 首页：森林绿 + 暖金 + 奶油，梦幻浪漫 ===== */
.mode-home .bg-photo {
  filter: saturate(1.06) brightness(.94) contrast(1.02);
}
.mode-home .bg-overlay {
  background:
    radial-gradient(900px 650px at 22% 16%, rgba(255,247,224,.22), transparent 55%),
    radial-gradient(900px 700px at 82% 84%, rgba(120,170,140,.28), transparent 55%),
    linear-gradient(160deg, rgba(34,54,44,.30) 0%, rgba(60,72,62,.40) 50%, rgba(40,54,46,.52) 100%);
}

/* ===== 光晕（随风格变色） ===== */
.glow {
  position: fixed;
  border-radius: 50%;
  filter: blur(60px);
  opacity: .5;
  z-index: 0;
  animation: drift 18s ease-in-out infinite alternate;
  transition: background .6s ease;
}
.glow.a { width: 480px; height: 480px; top: -120px; left: -80px; }
.glow.b { width: 520px; height: 520px; bottom: -160px; right: -100px; animation-delay: -6s; }
.glow.c { width: 360px; height: 360px; top: 40%; left: 55%; animation-delay: -11s; }

/* 登录：银灰 / 冷调 */
.mode-login .glow.a { background: radial-gradient(circle, #cfcfd8, transparent 70%); opacity: .35; }
.mode-login .glow.b { background: radial-gradient(circle, #8a8a98, transparent 70%); opacity: .3; }
.mode-login .glow.c { background: radial-gradient(circle, #e6e6ee, transparent 70%); opacity: .28; }

/* 首页：森林绿 / 暖金 / 奶油 */
.mode-home .glow.a { background: radial-gradient(circle, #a9d6bb, transparent 70%); opacity: .5; }
.mode-home .glow.b { background: radial-gradient(circle, #e9d49a, transparent 70%); opacity: .46; }
.mode-home .glow.c { background: radial-gradient(circle, #cfe7d6, transparent 70%); opacity: .42; }

@keyframes drift {
  from { transform: translate(0, 0) scale(1); }
  to { transform: translate(40px, 30px) scale(1.12); }
}

@media (prefers-reduced-motion: reduce) {
  .glow { animation: none; }
}
</style>
