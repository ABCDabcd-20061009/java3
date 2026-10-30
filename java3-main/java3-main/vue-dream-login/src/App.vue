<template>
  <div class="app-root" :class="theme">
    <button class="theme-toggle" @click="toggleTheme"
            :aria-label="theme === 'dark' ? '切换到日间模式' : '切换到夜空模式'">
      <span v-html="theme === 'dark' ? sunIcon : moonIcon"></span>
    </button>

    <DreamBackground />
    <HomePage v-if="currentUser && !adminView" :user="currentUser" @logout="onLogout" @open-admin="openAdmin" />
    <AdminLogin v-else-if="adminView === 'login'" @admin-login-success="onAdminLogin" @back="adminView = null" />
    <AdminDashboard v-else-if="adminView === 'dashboard'" :admin="adminUser"
                    @exit-admin="exitAdmin" @admin-session-expired="adminView = 'login'" />
    <LoginCard v-else @login-success="onLoginSuccess" />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import DreamBackground from './components/DreamBackground.vue'
import LoginCard from './components/LoginCard.vue'
import HomePage from './components/HomePage.vue'
import AdminLogin from './components/AdminLogin.vue'
import AdminDashboard from './components/AdminDashboard.vue'

const theme = ref('light')
function toggleTheme() {
  theme.value = theme.value === 'light' ? 'dark' : 'light'
  document.documentElement.setAttribute('data-theme', theme.value)
}

/* ---- 登录会话：记住我 → localStorage，否则 → sessionStorage ---- */
const currentUser = ref(null)
const saved = localStorage.getItem('dream-user') || sessionStorage.getItem('dream-user')
if (saved) {
  try { currentUser.value = JSON.parse(saved) } catch (_) { /* 数据损坏则忽略 */ }
}

function onLoginSuccess({ user, remember }) {
  currentUser.value = user
  const store = remember ? localStorage : sessionStorage
  store.setItem('dream-user', JSON.stringify(user))
}

function onLogout() {
  currentUser.value = null
  localStorage.removeItem('dream-user')
  sessionStorage.removeItem('dream-user')
}

/* ---- 后台管理会话（独立于普通用户会话） ---- */
const ADMIN_KEY = 'dream-admin'
const adminView = ref(null)        // null（未进入后台）| 'login'（后台登录页）| 'dashboard'（后台管理页）
const adminUser = ref(null)
const savedAdmin = localStorage.getItem(ADMIN_KEY) || sessionStorage.getItem(ADMIN_KEY)
if (savedAdmin) {
  try {
    adminUser.value = JSON.parse(savedAdmin)
    adminView.value = 'dashboard'
  } catch (_) { /* 数据损坏则忽略 */ }
}

/** 首页导航「后台管理」按钮：已登录后台则直接进后台，否则进后台登录页 */
function openAdmin() {
  const saved = localStorage.getItem(ADMIN_KEY) || sessionStorage.getItem(ADMIN_KEY)
  if (saved) {
    try {
      adminUser.value = JSON.parse(saved)
      adminView.value = 'dashboard'
      return
    } catch (_) { /* 忽略 */ }
  }
  adminView.value = 'login'
}

function onAdminLogin({ user }) {
  adminUser.value = user
  localStorage.setItem(ADMIN_KEY, JSON.stringify(user))
  adminView.value = 'dashboard'
}

/** 退出后台：返回用户首页（保留用户登录态） */
function exitAdmin() {
  adminView.value = null
}

const moonIcon = `<svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M21 12.8A9 9 0 1 1 11.2 3a7 7 0 0 0 9.8 9.8Z"/></svg>`
const sunIcon = `<svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="4"/><path d="M12 2v2M12 20v2M2 12h2M20 12h2M4.9 4.9l1.4 1.4M17.7 17.7l1.4 1.4M4.9 19.1l1.4-1.4M17.7 6.3l1.4-1.4"/></svg>`
</script>

<style>
:root {
  --pink-2: #ffc6dd;
  --pink-3: #ffd9e8;
  --gold-2: #f7d98a;
  --gold-3: #ffe9b0;
  --ink: #9a6b7d;
  --sub: #b88aa0;
  --muted: #a98298;
  --link: #c98aa6;
  --field-bg: rgba(255, 255, 255, .6);
  --field-border: rgba(255, 255, 255, .7);
  --input-text: #9a6b7d;
  --placeholder: #c9a9b8;
  --card-bg: rgba(255, 255, 255, .34);
  --card-border: rgba(255, 255, 255, .6);
  /* 弹层/浮层等需要更高不透明度的面板底色 */
  --panel-bg: rgba(255, 252, 254, .92);
}
.app-root.dark {
  --ink: #f3dce8;
  --sub: #e7c3d4;
  --muted: #d8b6c8;
  --link: #f0b6cf;
  --field-bg: rgba(255, 255, 255, .1);
  --field-border: rgba(255, 255, 255, .18);
  --input-text: #f3dce8;
  --placeholder: #caa9bd;
  --card-bg: rgba(40, 26, 60, .45);
  --card-border: rgba(255, 255, 255, .16);
  --panel-bg: rgba(50, 31, 63, .94);
}

* { margin: 0; padding: 0; box-sizing: border-box; }

html, body, #app { height: 100%; }

body {
  font-family: "Noto Serif SC", "Songti SC", STSong, "SimSun", serif;
  overflow: hidden;
  position: relative;
}

.app-root {
  position: fixed; inset: 0;
  color: var(--ink);
  transition: background .6s ease, color .6s ease;
  background:
    radial-gradient(1200px 800px at 20% 10%, rgba(255, 198, 221, .75), transparent 60%),
    radial-gradient(1000px 900px at 85% 80%, rgba(247, 217, 138, .7), transparent 60%),
    linear-gradient(160deg, #ffdcea 0%, #ffe8d4 48%, #fff1dd 100%);
}
.app-root.dark {
  background:
    radial-gradient(1200px 800px at 20% 10%, rgba(255, 178, 214, .3), transparent 60%),
    radial-gradient(1000px 900px at 85% 80%, rgba(247, 199, 110, .26), transparent 60%),
    linear-gradient(160deg, #2b1734 0%, #3d2246 50%, #4d2c4f 100%);
}

.theme-toggle {
  position: fixed; top: 20px; right: 20px; z-index: 20;
  width: 44px; height: 44px; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  cursor: pointer; color: var(--ink);
  background: var(--card-bg); border: 1px solid var(--card-border);
  backdrop-filter: blur(10px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, .12);
  transition: transform .2s, background .4s, border-color .4s, color .4s;
}
.theme-toggle:hover { transform: translateY(-2px) rotate(12deg); }
</style>
