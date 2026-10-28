<template>
  <div class="df" :class="[size === 'chip' ? 'df-chip' : 'df-stage', { speaking, isScene }]" :style="{ '--df-color': color }">
    <!-- 电视剧式剧照：存在则展示（电影信箱黑边 + 字幕条 + 慢推），加载失败自动回退 SVG -->
    <img v-if="src && !broken" class="df-img" :src="src" :alt="name" @error="broken = true" />

    <!-- 兜底：无图时沿用原有程序化 SVG 角色画面，保证页面始终可用 -->
    <DramaVisual v-else :visual="visual" :speaking="speaking" :size="size" />

    <template v-if="src && !broken">
      <span class="df-lb df-lb-top"></span>
      <span class="df-lb df-lb-bot"></span>
      <span class="df-vignette"></span>
      <span v-if="size !== 'chip'" class="df-topbar">
        <span class="df-ep">EP</span><span class="df-t">{{ title }}</span>
      </span>
      <span v-if="size !== 'chip'" class="df-sub">
        <span class="df-name">{{ displayName }}</span>
        <span class="df-role" v-if="role">{{ role }}</span>
      </span>
    </template>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import DramaVisual from './DramaVisual.vue'
import { sceneImg, charImg } from '../data/dramaMedia'

const props = defineProps({
  title: { type: String, default: '' },
  name: { type: String, default: '' },
  visual: { type: Object, required: true },
  role: { type: String, default: '' },
  speaking: { type: Boolean, default: false },
  size: { type: String, default: 'stage' }   // stage | chip
})

const broken = ref(false)
const isScene = computed(() => props.visual?.kind === 'scene' || props.name === '旁白')
const color = computed(() => props.visual?.color || '#5a6b8f')
const src = computed(() => {
  if (!props.title) return ''
  return isScene.value ? sceneImg(props.title) : charImg(props.title, props.name)
})
const displayName = computed(() =>
  props.name === '旁白' ? (props.title ? props.title + ' · 场景' : '场景') : props.name
)
</script>

<style scoped>
.df { position: relative; width: 100%; height: 100%; overflow: hidden; background: #0d0f1a; }
.df-chip { border-radius: 50%; }
.df-stage { border-radius: 14px; }

.df-img {
  position: absolute; inset: 0; width: 100%; height: 100%;
  object-fit: cover; display: block; transform: scale(1.02);
  animation: dfKB 9s ease-in-out infinite alternate;
}
@keyframes dfKB { from { transform: scale(1.0); } to { transform: scale(1.09); } }
.df.isScene .df-img { filter: saturate(1.06); }

/* 电影信箱黑边 */
.df-lb { position: absolute; left: 0; right: 0; height: 7%; background: #000; z-index: 3; pointer-events: none; }
.df-lb-top { top: 0; }
.df-lb-bot { bottom: 0; }

/* 暗角（电影质感） */
.df-vignette {
  position: absolute; inset: 0; z-index: 2; pointer-events: none;
  background: radial-gradient(120% 85% at 50% 45%, rgba(0,0,0,0) 52%, rgba(0,0,0,.55) 100%);
}
.df-stage.isScene .df-vignette { background: radial-gradient(120% 85% at 50% 45%, rgba(0,0,0,0) 38%, rgba(0,0,0,.62) 100%); }

/* 顶部 EP 条 */
.df-topbar {
  position: absolute; top: 7%; left: 0; right: 0; z-index: 4;
  display: flex; align-items: center; gap: 8px; padding: 8px 12px; pointer-events: none;
}
.df-ep {
  font-size: 10px; font-weight: 700; letter-spacing: .12em; color: #fff;
  background: var(--df-color); padding: 2px 7px; border-radius: 5px;
  box-shadow: 0 2px 8px rgba(0,0,0,.45);
}
.df-t { font-size: 11px; letter-spacing: .08em; color: rgba(255,255,255,.85); text-shadow: 0 1px 6px rgba(0,0,0,.6); }

/* 底部字幕条：角色名 + 身份 */
.df-sub {
  position: absolute; bottom: 7%; left: 0; right: 0; z-index: 4;
  display: flex; align-items: baseline; gap: 8px; flex-wrap: wrap;
  padding: 8px 14px; pointer-events: none;
  background: linear-gradient(0deg, rgba(0,0,0,.72), rgba(0,0,0,0));
}
.df-name { font-size: 16px; letter-spacing: .14em; color: #fff; text-shadow: 0 2px 10px rgba(0,0,0,.7); }
.df-role { font-size: 12px; letter-spacing: .05em; color: rgba(255,255,255,.82); text-shadow: 0 1px 8px rgba(0,0,0,.6); }

.df.speaking .df-img { animation-duration: 6s; }

@media (prefers-reduced-motion: reduce) {
  .df-img { animation: none; }
}
</style>
