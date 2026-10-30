<template>
  <div class="home-stage">
    <!-- ===== 顶部导航栏 ===== -->
    <nav class="navbar">
      <div class="nav-brand">
        <div class="brand-badge">
          <svg viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
            <path d="M12 3l2.2 4.6L19 8.4l-3.6 3.3.9 4.9L12 14.4 7.7 16.6l.9-4.9L5 8.4l4.8-.8z" />
          </svg>
        </div>
        <span class="brand-name">{{ siteText.brand_name }}</span>
      </div>

      <ul class="nav-menu">
        <li :class="{ active: view === 'feed' }" @click="onMenu('square')">首页</li>
        <li v-for="m in menus" :key="m.key" :class="{ active: view === m.key }" @click="onMenu(m.key)">
          {{ m.label }}
        </li>
      </ul>

      <div class="nav-user">
        <button class="admin-entry" type="button" @click="$emit('open-admin')">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M3 3h7v7H3zM14 3h7v7h-7zM14 14h7v7h-7zM3 14h7v7H3z"/></svg>
          后台管理
        </button>
        <div class="avatar" :title="user.username">{{ initial }}</div>
        <span class="nav-name">{{ user.username }}</span>
        <button class="logout-btn" type="button" @click="$emit('logout')">退出登录</button>
      </div>
    </nav>

    <!-- ===== 页面内容（广播剧视图时隐藏，由 DramaPage 接管） ===== -->
    <div class="content" v-if="view !== 'drama'">
      <!-- 搜索区 -->
      <section class="hero">
        <h1 class="hero-title">{{ siteText.hero_title }}</h1>
        <p class="hero-sub">{{ siteText.hero_sub }}</p>
        <p class="greet">欢迎回来，{{ user.username }} ✦ 今天想找一场怎样的梦？</p>

        <form class="search-bar" @submit.prevent="doSearch">
          <svg class="ico" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
            <circle cx="11" cy="11" r="7" />
            <path d="M20 20l-3.5-3.5" />
          </svg>
          <input type="text" v-model="keyword" placeholder="搜索梦境、故事或关键词…"
                 maxlength="50" @input="clearHint" />
          <button class="search-btn" type="submit" :disabled="searching">
            {{ searching ? '搜索中…' : '搜 索' }}
          </button>
        </form>

        <div class="hot">
          <span class="hot-label">热门搜索：</span>
          <button class="chip" v-for="w in hotWords" :key="w.keyword" type="button"
                  @click="quickSearch(w.keyword)">
            # {{ w.keyword }}<span class="chip-count" v-if="w.count">{{ w.count }}</span>
          </button>
        </div>
      </section>

      <!-- 搜索结果区（搜索后显示） -->
      <section class="section" v-if="searched">
        <div class="section-head">
          <span class="dot"></span>
          <span>「{{ lastKeyword }}」的搜索结果</span>
          <span class="count">共 {{ searchTotal }} 条</span>
          <span class="mode-tag" v-if="isDemo">演示模式</span>
          <button class="clear-btn" type="button" @click="clearSearch">清除搜索</button>
        </div>
        <div class="empty" v-if="!results.length">没有找到相关内容，换个关键词试试吧 ✦</div>
        <div class="cards" v-else>
          <article class="dream-card" v-for="(r, i) in results" :key="r.id || i"
                   :style="{ animationDelay: (i * 0.05) + 's' }" @click="openDetail(r)">
            <div class="card-cover" :class="'cover-' + (r.categoryCode || 'essay')">
              <span class="card-tag">{{ r.category || r.tag || '梦境' }}</span>
              <span class="cover-ini">{{ (r.title || '梦').charAt(0) }}</span>
            </div>
            <div class="card-body">
              <div class="card-title">{{ r.title }}<span class="hl" v-if="lastKeyword"> · 命中</span></div>
              <div class="card-desc">{{ r.summary || r.desc }}</div>
            </div>
            <div class="card-foot">
              <span class="mini-avatar">{{ r.avatarText || '梦' }}</span>
              <span class="foot-author">{{ r.authorName }}</span>
              <span class="foot-time">{{ r.timeText }}</span>
              <span class="foot-spacer"></span>
              <span class="foot-stat" title="浏览量">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round"><path d="M2.5 12S6 5.5 12 5.5 21.5 12 21.5 12 18 18.5 12 18.5 2.5 12 2.5 12Z"/><circle cx="12" cy="12" r="3"/></svg>
                {{ formatNum(r.views) }}
              </span>
              <button class="like-btn" :class="{ liked: r.liked }" type="button" @click.stop="likeDream(r)">
                <svg viewBox="0 0 24 24" :fill="r.liked ? 'currentColor' : 'none'" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round"><path d="M12 20s-7.5-4.6-7.5-9.4A4.1 4.1 0 0 1 12 8.1a4.1 4.1 0 0 1 7.5 2.5C19.5 15.4 12 20 12 20Z"/></svg>
                {{ formatNum(r.likes) }}
              </button>
            </div>
          </article>
        </div>
      </section>

      <!-- 首页统计条 -->
      <section class="stats" v-if="stats">
        <div class="stat-box" v-for="s in statList" :key="s.label">
          <span class="stat-num">{{ formatNum(s.value) }}</span>
          <span class="stat-label">{{ s.label }}</span>
        </div>
      </section>

      <!-- 个人中心头部（仅个人中心视图） -->
      <section class="section profile-section" v-if="view === 'profile'">
        <div class="profile-card">
          <div class="profile-avatar">{{ initial }}</div>
          <div class="profile-main">
            <div class="profile-name">{{ user.username }}</div>
            <div class="profile-desc">
              造梦者 · 已记录 {{ formatNum(profileStats.dreamCount || 0) }} 场梦 · 收获
              {{ formatNum(profileStats.likeCount || 0) }} 次点赞
            </div>
          </div>
          <button class="search-btn" type="button" @click="openEditor(null)">+ 写一场梦</button>
        </div>
        <div class="stats profile-stats">
          <div class="stat-box" v-for="s in profileStatList" :key="s.label">
            <span class="stat-num">{{ formatNum(s.value) }}</span>
            <span class="stat-label">{{ s.label }}</span>
          </div>
        </div>
      </section>

      <!-- 首页展示区：梦境精选 / 我的收藏 / 我的梦境 -->
      <section class="section">
        <div class="section-head">
          <span class="dot"></span>
          <span>{{ sectionTitle }}</span>
          <span class="count">{{ sectionCount }}</span>
          <span class="mode-tag" v-if="isDemo">演示模式</span>
          <button class="write-btn" type="button" v-if="view === 'feed'" @click="openEditor(null)">
            + 记录梦境
          </button>
          <button class="clear-btn" type="button" v-else @click="backToFeed">
            ← 返回精选
          </button>
        </div>

        <div class="filters" v-if="view === 'feed'">
          <div class="tabs">
            <button class="tab" :class="{ active: activeCategory === '' }" type="button"
                    @click="selectCategory('')">全部</button>
            <button class="tab" v-for="c in categories" :key="c.code || c.name" type="button"
                    :class="{ active: activeCategory === c.name }" @click="selectCategory(c.name)">
              {{ c.name }}<span class="tab-count">{{ c.count }}</span>
            </button>
          </div>
          <div class="sort-switch">
            <button class="sort-btn" :class="{ active: activeSort === 'newest' }" type="button"
                    @click="setSort('newest')">最新</button>
            <button class="sort-btn" :class="{ active: activeSort === 'hot' }" type="button"
                    @click="setSort('hot')">最热</button>
          </div>
        </div>

        <div class="empty" v-if="!displayList.length && !displayLoading">{{ emptyText }}</div>
        <div class="cards" v-else>
          <article class="dream-card" v-for="(d, i) in displayList" :key="d.id"
                   :style="{ animationDelay: (i * 0.05) + 's' }" @click="openDetail(d)">
            <div class="card-cover" :class="'cover-' + (d.categoryCode || 'essay')">
              <span class="card-tag">{{ d.category }}</span>
              <span class="cover-ini">{{ (d.title || '梦').charAt(0) }}</span>
            </div>
            <div class="card-body">
              <div class="card-title">{{ d.title }}</div>
              <div class="card-desc">{{ d.summary }}</div>
              <div class="tags" v-if="d.tags && d.tags.length">
                <span class="tag" v-for="t in d.tags.slice(0, 3)" :key="t">#{{ t }}</span>
              </div>
            </div>
            <div class="card-foot">
              <span class="mini-avatar">{{ d.avatarText }}</span>
              <span class="foot-author">{{ d.authorName }}</span>
              <span class="foot-time">{{ d.timeText }}</span>
              <span class="foot-spacer"></span>
              <span class="foot-stat" title="浏览量">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round"><path d="M2.5 12S6 5.5 12 5.5 21.5 12 21.5 12 18 18.5 12 18.5 2.5 12 2.5 12Z"/><circle cx="12" cy="12" r="3"/></svg>
                {{ formatNum(d.views) }}
              </span>
              <!-- 我的梦境：自己的内容不显示点赞/收藏，改为编辑与删除 -->
              <template v-if="view === 'profile'">
                <button class="mini-btn" type="button" title="编辑" @click.stop="openEditor(d)">编辑</button>
                <button class="mini-btn danger" type="button" title="删除" @click.stop="askDelete(d)">删除</button>
              </template>
              <template v-else>
                <button class="like-btn" :class="{ liked: d.liked }" type="button" title="点赞" @click.stop="likeDream(d)">
                  <svg viewBox="0 0 24 24" :fill="d.liked ? 'currentColor' : 'none'" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round"><path d="M12 20s-7.5-4.6-7.5-9.4A4.1 4.1 0 0 1 12 8.1a4.1 4.1 0 0 1 7.5 2.5C19.5 15.4 12 20 12 20Z"/></svg>
                  {{ formatNum(d.likes) }}
                </button>
                <button class="fav-btn" :class="{ active: d.favorited }" type="button" title="收藏" @click.stop="favoriteDream(d)">
                  <svg viewBox="0 0 24 24" :fill="d.favorited ? 'currentColor' : 'none'" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round"><path d="M7 4h10v16l-5-3.8L7 20z"/></svg>
                </button>
              </template>
            </div>
          </article>
        </div>

        <div class="more" v-if="displayHasMore">
          <button class="more-btn" type="button" :disabled="displayLoading" @click="onLoadMore">
            {{ displayLoading ? '加载中…' : '加载更多' }}
          </button>
        </div>
        <div class="more-hint" v-else-if="displayList.length">已经到底啦 ✦</div>
      </section>

      <!-- 热门推荐（大家都在看） -->
      <section class="section" v-if="view === 'feed' && hotRail.length && activeSort === 'newest' && !activeCategory">
        <div class="section-head">
          <span class="dot"></span>
          <span>大家都在看</span>
          <span class="count">按浏览量挑选的 6 场梦</span>
        </div>
        <div class="rail">
          <article class="rail-card" v-for="d in hotRail" :key="d.id" @click="openDetail(d)">
            <span class="rail-rank">{{ d.__rank }}</span>
            <div class="rail-main">
              <div class="card-title">{{ d.title }}</div>
              <div class="card-desc">{{ d.summary }}</div>
              <div class="rail-meta">
                <span>{{ d.authorName }}</span>
                <span class="foot-stat">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round"><path d="M2.5 12S6 5.5 12 5.5 21.5 12 21.5 12 18 18.5 12 18.5 2.5 12 2.5 12Z"/><circle cx="12" cy="12" r="3"/></svg>
                  {{ formatNum(d.views) }}
                </span>
              </div>
            </div>
          </article>
        </div>
      </section>

      <!-- 快捷入口区 -->
      <section class="section">
        <div class="section-head"><span class="dot"></span><span>快捷入口</span></div>
        <div class="cards">
          <button class="shortcut-card" v-for="s in shortcuts" :key="s.key || s.title" type="button"
                  @click="onShortcut(s)">
            <span class="sc-ico" v-html="shortcutIcon(s)"></span>
            <span class="sc-text">
              <span class="sc-title">{{ s.title }}</span>
              <span class="sc-desc">{{ s.desc }}</span>
            </span>
          </button>
        </div>
      </section>

      <footer class="footer">{{ siteText.footer }}</footer>
    </div>

    <!-- ===== 广播剧视图（首页导航「广播剧」时接管主区域） ===== -->
    <DramaPage v-if="view === 'drama'" />

    <!-- ===== 梦境详情弹层 ===== -->
    <div class="detail-mask" v-if="detail" @click.self="closeDetail">
      <div class="detail-panel">
        <button class="detail-close" type="button" @click="closeDetail" aria-label="关闭">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"><path d="M6 6l12 12M18 6L6 18"/></svg>
        </button>

        <div class="detail-cover" :class="'cover-' + (detail.categoryCode || 'essay')">
          <span class="card-tag">{{ detail.category }}</span>
          <span class="detail-ini">{{ (detail.title || '梦').charAt(0) }}</span>
        </div>

        <h2 class="detail-title">{{ detail.title }}</h2>
        <div class="detail-meta">
          <span class="mini-avatar">{{ detail.avatarText }}</span>
          <span>{{ detail.authorName }}</span>
          <span class="foot-time">{{ detail.createTime || detail.timeText }}</span>
          <span class="foot-spacer"></span>
          <span class="foot-stat">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round"><path d="M2.5 12S6 5.5 12 5.5 21.5 12 21.5 12 18 18.5 12 18.5 2.5 12 2.5 12Z"/><circle cx="12" cy="12" r="3"/></svg>
            {{ formatNum(detail.views) }} 次浏览
          </span>
          <button class="like-btn" :class="{ liked: detail.liked }" type="button" @click.stop="likeDream(detail)">
            <svg viewBox="0 0 24 24" :fill="detail.liked ? 'currentColor' : 'none'" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round"><path d="M12 20s-7.5-4.6-7.5-9.4A4.1 4.1 0 0 1 12 8.1a4.1 4.1 0 0 1 7.5 2.5C19.5 15.4 12 20 12 20Z"/></svg>
            {{ formatNum(detail.likes) }}
          </button>
          <button class="fav-btn" :class="{ active: detail.favorited }" type="button" @click.stop="favoriteDream(detail)">
            <svg viewBox="0 0 24 24" :fill="detail.favorited ? 'currentColor' : 'none'" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round"><path d="M7 4h10v16l-5-3.8L7 20z"/></svg>
            {{ detail.favorited ? '已收藏' : '收藏' }}
          </button>
        </div>

        <div class="detail-text">
          <p v-for="(p, i) in detailParagraphs" :key="i">{{ p }}</p>
        </div>

        <div class="tags detail-tags" v-if="detail.tags && detail.tags.length">
          <span class="tag" v-for="t in detail.tags" :key="t">#{{ t }}</span>
        </div>
      </div>
    </div>

    <!-- ===== 创作 / 编辑弹层 ===== -->
    <div class="editor-mask" v-if="editor" @click.self="closeEditor">
      <div class="editor-panel">
        <button class="detail-close" type="button" @click="closeEditor" aria-label="关闭">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"><path d="M6 6l12 12M18 6L6 18"/></svg>
        </button>

        <h2 class="editor-title">{{ editor.mode === 'edit' ? '编辑这场梦' : '写下今晚的梦' }}</h2>
        <p class="editor-sub">写得越具体，梦就越不容易溜走 ✦</p>

        <label class="field">
          <span class="field-label">梦境标题 <em>*</em></span>
          <input v-model="editor.title" type="text" maxlength="100" placeholder="例如：在云端城堡的午后" />
        </label>

        <div class="field-row">
          <label class="field">
            <span class="field-label">分类</span>
            <select v-model="editor.category">
              <option value="">默认（随笔）</option>
              <option v-for="c in categoryOptions" :key="c" :value="c">{{ c }}</option>
            </select>
          </label>
          <label class="field">
            <span class="field-label">标签<em class="hint">逗号分隔，最多 8 个</em></span>
            <input v-model="editor.tags" type="text" maxlength="120" placeholder="云朵, 午后, 温柔" />
          </label>
        </div>

        <label class="field">
          <span class="field-label">一句话摘要<em class="hint">留空自动截取正文</em></span>
          <input v-model="editor.summary" type="text" maxlength="255" placeholder="用一句话概括这场梦" />
        </label>

        <label class="field">
          <span class="field-label">梦境正文 <em>*</em></span>
          <textarea v-model="editor.content" rows="7" maxlength="5000"
                    placeholder="把梦里的画面、声音和心情都写下来…"></textarea>
          <span class="field-tip">{{ editor.content.length }} / 5000</span>
        </label>

        <div class="editor-error" v-if="editorError">{{ editorError }}</div>

        <div class="editor-foot">
          <button class="ghost-btn" type="button" @click="closeEditor">取消</button>
          <button class="search-btn" type="button" :disabled="saving" @click="saveEditor">
            {{ saving ? '保存中…' : (editor.mode === 'edit' ? '保存修改' : '发布梦境') }}
          </button>
        </div>
      </div>
    </div>

    <!-- ===== 删除确认 ===== -->
    <div class="editor-mask" v-if="deleteTarget" @click.self="deleteTarget = null">
      <div class="confirm-panel">
        <h3 class="confirm-title">删除这场梦？</h3>
        <p class="confirm-text">
          「{{ deleteTarget.title }}」将从你的梦境列表中移除，该操作不可撤销。
        </p>
        <div class="editor-foot">
          <button class="ghost-btn" type="button" @click="deleteTarget = null">再想想</button>
          <button class="danger-btn" type="button" :disabled="deleting" @click="confirmDelete">
            {{ deleting ? '删除中…' : '确认删除' }}
          </button>
        </div>
      </div>
    </div>

    <div class="toast" :class="{ show: toastMsg }">{{ toastMsg }}</div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import DramaPage from './DramaPage.vue'
import {
  fetchHomeOverview,
  fetchHomeFeed,
  searchDreams,
  fetchDreamDetail,
  toggleDreamLike,
  toggleDreamFavorite,
  fetchMyFavorites,
  publishDream,
  updateDream,
  deleteDream as removeDream,
  fetchMyDreams,
  fetchUserProfile
} from '../api'
import { loadHomeConfig, iconSvg, DEFAULT_CONFIG } from '../homeConfig'

const props = defineProps({
  user: { type: Object, required: true }
})
const emit = defineEmits(['logout', 'open-admin'])

const initial = ref((props.user.username || '梦').trim().charAt(0) || '梦')

/* ---- 导航菜单 ---- */
/* ---- 首页栏目配置 ----
 * 导航菜单、快捷入口、站点文案均来自后台管理系统的「首页配置」，
 * 接口不可用（后端未启动等）时由 homeConfig 回落到内置默认值，首页不会开天窗。
 */
// 先用内置默认值铺底，避免配置到达前导航栏/快捷入口闪一下空白
const menus = ref([...DEFAULT_CONFIG.navMenu])
const shortcuts = ref([...DEFAULT_CONFIG.shortcut])
const siteText = ref({ ...DEFAULT_CONFIG.text })

/** 快捷入口图标：按后台配置的 iconKey 取预置图标，未知 key 回落「星光」 */
function shortcutIcon(s) {
  return iconSvg(s && s.iconKey)
}

/** 每次进首页强制拉一次最新配置（管理员可能刚在后台改过） */
async function loadSiteConfig() {
  const cfg = await loadHomeConfig(true)
  menus.value = cfg.navMenu
  shortcuts.value = cfg.shortcut
  siteText.value = cfg.text
}

/* ---- 轻提示 ---- */
const toastMsg = ref('')
let timer = null
function flash(msg) {
  toastMsg.value = msg
  clearTimeout(timer)
  timer = setTimeout(() => (toastMsg.value = ''), 2600)
}

/* ---- 首页展示状态 ---- */
/** 当前主视图：feed 梦境精选 / favorites 我的收藏 / profile 个人中心（我的梦境） */
const view = ref('feed')
const stats = ref(null)
const categories = ref([])
const hotWords = ref([])
const hotRail = ref([])
const feed = ref([])
const feedTotal = ref(0)
const feedPage = ref(1)
const FEED_SIZE = 8
const hasMore = ref(false)
const loadingFeed = ref(false)
const activeCategory = ref('')
const activeSort = ref('newest')
const isDemo = ref(false)

/* ---- 我的收藏状态 ---- */
const favorites = ref([])
const favTotal = ref(0)
const favPage = ref(1)
const favHasMore = ref(false)
const loadingFav = ref(false)

/* ---- 个人中心状态 ---- */
const profile = ref(null)
const myDreams = ref([])
const myTotal = ref(0)
const myPage = ref(1)
const myHasMore = ref(false)
const loadingMine = ref(false)

/* 主列表区在三种视图间复用同一套卡片模板 */
const displayList = computed(() => {
  if (view.value === 'favorites') return favorites.value
  if (view.value === 'profile') return myDreams.value
  return feed.value
})
const displayLoading = computed(() => {
  if (view.value === 'favorites') return loadingFav.value
  if (view.value === 'profile') return loadingMine.value
  return loadingFeed.value
})
const displayHasMore = computed(() => {
  if (view.value === 'favorites') return favHasMore.value
  if (view.value === 'profile') return myHasMore.value
  return hasMore.value
})

const sectionTitle = computed(() => {
  if (view.value === 'favorites') return '我的收藏'
  if (view.value === 'profile') return '我的梦境'
  return '梦境精选'
})
const sectionCount = computed(() => {
  if (view.value === 'favorites') return `共 ${formatNum(favTotal.value)} 场梦`
  if (view.value === 'profile') return `共 ${formatNum(myTotal.value)} 场梦`
  return `共 ${formatNum(feedTotal.value)} 场梦`
})
const emptyText = computed(() => {
  if (view.value === 'favorites') return '还没有收藏任何梦，点卡片右下角的书签把它收下来吧 ✦'
  if (view.value === 'profile') return '你还没有记录过梦境，点上面的「写一场梦」开始吧 ✦'
  return '这里还空空的，去写下今晚的梦吧 ✦'
})

const profileStats = computed(() => profile.value || {})
const profileStatList = computed(() => {
  const s = profileStats.value
  return [
    { label: '我的梦境', value: s.dreamCount || 0 },
    { label: '累计浏览', value: s.viewCount || 0 },
    { label: '收获点赞', value: s.likeCount || 0 },
    { label: '被收藏', value: s.favoriteCount || 0 }
  ]
})

/** 编辑弹层的分类选项：优先用后端分类字典，兜底用内置白名单 */
const categoryOptions = computed(() => {
  const names = categories.value.map(c => c.name).filter(Boolean)
  return names.length ? names : ['美梦', '噩梦', '灵感', '随笔']
})

/* ---- 搜索状态 ---- */
const keyword = ref('')
const searching = ref(false)
const searched = ref(false)
const lastKeyword = ref('')
const results = ref([])
const searchTotal = ref(0)

/* ---- 详情弹层 ---- */
const detail = ref(null)
const detailParagraphs = computed(() => {
  const text = (detail.value && detail.value.content) || ''
  const parts = text.split(/\n+/).map(s => s.trim()).filter(Boolean)
  return parts.length ? parts : ['（这场梦还没来得及写下细节）']
})

const statList = computed(() => {
  const s = stats.value || {}
  return [
    { label: '梦境内容', value: s.dreamCount || 0 },
    { label: '累计浏览', value: s.viewCount || 0 },
    { label: '收获点赞', value: s.likeCount || 0 },
    { label: '造梦者', value: s.authorCount || 0 }
  ]
})

/* ---- 演示模式数据源：后端未启动时，用本地梦境库保证首页可浏览 ---- */
const DREAM_LIB = [
  { title: '粉色云海上的午睡', tag: '梦境', desc: '躺在会呼吸的云朵上，看金色的光从指缝间流过，连风都是甜的。' },
  { title: '糖果雨的下午', tag: '童话', desc: '整座城市下起了软糖雨，孩子们撑着棒棒糖伞在街上奔跑。' },
  { title: '星海灯塔', tag: '奇幻', desc: '守灯人每晚把星星一颗颗点亮，装进玻璃瓶寄给失眠的人。' },
  { title: '外婆的旧花园', tag: '回忆', desc: '蔷薇爬满篱笆，外婆摇着蒲扇讲那些讲了千百遍的老故事。' },
  { title: '海边的时光邮局', tag: '治愈', desc: '可以给过去的自己写信，邮票是一枚小小的贝壳。' },
  { title: '月光图书馆', tag: '静谧', desc: '每一本书里都住着一个月亮，翻开哪本，今晚就梦见哪里。' },
  { title: '金色麦田列车', tag: '旅途', desc: '开往秋天的慢车穿过麦浪，乘客们交换着彼此的梦。' },
  { title: '云端糖果屋', tag: '童话', desc: '屋顶是棉花糖，烟囱里飘出热可可的香气。' },
  { title: '雨后的彩虹桥', tag: '治愈', desc: '桥的尽头有人在等你，手里捧着一束刚摘的星光。' },
  { title: '旧时光照相馆', tag: '回忆', desc: '能拍下记忆里最柔软的瞬间，照片会轻轻发光。' },
  { title: '深海星光浴', tag: '奇幻', desc: '潜入夜晚的海，水母提着灯，鱼群衔来碎银般的月光。' },
  { title: '四季茶馆', tag: '静谧', desc: '春天上花茶，冬天煮姜茶，掌柜的会听你讲完整个梦。' }
]

function localCards() {
  // id 取负数：既有唯一 key，又能标记「非后端数据」，点赞/详情走本地逻辑
  return DREAM_LIB.map((d, i) => ({
    id: -(i + 1),
    title: d.title,
    summary: d.desc,
    content: d.desc,
    category: d.tag,
    categoryCode: 'essay',
    tags: [d.tag],
    authorName: '梦友',
    avatarText: '梦',
    views: 120 - i * 6,
    likes: 36 - i,
    liked: false,
    timeText: ''
  }))
}

function enterDemo() {
  const wasDemo = isDemo.value
  isDemo.value = true
  if (!wasDemo) flash('后端未启动，已进入演示模式')
  const lib = localCards()
  if (!categories.value.length) {
    categories.value = ['梦境', '童话', '奇幻', '回忆', '治愈', '静谧', '旅途'].map(name => ({ name, count: 0 }))
  }
  if (!hotWords.value.length) {
    hotWords.value = ['云朵', '糖果', '星星', '花园', '海'].map(word => ({ keyword: word, count: 0 }))
  }
  if (!stats.value) {
    stats.value = { dreamCount: lib.length, viewCount: 1180, likeCount: 316, authorCount: 1 }
  }
  hotRail.value = lib.slice(0, 6).map((d, i) => ({ ...d, __rank: i + 1 }))
  feed.value = lib.filter(d => !activeCategory.value || d.category === activeCategory.value)
  feedTotal.value = lib.length
  hasMore.value = false
}

/* ---- 统一错误处理：网络错误 → 演示模式；401 → 退出登录 ---- */
function handleError(e) {
  if (e && e.isUnauthorized) {
    emit('logout')
    return true
  }
  if (e && e.isNetwork) {
    enterDemo()
    return true
  }
  flash((e && e.message) || '请求失败，请稍后重试')
  return false
}

/* ---- 首页聚合数据 ---- */
async function loadOverview() {
  try {
    const res = await fetchHomeOverview()
    const data = (res && res.data) || {}
    stats.value = data.stats || null
    categories.value = data.categories || []
    hotWords.value = (data.hotKeywords || []).slice(0, 5)
    hotRail.value = (data.hotDreams || []).map((d, i) => ({ ...d, __rank: i + 1 }))
    if (!hotWords.value.length) {
      hotWords.value = ['云朵', '星星', '月亮', '糖果', '鲸鱼'].map(k => ({ keyword: k, count: 0 }))
    }
  } catch (e) {
    handleError(e)
  }
}

/* ---- 首页展示列表 ---- */
async function loadFeed(append = false) {
  if (loadingFeed.value) return
  loadingFeed.value = true
  try {
    const res = await fetchHomeFeed({
      page: feedPage.value,
      size: FEED_SIZE,
      category: activeCategory.value,
      sort: activeSort.value
    })
    const data = (res && res.data) || {}
    const list = data.list || []
    isDemo.value = false
    feed.value = append ? feed.value.concat(list) : list
    feedTotal.value = data.total || 0
    hasMore.value = !!data.hasMore
  } catch (e) {
    if (e && e.isNetwork) {
      enterDemo()
    } else if (!handleError(e)) {
      hasMore.value = false
    }
  } finally {
    loadingFeed.value = false
  }
}

function resetFeed() {
  feedPage.value = 1
  loadFeed(false)
}

function loadMore() {
  if (!hasMore.value || loadingFeed.value) return
  feedPage.value += 1
  loadFeed(true)
}

function selectCategory(name) {
  activeCategory.value = name
  resetFeed()
}

function setSort(sort) {
  if (activeSort.value === sort) return
  activeSort.value = sort
  resetFeed()
}

/* ---- 我的收藏 ---- */
async function openFavorites() {
  if (isDemo.value) {
    flash('演示模式下收藏不可用，请先启动后端服务')
    return
  }
  view.value = 'favorites'
  favPage.value = 1
  await loadFavorites(false)
}

function backToFeed() {
  view.value = 'feed'
}

async function loadFavorites(append = false) {
  if (loadingFav.value) return
  loadingFav.value = true
  try {
    const res = await fetchMyFavorites({ page: favPage.value, size: FEED_SIZE })
    const data = (res && res.data) || {}
    const list = data.list || []
    favorites.value = append ? favorites.value.concat(list) : list
    favTotal.value = data.total != null ? data.total : favorites.value.length
    favHasMore.value = !!data.hasMore
  } catch (e) {
    if (e && e.isUnauthorized) {
      flash('登录已过期，请重新登录')
      emit('logout')
    } else if (e && e.isNetwork) {
      flash('无法连接服务器')
    } else {
      flash((e && e.message) || '收藏加载失败，请稍后重试')
    }
  } finally {
    loadingFav.value = false
  }
}

/** 统一的「加载更多」入口：按当前视图决定翻哪一页 */
function onLoadMore() {
  if (view.value === 'favorites') {
    if (!favHasMore.value || loadingFav.value) return
    favPage.value += 1
    loadFavorites(true)
    return
  }
  if (view.value === 'profile') {
    if (!myHasMore.value || loadingMine.value) return
    myPage.value += 1
    loadMyDreams(true)
    return
  }
  loadMore()
}

/* ---- 搜索 ---- */
function clearHint() {
  if (!keyword.value.trim()) searched.value = false
}

async function doSearch() {
  const kw = keyword.value.trim()
  if (!kw) { flash('请输入搜索关键词'); return }
  if (searching.value) return
  searching.value = true
  try {
    const res = await searchDreams({ keyword: kw })
    const data = (res && res.data) || {}
    isDemo.value = false
    results.value = data.list || (Array.isArray(data) ? data : [])
    searchTotal.value = data.total != null ? data.total : results.value.length
    searched.value = true
    lastKeyword.value = kw
    if (!results.value.length) flash('没有找到相关内容，换个关键词试试吧')
  } catch (e) {
    if (e && e.isNetwork) {
      isDemo.value = true
      const hit = localCards().filter(d => (d.title + (d.summary || '') + d.category).includes(kw))
      results.value = hit
      searchTotal.value = hit.length
      searched.value = true
      lastKeyword.value = kw
      flash('后端未启动，已进入演示模式')
    } else {
      handleError(e)
    }
  } finally {
    searching.value = false
  }
}

function quickSearch(w) {
  keyword.value = w
  doSearch()
}

function clearSearch() {
  keyword.value = ''
  searched.value = false
  results.value = []
  searchTotal.value = 0
}

/* ---- 详情 ---- */
async function openDetail(card) {
  if (!card) return
  if (isLocalCard(card)) {
    // 演示模式的本地数据：直接打开，不请求后端
    detail.value = { ...card, content: card.content || card.summary, liked: !!card.liked }
    return
  }
  detail.value = { ...card, content: card.content || '正在展开这场梦…' }
  try {
    const res = await fetchDreamDetail({ id: card.id })
    if (res && res.data) {
      detail.value = res.data
      // 浏览量 +1 后同步到列表，避免数字对不上
      syncCard(card.id, { views: res.data.views, likes: res.data.likes, liked: res.data.liked })
    }
  } catch (e) {
    if (!handleError(e)) detail.value = null
  }
}

function closeDetail() {
  detail.value = null
}

/** 同步各处列表里的同一条内容（点赞、收藏、浏览量变化） */
function syncCard(id, patch) {
  ;[feed.value, results.value, hotRail.value, favorites.value].forEach(list => {
    list.forEach(item => {
      if (item.id === id) Object.assign(item, patch)
    })
  })
}

/** 本地演示数据（id 为负）不请求后端 */
function isLocalCard(card) {
  return !card || !card.id || card.id < 0
}

/* ---- 点赞 ---- */
async function likeDream(card) {
  if (!card) return
  if (isLocalCard(card)) {
    card.liked = !card.liked
    card.likes = Math.max((card.likes || 0) + (card.liked ? 1 : -1), 0)
    return
  }
  try {
    const res = await toggleDreamLike({ id: card.id })
    const data = (res && res.data) || {}
    card.liked = !!data.liked
    card.likes = data.likes
    syncCard(card.id, { liked: card.liked, likes: card.likes })
    flash(data.liked ? '已收进你的梦✦' : '已取消点赞')
  } catch (e) {
    handleError(e)
  }
}

/* ---- 收藏 / 取消收藏 ---- */
async function favoriteDream(card) {
  if (!card) return
  if (isLocalCard(card)) {
    flash('演示模式下收藏不可用')
    return
  }
  try {
    const res = await toggleDreamFavorite({ id: card.id })
    const data = (res && res.data) || {}
    card.favorited = !!data.favorited
    syncCard(card.id, { favorited: card.favorited })
    flash(data.favorited ? '已收进「我的收藏」✦' : '已取消收藏')
    // 在收藏列表里取消收藏：立即把它移出列表，避免看起来"点了没反应"
    if (view.value === 'favorites' && !card.favorited) {
      favorites.value = favorites.value.filter(d => d.id !== card.id)
      favTotal.value = Math.max(favTotal.value - 1, 0)
      if (detail.value && detail.value.id === card.id) closeDetail()
    }
  } catch (e) {
    handleError(e)
  }
}

/* ---- 个人中心：我的梦境 ---- */
async function openProfile() {
  if (isDemo.value) {
    flash('演示模式下个人中心不可用，请先启动后端服务')
    return
  }
  view.value = 'profile'
  myPage.value = 1
  loadProfile()
  await loadMyDreams(false)
}

async function loadProfile() {
  try {
    const res = await fetchUserProfile()
    profile.value = (res && res.data) || null
  } catch (e) {
    if (e && e.isUnauthorized) {
      flash('登录已过期，请重新登录')
      emit('logout')
    } else if (!(e && e.isNetwork)) {
      flash((e && e.message) || '个人数据加载失败')
    }
  }
}

async function loadMyDreams(append = false) {
  if (loadingMine.value) return
  loadingMine.value = true
  try {
    const res = await fetchMyDreams({ page: myPage.value, size: FEED_SIZE })
    const data = (res && res.data) || {}
    const list = data.list || []
    myDreams.value = append ? myDreams.value.concat(list) : list
    myTotal.value = data.total != null ? data.total : myDreams.value.length
    myHasMore.value = !!data.hasMore
  } catch (e) {
    if (e && e.isUnauthorized) {
      flash('登录已过期，请重新登录')
      emit('logout')
    } else if (e && e.isNetwork) {
      flash('无法连接服务器')
    } else {
      flash((e && e.message) || '我的梦境加载失败，请稍后重试')
    }
  } finally {
    loadingMine.value = false
  }
}

/* ---- 创作 / 编辑弹层 ---- */
const editor = ref(null)
const editorError = ref('')
const saving = ref(false)

function openEditor(card) {
  if (isDemo.value) {
    flash('演示模式下无法创作，请先启动后端服务')
    return
  }
  editorError.value = ''
  if (card) {
    editor.value = {
      mode: 'edit',
      id: card.id,
      title: card.title || '',
      summary: card.summary || '',
      content: card.content || '',
      category: card.category || '',
      tags: (card.tags || []).join(','),
      coverUrl: card.coverUrl || ''
    }
  } else {
    editor.value = {
      mode: 'create', id: null, title: '', summary: '', content: '',
      category: '', tags: '', coverUrl: ''
    }
  }
}

function closeEditor() {
  if (saving.value) return
  editor.value = null
  editorError.value = ''
}

async function saveEditor() {
  const form = editor.value
  if (!form || saving.value) return

  const title = (form.title || '').trim()
  const content = (form.content || '').trim()
  if (title.length < 2 || title.length > 100) { editorError.value = '标题长度需在 2-100 字之间'; return }
  if (!content) { editorError.value = '请写下梦境的正文内容'; return }

  editorError.value = ''
  saving.value = true
  try {
    const payload = {
      title,
      summary: (form.summary || '').trim(),
      content,
      category: form.category || '',
      tags: form.tags || '',
      coverUrl: (form.coverUrl || '').trim()
    }
    if (form.mode === 'edit') {
      await updateDream({ ...payload, id: form.id })
      flash('这场梦已更新 ✦')
    } else {
      await publishDream(payload)
      flash('梦境已发布，愿它被更多人看见 ✦')
    }
    editor.value = null
    await refreshAfterWrite()
  } catch (e) {
    if (e && e.isUnauthorized) {
      flash('请先登录后再创作')
      emit('logout')
    } else if (e && e.isNetwork) {
      flash('无法连接服务器')
    } else {
      // 校验类错误（标题过短、正文为空等）留在弹层里提示，用户不必重填
      editorError.value = (e && e.message) || '保存失败，请稍后重试'
    }
  } finally {
    saving.value = false
  }
}

/** 写操作成功后刷新相关列表：个人中心重拉自己的数据，首页内容流一并刷新 */
async function refreshAfterWrite() {
  if (view.value === 'profile') {
    myPage.value = 1
    loadProfile()
    await loadMyDreams(false)
  }
  resetFeed()
}

/* ---- 删除自己的梦境 ---- */
const deleteTarget = ref(null)
const deleting = ref(false)

function askDelete(card) {
  if (!card) return
  deleteTarget.value = card
}

async function confirmDelete() {
  const card = deleteTarget.value
  if (!card || deleting.value) return
  deleting.value = true
  try {
    await removeDream({ id: card.id })
    // 各列表里同步移除，避免留下点进去报「已删除」的僵尸卡片
    myDreams.value = myDreams.value.filter(d => d.id !== card.id)
    myTotal.value = Math.max(myTotal.value - 1, 0)
    feed.value = feed.value.filter(d => d.id !== card.id)
    favorites.value = favorites.value.filter(d => d.id !== card.id)
    if (detail.value && detail.value.id === card.id) closeDetail()
    deleteTarget.value = null
    flash('这场梦已删除 ✦')
    if (profile.value) profile.value.dreamCount = Math.max((profile.value.dreamCount || 1) - 1, 0)
    loadOverview()
  } catch (e) {
    deleteTarget.value = null
    handleError(e)
  } finally {
    deleting.value = false
  }
}

/* ---- 导航菜单 ---- */
function onMenu(key) {
  if (key === 'square') {
    view.value = 'feed'
    activeCategory.value = ''
    activeSort.value = 'newest'
    resetFeed()
    return
  }
  if (key === 'drama') {
    view.value = 'drama'
    return
  }
  if (key === 'favorites') { openFavorites(); return }
  if (key === 'profile') { openProfile(); return }
  flash('「帮助中心」页面建设中，敬请期待 ✦')
}

/* ---- 快捷入口 ---- */
/* 卡片数据取自后台配置（loadSiteConfig），此处不再硬编码。 */

function onShortcut(s) {
  // 统一按 target 跳转：与后台「首页配置」里填写的跳转目标一致，
  // 后台新增一张卡片无需改动前端代码。
  switch (s && s.target) {
    case 'square':
      view.value = 'feed'
      activeCategory.value = ''
      activeSort.value = 'newest'
      resetFeed()
      flash('已回到梦境精选 ✦')
      return
    case 'favorites':
      openFavorites()
      return
    case 'editor':
      openEditor(null)
      return
    case 'profile':
      openProfile()
      return
    case 'drama':
      view.value = 'drama'
      flash('已打开广播剧 ✦')
      return
    default:
      flash(`「${s && s.title}」页面建设中，敬请期待 ✦`)
  }
}

/* ---- 数字格式化：12345 → 1.2万 ---- */
function formatNum(n) {
  const v = Number(n) || 0
  if (v >= 10000) return (v / 10000).toFixed(1).replace(/\.0$/, '') + '万'
  return String(v)
}

onMounted(() => {
  loadSiteConfig()
  loadOverview()
  loadFeed(false)
})
</script>

<style scoped>
/* ===== 页面滚动容器 ===== */
.home-stage {
  position: relative; z-index: 5;
  height: 100%;
  overflow-y: auto;
  scroll-behavior: smooth;
}

/* ===== 导航栏（悬浮胶囊） ===== */
.navbar {
  position: fixed; top: 18px; left: 50%; transform: translateX(-50%);
  z-index: 30;
  width: min(1040px, calc(100vw - 170px));
  display: flex; align-items: center; gap: 16px;
  padding: 9px 10px 9px 16px;
  background: var(--card-bg);
  border: 1px solid var(--card-border);
  border-radius: 60px;
  backdrop-filter: blur(12px);
  box-shadow: 0 10px 30px rgba(214, 158, 184, .28);
}

.nav-brand { display: flex; align-items: center; gap: 10px; flex: 0 0 auto; }
.brand-badge {
  width: 38px; height: 38px; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  background: linear-gradient(135deg, #ffc6dd, #f7d98a);
  box-shadow: 0 6px 16px rgba(233, 184, 95, .4);
}
.brand-badge svg { width: 19px; height: 19px; }
.brand-name {
  font-size: 17px; font-weight: 600; letter-spacing: .1em; white-space: nowrap;
  background: linear-gradient(120deg, #c98aa6, #e9b85f 60%, #c98aa6);
  -webkit-background-clip: text; background-clip: text; color: transparent;
}

.nav-menu { display: flex; align-items: center; gap: 4px; list-style: none; flex: 1; margin: 0; padding: 0; }
.nav-menu li {
  padding: 8px 14px; border-radius: 20px; cursor: pointer; white-space: nowrap;
  font-size: 14px; letter-spacing: .05em; color: var(--muted);
  transition: color .2s, background .25s;
}
.nav-menu li:hover { color: var(--ink); background: var(--field-bg); }
.nav-menu li.active {
  color: #fff;
  background: linear-gradient(120deg, #f0a9c4, #e9b85f);
  box-shadow: 0 6px 14px rgba(233, 184, 95, .35);
}

.nav-user { display: flex; align-items: center; gap: 10px; flex: 0 0 auto; }

/* 首页导航栏「后台管理」入口 */
.admin-entry {
  display: inline-flex; align-items: center; gap: 6px;
  font-family: inherit; font-size: 13px; letter-spacing: .04em; cursor: pointer;
  color: #b3708c; background: var(--field-bg);
  border: 1px solid rgba(233, 184, 95, .45);
  padding: 7px 14px; border-radius: 18px;
  transition: color .2s, border-color .2s, background .25s, transform .15s;
}
.admin-entry svg { width: 15px; height: 15px; }
.admin-entry:hover { color: #c98aa6; border-color: var(--gold-2); background: rgba(247, 217, 138, .18); transform: translateY(-1px); }
.avatar {
  width: 36px; height: 36px; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  font-size: 15px; color: #fff;
  background: linear-gradient(135deg, #f0a9c4, #e9b85f);
  box-shadow: 0 6px 14px rgba(233, 184, 95, .35);
}
.nav-name {
  font-size: 13.5px; color: var(--ink); letter-spacing: .03em;
  max-width: 96px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
}
.logout-btn {
  border: 1px solid var(--field-border); background: var(--field-bg);
  color: var(--muted); font-family: inherit; font-size: 12.5px; letter-spacing: .05em;
  padding: 7px 14px; border-radius: 18px; cursor: pointer;
  transition: color .2s, border-color .2s, transform .15s;
}
.logout-btn:hover { color: #e58a97; border-color: rgba(229, 138, 151, .5); transform: translateY(-1px); }

/* ===== 内容区 ===== */
.content { max-width: 1040px; margin: 0 auto; padding: 122px 24px 40px; }

/* 搜索区 */
.hero { text-align: center; padding: 40px 0 6px; }
.hero-title {
  font-size: 44px; font-weight: 600; letter-spacing: .14em;
  background: linear-gradient(120deg, #c98aa6, #e9b85f 60%, #c98aa6);
  -webkit-background-clip: text; background-clip: text; color: transparent;
}
.hero-sub { margin-top: 12px; font-size: 13px; letter-spacing: .22em; color: var(--sub); }
.greet { margin-top: 20px; font-size: 14.5px; color: var(--ink); letter-spacing: .04em; }

.search-bar {
  margin: 30px auto 0; width: min(640px, 100%);
  display: flex; align-items: center; gap: 10px;
  background: var(--field-bg);
  border: 1px solid var(--field-border);
  border-radius: 30px;
  padding: 8px 8px 8px 20px;
  backdrop-filter: blur(8px);
  box-shadow: 0 14px 34px rgba(214, 158, 184, .22);
  transition: border-color .25s, box-shadow .25s;
}
.search-bar:focus-within {
  border-color: var(--gold-2);
  box-shadow: 0 0 0 3px rgba(247, 217, 138, .3), 0 14px 34px rgba(214, 158, 184, .28);
}
.search-bar .ico { width: 20px; height: 20px; flex: 0 0 auto; color: #d49bb4; }
.search-bar input {
  flex: 1; border: 0; outline: 0; background: transparent; min-width: 0;
  font-size: 15.5px; color: var(--input-text); font-family: inherit; letter-spacing: .02em;
}
.search-bar input::placeholder { color: var(--placeholder); }
.search-btn {
  flex: 0 0 auto; border: 0; cursor: pointer; font-family: inherit;
  font-size: 14px; letter-spacing: .2em; color: #fff;
  padding: 11px 24px; border-radius: 22px;
  background: linear-gradient(120deg, #f0a9c4, #e9b85f);
  box-shadow: 0 8px 18px rgba(233, 184, 95, .38);
  transition: transform .15s, filter .2s, opacity .2s;
}
.search-btn:hover:not(:disabled) { transform: translateY(-1px); filter: brightness(1.04); }
.search-btn:disabled { opacity: .6; cursor: not-allowed; }

.hot { margin-top: 18px; display: flex; align-items: center; justify-content: center; flex-wrap: wrap; gap: 10px; }
.hot-label { font-size: 12.5px; color: var(--muted); letter-spacing: .05em; }
.chip {
  border: 1px solid var(--field-border); background: var(--field-bg);
  color: var(--link); font-family: inherit; font-size: 12.5px; letter-spacing: .03em;
  padding: 6px 14px; border-radius: 16px; cursor: pointer;
  transition: color .2s, border-color .2s, transform .15s;
}
.chip:hover { color: #e9b85f; border-color: var(--gold-2); transform: translateY(-1px); }
.chip-count {
  margin-left: 6px; font-size: 11px; color: #d9a844;
  padding: 1px 6px; border-radius: 8px; background: rgba(247, 217, 138, .28);
}

/* ===== 统计条 ===== */
.stats {
  margin-top: 42px;
  display: grid; grid-template-columns: repeat(4, 1fr); gap: 14px;
}
.stat-box {
  display: flex; flex-direction: column; align-items: center; gap: 6px;
  padding: 18px 10px;
  background: var(--card-bg);
  border: 1px solid var(--card-border);
  border-radius: 18px;
  backdrop-filter: blur(8px);
  box-shadow: 0 10px 26px rgba(214, 158, 184, .16);
}
.stat-num {
  font-size: 24px; font-weight: 600; letter-spacing: .03em;
  background: linear-gradient(120deg, #c98aa6, #e9b85f 70%);
  -webkit-background-clip: text; background-clip: text; color: transparent;
}
.stat-label { font-size: 12.5px; color: var(--muted); letter-spacing: .1em; }

/* ===== 区块 ===== */
.section { margin-top: 46px; }
.section-head {
  display: flex; align-items: center; gap: 10px; flex-wrap: wrap;
  font-size: 15px; color: var(--ink); letter-spacing: .08em; margin-bottom: 16px;
}
.section-head .dot { width: 6px; height: 6px; border-radius: 50%; background: linear-gradient(135deg, #f0a9c4, #e9b85f); }
.section-head .count { font-size: 12.5px; color: var(--muted); letter-spacing: .03em; }
.mode-tag {
  font-size: 11.5px; color: #d9a844; letter-spacing: .05em;
  border: 1px solid rgba(233, 184, 95, .5); background: rgba(247, 217, 138, .15);
  padding: 3px 10px; border-radius: 12px;
}
.clear-btn {
  margin-left: auto; font-family: inherit; cursor: pointer;
  font-size: 12px; letter-spacing: .05em; color: var(--muted);
  background: transparent; border: 1px solid var(--field-border);
  padding: 5px 12px; border-radius: 14px;
  transition: color .2s, border-color .2s;
}
.clear-btn:hover { color: #e9b85f; border-color: var(--gold-2); }

.write-btn {
  margin-left: auto; font-family: inherit; cursor: pointer;
  font-size: 12.5px; letter-spacing: .08em; color: #fff;
  border: 0; padding: 8px 18px; border-radius: 18px;
  background: linear-gradient(120deg, #f0a9c4, #e9b85f);
  box-shadow: 0 8px 18px rgba(233, 184, 95, .34);
  transition: transform .15s, filter .2s;
}
.write-btn:hover { transform: translateY(-1px); filter: brightness(1.04); }

/* ===== 筛选（分类 tab + 排序） ===== */
.filters {
  display: flex; align-items: center; justify-content: space-between;
  gap: 14px; flex-wrap: wrap; margin-bottom: 18px;
}
.tabs { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
.tab {
  font-family: inherit; cursor: pointer; white-space: nowrap;
  font-size: 13px; letter-spacing: .05em; color: var(--muted);
  background: var(--field-bg); border: 1px solid var(--field-border);
  padding: 7px 15px; border-radius: 18px;
  transition: color .2s, background .25s, transform .15s, box-shadow .25s;
}
.tab:hover { color: var(--ink); transform: translateY(-1px); }
.tab.active {
  color: #fff; border-color: transparent;
  background: linear-gradient(120deg, #f0a9c4, #e9b85f);
  box-shadow: 0 6px 14px rgba(233, 184, 95, .35);
}
.tab-count { margin-left: 6px; font-size: 11px; opacity: .8; }

.sort-switch {
  display: flex; align-items: center; gap: 2px; padding: 3px;
  background: var(--field-bg); border: 1px solid var(--field-border);
  border-radius: 18px;
}
.sort-btn {
  font-family: inherit; cursor: pointer; border: 0; background: transparent;
  font-size: 12.5px; letter-spacing: .05em; color: var(--muted);
  padding: 5px 14px; border-radius: 15px; transition: color .2s, background .25s;
}
.sort-btn.active { color: #fff; background: linear-gradient(120deg, #f0a9c4, #e9b85f); }

/* ===== 卡片网格 ===== */
.cards { display: grid; grid-template-columns: repeat(auto-fill, minmax(230px, 1fr)); gap: 16px; }
.dream-card {
  display: flex; flex-direction: column;
  background: var(--card-bg);
  border: 1px solid var(--card-border);
  border-radius: 20px;
  padding: 14px;
  cursor: pointer;
  backdrop-filter: blur(8px);
  box-shadow: 0 10px 26px rgba(214, 158, 184, .18);
  transition: transform .2s, box-shadow .25s;
  animation: fadeUp .5s ease both;
}
.dream-card:hover { transform: translateY(-4px); box-shadow: 0 16px 34px rgba(214, 158, 184, .3); }

/* 封面占位（无封面图时按分类配渐变色） */
.card-cover {
  position: relative; height: 92px; border-radius: 14px; overflow: hidden;
  display: flex; align-items: center; justify-content: center;
  margin-bottom: 12px;
}
.cover-beauty { background: linear-gradient(135deg, #ffc9de, #ffe0b0); }
.cover-nightmare { background: linear-gradient(135deg, #c8b8e8, #a3b6e2); }
.cover-inspiration { background: linear-gradient(135deg, #ffe08a, #ffc79a); }
.cover-essay { background: linear-gradient(135deg, #cfe3ff, #e6d6ff); }
.cover-ini {
  font-size: 40px; font-weight: 600; letter-spacing: .06em;
  color: rgba(255, 255, 255, .92);
  text-shadow: 0 2px 12px rgba(150, 100, 125, .45);
}

.card-tag {
  position: absolute; top: 10px; left: 10px;
  display: inline-block; font-size: 11px; letter-spacing: .08em; color: #b3708c;
  padding: 3px 10px; border-radius: 10px;
  background: rgba(255, 255, 255, .82);
}

.card-body { flex: 1; }
.card-title { font-size: 15.5px; color: var(--ink); letter-spacing: .03em; margin-bottom: 8px; }
.card-title .hl { font-size: 11.5px; color: #d9a844; letter-spacing: .05em; }
.card-desc {
  font-size: 12.5px; line-height: 1.7; color: var(--muted);
  display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden;
}

.tags { margin-top: 10px; display: flex; gap: 6px; flex-wrap: wrap; }
.tag {
  font-size: 11px; color: var(--link); letter-spacing: .03em;
  padding: 2px 8px; border-radius: 9px;
  background: rgba(240, 169, 196, .16);
}

.card-foot {
  margin-top: 12px; padding-top: 10px;
  border-top: 1px solid var(--field-border);
  display: flex; align-items: center; gap: 7px;
  font-size: 11.5px; color: var(--muted);
}
.mini-avatar {
  width: 22px; height: 22px; flex: 0 0 auto; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  font-size: 11px; color: #fff;
  background: linear-gradient(135deg, #f0a9c4, #e9b85f);
}
.foot-author { max-width: 62px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.foot-time { white-space: nowrap; }
.foot-spacer { flex: 1; }
.foot-stat { display: inline-flex; align-items: center; gap: 3px; white-space: nowrap; }
.foot-stat svg { width: 13px; height: 13px; }

.like-btn {
  display: inline-flex; align-items: center; gap: 3px;
  font-family: inherit; font-size: 11.5px; cursor: pointer;
  color: var(--muted); background: transparent;
  border: 1px solid var(--field-border); border-radius: 12px;
  padding: 3px 8px;
  transition: color .2s, border-color .2s, transform .15s;
}
.like-btn svg { width: 13px; height: 13px; }
.like-btn:hover { color: #e5718c; border-color: rgba(229, 113, 140, .5); transform: translateY(-1px); }
.like-btn.liked { color: #e5718c; border-color: rgba(229, 113, 140, .55); background: rgba(240, 169, 196, .18); }

/* 收藏按钮：金色调，与点赞区分 */
.fav-btn {
  display: inline-flex; align-items: center; gap: 3px;
  font-family: inherit; font-size: 11.5px; cursor: pointer;
  color: var(--muted); background: transparent;
  border: 1px solid var(--field-border); border-radius: 12px;
  padding: 3px 8px;
  transition: color .2s, border-color .2s, transform .15s;
}
.fav-btn svg { width: 13px; height: 13px; }
.fav-btn:hover { color: #d9a844; border-color: rgba(233, 184, 95, .55); transform: translateY(-1px); }
.fav-btn.active { color: #d9a844; border-color: rgba(233, 184, 95, .6); background: rgba(247, 217, 138, .22); }

/* 我的梦境卡片上的编辑 / 删除按钮 */
.mini-btn {
  font-family: inherit; font-size: 11.5px; cursor: pointer;
  color: var(--muted); background: transparent;
  border: 1px solid var(--field-border); border-radius: 12px;
  padding: 3px 10px;
  transition: color .2s, border-color .2s, transform .15s;
}
.mini-btn:hover { color: #d9a844; border-color: var(--gold-2); transform: translateY(-1px); }
.mini-btn.danger:hover { color: #e5718c; border-color: rgba(229, 113, 140, .55); }

@keyframes fadeUp {
  from { opacity: 0; transform: translateY(12px); }
  to { opacity: 1; transform: translateY(0); }
}

.empty { text-align: center; padding: 30px 0; font-size: 13.5px; color: var(--muted); letter-spacing: .05em; }

/* 加载更多 */
.more { margin-top: 26px; text-align: center; }
.more-btn {
  font-family: inherit; cursor: pointer;
  font-size: 13.5px; letter-spacing: .12em; color: #fff;
  padding: 11px 34px; border: 0; border-radius: 24px;
  background: linear-gradient(120deg, #f0a9c4, #e9b85f);
  box-shadow: 0 10px 22px rgba(233, 184, 95, .34);
  transition: transform .15s, filter .2s, opacity .2s;
}
.more-btn:hover:not(:disabled) { transform: translateY(-2px); filter: brightness(1.04); }
.more-btn:disabled { opacity: .6; cursor: not-allowed; }
.more-hint { margin-top: 24px; text-align: center; font-size: 12.5px; color: var(--muted); letter-spacing: .12em; }

/* 热门推荐（横向） */
.rail {
  display: grid; grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); gap: 14px;
}
.rail-card {
  display: flex; align-items: flex-start; gap: 12px; cursor: pointer;
  background: var(--card-bg); border: 1px solid var(--card-border);
  border-radius: 18px; padding: 16px 18px;
  backdrop-filter: blur(8px);
  box-shadow: 0 10px 26px rgba(214, 158, 184, .16);
  transition: transform .2s, box-shadow .25s;
}
.rail-card:hover { transform: translateY(-3px); box-shadow: 0 16px 34px rgba(214, 158, 184, .28); }
.rail-rank {
  flex: 0 0 auto; width: 26px; height: 26px; border-radius: 9px;
  display: flex; align-items: center; justify-content: center;
  font-size: 13px; color: #fff;
  background: linear-gradient(135deg, #f0a9c4, #e9b85f);
}
.rail-main { min-width: 0; flex: 1; }
.rail-meta {
  margin-top: 8px; display: flex; align-items: center; gap: 12px;
  font-size: 11.5px; color: var(--muted);
}

/* 快捷入口 */
.shortcut-card {
  text-align: left; font-family: inherit; cursor: pointer;
  display: flex; align-items: flex-start; gap: 14px;
  background: var(--card-bg);
  border: 1px solid var(--card-border);
  border-radius: 18px;
  padding: 20px;
  backdrop-filter: blur(8px);
  box-shadow: 0 10px 26px rgba(214, 158, 184, .16);
  transition: transform .2s, box-shadow .25s;
}
.shortcut-card:hover { transform: translateY(-4px); box-shadow: 0 16px 34px rgba(214, 158, 184, .28); }
.sc-ico {
  width: 42px; height: 42px; flex: 0 0 auto; border-radius: 12px;
  display: flex; align-items: center; justify-content: center;
  background: linear-gradient(135deg, #ffd9e8, #ffe9b0);
  color: #c98aa6;
}
.sc-text { display: flex; flex-direction: column; gap: 6px; min-width: 0; }
.sc-title { font-size: 15px; color: var(--ink); letter-spacing: .04em; }
.sc-desc { font-size: 12.5px; line-height: 1.5; color: var(--muted); }

/* 页脚 */
.footer {
  margin-top: 54px; text-align: center;
  font-size: 12px; color: var(--muted); letter-spacing: .12em;
}

/* ===== 详情弹层 ===== */
.detail-mask {
  position: fixed; inset: 0; z-index: 60;
  display: flex; align-items: center; justify-content: center;
  padding: 30px 20px;
  background: rgba(90, 52, 76, .3);
  backdrop-filter: blur(6px);
  animation: maskIn .25s ease both;
}
@keyframes maskIn { from { opacity: 0; } to { opacity: 1; } }

.detail-panel {
  position: relative;
  width: min(680px, 100%); max-height: 82vh; overflow-y: auto;
  padding: 26px 30px 30px;
  border-radius: 24px;
  background: var(--panel-bg);
  border: 1px solid var(--card-border);
  box-shadow: 0 24px 60px rgba(120, 70, 100, .3);
  animation: panelIn .3s ease both;
}
@keyframes panelIn {
  from { opacity: 0; transform: translateY(16px) scale(.98); }
  to { opacity: 1; transform: translateY(0) scale(1); }
}

.detail-close {
  position: absolute; top: 16px; right: 16px;
  width: 34px; height: 34px; border-radius: 50%; cursor: pointer;
  display: flex; align-items: center; justify-content: center;
  color: var(--muted); background: var(--field-bg); border: 1px solid var(--field-border);
  transition: color .2s, transform .15s;
}
.detail-close:hover { color: #e5718c; transform: rotate(90deg); }
.detail-close svg { width: 15px; height: 15px; }

.detail-cover {
  position: relative; height: 118px; border-radius: 16px;
  display: flex; align-items: center; justify-content: center;
  margin-bottom: 18px;
}
.detail-ini { font-size: 52px; font-weight: 600; color: rgba(255, 255, 255, .92); text-shadow: 0 2px 14px rgba(150, 100, 125, .45); }

.detail-title { font-size: 22px; color: var(--ink); letter-spacing: .05em; margin-bottom: 12px; }
.detail-meta {
  display: flex; align-items: center; gap: 10px; flex-wrap: wrap;
  font-size: 12.5px; color: var(--muted);
  padding-bottom: 16px; border-bottom: 1px solid var(--field-border);
}
.detail-text { margin-top: 18px; }
.detail-text p {
  font-size: 14.5px; line-height: 2.05; color: var(--ink);
  letter-spacing: .03em; margin-bottom: 12px;
}
.detail-tags { margin-top: 16px; }

/* ===== 个人中心 ===== */
.profile-card {
  display: flex; align-items: center; gap: 16px;
  padding: 22px 24px;
  background: var(--card-bg);
  border: 1px solid var(--card-border);
  border-radius: 20px;
  backdrop-filter: blur(8px);
  box-shadow: 0 12px 30px rgba(214, 158, 184, .2);
}
.profile-avatar {
  width: 58px; height: 58px; flex: 0 0 auto; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  font-size: 23px; color: #fff; letter-spacing: .04em;
  background: linear-gradient(135deg, #f0a9c4, #e9b85f);
  box-shadow: 0 8px 20px rgba(233, 184, 95, .38);
}
.profile-main { flex: 1; min-width: 0; display: flex; flex-direction: column; gap: 7px; }
.profile-name { font-size: 19px; color: var(--ink); letter-spacing: .06em; }
.profile-desc { font-size: 12.5px; color: var(--muted); letter-spacing: .03em; }
.profile-stats { margin-top: 16px; }

/* ===== 创作 / 编辑弹层 ===== */
.editor-mask {
  position: fixed; inset: 0; z-index: 65;
  display: flex; align-items: center; justify-content: center;
  padding: 26px 20px;
  background: rgba(90, 52, 76, .3);
  backdrop-filter: blur(6px);
  animation: maskIn .25s ease both;
}
.editor-panel {
  position: relative;
  width: min(620px, 100%); max-height: 88vh; overflow-y: auto;
  padding: 24px 28px 26px;
  border-radius: 24px;
  background: var(--panel-bg);
  border: 1px solid var(--card-border);
  box-shadow: 0 24px 60px rgba(120, 70, 100, .3);
  animation: panelIn .3s ease both;
}
.editor-title { font-size: 20px; color: var(--ink); letter-spacing: .05em; }
.editor-sub { margin-top: 8px; font-size: 12.5px; color: var(--muted); letter-spacing: .04em; }

.field { display: block; margin-top: 18px; }
.field-label {
  display: block; margin-bottom: 8px;
  font-size: 12.5px; color: var(--ink); letter-spacing: .06em;
}
.field-label em { color: #e5718c; font-style: normal; margin-left: 3px; }
.field-label em.hint { color: var(--muted); font-size: 11.5px; margin-left: 6px; }
.field input, .field select, .field textarea {
  width: 100%; box-sizing: border-box;
  font-family: inherit; font-size: 14px; color: var(--input-text);
  background: var(--field-bg);
  border: 1px solid var(--field-border);
  border-radius: 14px;
  padding: 11px 14px;
  outline: none;
  transition: border-color .25s, box-shadow .25s;
}
.field textarea { line-height: 1.85; resize: vertical; }
.field input::placeholder, .field textarea::placeholder { color: var(--placeholder); }
.field input:focus, .field select:focus, .field textarea:focus {
  border-color: var(--gold-2);
  box-shadow: 0 0 0 3px rgba(247, 217, 138, .28);
}
.field-tip { display: block; margin-top: 6px; text-align: right; font-size: 11.5px; color: var(--muted); }
.field-row { display: grid; grid-template-columns: 1fr 1fr; gap: 14px; }

.editor-error {
  margin-top: 16px; font-size: 12.5px; color: #d0657d; letter-spacing: .03em;
  background: rgba(240, 169, 196, .16);
  border: 1px solid rgba(229, 113, 140, .35);
  border-radius: 12px; padding: 10px 14px;
}
.editor-foot {
  margin-top: 22px; display: flex; align-items: center; justify-content: flex-end; gap: 12px;
}
.ghost-btn, .danger-btn {
  font-family: inherit; font-size: 13.5px; letter-spacing: .08em; cursor: pointer;
  padding: 10px 22px; border-radius: 22px;
  transition: color .2s, border-color .2s, transform .15s, filter .2s, opacity .2s;
}
.ghost-btn { color: var(--muted); background: var(--field-bg); border: 1px solid var(--field-border); }
.ghost-btn:hover { color: var(--ink); transform: translateY(-1px); }
.danger-btn {
  color: #fff; border: 0;
  background: linear-gradient(120deg, #f0899f, #e2617c);
  box-shadow: 0 8px 20px rgba(226, 97, 124, .34);
}
.danger-btn:hover:not(:disabled) { transform: translateY(-2px); filter: brightness(1.04); }
.danger-btn:disabled { opacity: .6; cursor: not-allowed; }

/* ===== 删除确认 ===== */
.confirm-panel {
  width: min(420px, 100%);
  padding: 26px 28px 22px;
  border-radius: 22px;
  background: var(--panel-bg);
  border: 1px solid var(--card-border);
  box-shadow: 0 24px 60px rgba(120, 70, 100, .3);
  animation: panelIn .28s ease both;
}
.confirm-title { font-size: 18px; color: var(--ink); letter-spacing: .05em; }
.confirm-text { margin-top: 12px; font-size: 13.5px; line-height: 1.8; color: var(--muted); }
.confirm-panel .editor-foot { margin-top: 22px; }

/* 轻提示 */
.toast {
  position: fixed; left: 50%; bottom: 36px;
  transform: translateX(-50%) translateY(20px);
  background: rgba(201, 138, 166, .95); color: #fff;
  padding: 11px 22px; border-radius: 30px;
  font-size: 14px; letter-spacing: .05em; opacity: 0; pointer-events: none;
  transition: opacity .3s, transform .3s; z-index: 70;
  box-shadow: 0 10px 30px rgba(201, 138, 166, .4);
  max-width: 90vw; text-align: center;
}
.toast.show { opacity: 1; transform: translateX(-50%) translateY(0); }

/* 移动端适配 */
@media (max-width: 760px) {
  .nav-menu, .nav-name { display: none; }
  .navbar { gap: 10px; }
  .content { padding-top: 104px; }
  .hero { padding-top: 26px; }
  .hero-title { font-size: 32px; }
  .stats { grid-template-columns: repeat(2, 1fr); }
  .detail-panel { padding: 22px 20px 26px; }
  .detail-title { font-size: 19px; }
  .profile-card { flex-wrap: wrap; }
  .profile-card .search-btn { width: 100%; }
  .field-row { grid-template-columns: 1fr; }
  .editor-panel { padding: 20px 18px 22px; }
  .confirm-panel { padding: 22px 20px 18px; }
}

@media (prefers-reduced-motion: reduce) {
  .dream-card, .detail-mask, .detail-panel, .editor-mask, .editor-panel, .confirm-panel { animation: none; }
}
</style>
