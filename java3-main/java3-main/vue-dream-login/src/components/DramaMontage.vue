<template>
  <div class="montage">
    <!-- 每个故事一段不同的角色动画：场景 → 依次出现本剧所有人物（各有专属画面） -->
    <div class="mt-stage">
      <div
        v-for="(s, i) in slides"
        :key="i"
        class="mt-slide"
        :class="{ active: i === index }"
      >
        <div class="mt-kb">
          <DramaVisual :visual="s.visual" :speaking="false" size="stage" />
        </div>
        <div class="mt-caption">
          <span class="mt-name">{{ s.name }}</span>
          <span class="mt-role" v-if="s.role">{{ s.role }}</span>
        </div>
      </div>

      <span class="mt-badge">▶ 本剧角色动画 · 每部不同</span>
      <span class="mt-vignette"></span>
    </div>

    <!-- 进度点：可点击跳转，便于对照人物看画面 -->
    <div class="mt-dots">
      <button
        v-for="(s, i) in slides"
        :key="'d' + i"
        type="button"
        class="mt-dot"
        :class="{ active: i === index }"
        :title="s.name"
        @click="go(i)"
      ></button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, watch, nextTick } from 'vue'
import { castOf, sceneOf } from '../data/dramaScripts'
import DramaVisual from './DramaVisual.vue'

const props = defineProps({
  title: { type: String, default: '' }
})

const SLIDE_MS = 3600

/* 场景 + 本剧所有人物（旁白用场景画面表示），构成这部剧专属的"视频" */
const slides = computed(() => {
  const sc = sceneOf(props.title)
  const sceneSlide = {
    name: (props.title || '本剧') + ' · 场景',
    role: sc && sc.desc ? sc.desc : '场景氛围',
    visual: { name: 'scene', kind: 'scene', motif: sc.motif, color: sc.color, desc: sc.desc }
  }
  const cast = castOf(props.title).map(p => ({
    name: p.name,
    role: p.role || (p.kind === 'scene' ? '旁白' : ''),
    visual: p
  }))
  return [sceneSlide, ...cast]
})

const index = ref(0)
let timer = null

function clear() {
  if (timer) { clearInterval(timer); timer = null }
}
function start() {
  clear()
  if (slides.value.length <= 1) return
  timer = setInterval(() => {
    index.value = (index.value + 1) % slides.value.length
  }, SLIDE_MS)
}
function go(i) {
  index.value = i
  start() // 手动跳转后重置计时，避免刚点完就被自动翻走
}

watch(() => props.title, async () => {
  index.value = 0
  await nextTick()
  start()
})

onMounted(start)
onBeforeUnmount(clear)
</script>

<style scoped>
.montage {
  position: absolute; inset: 0;
  display: flex; flex-direction: column;
  border-radius: 16px; overflow: hidden;
  background: linear-gradient(135deg, #2c3e6b, #5b6fb0);
}
.mt-stage {
  position: relative; flex: 1; min-height: 0;
  overflow: hidden;
}
.mt-slide {
  position: absolute; inset: 0;
  display: flex; align-items: center; justify-content: center;
  opacity: 0; transition: opacity .9s ease;
  pointer-events: none;
}
.mt-slide.active { opacity: 1; pointer-events: auto; }

/* Ken Burns：当前画面缓慢推近，更像"视频"而非静止图 */
.mt-kb {
  width: 78%; height: 78%;
  border-radius: 16px; overflow: hidden;
  border: 1px solid rgba(255, 255, 255, .3);
  box-shadow: 0 14px 36px rgba(20, 12, 30, .4);
  transform: scale(1.02);
  animation: mtKB 7s ease-in-out infinite alternate;
}
.mt-slide.active .mt-kb { animation: mtKB 7s ease-in-out infinite alternate; }
@keyframes mtKB {
  from { transform: scale(1.0) translateY(0); }
  to   { transform: scale(1.08) translateY(-2%); }
}

.mt-caption {
  position: absolute; left: 0; right: 0; bottom: 14px;
  display: flex; align-items: baseline; justify-content: center; gap: 10px;
  flex-wrap: wrap; padding: 0 16px;
}
.mt-name {
  font-size: 17px; letter-spacing: .14em; color: #fff;
  padding: 3px 12px; border-radius: 12px;
  background: rgba(28, 16, 34, .5); backdrop-filter: blur(4px);
  text-shadow: 0 2px 10px rgba(40, 20, 50, .5);
}
.mt-role {
  font-size: 12.5px; letter-spacing: .06em; color: rgba(255, 255, 255, .82);
  text-shadow: 0 1px 8px rgba(40, 20, 50, .5);
}

.mt-badge {
  position: absolute; top: 12px; left: 12px;
  font-size: 11px; letter-spacing: .06em; color: #fff;
  padding: 4px 10px; border-radius: 12px;
  background: rgba(28, 16, 34, .42); backdrop-filter: blur(4px);
}
.mt-vignette {
  position: absolute; inset: 0; pointer-events: none;
  background: radial-gradient(120% 80% at 50% 50%, rgba(0,0,0,0) 55%, rgba(20,12,30,.5) 100%);
}

.mt-dots {
  display: flex; align-items: center; justify-content: center; gap: 8px;
  padding: 8px; background: rgba(20, 12, 24, .3);
}
.mt-dot {
  width: 9px; height: 9px; border-radius: 50%; cursor: pointer;
  border: 0; padding: 0;
  background: rgba(255, 255, 255, .35);
  transition: background .25s, transform .2s, width .25s;
}
.mt-dot:hover { background: rgba(255, 255, 255, .6); }
.mt-dot.active {
  width: 22px; border-radius: 5px;
  background: linear-gradient(120deg, #f0a9c4, #e9b85f);
}

@media (prefers-reduced-motion: reduce) {
  .mt-kb { animation: none; }
  .mt-slide { transition: none; }
}
</style>
