// 统一后端接口封装
// 接口地址可通过环境变量覆盖：在 .env 中配置 VITE_API_BASE=https://your-domain/api
const BASE = import.meta.env.VITE_API_BASE || '/api'

// 读取当前登录会话（记住我 → localStorage，否则 → sessionStorage）
export function getSession() {
  const raw = localStorage.getItem('dream-user') || sessionStorage.getItem('dream-user')
  try { return raw ? JSON.parse(raw) : null } catch (_) { return null }
}

// 当前登录用户ID（未登录返回 undefined，后端据此跳过点赞状态查询）
function currentUserId() {
  const session = getSession()
  return session && session.id ? session.id : undefined
}

async function request(path, payload) {
  const session = getSession()
  const headers = { 'Content-Type': 'application/json' }
  // 携带 JWT token（登录后接口鉴权）
  if (session && session.token) {
    headers.Authorization = `Bearer ${session.token}`
  }
  let res
  try {
    res = await fetch(`${BASE}${path}`, {
      method: 'POST',
      headers,
      body: JSON.stringify(payload)
    })
  } catch (e) {
    // 后端未启动 / 网络不通：标记为网络错误，供调用方降级处理
    const err = new Error('无法连接服务器')
    err.isNetwork = true
    throw err
  }
  let data = null
  try { data = await res.json() } catch (_) { /* 无响应体 */ }
  if (!res.ok) {
    const err = new Error((data && data.message) || `请求失败 (${res.status})`)
    if (res.status === 401) err.isUnauthorized = true
    throw err
  }
  return data
}

/* ==================== 登录注册 ==================== */

// 发送验证码（注册场景，基于用户名）
export function sendCode({ account, type = 'register' }) {
  return request('/send-code', { account, type })
}

// 注册
export function registerUser({ username, password, code }) {
  return request('/register', { username, password, code })
}

// 登录（成功返回 data.token）
export function loginUser({ account, password }) {
  return request('/login', { account, password })
}

// 获取当前登录用户信息（受保护接口，需已登录）
export function getUserInfo() {
  return request('/user-info', {})
}

/* ==================== 首页展示 ==================== */

// 首页聚合数据：统计 + 分类导航 + 热门搜索词 + 热门推荐 + 最新上架
// 返回 data = { stats, categories, hotKeywords, hotDreams, latestDreams }
export function fetchHomeOverview() {
  return request('/home/overview', { userId: currentUserId() })
}

// 首页内容流（分页）
// 参数：{ page, size, keyword, category, sort }  sort: newest | hot
// 返回 data = { list, total, page, size, pages, hasMore }
export function fetchHomeFeed({ page = 1, size = 8, keyword = '', category = '', sort = 'newest' } = {}) {
  return request('/home/feed', { page, size, keyword, category, sort, userId: currentUserId() })
}

// 分类导航（含各分类内容数量）
export function fetchCategories() {
  return request('/home/categories', {})
}

// 热门搜索词
export function fetchHotKeywords() {
  return request('/home/hot-keywords', {})
}

/* ==================== 搜索 ==================== */

// 首页搜索：POST /api/search，返回 data = { list, total, page, size, pages, hasMore }
export function searchDreams({ keyword, page = 1, size = 12, category = '', sort = 'newest' } = {}) {
  return request('/search', { keyword, page, size, category, sort, userId: currentUserId() })
}

// 兼容旧调用名（等价于 searchDreams）
export function searchAll({ keyword, page = 1, size = 20 } = {}) {
  return searchDreams({ keyword, page, size })
}

/* ==================== 详情 / 点赞 / 发布 ==================== */

// 梦境详情（浏览量 +1）
export function fetchDreamDetail({ id }) {
  return request('/dream/detail', { id, userId: currentUserId() })
}

// 点赞 / 取消点赞：action = like | unlike | toggle（需登录，身份取自 token）
export function toggleDreamLike({ id, action = 'toggle' }) {
  return request('/dream/like', { id, userId: currentUserId(), action })
}

// 收藏 / 取消收藏：action = favorite | unfavorite | toggle（需登录）
export function toggleDreamFavorite({ id, action = 'toggle' }) {
  return request('/dream/favorite', { id, userId: currentUserId(), action })
}

// 我的收藏（需登录）：返回 { list, total, page, size, pages, hasMore }
export function fetchMyFavorites({ page = 1, size = 8 } = {}) {
  return request('/user/favorites', { page, size })
}

// 发布梦境（供「记录梦境」页面使用）
export function publishDream(dream) {
  return request('/dream/publish', { ...dream, userId: currentUserId() })
}

/* ==================== 创作闭环 / 个人中心 ==================== */

// 编辑自己发布的梦境（需登录，非本人的内容后端会拒绝）
export function updateDream({ id, title, summary, content, coverUrl, category, tags }) {
  return request('/dream/update', { id, title, summary, content, coverUrl, category, tags })
}

// 删除自己发布的梦境（需登录，后端为逻辑删除）
export function deleteDream({ id }) {
  return request('/dream/delete', { id })
}

// 我的梦境（需登录）：只返回自己发布的内容，带正文，可直接打开编辑
export function fetchMyDreams({ page = 1, size = 8, keyword = '', sort = 'newest' } = {}) {
  return request('/user/dreams', { page, size, keyword, sort })
}

// 个人中心统计：{ userId, username, dreamCount, viewCount, likeCount, favoriteCount }
export function fetchUserProfile() {
  return request('/user/profile', {})
}
