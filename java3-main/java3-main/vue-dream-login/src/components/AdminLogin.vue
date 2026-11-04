<template>
  <div class="stage">
    <div class="card">
      <div class="brand">
        <svg viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
          <rect x="4" y="10" width="16" height="10" rx="2" />
          <path d="M8 10V7a4 4 0 0 1 8 0v3" />
          <circle cx="12" cy="15" r="1.6" />
        </svg>
      </div>
      <div class="title">后台管理</div>
      <div class="subtitle">粉 金 梦 境 · 管 理 员 登 录</div>

      <form class="form" @submit.prevent="onSubmit">
        <label class="field">
          <svg class="ico" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
            <circle cx="12" cy="8" r="4" />
            <path d="M4 21c0-4 3.6-7 8-7s8 3 8 7" />
          </svg>
          <input type="text" v-model="username" placeholder="管理员用户名" autocomplete="username" required />
        </label>

        <label class="field">
          <svg class="ico" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
            <rect x="5" y="11" width="14" height="9" rx="2.2" />
            <path d="M8 11V8a4 4 0 0 1 8 0v3" />
          </svg>
          <input type="password" v-model="password" placeholder="密码" autocomplete="current-password" required />
        </label>

        <div class="error" v-if="errorMsg">{{ errorMsg }}</div>

        <button class="btn" type="submit" :disabled="logging">{{ logging ? '登 录 中 …' : '登 录' }}</button>
        <button class="back" type="button" @click="$emit('back')">← 返回首页</button>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { adminLogin } from '../api'

const emit = defineEmits(['admin-login-success', 'back'])

const username = ref('')
const password = ref('')
const logging = ref(false)
const errorMsg = ref('')

async function onSubmit() {
  if (logging.value) return
  errorMsg.value = ''
  logging.value = true
  try {
    const res = await adminLogin({ username: username.value.trim(), password: password.value })
    const user = (res && res.data) || null
    if (!user || !user.token) {
      errorMsg.value = '登录响应异常，请稍后再试'
      return
    }
    emit('admin-login-success', { user })
  } catch (e) {
    errorMsg.value = (e && e.message) || '登录失败，请稍后再试'
  } finally {
    logging.value = false
  }
}
</script>

<style scoped>
.stage {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
}
.card {
  width: min(400px, 92vw);
  background: var(--panel-bg, rgba(255, 252, 254, .92));
  border: 1px solid var(--card-border, rgba(255, 255, 255, .6));
  border-radius: 22px;
  padding: 34px 30px 26px;
  box-shadow: 0 18px 50px rgba(154, 107, 125, .25);
  text-align: center;
}
.brand {
  width: 52px; height: 52px;
  margin: 0 auto 12px;
  border-radius: 16px;
  display: flex; align-items: center; justify-content: center;
  background: linear-gradient(135deg, #f6a5c0, #f3c98b);
  box-shadow: 0 8px 18px rgba(246, 165, 192, .45);
}
.brand svg { width: 26px; height: 26px; }
.title {
  font-size: 22px;
  color: var(--ink, #9a6b7d);
  letter-spacing: 2px;
}
.subtitle {
  font-size: 12px;
  color: var(--sub, #b88aa0);
  margin: 6px 0 22px;
  letter-spacing: 1px;
}
.form { display: flex; flex-direction: column; gap: 14px; text-align: left; }
.field {
  display: flex; align-items: center; gap: 8px;
  background: var(--field-bg, rgba(255, 255, 255, .6));
  border: 1px solid var(--field-border, rgba(255, 255, 255, .7));
  border-radius: 14px;
  padding: 0 14px;
  height: 46px;
}
.field:focus-within { border-color: #f0a8c4; }
.field .ico { width: 18px; height: 18px; color: var(--muted, #a98298); flex: none; }
.field input {
  flex: 1; min-width: 0;
  border: none; outline: none; background: transparent;
  font-size: 14px;
  color: var(--input-text, #9a6b7d);
}
.field input::placeholder { color: var(--placeholder, #c9a9b8); }
.error {
  font-size: 12px;
  color: #d25c72;
  background: rgba(210, 92, 114, .08);
  border-radius: 10px;
  padding: 8px 12px;
}
.btn {
  height: 46px;
  border: none;
  border-radius: 14px;
  font-size: 15px;
  letter-spacing: 4px;
  cursor: pointer;
  color: #fff;
  background: linear-gradient(135deg, #f394b8, #eeb878);
  box-shadow: 0 8px 18px rgba(243, 148, 184, .4);
  transition: transform .15s, box-shadow .15s;
}
.btn:hover:not(:disabled) { transform: translateY(-1px); }
.btn:disabled { opacity: .7; cursor: not-allowed; }
.back {
  border: none;
  background: transparent;
  color: var(--link, #c98aa6);
  font-size: 13px;
  cursor: pointer;
  padding: 4px 0 0;
}
.back:hover { text-decoration: underline; }
</style>
