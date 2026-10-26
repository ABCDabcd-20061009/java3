<template>
  <div class="drama-stage">
    <!-- ===== 头部：标题 + 搜索 + 热门平台 ===== -->
    <section class="drama-hero">
      <div class="hero-badge">
        <svg viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
          <path d="M3 9.5 12 5l9 4.5v5L12 19l-9-4.5z" /><path d="M3 9.5 12 14l9-4.5" /><path d="M12 14v5" />
        </svg>
      </div>
      <h1 class="hero-title">广播剧 · 小说声画剧场</h1>
      <p class="hero-sub">挑一部热门平台的小说广播剧，边听声音边看画面 ✦</p>

      <form class="search-bar" @submit.prevent="doSearch">
        <svg class="ico" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
          <circle cx="11" cy="11" r="7" /><path d="M20 20l-3.5-3.5" />
        </svg>
        <input type="text" v-model="keyword" placeholder="搜索广播剧、主角或关键词…" maxlength="50" @input="clearHint" />
        <button class="search-btn" type="submit" :disabled="searching">{{ searching ? '搜索中…' : '搜 索' }}</button>
      </form>

      <div class="hot">
        <span class="hot-label">热门平台：</span>
        <button class="chip" :class="{ active: activePlatform === '' }" type="button" @click="selectPlatform('')">全部</button>
        <button class="chip" v-for="p in platforms" :key="p" type="button"
                :class="{ active: activePlatform === p }" @click="selectPlatform(p)">{{ p }}</button>
      </div>
    </section>

    <!-- ===== 列表区 ===== -->
    <section class="section">
      <div class="section-head">
        <span class="dot"></span>
        <span>{{ sectionTitle }}</span>
        <span class="count">{{ sectionCount }}</span>
        <span class="mode-tag" v-if="isDemo">演示模式</span>
      </div>

      <div class="filters">
        <div class="tabs">
          <button class="tab" :class="{ active: activeCategory === '' }" type="button" @click="selectCategory('')">全部</button>
          <button class="tab" v-for="c in categories" :key="c.code || c.name" type="button"
                  :class="{ active: activeCategory === c.name }" @click="selectCategory(c.name)">
            {{ c.name }}<span class="tab-count">{{ c.count }}</span>
          </button>
        </div>
        <div class="sort-switch">
          <button class="sort-btn" :class="{ active: activeSort === 'newest' }" type="button" @click="setSort('newest')">最新</button>
          <button class="sort-btn" :class="{ active: activeSort === 'hot' }" type="button" @click="setSort('hot')">最热</button>
        </div>
      </div>

      <div class="empty" v-if="!list.length && !loading">{{ emptyText }}</div>
      <div class="cards" v-else>
        <article class="drama-card" v-for="(d, i) in list" :key="d.id"
                 :style="{ animationDelay: (i * 0.05) + 's' }" @click="openPlayer(d)">
          <div class="card-cover" :class="'cover-' + (d.categoryCode || 'other')">
            <span class="card-tag">{{ d.category }}</span>
            <span class="cover-ini">{{ (d.title || '剧').charAt(0) }}</span>
            <span class="play-badge">
              <svg viewBox="0 0 24 24" fill="currentColor"><path d="M8 5v14l11-7z" /></svg>
              播放
            </span>
            <span class="platform-badge" v-if="d.platform">{{ d.platform }}</span>
          </div>
          <div class="card-body">
            <div class="card-title">{{ d.title }}</div>
            <div class="card-desc">{{ d.summary }}</div>
            <div class="tags" v-if="d.tags && d.tags.length">
              <span class="tag" v-for="t in d.tags.slice(0, 3)" :key="t">#{{ t }}</span>
            </div>
          </div>
          <div class="card-foot">
            <span class="foot-dur" v-if="d.duration">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="9"/><path d="M12 7v5l3 2"/></svg>
              {{ d.duration }}
            </span>
            <span class="foot-spacer"></span>
            <span class="foot-stat" title="浏览量">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round"><path d="M2.5 12S6 5.5 12 5.5 21.5 12 21.5 12 18 18.5 12 18.5 2.5 12 2.5 12Z"/><circle cx="12" cy="12" r="3"/></svg>
              {{ formatNum(d.views) }}
            </span>
            <span class="foot-stat" title="点赞">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round"><path d="M12 20s-7.5-4.6-7.5-9.4A4.1 4.1 0 0 1 12 8.1a4.1 4.1 0 0 1 7.5 2.5C19.5 15.4 12 20 12 20Z"/></svg>
              {{ formatNum(d.likes) }}
            </span>
          </div>
        </article>
      </div>

      <div class="more" v-if="hasMore">
        <button class="more-btn" type="button" :disabled="loading" @click="loadMore">
          {{ loading ? '加载中…' : '加载更多' }}
        </button>
      </div>
      <div class="more-hint" v-else-if="list.length">已经到底啦 ✦</div>
    </section>

    <footer class="footer">广 播 剧 · 小 说 改 编 · 声 画 同 享 ✦</footer>

    <!-- ===== 播放器弹层（有声音也有画面） ===== -->
    <div class="player-mask" v-if="player" @click.self="closePlayer">
      <div class="player-panel">
        <button class="player-close" type="button" @click="closePlayer" aria-label="关闭">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"><path d="M6 6l12 12M18 6L6 18"/></svg>
        </button>

        <!-- 播放模式切换：既有剧本又有视频时才出现，默认进剧场 -->
        <div class="play-modes" v-if="hasScript && hasVideoSource">
          <button class="mode-btn" :class="{ active: playMode === 'theatre' }" type="button" @click="setMode('theatre')">声画剧场</button>
          <button class="mode-btn" :class="{ active: playMode === 'video' }" type="button" @click="setMode('video')">视频画面</button>
        </div>

        <div class="player-media" :class="{ 'is-theatre': playMode === 'theatre' }">
          <!-- ===== 声画剧场：逐句朗读本剧小说片段 ===== -->
          <div class="theatre" v-if="playMode === 'theatre'" :class="'theatre-' + (player.categoryCode || 'other')">
            <span class="theatre-tint" :style="{ background: tintBg }"></span>
            <span class="theatre-glow"></span>

            <!-- 舞台：左=该角色专属画面，右=台词字幕 -->
            <div class="theatre-stage">
              <div class="stage-art">
                <DramaVisual :key="curVisual.name" :visual="curVisual" :speaking="speaking" size="stage" />
                <span class="stage-tag">{{ curVisual.kind === 'scene' ? '场景' : (curVisual.role || '角色') }}</span>
              </div>
              <div class="stage-caption">
                <p class="line-speaker" v-if="curLine && curLine.speaker">
                  <span class="sp-name">{{ curLine.speaker }}</span>
                  <span class="sp-role" v-if="curVisual.desc">{{ curVisual.desc }}</span>
                </p>
                <p class="line-text" :key="lineIndex">{{ curLine ? curLine.text : '' }}</p>
              </div>
            </div>

            <!-- 角色栏：本剧所有人物，当前说话者高亮，点击可跳到他的台词 -->
            <div class="theatre-cast" v-if="castList.length">
              <button v-for="p in castList" :key="p.name" type="button" class="cast-chip"
                      :class="{ active: p.name === curSpeaker, mute: p.name !== curSpeaker }"
                      :title="p.desc || p.name" @click="jumpToSpeaker(p.name)">
                <span class="cast-av"><DramaVisual :visual="p" :speaking="false" size="chip" /></span>
                <span class="cast-name">{{ p.name }}</span>
              </button>
            </div>

            <div class="theatre-foot">
              <button class="tf-btn primary" type="button" @click="toggleSpeak">{{ speaking ? '暂停' : '播放' }}</button>
              <button class="tf-btn" type="button" @click="prevLine">上一句</button>
              <button class="tf-btn" type="button" @click="nextLine">下一句</button>
              <span class="tf-prog">{{ lineIndex + 1 }} / {{ scriptLines.length }}</span>
              <label class="tf-rate">语速
                <input type="range" min="0.6" max="1.6" step="0.1" v-model.number="speakRate" @change="onRateChange" />
              </label>
            </div>
            <p class="theatre-tip" v-if="!ttsReady">当前浏览器没有可用的中文语音，已改为自动翻页（仍可逐句阅读）</p>
          </div>

          <!-- ===== 视频 / 音频播放 ===== -->
          <template v-else>
            <video v-if="player.videoUrl && !forceAudio" :key="'v' + mediaKey" ref="videoEl" class="player-video"
                   :src="player.videoUrl" :poster="player.posterUrl || player.coverUrl"
                   controls playsinline preload="metadata"
                   @error="onMediaError('video')" @waiting="onMediaWaiting"
                   @canplay="onMediaReady" @playing="onMediaReady"></video>
            <audio v-else-if="player.audioUrl" :key="'a' + mediaKey" ref="audioEl" class="player-audio"
                   :src="player.audioUrl" controls preload="metadata"
                   @error="onMediaError('audio')" @waiting="onMediaWaiting"
                   @canplay="onMediaReady" @playing="onMediaReady"></audio>
            <div v-else class="no-media">该广播剧暂无可播放的媒体，请稍后再来 ✦</div>

            <!-- 媒体状态层：加载中 / 加载失败（可重试或回退音频），避免出现「点了没反应」 -->
            <div class="media-state" v-if="mediaState === 'loading'">
              <span class="spinner"></span>
              <span>正在加载媒体…</span>
            </div>
            <div class="media-state is-error" v-else-if="mediaState === 'error'">
              <p class="err-title">媒体加载失败</p>
              <p class="err-desc">{{ mediaErrorText }}</p>
              <div class="err-actions">
                <button class="err-btn" type="button" @click="retryMedia">重新加载</button>
                <button class="err-btn ghost" type="button" v-if="player.audioUrl" @click="useAudio">改用音频播放</button>
              </div>
            </div>
          </template>
        </div>

        <div class="player-meta">
          <span class="mini-avatar">{{ (player.title || '剧').charAt(0) }}</span>
          <span class="player-cat">{{ player.category }}</span>
          <span class="player-platform" v-if="player.platform">{{ player.platform }}</span>
          <span class="foot-spacer"></span>
          <span class="foot-stat" v-if="player.duration">{{ player.duration }}</span>
          <span class="foot-stat">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round"><path d="M2.5 12S6 5.5 12 5.5 21.5 12 21.5 12 18 18.5 12 18.5 2.5 12 2.5 12Z"/><circle cx="12" cy="12" r="3"/></svg>
            {{ formatNum(player.views) }} 次播放
          </span>
        </div>

        <h2 class="player-title">{{ player.title }}</h2>
        <p class="player-summary" v-if="player.summary">{{ player.summary }}</p>
        <div class="tags detail-tags" v-if="player.tags && player.tags.length">
          <span class="tag" v-for="t in player.tags" :key="t">#{{ t }}</span>
        </div>
        <p class="player-tip">
          <template v-if="playMode === 'theatre'">
            声画剧场：逐句朗读本剧小说片段，可调语速、前后翻句 ✦
          </template>
          <template v-else>
            提示：视频为画面占位素材，若未自动播放请点击播放按钮 🔊
            <span v-if="mutedHint" class="tip-muted">（浏览器拦截了带声自动播放，已先静音播放，点一下音量图标即可恢复 🔈）</span>
          </template>
        </p>
      </div>
    </div>

    <div class="toast" :class="{ show: toastMsg }">{{ toastMsg }}</div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, watch, nextTick } from 'vue'
import { fetchDramaList, fetchDramaDetail, fetchDramaCategories } from '../api'
import { scriptOf, castOf, visualOf } from '../data/dramaScripts'
import DramaVisual from './DramaVisual.vue'

/* ---- 轻提示 ---- */
const toastMsg = ref('')
let timer = null
function flash(msg) {
  toastMsg.value = msg
  clearTimeout(timer)
  timer = setTimeout(() => (toastMsg.value = ''), 2600)
}

/* ---- 状态 ---- */
const categories = ref([])
const platforms = ['喜马拉雅', '猫耳FM', '晋江文学城', '蜻蜓FM', '凯叔讲故事']
const activeCategory = ref('')
const activePlatform = ref('')
const activeSort = ref('newest')
const keyword = ref('')
const searching = ref(false)
const searched = ref(false)
const lastKeyword = ref('')

const list = ref([])
const total = ref(0)
const page = ref(1)
const DRAMA_SIZE = 8
const hasMore = ref(false)
const loading = ref(false)
const isDemo = ref(false)

const player = ref(null)
const videoEl = ref(null)
const audioEl = ref(null)

/* ---- 播放器媒体状态 ---- */
const mediaState = ref('')          // '' | 'loading' | 'error'
const mediaErrorText = ref('')
const forceAudio = ref(false)       // 视频失败后手动回退到音频
const mediaKey = ref(0)             // 变更即强制重建 media 元素（重试用）
const mutedHint = ref(false)        // 是否被浏览器静音自动播放

/* ---- 声画剧场：逐句朗读小说正文 ---- */
/* 示例视频只是画面占位、与小说内容无关，真正的内容在剧本里，所以默认走剧场模式 */
const playMode = ref('video')       // 'video' | 'theatre'
const lineIndex = ref(0)
const speaking = ref(false)
const speakRate = ref(1)
const ttsReady = ref(true)          // 浏览器是否有可用的中文语音
let speakTimer = null

const scriptLines = computed(() => (player.value ? scriptOf(player.value.title) : []))
const hasScript = computed(() => scriptLines.value.length > 0)
const hasVideoSource = computed(() => !!(player.value && (player.value.videoUrl || player.value.audioUrl)))
const curLine = computed(() => scriptLines.value[lineIndex.value] || null)

/* ---- 每个角色一套专属画面：说话人 → 该角色立绘；旁白 → 本剧场景画面 ---- */
const castList = computed(() => (player.value ? castOf(player.value.title) : []))
const curVisual = computed(() => visualOf(player.value ? player.value.title : '', curLine.value))
const curSpeaker = computed(() => (curLine.value && curLine.value.speaker) || '')
const tintBg = computed(() => `linear-gradient(135deg, ${curVisual.value.color}E6, ${curVisual.value.color}66)`)

/* 点角色头像 → 跳到他第一次开口的那句（方便对照人物看画面） */
function jumpToSpeaker(name) {
  const i = scriptLines.value.findIndex(l => l && l.speaker === name)
  if (i < 0) return
  const was = speaking.value
  stopSpeak()
  lineIndex.value = i
  if (was) speakCurrent()
}

function pickVoice() {
  const synth = window.speechSynthesis
  if (!synth) return null
  const vs = synth.getVoices() || []
  return vs.find(v => /zh|cmn|chinese|huihui|yaoyao|xiaoxiao/i.test(`${v.lang} ${v.name}`)) || vs[0] || null
}

function stopSpeak() {
  clearTimeout(speakTimer)
  speakTimer = null
  try { window.speechSynthesis && window.speechSynthesis.cancel() } catch (_) { /* 忽略 */ }
  speaking.value = false
}

/* 没有可用语音时，按字数估算时长自动翻页，保证仍能逐句「看到」小说内容 */
function fallbackAdvance() {
  speaking.value = true
  const len = curLine.value ? curLine.value.text.length : 10
  const ms = Math.min(9000, Math.max(1600, len * 230))
  speakTimer = setTimeout(() => { if (speaking.value) advance(true) }, ms)
}

function speakCurrent() {
  stopSpeak()
  const line = curLine.value
  if (!line) return
  const synth = window.speechSynthesis
  const voice = pickVoice()
  if (synth && voice) {
    ttsReady.value = true
    const u = new SpeechSynthesisUtterance(line.text)
    u.voice = voice
    u.lang = voice.lang || 'zh-CN'
    u.rate = Number(speakRate.value) || 1
    u.onend = () => { if (speaking.value) advance(true) }
    u.onerror = () => { if (speaking.value) fallbackAdvance() }
    speaking.value = true
    try { synth.speak(u) } catch (_) { fallbackAdvance() }
  } else {
    ttsReady.value = false
    fallbackAdvance()
  }
}

function advance(auto) {
  if (lineIndex.value < scriptLines.value.length - 1) {
    lineIndex.value += 1
    if (auto) speakCurrent()
  } else {
    stopSpeak()
  }
}

function toggleSpeak() {
  if (speaking.value) stopSpeak()
  else speakCurrent()
}
function prevLine() {
  const was = speaking.value
  stopSpeak()
  if (lineIndex.value > 0) lineIndex.value -= 1
  if (was) speakCurrent()
}
function nextLine() {
  const was = speaking.value
  stopSpeak()
  if (lineIndex.value < scriptLines.value.length - 1) {
    lineIndex.value += 1
    if (was) speakCurrent()
  }
}
function onRateChange() {
  if (speaking.value) speakCurrent()
}
function pauseMediaEls() {
  ;[videoEl.value, audioEl.value].forEach(el => {
    if (!el) return
    try { el.pause() } catch (_) { /* 忽略 */ }
  })
}
function setMode(m) {
  playMode.value = m
  if (m === 'theatre') {
    pauseMediaEls()
    lineIndex.value = 0
    nextTick(speakCurrent)
  } else {
    stopSpeak()
    nextTick(() => { tryAutoPlay(false) })
  }
}

const sectionTitle = computed(() => (searched.value ? '搜索结果' : '广播剧精选'))
const sectionCount = computed(() => `共 ${formatNum(total.value)} 部`)
const emptyText = computed(() =>
  searched.value ? '没有找到相关广播剧，换个关键词试试吧 ✦' : '这里还空空的，换换分类或平台看看吧 ✦')

/* ---- 热门平台 / 分类筛选 ---- */
function selectCategory(name) {
  activeCategory.value = name
  resetList()
}
function selectPlatform(p) {
  activePlatform.value = p
  resetList()
}
function setSort(sort) {
  if (activeSort.value === sort) return
  activeSort.value = sort
  resetList()
}

/* ---- 搜索 ---- */
function clearHint() {
  if (!keyword.value.trim()) searched.value = false
}
async function doSearch() {
  const kw = keyword.value.trim()
  if (!kw) { flash('请输入搜索关键词'); return }
  if (searching.value) return
  searched.value = true
  lastKeyword.value = kw
  resetList()
}

/* ---- 列表加载 ---- */
async function loadList(append = false) {
  if (loading.value) return
  loading.value = true
  try {
    const res = await fetchDramaList({
      page: page.value,
      size: DRAMA_SIZE,
      keyword: searched.value ? lastKeyword.value : '',
      category: activeCategory.value,
      platform: activePlatform.value,
      sort: activeSort.value
    })
    const data = (res && res.data) || {}
    const rows = data.list || []
    isDemo.value = false
    list.value = append ? list.value.concat(rows) : rows
    total.value = data.total != null ? data.total : list.value.length
    hasMore.value = !!data.hasMore
  } catch (e) {
    if (e && e.isNetwork) {
      enterDemo()
    } else {
      flash((e && e.message) || '广播剧加载失败，请稍后重试')
      hasMore.value = false
    }
  } finally {
    loading.value = false
  }
}
function resetList() {
  page.value = 1
  loadList(false)
}
function loadMore() {
  if (!hasMore.value || loading.value) return
  page.value += 1
  loadList(true)
}

/* ---- 分类导航 ---- */
async function loadCategories() {
  try {
    const res = await fetchDramaCategories()
    const data = (res && res.data) || []
    categories.value = Array.isArray(data) ? data : []
  } catch (e) {
    if (e && e.isNetwork) {
      categories.value = ['悬疑', '言情', '科幻', '治愈', '古风', '儿童'].map(name => ({
        name, code: codeOf(name), count: 0
      }))
    }
  }
}

/* ---- 媒体地址归一化：把不可达的外链换成后端本地媒体 ---- */
/* 旧种子数据把 video_url 指向 Google 公开示例桶（commondatastorage.googleapis.com），
   国内网络访问不到会导致黑屏「播放不了」。这里按文件名映射到本地 /media/drama 下的示例视频，
   即使数据库还没迁移也能正常播放。 */
const LEGACY_VIDEO_MAP = {
  'BigBuckBunny.mp4': 'bunny-trailer.mp4',
  'ElephantsDream.mp4': 'sintel-trailer.mp4',
  'ForBiggerBlazes.mp4': 'flower.mp4',
  'ForBiggerEscapes.mp4': 'friday.mp4',
  'ForBiggerFun.mp4': 'bunny-trailer.mp4',
  'ForBiggerJoyrides.mp4': 'sintel-trailer.mp4',
  'ForBiggerMeltdowns.mp4': 'flower.mp4',
  'Sintel.mp4': 'friday.mp4',
  'TearsOfSteel.mp4': 'bunny-trailer.mp4'
}
function normalizeMedia(url) {
  if (!url) return ''
  const m = /\/([^/?#]+\.mp4)$/i.exec(String(url))
  if (m && LEGACY_VIDEO_MAP[m[1]]) return '/media/drama/' + LEGACY_VIDEO_MAP[m[1]]
  if (/commondatastorage\.googleapis\.com/i.test(String(url))) return '/media/drama/sintel-trailer.mp4'
  return url
}

/* ---- 详情 / 播放 ---- */
async function openPlayer(card) {
  if (!card) return
  forceAudio.value = false
  mutedHint.value = false
  mediaState.value = 'loading'
  mediaErrorText.value = ''
  lineIndex.value = 0
  // 有剧本就默认进「声画剧场」：示例视频跟小说无关，剧场才是内容本体
  playMode.value = scriptOf(card.title).length ? 'theatre' : 'video'
  if (isLocalCard(card)) {
    player.value = { ...card, videoUrl: normalizeMedia(card.videoUrl), audioUrl: card.audioUrl || '' }
    return
  }
  player.value = {
    ...card,
    videoUrl: normalizeMedia(card.videoUrl),
    audioUrl: card.audioUrl || ''
  }
  try {
    const res = await fetchDramaDetail({ id: card.id })
    if (res && res.data) {
      const d = res.data
      player.value = {
        ...player.value, ...d,
        videoUrl: normalizeMedia(d.videoUrl),
        audioUrl: d.audioUrl || ''
      }
      // 浏览量 +1 后同步回列表卡片
      const hit = list.value.find(x => x.id === card.id)
      if (hit) hit.views = d.views
    }
  } catch (e) {
    if (!(e && e.isNetwork)) flash((e && e.message) || '详情加载失败')
  }
}

function closePlayer() {
  // 关闭时暂停并释放媒体，避免弹层关掉后声音还在后台响
  ;[videoEl.value, audioEl.value].forEach(el => {
    if (!el) return
    try { el.pause() } catch (_) { /* 忽略 */ }
    try { el.removeAttribute('src'); el.load() } catch (_) { /* 忽略 */ }
  })
  player.value = null
  mediaState.value = ''
  mediaErrorText.value = ''
  mutedHint.value = false
  stopSpeak()
  lineIndex.value = 0
}

/* ---- 媒体加载状态 ---- */
function onMediaWaiting() {
  if (mediaState.value !== 'error') mediaState.value = 'loading'
}
function onMediaReady() {
  mediaState.value = ''
}
function onMediaError(kind) {
  mediaState.value = 'error'
  mediaErrorText.value = kind === 'video'
    ? '视频源加载失败：可能是后端未启动、媒体文件缺失或网络不通'
    : '音频源加载失败：可能是后端未启动、媒体文件缺失或网络不通'
}
function retryMedia() {
  mediaState.value = 'loading'
  mediaErrorText.value = ''
  mediaKey.value += 1
  nextTick(() => { tryAutoPlay(true) })
}
function useAudio() {
  forceAudio.value = true
  mediaState.value = 'loading'
  mediaErrorText.value = ''
  nextTick(() => { tryAutoPlay(true) })
}

/* 弹层出现后尝试自动播放：带声被拦截时退化为静音播放（控件仍可手动出声） */
async function tryAutoPlay(isRetry = false) {
  const el = (!forceAudio.value && videoEl.value) ? videoEl.value : audioEl.value
  if (!el || !el.play) return
  try {
    await el.play()
    mutedHint.value = false
  } catch (_) {
    try {
      el.muted = true
      await el.play()
      mutedHint.value = true
    } catch (e2) {
      // 自动播放彻底失败：交给 controls 手动播放；若是资源本身有问题则提示
      if (isRetry) onMediaError(forceAudio.value ? 'audio' : 'video')
    }
  }
}

watch(player, async (val) => {
  if (!val) return
  mediaState.value = 'loading'
  mutedHint.value = false
  await nextTick()
  if (playMode.value === 'theatre') {
    lineIndex.value = 0
    speakCurrent()
  } else {
    tryAutoPlay(false)
  }
})

/* ---- 演示模式数据源（后端未启动时，用本地广播剧库保证可浏览） ----
   视频地址统一指向后端托管的 /media/drama（项目本地文件，不依赖外网），
   与数据库 t_drama.video_url 保持一致。 */
const DRAMA_MEDIA = '/media/drama/'
const DRAMA_LIB = [
  { title: '雾港谜案', summary: '潮水退去后，码头石阶上多了一枚没有主人的旧怀表。', category: '悬疑', tags: ['悬疑', '推理', '怀表'], platform: '喜马拉雅', duration: '全 8 集', videoUrl: DRAMA_MEDIA + 'bunny-trailer.mp4', views: 821, likes: 142 },
  { title: '晚风寄来的情书', summary: '一封没有邮票的信，在三个人的抽屉里辗转了十年。', category: '言情', tags: ['言情', '信', '十年'], platform: '猫耳FM', duration: '全 6 集', videoUrl: DRAMA_MEDIA + 'sintel-trailer.mp4', views: 654, likes: 98 },
  { title: '星轨之外', summary: '最后一艘方舟驶离地球时，她在舷窗上写下一个名字。', category: '科幻', tags: ['科幻', '方舟', '末日'], platform: '蜻蜓FM', duration: '12:34', videoUrl: DRAMA_MEDIA + 'flower.mp4', views: 932, likes: 177 },
  { title: '猫咪咖啡馆的午后', summary: '一只不爱说话的猫，治愈了所有推门进来的人。', category: '治愈', tags: ['治愈', '猫咪', '咖啡馆'], platform: '喜马拉雅', duration: '全 10 集', videoUrl: DRAMA_MEDIA + 'sintel-trailer.mp4', views: 1180, likes: 263 },
  { title: '长安月明', summary: '上元灯夜里，一位画师为素未谋面的女子画了一整面墙的月亮。', category: '古风', tags: ['古风', '长安', '灯会'], platform: '猫耳FM', duration: '全 9 集', videoUrl: DRAMA_MEDIA + 'friday.mp4', views: 743, likes: 151 },
  { title: '会发光的小怪兽', summary: '夜里躲在被窝发光的它，其实只是想陪你一起怕黑。', category: '儿童', tags: ['儿童', '怪兽', '陪伴'], platform: '凯叔讲故事', duration: '全 12 集', videoUrl: DRAMA_MEDIA + 'flower.mp4', views: 560, likes: 119 },
  { title: '第七次钟声', summary: '钟楼每响一次，就有一个人忘记今天发生过的事。', category: '悬疑', tags: ['悬疑', '钟楼', '失忆'], platform: '喜马拉雅', duration: '全 7 集', videoUrl: DRAMA_MEDIA + 'sintel-trailer.mp4', views: 689, likes: 133 },
  { title: '时间旅人的信', summary: '寄给十年前的自己，却先被十年后的人收下了。', category: '科幻', tags: ['科幻', '时间', '信'], platform: '蜻蜓FM', duration: '全 5 集', videoUrl: DRAMA_MEDIA + 'sintel-trailer.mp4', views: 877, likes: 190 },
  { title: '海边的旧信封', summary: '退潮后沙滩上出现一只漂流瓶，里面是三十年前的告白。', category: '治愈', tags: ['治愈', '海边', '漂流瓶'], platform: '喜马拉雅', duration: '11:20', videoUrl: DRAMA_MEDIA + 'bunny-trailer.mp4', views: 1024, likes: 208 }
]

function codeOf(name) {
  const map = { '悬疑': 'suspense', '言情': 'romance', '科幻': 'sci-fi', '治愈': 'healing', '古风': 'ancient', '儿童': 'children' }
  return map[name] || 'other'
}

function localCards() {
  // id 取负数：既有唯一 key，又能标记「非后端数据」
  return DRAMA_LIB.map((d, i) => ({
    id: -(i + 1),
    title: d.title, summary: d.summary, category: d.category,
    categoryCode: codeOf(d.category), tags: d.tags,
    platform: d.platform, duration: d.duration,
    videoUrl: d.videoUrl, audioUrl: '', coverUrl: '', posterUrl: '',
    views: d.views, likes: d.likes
  }))
}

function enterDemo() {
  const wasDemo = isDemo.value
  isDemo.value = true
  if (!wasDemo) flash('后端未启动，已进入演示模式（媒体为公开示例）')
  if (!categories.value.length) {
    categories.value = ['悬疑', '言情', '科幻', '治愈', '古风', '儿童'].map(name => ({ name, code: codeOf(name), count: 0 }))
  }
  const lib = localCards()
  list.value = lib.filter(d =>
    (!activeCategory.value || d.category === activeCategory.value) &&
    (!activePlatform.value || d.platform === activePlatform.value) &&
    (!searched.value || (d.title + (d.summary || '') + d.category).includes(lastKeyword.value))
  )
  total.value = list.value.length
  hasMore.value = false
}

function isLocalCard(card) {
  return !card || !card.id || card.id < 0
}

/* ---- 数字格式化：12345 → 1.2万 ---- */
function formatNum(n) {
  const v = Number(n) || 0
  if (v >= 10000) return (v / 10000).toFixed(1).replace(/\.0$/, '') + '万'
  return String(v)
}

onMounted(() => {
  loadCategories()
  loadList(false)
  // 预热语音列表：部分浏览器首次 getVoices() 返回空，需等 onvoiceschanged
  if (window.speechSynthesis) {
    try { window.speechSynthesis.getVoices() } catch (_) { /* 忽略 */ }
  }
})

onBeforeUnmount(() => {
  stopSpeak()
})
</script>

<style scoped>
.drama-stage {
  min-height: 100%;
  padding: 122px 24px 40px;
  max-width: 1040px;
  margin: 0 auto;
}

/* ===== 头部 ===== */
.drama-hero { text-align: center; padding: 30px 0 6px; }
.hero-badge {
  width: 56px; height: 56px; border-radius: 18px; margin: 0 auto 16px;
  display: flex; align-items: center; justify-content: center;
  background: linear-gradient(135deg, #ffc6dd, #f7d98a);
  box-shadow: 0 10px 24px rgba(233, 184, 95, .4);
}
.hero-badge svg { width: 28px; height: 28px; }
.hero-title {
  font-size: 36px; font-weight: 600; letter-spacing: .1em;
  background: linear-gradient(120deg, #c98aa6, #e9b85f 60%, #c98aa6);
  -webkit-background-clip: text; background-clip: text; color: transparent;
}
.hero-sub { margin-top: 10px; font-size: 13px; letter-spacing: .12em; color: var(--sub); }

.search-bar {
  margin: 26px auto 0; width: min(640px, 100%);
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
  transition: color .2s, border-color .2s, transform .15s, background .25s;
}
.chip:hover { color: #e9b85f; border-color: var(--gold-2); transform: translateY(-1px); }
.chip.active { color: #fff; border-color: transparent; background: linear-gradient(120deg, #f0a9c4, #e9b85f); }

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

/* ===== 筛选 ===== */
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
.drama-card {
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
.drama-card:hover { transform: translateY(-4px); box-shadow: 0 16px 34px rgba(214, 158, 184, .3); }

.card-cover {
  position: relative; height: 120px; border-radius: 14px; overflow: hidden;
  display: flex; align-items: center; justify-content: center;
  margin-bottom: 12px;
}
.cover-suspense { background: linear-gradient(135deg, #5b6fb0, #2c3e6b); }
.cover-romance { background: linear-gradient(135deg, #ff9ec4, #ffd0a6); }
.cover-sci-fi { background: linear-gradient(135deg, #5ec5d8, #6f8cff); }
.cover-healing { background: linear-gradient(135deg, #a9e5c9, #ffe0a8); }
.cover-ancient { background: linear-gradient(135deg, #e6c98a, #cfa86a); }
.cover-children { background: linear-gradient(135deg, #ffb3c9, #b8e0ff); }
.cover-other { background: linear-gradient(135deg, #cfe3ff, #e6d6ff); }

.card-tag {
  position: absolute; top: 10px; left: 10px;
  display: inline-block; font-size: 11px; letter-spacing: .08em; color: #b3708c;
  padding: 3px 10px; border-radius: 10px;
  background: rgba(255, 255, 255, .82);
}
.cover-ini {
  font-size: 44px; font-weight: 600; letter-spacing: .06em;
  color: rgba(255, 255, 255, .92);
  text-shadow: 0 2px 12px rgba(80, 50, 90, .45);
}
.play-badge {
  position: absolute; bottom: 10px; left: 10px;
  display: inline-flex; align-items: center; gap: 5px;
  font-size: 12px; letter-spacing: .05em; color: #fff;
  padding: 5px 12px; border-radius: 14px;
  background: rgba(0, 0, 0, .42); backdrop-filter: blur(4px);
  transition: background .2s, transform .15s;
}
.drama-card:hover .play-badge { background: rgba(0, 0, 0, .6); transform: scale(1.04); }
.play-badge svg { width: 13px; height: 13px; }
.platform-badge {
  position: absolute; top: 10px; right: 10px;
  font-size: 10.5px; letter-spacing: .03em; color: #6a4a5e;
  padding: 3px 9px; border-radius: 10px;
  background: rgba(255, 255, 255, .88);
}

.card-body { flex: 1; }
.card-title { font-size: 15.5px; color: var(--ink); letter-spacing: .03em; margin-bottom: 8px; }
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
.foot-dur { display: inline-flex; align-items: center; gap: 3px; white-space: nowrap; }
.foot-dur svg { width: 13px; height: 13px; }
.foot-spacer { flex: 1; }
.foot-stat { display: inline-flex; align-items: center; gap: 3px; white-space: nowrap; }
.foot-stat svg { width: 13px; height: 13px; }

.empty { text-align: center; padding: 30px 0; font-size: 13.5px; color: var(--muted); letter-spacing: .05em; }
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

.footer {
  margin-top: 54px; text-align: center;
  font-size: 12px; color: var(--muted); letter-spacing: .12em;
}

/* ===== 播放器弹层 ===== */
.player-mask {
  position: fixed; inset: 0; z-index: 60;
  display: flex; align-items: center; justify-content: center;
  padding: 30px 20px;
  background: rgba(90, 52, 76, .42);
  backdrop-filter: blur(6px);
  animation: maskIn .25s ease both;
}
@keyframes maskIn { from { opacity: 0; } to { opacity: 1; } }
.player-panel {
  position: relative;
  width: min(720px, 100%); max-height: 86vh; overflow-y: auto;
  padding: 22px 26px 26px;
  border-radius: 24px;
  background: var(--panel-bg);
  border: 1px solid var(--card-border);
  box-shadow: 0 24px 60px rgba(120, 70, 100, .35);
  animation: panelIn .3s ease both;
}
@keyframes panelIn {
  from { opacity: 0; transform: translateY(16px) scale(.98); }
  to { opacity: 1; transform: translateY(0) scale(1); }
}
.player-close {
  position: absolute; top: 14px; right: 14px;
  width: 34px; height: 34px; border-radius: 50%; cursor: pointer;
  display: flex; align-items: center; justify-content: center;
  color: var(--muted); background: var(--field-bg); border: 1px solid var(--field-border);
  transition: color .2s, transform .15s;
}
.player-close:hover { color: #e5718c; transform: rotate(90deg); }
.player-close svg { width: 15px; height: 15px; }

.player-media {
  position: relative; width: 100%; aspect-ratio: 16 / 9;
  border-radius: 16px; overflow: hidden; margin-bottom: 16px;
  background: linear-gradient(135deg, #2c3e6b, #5b6fb0);
  display: flex; align-items: center; justify-content: center;
}
/* 剧场模式要放画面 + 字幕 + 角色栏，给足高度，避免控制条被裁掉 */
.player-media.is-theatre { aspect-ratio: 4 / 3; }
.player-video { width: 100%; height: 100%; object-fit: contain; background: #000; }
.player-audio { width: 100%; }
.no-media { color: rgba(255, 255, 255, .9); font-size: 14px; letter-spacing: .05em; padding: 20px; text-align: center; }

/* 媒体加载 / 失败状态浮层 */
.media-state {
  position: absolute; inset: 0; z-index: 2;
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  gap: 8px; padding: 18px; text-align: center;
  background: rgba(28, 20, 30, .72);
  color: rgba(255, 255, 255, .92);
  font-size: 13px; letter-spacing: .05em;
  animation: maskIn .2s ease both;
  /* 加载中不挡住播放器控件，用户仍可手动点击播放 */
  pointer-events: none;
}
.media-state.is-error { pointer-events: auto; background: rgba(72, 30, 46, .86); }
.media-state .spinner {
  width: 26px; height: 26px; border-radius: 50%;
  border: 2.5px solid rgba(255, 255, 255, .28);
  border-top-color: #f7d98a;
  animation: spin .8s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }
.media-state .err-title { margin: 0; font-size: 15px; color: #ffd9e4; letter-spacing: .08em; }
.media-state .err-desc { margin: 0; font-size: 12.5px; line-height: 1.8; color: rgba(255, 255, 255, .78); max-width: 420px; }
.media-state .err-actions { margin-top: 6px; display: flex; gap: 10px; flex-wrap: wrap; justify-content: center; }
.media-state .err-btn {
  font-family: inherit; cursor: pointer; font-size: 12.5px; letter-spacing: .06em;
  color: #fff; border: 0; padding: 8px 18px; border-radius: 18px;
  background: linear-gradient(120deg, #f0a9c4, #e9b85f);
  box-shadow: 0 6px 16px rgba(233, 184, 95, .3);
  transition: transform .15s, filter .2s;
}
.media-state .err-btn:hover { transform: translateY(-1px); filter: brightness(1.05); }
.media-state .err-btn.ghost {
  background: transparent; border: 1px solid rgba(255, 255, 255, .45);
  color: rgba(255, 255, 255, .9); box-shadow: none;
}
.tip-muted { color: #d9a844; }

/* ===== 播放模式切换 ===== */
.play-modes { display: flex; gap: 6px; margin-bottom: 12px; }
.mode-btn {
  font-family: inherit; cursor: pointer; font-size: 12.5px; letter-spacing: .06em;
  color: var(--muted); background: var(--field-bg); border: 1px solid var(--field-border);
  padding: 6px 16px; border-radius: 16px;
  transition: color .2s, background .25s, transform .15s, box-shadow .25s;
}
.mode-btn:hover { color: var(--ink); transform: translateY(-1px); }
.mode-btn.active {
  color: #fff; border-color: transparent;
  background: linear-gradient(120deg, #f0a9c4, #e9b85f);
  box-shadow: 0 6px 14px rgba(233, 184, 95, .32);
}

/* ===== 声画剧场：逐句朗读小说正文 ===== */
.theatre {
  position: absolute; inset: 0;
  display: flex; flex-direction: column; overflow: hidden;
  border-radius: 16px;
}
.theatre-suspense { background: linear-gradient(135deg, #2c3e6b, #5b6fb0); }
.theatre-romance  { background: linear-gradient(135deg, #ff9ec4, #ffd0a6); }
.theatre-sci-fi   { background: linear-gradient(135deg, #5ec5d8, #6f8cff); }
.theatre-healing  { background: linear-gradient(135deg, #a9e5c9, #ffe0a8); }
.theatre-ancient  { background: linear-gradient(135deg, #e6c98a, #cfa86a); }
.theatre-children { background: linear-gradient(135deg, #ffb3c9, #b8e0ff); }
.theatre-other    { background: linear-gradient(135deg, #cfe3ff, #e6d6ff); }

.theatre-glow {
  position: absolute; left: 50%; top: 42%;
  width: 62%; aspect-ratio: 1; border-radius: 50%;
  background: radial-gradient(circle, rgba(255, 255, 255, .34), rgba(255, 255, 255, 0) 70%);
  animation: breathe 7s ease-in-out infinite;
  pointer-events: none;
}
@keyframes breathe {
  0%, 100% { transform: translate(-50%, -50%) scale(.9); opacity: .5; }
  50%      { transform: translate(-50%, -50%) scale(1.15); opacity: .85; }
}

/* 当前说话角色的专属色调，随台词切换而渐变 */
.theatre-tint {
  position: absolute; inset: 0; pointer-events: none;
  transition: background .8s ease;
}

.theatre-stage {
  position: relative; flex: 1; min-height: 0;
  display: flex; align-items: center; gap: 18px;
  padding: 16px 20px 6px;
}
.stage-art {
  position: relative; flex: 0 0 auto;
  width: min(40%, 190px); aspect-ratio: 1 / 1;
  border-radius: 14px; overflow: hidden;
  border: 1px solid rgba(255, 255, 255, .34);
  box-shadow: 0 10px 26px rgba(30, 16, 40, .34);
  animation: artIn .45s ease both;
}
@keyframes artIn {
  from { opacity: 0; transform: translateY(6px) scale(.98); }
  to   { opacity: 1; transform: none; }
}
.stage-tag {
  position: absolute; left: 8px; bottom: 8px;
  font-size: 10.5px; letter-spacing: .12em;
  padding: 2px 8px; border-radius: 10px;
  color: #fff; background: rgba(28, 16, 34, .46); backdrop-filter: blur(4px);
}
.stage-caption {
  flex: 1; min-width: 0; max-height: 100%; overflow-y: auto;
  display: flex; flex-direction: column; gap: 8px;
}
.theatre .line-speaker {
  margin: 0; display: flex; align-items: baseline; gap: 8px; flex-wrap: wrap;
}
.sp-name {
  font-size: 12.5px; letter-spacing: .18em; color: #fff;
  padding: 2px 9px; border-radius: 10px;
  background: rgba(255, 255, 255, .18); border: 1px solid rgba(255, 255, 255, .3);
}
.sp-role { font-size: 11.5px; letter-spacing: .06em; color: rgba(255, 255, 255, .8); }
.theatre .line-text {
  margin: 0;
  font-size: 18px; line-height: 1.85; font-weight: 500; letter-spacing: .04em;
  color: #fff; text-shadow: 0 2px 14px rgba(40, 20, 50, .45);
  animation: lineIn .5s ease both;
}

/* 角色栏：本剧所有人物，当前说话者高亮 */
.theatre-cast {
  position: relative;
  display: flex; align-items: center; gap: 10px;
  padding: 8px 14px; overflow-x: auto;
  background: rgba(20, 12, 24, .28); backdrop-filter: blur(6px);
}
.cast-chip {
  flex: 0 0 auto; display: flex; flex-direction: column; align-items: center; gap: 3px;
  padding: 2px; border: 0; background: none; cursor: pointer; font-family: inherit;
  transition: transform .2s ease, opacity .25s ease;
}
.cast-av {
  display: block; width: 36px; height: 36px; border-radius: 50%; overflow: hidden;
  border: 2px solid rgba(255, 255, 255, .35);
  transition: border-color .25s, box-shadow .25s;
}
.cast-name { font-size: 11px; letter-spacing: .04em; color: rgba(255, 255, 255, .85); }
.cast-chip.mute { opacity: .55; }
.cast-chip:hover { transform: translateY(-2px); opacity: 1; }
.cast-chip.active .cast-av {
  border-color: #f7d98a;
  box-shadow: 0 0 0 3px rgba(247, 217, 138, .28), 0 6px 14px rgba(30, 16, 40, .4);
}
.cast-chip.active .cast-name { color: #fff; font-weight: 600; }
@keyframes lineIn {
  from { opacity: 0; transform: translateY(8px); }
  to   { opacity: 1; transform: none; }
}

.theatre-foot {
  position: relative;
  display: flex; align-items: center; gap: 8px; flex-wrap: wrap;
  padding: 11px 16px;
  background: rgba(20, 12, 24, .34); backdrop-filter: blur(6px);
}
.tf-btn {
  font-family: inherit; cursor: pointer; font-size: 12.5px; letter-spacing: .05em;
  color: rgba(255, 255, 255, .92);
  background: rgba(255, 255, 255, .16); border: 1px solid rgba(255, 255, 255, .28);
  padding: 6px 14px; border-radius: 16px;
  transition: background .2s, transform .15s;
}
.tf-btn:hover { background: rgba(255, 255, 255, .3); transform: translateY(-1px); }
.tf-btn.primary {
  color: #fff; border-color: transparent;
  background: linear-gradient(120deg, #f0a9c4, #e9b85f);
  box-shadow: 0 6px 14px rgba(233, 184, 95, .32);
}
.tf-prog { font-size: 12px; color: rgba(255, 255, 255, .82); letter-spacing: .06em; }
.tf-rate {
  display: inline-flex; align-items: center; gap: 6px;
  font-size: 12px; color: rgba(255, 255, 255, .86); letter-spacing: .04em;
}
.tf-rate input { width: 84px; accent-color: #f7d98a; }
.theatre-tip {
  margin: 0; padding: 7px 16px;
  font-size: 11.5px; letter-spacing: .04em; color: rgba(255, 255, 255, .85);
  background: rgba(20, 12, 24, .5);
}

@media (prefers-reduced-motion: reduce) {
  .theatre-glow { animation: none; }
  .theatre .line-text { animation: none; }
  .stage-art { animation: none; }
}

.player-meta {
  display: flex; align-items: center; gap: 10px; flex-wrap: wrap;
  font-size: 12.5px; color: var(--muted);
  padding-bottom: 14px; border-bottom: 1px solid var(--field-border);
}
.mini-avatar {
  width: 26px; height: 26px; flex: 0 0 auto; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  font-size: 12px; color: #fff;
  background: linear-gradient(135deg, #f0a9c4, #e9b85f);
}
.player-cat {
  font-size: 12px; color: #b3708c;
  padding: 3px 10px; border-radius: 10px; background: rgba(255, 255, 255, .7);
}
.player-platform {
  font-size: 11.5px; color: #6a4a5e;
  padding: 3px 10px; border-radius: 10px; background: rgba(247, 217, 138, .25);
}
.player-title { font-size: 21px; color: var(--ink); letter-spacing: .05em; margin: 16px 0 10px; }
.player-summary { font-size: 14px; line-height: 1.9; color: var(--ink); letter-spacing: .02em; }
.detail-tags { margin-top: 14px; }
.player-tip { margin-top: 14px; font-size: 12px; color: var(--muted); letter-spacing: .03em; }

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

@keyframes fadeUp {
  from { opacity: 0; transform: translateY(12px); }
  to { opacity: 1; transform: translateY(0); }
}

@media (max-width: 760px) {
  .drama-stage { padding-top: 104px; }
  .hero-title { font-size: 26px; }
  .cards { grid-template-columns: repeat(auto-fill, minmax(150px, 1fr)); }
  .player-panel { padding: 18px 16px 20px; }
  .player-title { font-size: 18px; }
}
@media (prefers-reduced-motion: reduce) {
  .drama-card, .player-mask, .player-panel { animation: none; }
}
</style>
