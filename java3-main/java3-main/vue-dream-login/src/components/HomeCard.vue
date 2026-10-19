<template>
  <div class="stage">
    <div class="card">
      <div class="avatar">{{ initial }}</div>
      <div class="title">粉金梦境</div>
      <div class="subtitle">梦 境 之 门 已 为 你 开 启</div>

      <div class="welcome">
        欢迎回来，<span class="name">{{ user.username }}</span>
      </div>
      <div class="info">本次登录时间：{{ loginTime }}</div>
      <div class="info" v-if="serverUser && serverUser.createTime">账号注册时间：{{ serverUser.createTime }}</div>

      <div class="dream-line">
        <span class="dot"></span>
        愿你在粉金色的梦里，拾得星光与糖
        <span class="dot"></span>
      </div>

      <button class="btn" type="button" @click="$emit('logout')">退 出 登 录</button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getUserInfo } from '../api'

const props = defineProps({
  user: { type: Object, required: true }
})
const emit = defineEmits(['logout'])

const initial = computed(() => (props.user.username || '梦').trim().charAt(0) || '梦')
const loginTime = new Date().toLocaleString('zh-CN', { hour12: false })

// 调受保护接口验证 token 并拉取服务端用户信息；token 失效则退出登录
const serverUser = ref(null)
onMounted(async () => {
  try {
    const res = await getUserInfo()
    serverUser.value = res && res.data
  } catch (e) {
    if (e.isUnauthorized) emit('logout')
  }
})
</script>

<style scoped>
.stage {
  position: relative; z-index: 5;
  height: 100%;
  display: flex; flex-direction: column;
  align-items: center; justify-content: center;
  text-align: center; padding: 24px;
}

.card {
  width: min(380px, 92vw);
  background: var(--card-bg);
  border: 1px solid var(--card-border);
  border-radius: 26px;
  padding: 40px 36px 34px;
  backdrop-filter: blur(10px);
  box-shadow: 0 18px 50px rgba(214, 158, 184, .3);
  animation: floaty 7s ease-in-out infinite;
}
@keyframes floaty {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-8px); }
}

.avatar {
  width: 72px; height: 72px; margin: 0 auto 16px;
  border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  font-size: 30px; color: #fff;
  background: linear-gradient(135deg, #f0a9c4, #e9b85f);
  box-shadow: 0 10px 26px rgba(233, 184, 95, .45);
}

.title {
  font-size: 30px; font-weight: 600; letter-spacing: .14em;
  background: linear-gradient(120deg, #c98aa6, #e9b85f 60%, #c98aa6);
  -webkit-background-clip: text; background-clip: text; color: transparent;
}
.subtitle { margin-top: 8px; font-size: 13px; letter-spacing: .22em; color: var(--sub); }

.welcome { margin-top: 26px; font-size: 18px; color: var(--ink); letter-spacing: .04em; }
.welcome .name {
  font-weight: 600;
  background: linear-gradient(120deg, #c98aa6, #e9b85f);
  -webkit-background-clip: text; background-clip: text; color: transparent;
}
.info { margin-top: 8px; font-size: 12.5px; color: var(--muted); letter-spacing: .05em; }

.dream-line {
  display: flex; align-items: center; justify-content: center; gap: 10px;
  margin: 24px 0 26px; font-size: 13px; color: var(--sub); letter-spacing: .08em;
}
.dream-line .dot {
  width: 5px; height: 5px; border-radius: 50%;
  background: linear-gradient(135deg, #f0a9c4, #e9b85f);
}

.btn {
  width: 100%; border: 0; cursor: pointer;
  padding: 13px; border-radius: 14px;
  font-size: 16px; letter-spacing: .3em; font-family: inherit; color: #fff;
  background: linear-gradient(120deg, #f0a9c4, #e9b85f);
  box-shadow: 0 10px 24px rgba(233, 184, 95, .4);
  transition: transform .15s, box-shadow .25s, filter .25s;
}
.btn:hover { transform: translateY(-2px); filter: brightness(1.04); box-shadow: 0 14px 30px rgba(233, 184, 95, .5); }
.btn:active { transform: translateY(0); }

@media (prefers-reduced-motion: reduce) {
  .card { animation: none; }
}
</style>
