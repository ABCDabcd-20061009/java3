<template>
  <div class="stage">
    <div class="card">
      <div class="brand">
        <svg viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
          <path d="M12 3l2.2 4.6L19 8.4l-3.6 3.3.9 4.9L12 14.4 7.7 16.6l.9-4.9L5 8.4l4.8-.8z" />
        </svg>
      </div>
      <div class="title">粉金梦境</div>
      <div class="subtitle">{{ mode === 'login' ? '欢 迎 回 来' : '创 建 账 号' }}</div>

      <!-- 登录 -->
      <form v-if="mode === 'login'" class="form" @submit.prevent="onLogin">
        <label class="field">
          <svg class="ico" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
            <circle cx="12" cy="8" r="4" />
            <path d="M4 21c0-4 3.6-7 8-7s8 3 8 7" />
          </svg>
          <input type="text" v-model="account" placeholder="用户名" autocomplete="username" required />
        </label>

        <label class="field">
          <svg class="ico" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
            <rect x="5" y="11" width="14" height="9" rx="2.2" />
            <path d="M8 11V8a4 4 0 0 1 8 0v3" />
          </svg>
          <input :type="pwdType" v-model="password" id="pwd" placeholder="密码" autocomplete="current-password" required />
          <button type="button" class="eye" @click="togglePwd" :aria-label="showPwd ? '隐藏密码' : '显示密码'">
            <span v-html="showPwd ? eyeOff : eyeOn"></span>
          </button>
        </label>

        <div class="row">
          <label class="remember"><input type="checkbox" v-model="remember" /> 记住我</label>
          <a href="#" class="link" @click.prevent="flash('已发送重置密码链接（示例）')">忘记密码？</a>
        </div>

        <button class="btn" type="submit" :disabled="loggingIn">{{ loggingIn ? '登 录 中 …' : '登 录' }}</button>

        <div class="social-sep">其他方式登录</div>
        <div class="social">
          <button class="social-btn" type="button" @click="flash('微信登录（示例）')" aria-label="微信登录">
            <span v-html="wechatIcon"></span>
          </button>
          <button class="social-btn" type="button" @click="flash('Google 登录（示例）')" aria-label="Google 登录">
            <span v-html="googleIcon"></span>
          </button>
        </div>

        <div class="alt">还没有账号？<a href="#" class="link" @click.prevent="switchMode('register')">立即注册</a></div>
      </form>

      <!-- 注册 -->
      <form v-else class="form" @submit.prevent="onRegister" novalidate>
        <!-- 用户名 -->
        <div class="field-wrap">
          <label class="field" :class="fieldClass('username')">
            <svg class="ico" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
              <circle cx="12" cy="8" r="4" />
              <path d="M4 21c0-4 3.6-7 8-7s8 3 8 7" />
            </svg>
            <input type="text" v-model="username" placeholder="用户名（2-20 个字符）" maxlength="20"
                   autocomplete="nickname" @blur="validateField('username')" @input="clearError('username')" />
          </label>
          <div class="hint" v-if="errors.username">{{ errors.username }}</div>
        </div>

        <!-- 验证码 -->
        <div class="field-wrap">
          <label class="field code-field" :class="fieldClass('code')">
            <svg class="ico" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
              <path d="M12 3l8 4v5c0 4.5-3.2 7.2-8 9-4.8-1.8-8-4.5-8-9V7z" />
              <path d="M9 12l2 2 4-4" />
            </svg>
            <input type="text" :value="code" placeholder="6 位数字验证码" maxlength="6" inputmode="numeric"
                   autocomplete="one-time-code" @input="onCodeInput" @blur="validateField('code')" />
            <button type="button" class="code-btn" :disabled="countdown > 0 || sending" @click="sendVerificationCode">
              {{ countdown > 0 ? countdown + 's 后重发' : (sending ? '发送中…' : '获取验证码') }}
            </button>
          </label>
          <div class="hint" v-if="errors.code">{{ errors.code }}</div>
        </div>

        <!-- 密码 -->
        <div class="field-wrap">
          <label class="field" :class="fieldClass('password')">
            <svg class="ico" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
              <rect x="5" y="11" width="14" height="9" rx="2.2" />
              <path d="M8 11V8a4 4 0 0 1 8 0v3" />
            </svg>
            <input :type="pwdType" v-model="password" placeholder="设置密码（1-18 位，不含空格）" maxlength="18"
                   autocomplete="new-password" @blur="validateField('password')" @input="clearError('password')" />
            <span class="pwd-count" v-if="password">{{ password.length }}/18</span>
            <button type="button" class="eye" @click="togglePwd" :aria-label="showPwd ? '隐藏密码' : '显示密码'">
              <span v-html="showPwd ? eyeOff : eyeOn"></span>
            </button>
          </label>
          <div class="hint" v-if="errors.password">{{ errors.password }}</div>
          <!-- 密码强度 -->
          <div class="strength" v-if="password && !errors.password">
            <div class="strength-bars">
              <span v-for="i in 3" :key="i" class="bar" :class="['lv' + strengthScore, { on: strengthScore >= i }]"></span>
            </div>
            <span class="strength-text" :class="'lv' + strengthScore">{{ strengthText }}</span>
          </div>
        </div>

        <!-- 确认密码 -->
        <div class="field-wrap">
          <label class="field" :class="fieldClass('confirm')">
            <svg class="ico" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
              <path d="M20 6L9 17l-5-5" />
            </svg>
            <input :type="pwdType" v-model="confirm" placeholder="确认密码" maxlength="18"
                   autocomplete="new-password" @blur="validateField('confirm')" @input="clearError('confirm')" />
          </label>
          <div class="hint" v-if="errors.confirm">{{ errors.confirm }}</div>
          <div class="hint ok" v-else-if="confirm && confirm === password">两次密码一致 ✓</div>
        </div>

        <!-- 用户协议 -->
        <div class="field-wrap">
          <label class="agree">
            <input type="checkbox" v-model="agree" @change="clearError('agree')" />
            <span>我已阅读并同意
              <a href="#" class="link" @click.prevent="flash('用户协议（示例）')">《用户协议》</a> 和
              <a href="#" class="link" @click.prevent="flash('隐私政策（示例）')">《隐私政策》</a>
            </span>
          </label>
          <div class="hint" v-if="errors.agree">{{ errors.agree }}</div>
        </div>

        <button class="btn" type="submit" :disabled="submitting">{{ submitting ? '注 册 中 …' : '注 册' }}</button>

        <div class="alt">已有账号？<a href="#" class="link" @click.prevent="switchMode('login')">去登录</a></div>
      </form>
    </div>

    <div class="toast" :class="{ show: toastMsg }">{{ toastMsg }}</div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onBeforeUnmount } from 'vue'
import { sendCode, registerUser, loginUser } from '../api'

const emit = defineEmits(['login-success'])

const mode = ref('login')
const account = ref('')
const username = ref('')
const password = ref('')
const confirm = ref('')
const remember = ref(false)
const showPwd = ref(false)
const toastMsg = ref('')
const code = ref('')
const sending = ref(false)
const submitting = ref(false)
const loggingIn = ref(false)
const countdown = ref(0)
const agree = ref(false)
// 演示模式：后端不可用时本地生成验证码
const mockCode = ref('')
const isMock = ref(false)
let sendTimer = null

const errors = reactive({ username: '', code: '', password: '', confirm: '', agree: '' })
// 记录字段是否已校验通过（用于绿色边框）
const passed = reactive({ username: false, code: false, password: false, confirm: false })

const pwdType = computed(() => (showPwd.value ? 'text' : 'password'))

const eyeOn = `<svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M2 12s3.5-7 10-7 10 7 10 7-3.5 7-10 7-10-7-10-7Z"/><circle cx="12" cy="12" r="3"/></svg>`
const eyeOff = `<svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M2 12s3.5-7 10-7 10 7 10 7-3.5 7-10 7-10-7-10-7Z"/><circle cx="12" cy="12" r="3"/><path d="M3 3l18 18"/></svg>`
const wechatIcon = `<svg viewBox="0 0 24 24" width="22" height="22" fill="#1aad19"><path d="M9 4C5.1 4 2 6.7 2 10c0 1.9 1.1 3.6 2.8 4.7L4.2 17l2.6-1.4c.8.2 1.6.3 2.5.3h.5a5 5 0 0 1-.2-1.4c0-3 2.9-5.4 6.6-5.4h.4C15.9 6.4 12.8 4 9 4Zm-2.6 3.6a.9.9 0 1 1 0 1.8.9.9 0 0 1 0-1.8Zm5.2 0a.9.9 0 1 1 0 1.8.9.9 0 0 1 0-1.8Z"/><path d="M22 14.6c0-2.7-2.6-4.9-5.8-4.9s-5.8 2.2-5.8 4.9 2.6 4.9 5.8 4.9c.7 0 1.4-.1 2-.3l2 1.1-.5-1.8c1.5-.9 2.3-2.3 2.3-3.9Zm-7.6-.9a.8.8 0 1 1 0 1.6.8.8 0 0 1 0-1.6Zm3.6 0a.8.8 0 1 1 0 1.6.8.8 0 0 1 0-1.6Z"/></svg>`
const googleIcon = `<svg viewBox="0 0 24 24" width="20" height="20"><text x="12" y="17" font-size="17" font-family="Arial, sans-serif" font-weight="700" text-anchor="middle" fill="#4285F4">G</text></svg>`

let timer = null
function flash(msg) {
  toastMsg.value = msg
  clearTimeout(timer)
  timer = setTimeout(() => (toastMsg.value = ''), 2600)
}

function togglePwd() { showPwd.value = !showPwd.value }

function switchMode(m) {
  mode.value = m
  Object.keys(errors).forEach(k => (errors[k] = ''))
  Object.keys(passed).forEach(k => (passed[k] = false))
}

async function onLogin() {
  if (!account.value.trim() || !password.value) {
    flash('请填写账号和密码')
    return
  }
  if (loggingIn.value) return
  loggingIn.value = true
  try {
    const res = await loginUser({ account: account.value.trim(), password: password.value })
    const user = (res && res.data) || { username: account.value.trim() }
    flash(`登录成功，欢迎回来 ${user.username} ✦`)
    emit('login-success', { user, remember: remember.value })
  } catch (e) {
    flash(e.message || '登录失败，请稍后重试')
  } finally {
    loggingIn.value = false
  }
}

/* ---------------- 注册校验 ---------------- */

// 密码：1-18 位，不允许空格
const PWD_RE = /^\S{1,18}$/

function setError(name, msg) {
  errors[name] = msg
  if (name in passed) passed[name] = false
  return false
}
function setPass(name) {
  errors[name] = ''
  if (name in passed) passed[name] = true
  return true
}

function validateField(name) {
  switch (name) {
    case 'username': {
      const v = username.value.trim()
      if (!v) return setError(name, '请填写用户名')
      if (v.length < 2 || v.length > 20) return setError(name, '用户名需为 2-20 个字符')
      return setPass(name)
    }
    case 'code': {
      const v = code.value.trim()
      if (!v) return setError(name, '请填写验证码')
      if (!/^\d{6}$/.test(v)) return setError(name, '验证码为 6 位数字')
      return setPass(name)
    }
    case 'password': {
      const v = password.value
      if (!v) return setError(name, '请设置密码')
      if (!PWD_RE.test(v)) return setError(name, '密码需为 1-18 位，且不能包含空格')
      return setPass(name)
    }
    case 'confirm': {
      if (!confirm.value) return setError(name, '请再次输入密码')
      if (confirm.value !== password.value) return setError(name, '两次输入的密码不一致')
      return setPass(name)
    }
    case 'agree': {
      if (!agree.value) return setError(name, '请先阅读并勾选用户协议')
      errors.agree = ''
      return true
    }
  }
  return true
}

function validateAll() {
  // 依次校验并在第一个错误处停下聚焦提示
  const order = ['username', 'code', 'password', 'confirm', 'agree']
  let ok = true
  for (const name of order) {
    if (!validateField(name)) ok = false
  }
  return ok
}

function clearError(name) {
  errors[name] = ''
  if (name in passed) passed[name] = false
}

function fieldClass(name) {
  if (errors[name]) return 'invalid'
  if (passed[name]) return 'valid'
  return ''
}

/* ---------------- 密码强度 ---------------- */

const strengthScore = computed(() => {
  const p = password.value
  if (!p || !PWD_RE.test(p)) return 0
  let s = 1
  if (p.length >= 8) s++
  const kinds = [/[a-zA-Z]/, /\d/, /[^a-zA-Z0-9]/].filter(r => r.test(p)).length
  if (kinds >= 3 || (kinds >= 2 && p.length >= 10)) s++
  return Math.min(s, 3)
})
const strengthText = computed(() => ({ 1: '弱', 2: '中', 3: '强' }[strengthScore.value] || ''))

/* ---------------- 验证码 ---------------- */

// 只保留数字，最多 6 位
function onCodeInput(e) {
  code.value = e.target.value.replace(/\D/g, '').slice(0, 6)
  clearError('code')
}

function startCountdown() {
  countdown.value = 60
  clearInterval(sendTimer)
  sendTimer = setInterval(() => {
    countdown.value -= 1
    if (countdown.value <= 0) { clearInterval(sendTimer); sendTimer = null }
  }, 1000)
}

async function sendVerificationCode() {
  if (sending.value || countdown.value > 0) return
  if (!validateField('username')) return
  sending.value = true
  try {
    const res = await sendCode({ account: username.value.trim(), type: 'register' })
    isMock.value = false
    mockCode.value = ''
    // 后端会在 data.devCode 里返回验证码，便于开发联调
    const devCode = res && res.data && res.data.devCode
    flash(devCode ? `验证码已发送（演示：${devCode}）` : '验证码已发送 ✉')
    startCountdown()
  } catch (e) {
    if (!e.isNetwork) {
      // 后端返回的业务错误（如用户名已被使用），直接展示
      flash(e.message || '验证码发送失败，请稍后重试')
      return
    }
    // 后端未连接时进入演示模式：本地生成验证码，方便前端联调
    mockCode.value = String(Math.floor(100000 + Math.random() * 900000))
    isMock.value = true
    flash(`演示模式：本次验证码为 ${mockCode.value}`)
    startCountdown()
  } finally {
    sending.value = false
  }
}

/* ---------------- 注册提交 ---------------- */

async function onRegister() {
  if (submitting.value) return
  if (!validateAll()) {
    flash('请检查表单中标红的项')
    return
  }
  // 演示模式下本地校验验证码
  if (isMock.value && code.value.trim() !== mockCode.value) {
    setError('code', '验证码不正确，请重新输入')
    flash('验证码不正确')
    return
  }
  submitting.value = true
  try {
    await registerUser({
      username: username.value.trim(),
      password: password.value,
      code: code.value.trim()
    })
    flash('注册成功，欢迎加入粉金梦境 ✦')
    resetRegisterForm()
    switchMode('login')
  } catch (e) {
    if (e.isNetwork && isMock.value) {
      // 演示模式：后端不可用时直接视为注册成功
      flash('注册成功（演示模式），请登录 ✦')
      resetRegisterForm()
      switchMode('login')
    } else {
      flash(e.message || '注册失败，请稍后重试')
    }
  } finally {
    submitting.value = false
  }
}

function resetRegisterForm() {
  username.value = ''
  password.value = ''
  confirm.value = ''
  code.value = ''
  agree.value = false
  mockCode.value = ''
  isMock.value = false
  countdown.value = 0
  clearInterval(sendTimer)
  sendTimer = null
}

onBeforeUnmount(() => { if (sendTimer) clearInterval(sendTimer) })
</script>

<style scoped>
.stage {
  position: relative; z-index: 5;
  height: 100%;
  display: flex; flex-direction: column;
  align-items: center; justify-content: center;
  text-align: center; padding: 24px;
  overflow-y: auto;
}

.card {
  width: min(380px, 92vw);
  background: var(--card-bg);
  border: 1px solid var(--card-border);
  border-radius: 26px;
  padding: 34px 36px 30px;
  backdrop-filter: blur(10px);
  box-shadow: 0 18px 50px rgba(214, 158, 184, .3);
  animation: floaty 7s ease-in-out infinite;
  margin: auto;
}
@keyframes floaty {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-8px); }
}

.brand {
  width: 54px; height: 54px; margin: 0 auto 14px;
  border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  background: linear-gradient(135deg, #ffc6dd, #f7d98a);
  box-shadow: 0 8px 22px rgba(233, 184, 95, .4);
}
.brand svg { width: 26px; height: 26px; }

.title {
  font-size: 30px; font-weight: 600; letter-spacing: .14em;
  background: linear-gradient(120deg, #c98aa6, #e9b85f 60%, #c98aa6);
  -webkit-background-clip: text; background-clip: text; color: transparent;
}
.subtitle { margin-top: 8px; font-size: 13px; letter-spacing: .22em; color: var(--sub); }

.form { margin-top: 22px; text-align: left; }

.field-wrap { margin-bottom: 12px; }

.field {
  display: flex; align-items: center; gap: 10px;
  background: var(--field-bg);
  border: 1px solid var(--field-border);
  border-radius: 14px;
  padding: 12px 14px;
  transition: border-color .25s, box-shadow .25s, background .25s;
}
.form > .field { margin-bottom: 14px; }
.field:focus-within {
  border-color: var(--gold-2);
  background: var(--field-bg);
  box-shadow: 0 0 0 3px rgba(247, 217, 138, .3);
}
.field.invalid {
  border-color: #e58a97;
  box-shadow: 0 0 0 3px rgba(229, 138, 151, .18);
}
.field.valid { border-color: rgba(143, 208, 160, .8); }
.field .ico { width: 18px; height: 18px; flex: 0 0 auto; color: #d49bb4; }
.field input {
  flex: 1; border: 0; outline: 0; background: transparent;
  font-size: 15px; color: var(--input-text); font-family: inherit; min-width: 0;
}
.field input::placeholder { color: var(--placeholder); }

.hint {
  margin: 5px 4px 0;
  font-size: 12px; line-height: 1.4; letter-spacing: .03em;
  color: #d06a78;
}
.hint.ok { color: #6faf82; }

.pwd-count { flex: 0 0 auto; font-size: 11px; color: var(--placeholder); letter-spacing: .03em; }

/* 密码强度条 */
.strength { display: flex; align-items: center; gap: 10px; margin: 7px 4px 0; }
.strength-bars { display: flex; gap: 5px; flex: 1; }
.strength-bars .bar {
  height: 4px; flex: 1; border-radius: 4px;
  background: var(--field-border);
  transition: background .3s;
}
.strength-bars .bar.on.lv1 { background: #f0a0a8; }
.strength-bars .bar.on.lv2 { background: #e9b85f; }
.strength-bars .bar.on.lv3 { background: #8fd0a0; }
.strength-text { font-size: 12px; letter-spacing: .1em; }
.strength-text.lv1 { color: #d06a78; }
.strength-text.lv2 { color: #d9a844; }
.strength-text.lv3 { color: #6faf82; }

.eye { display: flex; align-items: center; justify-content: center; border: 0; background: transparent; cursor: pointer; color: #d49bb4; padding: 2px; transition: color .2s; }
.eye:hover { color: #e9b85f; }
.eye :deep(svg) { width: 18px; height: 18px; }

.code-field { padding-right: 10px; }
.code-btn {
  flex: 0 0 auto; border: 0; cursor: pointer; white-space: nowrap;
  font-family: inherit; font-size: 12px; letter-spacing: .05em; color: #fff;
  padding: 7px 12px; border-radius: 10px;
  background: linear-gradient(120deg, #f0a9c4, #e9b85f);
  box-shadow: 0 6px 14px rgba(233, 184, 95, .35);
  transition: filter .2s, opacity .2s;
}
.code-btn:hover:not(:disabled) { filter: brightness(1.05); }
.code-btn:disabled { opacity: .55; cursor: not-allowed; }

.agree {
  display: flex; align-items: flex-start; gap: 8px;
  font-size: 12.5px; color: var(--muted); cursor: pointer;
  margin: 2px 2px 0; line-height: 1.5;
}
.agree input { accent-color: #e9b85f; width: 15px; height: 15px; margin-top: 1px; flex: 0 0 auto; }

.row { display: flex; align-items: center; justify-content: space-between; font-size: 13px; margin: 2px 2px 20px; }
.remember { display: flex; align-items: center; gap: 7px; color: var(--muted); cursor: pointer; }
.remember input { accent-color: #e9b85f; width: 15px; height: 15px; }
.link { color: var(--link); text-decoration: none; transition: color .2s; }
.link:hover { color: #e9b85f; text-decoration: underline; }

.btn {
  width: 100%; border: 0; cursor: pointer;
  padding: 13px; border-radius: 14px;
  font-size: 16px; letter-spacing: .3em; font-family: inherit; color: #fff;
  background: linear-gradient(120deg, #f0a9c4, #e9b85f);
  box-shadow: 0 10px 24px rgba(233, 184, 95, .4);
  transition: transform .15s, box-shadow .25s, filter .25s, opacity .2s;
}
.btn:hover:not(:disabled) { transform: translateY(-2px); filter: brightness(1.04); box-shadow: 0 14px 30px rgba(233, 184, 95, .5); }
.btn:active:not(:disabled) { transform: translateY(0); }
.btn:disabled { opacity: .6; cursor: not-allowed; }

.social-sep {
  display: flex; align-items: center; gap: 10px;
  margin: 18px 0 12px; color: var(--muted);
  font-size: 12px; letter-spacing: .1em;
}
.social-sep::before, .social-sep::after { content: ""; flex: 1; height: 1px; background: var(--field-border); }
.social { display: flex; align-items: center; justify-content: center; gap: 16px; }
.social-btn {
  width: 44px; height: 44px; border-radius: 50%;
  display: flex; align-items: center; justify-content: center; cursor: pointer;
  background: var(--field-bg); border: 1px solid var(--field-border);
  transition: transform .2s, box-shadow .2s;
}
.social-btn:hover { transform: translateY(-2px); box-shadow: 0 8px 18px rgba(0, 0, 0, .12); }

.alt { margin-top: 18px; text-align: center; font-size: 13px; color: var(--muted); }

.toast {
  position: fixed; left: 50%; bottom: 36px;
  transform: translateX(-50%) translateY(20px);
  background: rgba(201, 138, 166, .95); color: #fff;
  padding: 11px 22px; border-radius: 30px;
  font-size: 14px; letter-spacing: .05em; opacity: 0; pointer-events: none;
  transition: opacity .3s, transform .3s; z-index: 9;
  box-shadow: 0 10px 30px rgba(201, 138, 166, .4);
  max-width: 90vw; text-align: center;
}
.toast.show { opacity: 1; transform: translateX(-50%) translateY(0); }

@media (prefers-reduced-motion: reduce) {
  .card { animation: none; }
}
</style>
