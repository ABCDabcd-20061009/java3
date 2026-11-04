<template>
  <div class="dash">
    <!-- ===== 顶栏 ===== -->
    <header class="topbar">
      <div class="brand">
        <svg viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
          <path d="M12 3l2.2 4.6L19 8.4l-3.6 3.3.9 4.9L12 14.4 7.7 16.6l.9-4.9L5 8.4l4.8-.8z" />
        </svg>
        <span>粉金梦境 · 管理后台</span>
      </div>
      <div class="top-user">
        <span class="uname">{{ admin.username }}</span>
        <button class="ghost" type="button" @click="$emit('exit-admin')">退出后台</button>
      </div>
    </header>

    <div class="layout">
      <!-- ===== 侧边导航 ===== -->
      <nav class="side">
        <button v-for="t in tabs" :key="t.key" type="button" class="side-btn"
                :class="{ active: tab === t.key }" @click="switchTab(t.key)">
          {{ t.label }}
        </button>
      </nav>

      <!-- ===== 主区域 ===== -->
      <main class="main">
        <!-- 概览 -->
        <section v-if="tab === 'overview'">
          <h2 class="sec-title">数据概览</h2>
          <div class="stat-grid" v-if="stats">
            <div class="stat" v-for="s in statList" :key="s.label">
              <span class="num">{{ s.value }}</span>
              <span class="label">{{ s.label }}</span>
            </div>
          </div>
          <div class="empty" v-else>加载中…</div>
        </section>

        <!-- 通用列表（梦境 / 广播剧 / 用户 / 搜索日志 / 点赞 / 收藏） -->
        <section v-else-if="listTabs.includes(tab)">
          <div class="sec-head">
            <h2 class="sec-title">{{ tabLabel }}</h2>
            <div class="tools">
              <input class="search" v-if="showSearch" type="text" v-model="keyword"
                     :placeholder="'按' + (tab === 'logs' ? '关键词' : '标题/名称') + '搜索'" @keyup.enter="reload" />
              <button class="ghost" type="button" v-if="tab === 'logs'" @click="clearLogs">清空日志</button>
              <button class="primary" type="button" v-if="tab === 'dreams'" @click="openEditor('dream', null)">+ 新增梦境</button>
              <button class="primary" type="button" v-if="tab === 'dramas'" @click="openEditor('drama', null)">+ 新增广播剧</button>
              <button class="ghost" type="button" @click="reload">刷新</button>
            </div>
          </div>

          <div class="table-wrap">
            <table class="tbl">
              <thead>
                <tr><th v-for="c in columns" :key="c.key" :style="c.w ? { width: c.w } : null">{{ c.label }}</th></tr>
              </thead>
              <tbody>
                <tr v-for="(row, i) in rows" :key="row.id || i">
                  <td v-for="c in columns" :key="c.key" :class="c.cls">
                    <template v-if="c.key === 'status'">
                      <span class="badge" :class="row.status === 1 ? 'on' : 'off'">{{ row.status === 1 ? '已发布' : '未发布' }}</span>
                    </template>
                    <template v-else-if="c.key === 'deleted'">
                      <span class="badge" :class="row.deleted ? 'off' : 'on'">{{ row.deleted ? '已注销' : '正常' }}</span>
                    </template>
                    <template v-else-if="c.key === 'ops'">
                      <template v-if="tab === 'dreams'">
                        <button class="mini" type="button" @click="openEditor('dream', row)">编辑</button>
                        <button class="mini danger" type="button" @click="removeRow(row)">删除</button>
                      </template>
                      <template v-else-if="tab === 'dramas'">
                        <button class="mini" type="button" @click="openEditor('drama', row)">编辑</button>
                        <button class="mini danger" type="button" @click="removeRow(row)">删除</button>
                      </template>
                      <template v-else-if="tab === 'users'">
                        <button class="mini danger" type="button" :disabled="!!row.deleted" @click="removeUser(row)">注销</button>
                      </template>
                    </template>
                    <template v-else>{{ row[c.key] }}</template>
                  </td>
                </tr>
              </tbody>
            </table>
            <div class="empty" v-if="!rows.length && !loading">暂无数据</div>
          </div>

          <div class="pager" v-if="total > 0">
            <span class="total">共 {{ total }} 条 · 第 {{ page }}/{{ pages }} 页</span>
            <button class="ghost" type="button" :disabled="page <= 1" @click="goPage(page - 1)">上一页</button>
            <button class="ghost" type="button" :disabled="!hasMore" @click="goPage(page + 1)">下一页</button>
          </div>
        </section>

        <!-- 分类管理 -->
        <section v-else-if="tab === 'categories'">
          <div class="sec-head">
            <h2 class="sec-title">分类管理</h2>
            <div class="tools">
              <div class="seg">
                <button type="button" :class="{ on: catType === 'dream' }" @click="switchCatType('dream')">梦境分类</button>
                <button type="button" :class="{ on: catType === 'drama' }" @click="switchCatType('drama')">广播剧分类</button>
              </div>
              <button class="primary" type="button" @click="openEditor('category', null)">+ 新增分类</button>
            </div>
          </div>
          <div class="table-wrap">
            <table class="tbl">
              <thead><tr><th style="width:60px">ID</th><th>编码</th><th>名称</th><th>图标</th><th style="width:70px">排序</th><th style="width:90px">状态</th><th style="width:150px">操作</th></tr></thead>
              <tbody>
                <tr v-for="c in catRows" :key="c.id">
                  <td>{{ c.id }}</td>
                  <td>{{ c.code }}</td>
                  <td>{{ c.name }}</td>
                  <td>{{ c.icon || '—' }}</td>
                  <td>{{ c.sortNo }}</td>
                  <td><span class="badge" :class="c.status === 1 ? 'on' : 'off'">{{ c.status === 1 ? '启用' : '停用' }}</span></td>
                  <td class="ops">
                    <button class="mini" type="button" @click="openEditor('category', c)">编辑</button>
                    <button class="mini danger" type="button" @click="removeCategory(c)">删除</button>
                  </td>
                </tr>
              </tbody>
            </table>
            <div class="empty" v-if="!catRows.length && !loading">暂无分类</div>
          </div>
        </section>

        <!-- 首页配置 -->
        <section v-else-if="tab === 'homecfg'">
          <div class="sec-head">
            <h2 class="sec-title">首页配置</h2>
            <div class="tools">
              <div class="seg">
                <button v-for="t in cfgTypes" :key="t.value" type="button"
                        :class="{ on: cfgType === t.value }" @click="switchCfgType(t.value)">{{ t.label }}</button>
              </div>
              <button class="primary" type="button" v-if="cfgType !== 'site_text'" @click="openEditor('sitecfg', null)">+ 新增配置项</button>
            </div>
          </div>
          <p class="tip" v-if="cfgType === 'site_text'">站点文案是首页强依赖的固定键（{{ cfgTextKeys.join(' / ') }}），只能修改内容或停用，不能删除。</p>
          <div class="table-wrap">
            <table class="tbl">
              <thead><tr><th style="width:60px">ID</th><th>键</th><th>主文案</th><th>副文案/图标</th><th>跳转</th><th style="width:70px">排序</th><th style="width:90px">状态</th><th style="width:150px">操作</th></tr></thead>
              <tbody>
                <tr v-for="c in cfgRows" :key="c.id">
                  <td>{{ c.id }}</td>
                  <td>{{ c.configKey }}</td>
                  <td>{{ c.label }}</td>
                  <td>{{ c.subLabel || c.iconKey || '—' }}</td>
                  <td>{{ c.target || '—' }}</td>
                  <td>{{ c.sortNo }}</td>
                  <td><span class="badge" :class="c.status === 1 ? 'on' : 'off'">{{ c.status === 1 ? '启用' : '停用' }}</span></td>
                  <td class="ops">
                    <button class="mini" type="button" @click="openEditor('sitecfg', c)">编辑</button>
                    <button class="mini danger" type="button" v-if="cfgType !== 'site_text'" @click="removeSiteCfg(c)">删除</button>
                  </td>
                </tr>
              </tbody>
            </table>
            <div class="empty" v-if="!cfgRows.length && !loading">暂无配置</div>
          </div>
        </section>
      </main>
    </div>

    <!-- ===== 编辑弹层（梦境 / 广播剧 / 分类 / 首页配置共用） ===== -->
    <div class="mask" v-if="editing" @click.self="closeEditor">
      <div class="modal">
        <h3 class="modal-title">{{ editing.id ? '编辑' : '新增' }} {{ editorTitles[editing.kind] }}</h3>
        <div class="form">
          <label class="f" v-for="f in editorFields" :key="f.key">
            <span class="f-label">{{ f.label }}</span>
            <textarea v-if="f.type === 'textarea'" class="f-input" rows="4" v-model="editing.form[f.key]"></textarea>
            <select v-else-if="f.type === 'select'" class="f-input" v-model="editing.form[f.key]">
              <option v-for="o in f.options" :key="o.value" :value="o.value">{{ o.label }}</option>
            </select>
            <input v-else class="f-input" :type="f.type || 'text'" v-model="editing.form[f.key]" :placeholder="f.placeholder || ''" />
          </label>
        </div>
        <div class="error" v-if="editorError">{{ editorError }}</div>
        <div class="modal-foot">
          <button class="ghost" type="button" @click="closeEditor">取消</button>
          <button class="primary" type="button" :disabled="saving" @click="saveEditor">{{ saving ? '保存中…' : '保 存' }}</button>
        </div>
      </div>
    </div>

    <!-- ===== 轻提示 ===== -->
    <div class="toast" v-if="toastMsg">{{ toastMsg }}</div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import {
  adminOverview, adminListDreams, adminSaveDream, adminDeleteDream,
  adminListDramas, adminSaveDrama, adminDeleteDrama,
  adminListCategories, adminSaveCategory, adminDeleteCategory,
  adminListUsers, adminDeleteUser,
  adminListSearchLogs, adminClearSearchLogs,
  adminListLikes, adminListFavorites,
  adminListSiteConfig, adminSaveSiteConfig, adminDeleteSiteConfig,
  adminInfo
} from '../api'

const props = defineProps({
  admin: { type: Object, required: true }
})
const emit = defineEmits(['exit-admin', 'admin-session-expired'])

/* ---- 标签页 ---- */
const tabs = [
  { key: 'overview', label: '数据概览' },
  { key: 'dreams', label: '梦境管理' },
  { key: 'dramas', label: '广播剧' },
  { key: 'categories', label: '分类管理' },
  { key: 'users', label: '用户' },
  { key: 'logs', label: '搜索日志' },
  { key: 'interact', label: '互动数据' },
  { key: 'homecfg', label: '首页配置' }
]
const tab = ref('overview')
const tabLabel = computed(() => {
  const t = tabs.find(t => t.key === tab.value)
  if (t) return t.label
  return interactTab.value === 'likes' ? '点赞记录' : '收藏记录'
})
const listTabs = ['dreams', 'dramas', 'users', 'logs', 'interact']
const showSearch = computed(() => ['dreams', 'dramas', 'users', 'logs'].includes(tab.value))

/* ---- 轻提示 ---- */
const toastMsg = ref('')
let timer = null
function flash(msg) {
  toastMsg.value = msg
  clearTimeout(timer)
  timer = setTimeout(() => (toastMsg.value = ''), 2600)
}

/* ---- 请求包装：401 时通知父组件清会话回登录页 ---- */
async function guard(fn) {
  try {
    return await fn()
  } catch (e) {
    if (e && e.isUnauthorized) {
      emit('admin-session-expired')
      return null
    }
    throw e
  }
}

/* ==================== 概览 ==================== */
const stats = ref(null)
const statList = computed(() => {
  const s = stats.value || {}
  return [
    { label: '梦境总数', value: s.dreamCount ?? 0 },
    { label: '已发布梦境', value: s.dreamPublishedCount ?? 0 },
    { label: '广播剧', value: s.dramaCount ?? 0 },
    { label: '注册用户', value: s.userCount ?? 0 },
    { label: '累计浏览', value: s.viewCount ?? 0 },
    { label: '累计点赞', value: s.likeCount ?? 0 },
    { label: '累计收藏', value: s.favoriteCount ?? 0 },
    { label: '搜索次数', value: s.searchCount ?? 0 }
  ]
})
async function loadOverview() {
  try {
    const res = await guard(() => adminOverview())
    if (res) stats.value = res.data
  } catch (e) {
    flash(e.message || '加载失败')
  }
}

/* ==================== 通用列表 ==================== */
const rows = ref([])
const total = ref(0)
const page = ref(1)
const pages = ref(1)
const hasMore = ref(false)
const loading = ref(false)
const keyword = ref('')
const interactTab = ref('likes')   // interact 视图里看点赞还是收藏

const SIZE = 10

const columns = computed(() => {
  switch (tab.value) {
    case 'dreams':
      return [
        { key: 'id', label: 'ID', w: '60px' },
        { key: 'title', label: '标题' },
        { key: 'category', label: '分类', w: '90px' },
        { key: 'authorName', label: '作者', w: '110px' },
        { key: 'status', label: '状态', w: '90px' },
        { key: 'views', label: '浏览', w: '70px' },
        { key: 'createTime', label: '发布时间', w: '150px' },
        { key: 'ops', label: '操作', w: '150px' }
      ]
    case 'dramas':
      return [
        { key: 'id', label: 'ID', w: '60px' },
        { key: 'title', label: '标题' },
        { key: 'category', label: '分类', w: '80px' },
        { key: 'platform', label: '平台', w: '110px' },
        { key: 'status', label: '状态', w: '90px' },
        { key: 'views', label: '浏览', w: '70px' },
        { key: 'createTime', label: '发布时间', w: '150px' },
        { key: 'ops', label: '操作', w: '150px' }
      ]
    case 'users':
      return [
        { key: 'id', label: 'ID', w: '60px' },
        { key: 'username', label: '用户名' },
        { key: 'deleted', label: '状态', w: '90px' },
        { key: 'dreamCount', label: '梦境数', w: '80px' },
        { key: 'createTime', label: '注册时间', w: '150px' },
        { key: 'ops', label: '操作', w: '120px' }
      ]
    case 'logs':
      return [
        { key: 'id', label: 'ID', w: '70px' },
        { key: 'keyword', label: '关键词' },
        { key: 'userId', label: '用户ID', w: '80px' },
        { key: 'resultCount', label: '命中', w: '70px' },
        { key: 'createTime', label: '时间', w: '150px' }
      ]
    case 'interact':
      return interactTab.value === 'likes'
        ? [
            { key: 'id', label: 'ID', w: '70px' },
            { key: 'dreamTitle', label: '梦境' },
            { key: 'username', label: '用户', w: '130px' },
            { key: 'createTime', label: '时间', w: '150px' }
          ]
        : [
            { key: 'id', label: 'ID', w: '70px' },
            { key: 'dreamTitle', label: '梦境' },
            { key: 'username', label: '用户', w: '130px' },
            { key: 'createTime', label: '时间', w: '150px' }
          ]
    default:
      return []
  }
})

async function loadList() {
  loading.value = true
  try {
    const params = { page: page.value, size: SIZE }
    if (keyword.value.trim()) params.keyword = keyword.value.trim()
    let res = null
    if (tab.value === 'dreams') {
      res = await guard(() => adminListDreams(params))
    } else if (tab.value === 'dramas') {
      res = await guard(() => adminListDramas(params))
    } else if (tab.value === 'users') {
      res = await guard(() => adminListUsers(params))
    } else if (tab.value === 'logs') {
      res = await guard(() => adminListSearchLogs(params))
    } else if (tab.value === 'interact') {
      res = interactTab.value === 'likes'
        ? await guard(() => adminListLikes({ page: page.value, size: SIZE }))
        : await guard(() => adminListFavorites({ page: page.value, size: SIZE }))
    }
    if (!res) return
    const data = res.data || {}
    rows.value = data.list || []
    total.value = data.total || 0
    page.value = data.page || 1
    pages.value = data.pages || 1
    hasMore.value = !!data.hasMore
  } catch (e) {
    flash(e.message || '加载失败')
  } finally {
    loading.value = false
  }
}

function reload() {
  page.value = 1
  if (tab.value === 'overview') loadOverview()
  else if (tab.value === 'categories') loadCategories()
  else if (tab.value === 'homecfg') loadSiteCfg()
  else loadList()
}

function goPage(p) {
  page.value = p
  loadList()
}

function switchTab(key) {
  tab.value = key
  keyword.value = ''
  rows.value = []
  total.value = 0
  page.value = 1
  if (key === 'overview') loadOverview()
  else if (key === 'categories') loadCategories()
  else if (key === 'homecfg') loadSiteCfg()
  else if (key === 'interact') { interactTab.value = 'likes'; loadList() }
  else loadList()
}

/* ==================== 行内操作 ==================== */
const editorTitles = { dream: '梦境', drama: '广播剧', category: '分类', sitecfg: '首页配置' }

const DREAM_FIELDS = [
  { key: 'title', label: '标题（2-100 字）' },
  { key: 'category', label: '分类（美梦/噩梦/灵感/随笔）' },
  { key: 'tags', label: '标签（逗号分隔，最多 8 个）' },
  { key: 'coverUrl', label: '封面图URL（可留空）' },
  { key: 'summary', label: '摘要（留空自动取正文前 60 字）', type: 'textarea' },
  { key: 'content', label: '正文（必填，≤5000 字）', type: 'textarea' },
  { key: 'status', label: '状态', type: 'select', options: [{ value: 1, label: '发布' }, { value: 0, label: '草稿' }] }
]
const DRAMA_FIELDS = [
  { key: 'title', label: '标题（2-100 字）' },
  { key: 'category', label: '分类（悬疑/言情/科幻/治愈/古风/儿童）' },
  { key: 'platform', label: '来源平台（喜马拉雅/猫耳FM…）' },
  { key: 'tags', label: '标签（逗号分隔）' },
  { key: 'duration', label: '时长/集数（如 12:34）' },
  { key: 'coverUrl', label: '封面图URL（可留空）' },
  { key: 'videoUrl', label: '视频地址（主播放源，/media/drama/xxx.mp4）' },
  { key: 'audioUrl', label: '音频地址（无视频时回退）' },
  { key: 'posterUrl', label: '播放器封面URL（可留空）' },
  { key: 'summary', label: '简介', type: 'textarea' },
  { key: 'status', label: '状态', type: 'select', options: [{ value: 1, label: '发布' }, { value: 0, label: '下架' }] }
]
const CATEGORY_FIELDS = [
  { key: 'name', label: '分类名称' },
  { key: 'code', label: '编码（留空自动生成）' },
  { key: 'icon', label: '图标标识（前端映射 SVG）' },
  { key: 'sortNo', label: '排序号（越小越靠前）', type: 'number' },
  { key: 'status', label: '状态', type: 'select', options: [{ value: 1, label: '启用' }, { value: 0, label: '停用' }] }
]
const SITECFG_FIELDS = [
  { key: 'label', label: '主文案' },
  { key: 'subLabel', label: '副文案（快捷入口 desc）' },
  { key: 'iconKey', label: '图标标识（快捷入口）' },
  { key: 'target', label: '跳转目标（square/favorites/profile/drama/help）' },
  { key: 'sortNo', label: '排序号（越小越靠前）', type: 'number' },
  { key: 'status', label: '状态', type: 'select', options: [{ value: 1, label: '启用' }, { value: 0, label: '停用' }] }
]

const editing = ref(null)   // { kind, id, form }
const editorFields = computed(() => {
  if (!editing.value) return []
  switch (editing.value.kind) {
    case 'dream': return DREAM_FIELDS
    case 'drama': return DRAMA_FIELDS
    case 'category': return CATEGORY_FIELDS
    case 'sitecfg': return SITECFG_FIELDS
    default: return []
  }
})
const saving = ref(false)
const editorError = ref('')

function openEditor(kind, row) {
  editorError.value = ''
  if (kind === 'dream') {
    editing.value = { kind, id: row ? row.id : null, form: {
      title: row?.title || '', category: row?.category || '', tags: row?.tags || '',
      coverUrl: row?.coverUrl || '', summary: row?.summary || '', content: row?.content || '',
      status: row ? row.status : 1 } }
  } else if (kind === 'drama') {
    editing.value = { kind, id: row ? row.id : null, form: {
      title: row?.title || '', category: row?.category || '', platform: row?.platform || '',
      tags: row?.tags || '', duration: row?.duration || '', coverUrl: row?.coverUrl || '',
      videoUrl: row?.videoUrl || '', audioUrl: row?.audioUrl || '', posterUrl: row?.posterUrl || '',
      summary: row?.summary || '', status: row ? row.status : 1 } }
  } else if (kind === 'category') {
    editing.value = { kind, id: row ? row.id : null, form: {
      name: row?.name || '', code: row?.code || '', icon: row?.icon || '',
      sortNo: row ? row.sortNo : 99, status: row ? row.status : 1 } }
  } else if (kind === 'sitecfg') {
    editing.value = { kind, id: row ? row.id : null, form: {
      label: row?.label || '', subLabel: row?.subLabel || '', iconKey: row?.iconKey || '',
      target: row?.target || '', sortNo: row ? row.sortNo : 99, status: row ? row.status : 1 } }
  }
}

function closeEditor() {
  editing.value = null
}

async function saveEditor() {
  if (!editing.value || saving.value) return
  saving.value = true
  editorError.value = ''
  const { kind, id, form } = editing.value
  try {
    let res
    if (kind === 'dream') {
      res = await guard(() => adminSaveDream({ id, ...form }))
    } else if (kind === 'drama') {
      res = await guard(() => adminSaveDrama({ id, ...form }))
    } else if (kind === 'category') {
      res = await guard(() => adminSaveCategory({ id, type: catType.value, ...form }))
    } else if (kind === 'sitecfg') {
      res = await guard(() => adminSaveSiteConfig({ id, configType: cfgType.value, ...form }))
    }
    if (res) {
      editing.value = null
      flash('保存成功 ✦')
      if (kind === 'category') loadCategories()
      else if (kind === 'sitecfg') loadSiteCfg()
      else loadList()
    }
  } catch (e) {
    editorError.value = e.message || '保存失败'
  } finally {
    saving.value = false
  }
}

async function removeRow(row) {
  if (!confirm(`确认删除「${row.title}」？（逻辑删除，可恢复数据）`)) return
  try {
    const res = tab.value === 'dreams'
      ? await guard(() => adminDeleteDream({ id: row.id }))
      : await guard(() => adminDeleteDrama({ id: row.id }))
    if (res) {
      flash(res.message || '已删除')
      loadList()
    }
  } catch (e) {
    flash(e.message || '删除失败')
  }
}

async function removeUser(row) {
  if (!confirm(`确认注销用户「${row.username}」？注销后该用户无法登录。`)) return
  try {
    const res = await guard(() => adminDeleteUser({ id: row.id }))
    if (res) {
      flash(res.message || '已注销')
      loadList()
    }
  } catch (e) {
    flash(e.message || '操作失败')
  }
}

async function clearLogs() {
  if (!confirm('确认清空全部搜索日志？此操作不可恢复。')) return
  try {
    const res = await guard(() => adminClearSearchLogs())
    if (res) {
      flash(res.message || '已清空')
      loadList()
    }
  } catch (e) {
    flash(e.message || '操作失败')
  }
}

/* ==================== 分类管理 ==================== */
const catType = ref('dream')
const catRows = ref([])

async function loadCategories() {
  loading.value = true
  try {
    const res = await guard(() => adminListCategories({ type: catType.value }))
    if (res) catRows.value = res.data || []
  } catch (e) {
    flash(e.message || '加载失败')
  } finally {
    loading.value = false
  }
}

function switchCatType(t) {
  catType.value = t
  loadCategories()
}

async function removeCategory(c) {
  if (!confirm(`确认删除分类「${c.name}」？`)) return
  try {
    const res = await guard(() => adminDeleteCategory({ id: c.id, type: catType.value }))
    if (res) {
      flash(res.message || '已删除')
      loadCategories()
    }
  } catch (e) {
    flash(e.message || '删除失败')
  }
}

/* ==================== 首页配置 ==================== */
const cfgTypes = [
  { value: 'nav_menu', label: '导航菜单' },
  { value: 'shortcut', label: '快捷入口' },
  { value: 'platform', label: '广播剧平台' },
  { value: 'site_text', label: '站点文案' }
]
const cfgType = ref('nav_menu')
const cfgRows = ref([])
const cfgTextKeys = ['brand_name', 'hero_title', 'hero_sub', 'footer']

async function loadSiteCfg() {
  loading.value = true
  try {
    const res = await guard(() => adminListSiteConfig({ type: cfgType.value }))
    if (res) cfgRows.value = res.data || []
  } catch (e) {
    flash(e.message || '加载失败')
  } finally {
    loading.value = false
  }
}

function switchCfgType(t) {
  cfgType.value = t
  loadSiteCfg()
}

async function removeSiteCfg(c) {
  if (!confirm(`确认删除配置项「${c.label}」？删除后前台首页不再显示。`)) return
  try {
    const res = await guard(() => adminDeleteSiteConfig({ id: c.id }))
    if (res) {
      flash(res.message || '已删除')
      loadSiteCfg()
    }
  } catch (e) {
    flash(e.message || '删除失败')
  }
}

/* ==================== 生命周期 ==================== */
onMounted(async () => {
  // token 可能过期：进后台先校验一次，失败直接回登录页
  try {
    const res = await guard(() => adminInfo())
    if (!res) return
    loadOverview()
  } catch (e) {
    flash(e.message || '管理员状态校验失败')
    emit('admin-session-expired')
  }
})
</script>

<style scoped>
.dash {
  position: fixed; inset: 0;
  display: flex; flex-direction: column;
  background:
    radial-gradient(1000px 700px at 15% 8%, rgba(255, 198, 221, .5), transparent 60%),
    radial-gradient(900px 700px at 90% 90%, rgba(247, 217, 138, .4), transparent 60%),
    linear-gradient(160deg, #fff3f8 0%, #fff6ec 100%);
  color: var(--ink, #9a6b7d);
  overflow: hidden;
}
.topbar {
  display: flex; align-items: center; justify-content: space-between;
  padding: 14px 24px;
  background: var(--panel-bg, rgba(255, 252, 254, .92));
  border-bottom: 1px solid rgba(154, 107, 125, .12);
}
.brand { display: flex; align-items: center; gap: 10px; font-size: 16px; letter-spacing: 1px; }
.brand svg {
  width: 30px; height: 30px; padding: 6px;
  border-radius: 9px;
  background: linear-gradient(135deg, #f6a5c0, #f3c98b);
}
.top-user { display: flex; align-items: center; gap: 12px; font-size: 14px; }
.uname { color: var(--sub, #b88aa0); }

.layout { flex: 1; display: flex; min-height: 0; }
.side {
  width: 170px; flex: none;
  display: flex; flex-direction: column; gap: 6px;
  padding: 18px 14px;
  border-right: 1px solid rgba(154, 107, 125, .1);
}
.side-btn {
  text-align: left;
  border: none; background: transparent;
  padding: 10px 14px;
  border-radius: 12px;
  font-size: 14px;
  color: var(--sub, #b88aa0);
  cursor: pointer;
  transition: background .15s, color .15s;
}
.side-btn:hover { background: rgba(246, 165, 192, .12); }
.side-btn.active {
  background: linear-gradient(135deg, #f6a5c0, #f3c98b);
  color: #fff;
  box-shadow: 0 6px 14px rgba(246, 165, 192, .35);
}
.main { flex: 1; overflow: auto; padding: 22px 26px; }
.sec-title { font-size: 18px; margin: 0 0 16px; letter-spacing: 1px; }
.sec-head { display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap; gap: 10px; margin-bottom: 16px; }
.sec-head .sec-title { margin: 0; }
.tools { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
.search {
  height: 34px;
  border: 1px solid rgba(154, 107, 125, .25);
  border-radius: 10px;
  padding: 0 12px;
  background: rgba(255, 255, 255, .7);
  color: var(--ink, #9a6b7d);
  outline: none;
  font-size: 13px;
}
button.primary {
  height: 34px; padding: 0 16px;
  border: none; border-radius: 10px;
  color: #fff; font-size: 13px; cursor: pointer;
  background: linear-gradient(135deg, #f394b8, #eeb878);
  box-shadow: 0 5px 12px rgba(243, 148, 184, .35);
}
button.primary:disabled { opacity: .7; cursor: not-allowed; }
button.ghost {
  height: 34px; padding: 0 14px;
  border: 1px solid rgba(154, 107, 125, .3);
  border-radius: 10px;
  background: rgba(255, 255, 255, .6);
  color: var(--sub, #b88aa0);
  font-size: 13px; cursor: pointer;
}
button.ghost:disabled { opacity: .5; cursor: not-allowed; }
.seg { display: flex; border: 1px solid rgba(154, 107, 125, .25); border-radius: 10px; overflow: hidden; }
.seg button {
  border: none; background: rgba(255, 255, 255, .6);
  padding: 8px 14px; font-size: 13px;
  color: var(--sub, #b88aa0); cursor: pointer;
}
.seg button.on { background: linear-gradient(135deg, #f6a5c0, #f3c98b); color: #fff; }

.table-wrap {
  background: var(--panel-bg, rgba(255, 252, 254, .92));
  border: 1px solid rgba(154, 107, 125, .12);
  border-radius: 14px;
  overflow: auto;
}
.tbl { width: 100%; border-collapse: collapse; font-size: 13px; }
.tbl th, .tbl td { text-align: left; padding: 10px 12px; border-bottom: 1px solid rgba(154, 107, 125, .08); }
.tbl th { color: var(--sub, #b88aa0); font-weight: 600; white-space: nowrap; }
.tbl td { color: var(--ink, #9a6b7d); word-break: break-all; }
.tbl td.ops { white-space: nowrap; }
.badge { font-size: 12px; padding: 2px 8px; border-radius: 999px; }
.badge.on { background: rgba(126, 200, 152, .18); color: #3d8f5f; }
.badge.off { background: rgba(210, 92, 114, .12); color: #c96a80; }
button.mini {
  border: 1px solid rgba(154, 107, 125, .3);
  background: rgba(255, 255, 255, .6);
  border-radius: 8px;
  padding: 3px 10px;
  font-size: 12px;
  color: var(--sub, #b88aa0);
  cursor: pointer;
  margin-right: 6px;
}
button.mini.danger { color: #c96a80; border-color: rgba(210, 92, 114, .4); }
.empty { text-align: center; color: var(--muted, #a98298); padding: 30px 0; font-size: 13px; }
.pager { display: flex; align-items: center; gap: 10px; margin-top: 14px; }
.total { font-size: 12px; color: var(--muted, #a98298); }

.stat-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 14px; }
.stat {
  background: var(--panel-bg, rgba(255, 252, 254, .92));
  border: 1px solid rgba(154, 107, 125, .12);
  border-radius: 14px;
  padding: 18px;
  display: flex; flex-direction: column; gap: 6px;
}
.stat .num { font-size: 26px; font-weight: 700; color: #e08aa8; }
.stat .label { font-size: 12px; color: var(--muted, #a98298); }

.tip { font-size: 12px; color: var(--muted, #a98298); margin: -6px 0 12px; }

.mask {
  position: fixed; inset: 0;
  background: rgba(80, 50, 70, .35);
  display: flex; align-items: center; justify-content: center;
  z-index: 50;
  backdrop-filter: blur(4px);
}
.modal {
  width: min(560px, 92vw);
  max-height: 84vh;
  overflow: auto;
  background: var(--panel-bg, rgba(255, 252, 254, .96));
  border-radius: 18px;
  padding: 22px 24px;
  box-shadow: 0 20px 60px rgba(120, 60, 90, .3);
}
.modal-title { margin: 0 0 16px; font-size: 16px; }
.form { display: flex; flex-direction: column; gap: 12px; }
.f { display: flex; flex-direction: column; gap: 5px; }
.f-label { font-size: 12px; color: var(--sub, #b88aa0); }
.f-input {
  border: 1px solid rgba(154, 107, 125, .25);
  border-radius: 10px;
  padding: 9px 12px;
  font-size: 13px;
  background: rgba(255, 255, 255, .75);
  color: var(--ink, #9a6b7d);
  outline: none;
  font-family: inherit;
}
.f-input:focus { border-color: #f0a8c4; }
textarea.f-input { resize: vertical; }
.error {
  margin-top: 10px;
  font-size: 12px;
  color: #d25c72;
  background: rgba(210, 92, 114, .08);
  border-radius: 10px;
  padding: 8px 12px;
}
.modal-foot { display: flex; justify-content: flex-end; gap: 10px; margin-top: 18px; }
.toast {
  position: fixed; left: 50%; bottom: 40px;
  transform: translateX(-50%);
  background: rgba(90, 55, 75, .9);
  color: #fff;
  padding: 9px 20px;
  border-radius: 999px;
  font-size: 13px;
  z-index: 60;
}
</style>
