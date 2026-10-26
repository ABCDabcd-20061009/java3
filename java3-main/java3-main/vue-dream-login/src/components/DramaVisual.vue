<template>
  <div class="dv" :class="[size === 'chip' ? 'dv-chip' : 'dv-stage', { 'is-speaking': speaking }]"
       :style="{ '--dv-color': c.color }" :title="c.desc || c.name">
    <svg class="dv-art" viewBox="0 0 200 200" role="img" :aria-label="c.name + (c.role ? '·' + c.role : '')">
      <defs>
        <radialGradient :id="gid + 'bg'" cx="50%" cy="38%" r="78%">
          <stop offset="0%" :stop-color="c.color" stop-opacity=".95" />
          <stop offset="100%" :stop-color="c.color" stop-opacity=".45" />
        </radialGradient>
        <radialGradient :id="gid + 'halo'" cx="50%" cy="50%" r="50%">
          <stop offset="0%" stop-color="#fff" stop-opacity=".34" />
          <stop offset="100%" stop-color="#fff" stop-opacity="0" />
        </radialGradient>
        <linearGradient :id="gid + 'shade'" x1="0" y1="0" x2="0" y2="1">
          <stop offset="0%" stop-color="#000" stop-opacity="0" />
          <stop offset="100%" stop-color="#000" stop-opacity=".26" />
        </linearGradient>
        <linearGradient :id="gid + 'cloth'" x1="0" y1="0" x2="0" y2="1">
          <stop offset="0%" stop-color="#fff" stop-opacity=".26" />
          <stop offset="100%" stop-color="#000" stop-opacity=".22" />
        </linearGradient>
      </defs>

      <!-- 底色：每个角色/场景一个专属色调，底部压暗增强层次 -->
      <rect x="0" y="0" width="200" height="200" :fill="`url(#${gid}bg)`" />
      <rect x="0" y="0" width="200" height="200" :fill="`url(#${gid}shade)`" />
      <circle class="dv-halo" cx="100" cy="96" r="72" :fill="`url(#${gid}halo)`" />

      <!-- 氛围元素：海浪 / 信封 / 星光 / 雨 / 钟 / 灯笼 / 月 / 被窝 / 光 -->
      <g :class="['dv-motif', 'm-' + motif, { 'is-big': !isFigure }]">
        <template v-if="motif === 'sea' || motif === 'wave'">
          <circle cx="146" cy="56" r="15" fill="#fff" opacity=".5" />
          <path d="M28 132 q16 -11 32 0 t32 0 t32 0 t32 0 t32 0" fill="none" stroke="#fff" stroke-width="3"
                stroke-linecap="round" opacity=".42" />
          <path d="M28 150 q16 -11 32 0 t32 0 t32 0 t32 0 t32 0" fill="none" stroke="#fff" stroke-width="3"
                stroke-linecap="round" opacity=".28" />
          <path d="M28 168 q16 -11 32 0 t32 0 t32 0 t32 0 t32 0" fill="none" stroke="#fff" stroke-width="3"
                stroke-linecap="round" opacity=".18" />
        </template>

        <template v-else-if="motif === 'letter'">
          <g opacity=".45">
            <rect x="62" y="86" width="76" height="50" rx="5" fill="none" stroke="#fff" stroke-width="3" />
            <path d="M62 88 L100 116 L138 88" fill="none" stroke="#fff" stroke-width="3" stroke-linejoin="round" />
            <rect x="120" y="52" width="46" height="30" rx="4" fill="none" stroke="#fff" stroke-width="2.4"
                  opacity=".55" transform="rotate(12 143 67)" />
          </g>
        </template>

        <template v-else-if="motif === 'stars'">
          <g fill="#fff">
            <circle cx="62" cy="66" r="2.6" opacity=".8" />
            <circle cx="140" cy="58" r="3.2" opacity=".85" />
            <circle cx="156" cy="104" r="2.2" opacity=".6" />
            <circle cx="52" cy="120" r="2" opacity=".55" />
            <circle cx="118" cy="44" r="2" opacity=".5" />
            <path d="M100 62 l3.4 9.6 9.6 3.4 -9.6 3.4 -3.4 9.6 -3.4 -9.6 -9.6 -3.4 9.6 -3.4Z" opacity=".9" />
            <path d="M74 108 l2.4 6.8 6.8 2.4 -6.8 2.4 -2.4 6.8 -2.4 -6.8 -6.8 -2.4 6.8 -2.4Z" opacity=".6" />
            <path d="M138 132 l2.2 6.2 6.2 2.2 -6.2 2.2 -2.2 6.2 -2.2 -6.2 -6.2 -2.2 6.2 -2.2Z" opacity=".55" />
          </g>
        </template>

        <template v-else-if="motif === 'rain'">
          <g opacity=".45">
            <rect x="58" y="56" width="84" height="92" rx="7" fill="none" stroke="#fff" stroke-width="3" />
            <path d="M100 56 v92 M58 102 h84" stroke="#fff" stroke-width="2.4" opacity=".7" />
            <g stroke="#fff" stroke-width="2.6" stroke-linecap="round">
              <path d="M70 74 l-8 14" /><path d="M92 68 l-8 14" /><path d="M126 76 l-8 14" />
              <path d="M78 122 l-8 14" /><path d="M134 118 l-8 14" />
            </g>
          </g>
        </template>

        <template v-else-if="motif === 'clock'">
          <g opacity=".5">
            <path d="M76 72 L100 46 L124 72Z" fill="#fff" />
            <rect x="82" y="70" width="36" height="86" fill="none" stroke="#fff" stroke-width="3" />
            <circle cx="100" cy="96" r="14" fill="none" stroke="#fff" stroke-width="3" />
            <path d="M100 96 v-8 M100 96 l6 4" stroke="#fff" stroke-width="3" stroke-linecap="round" />
            <path d="M40 164 h120" stroke="#fff" stroke-width="3" stroke-linecap="round" opacity=".7" />
          </g>
        </template>

        <template v-else-if="motif === 'lantern'">
          <g opacity=".5">
            <g fill="none" stroke="#fff" stroke-width="3">
              <ellipse cx="72" cy="86" rx="14" ry="17" /><path d="M62 74 h20 M62 98 h20 M72 69 v-8" />
              <ellipse cx="128" cy="80" rx="11" ry="14" /><path d="M120 70 h16 M120 90 h16 M128 66 v-8" />
              <ellipse cx="104" cy="122" rx="16" ry="19" /><path d="M92 110 h24 M92 134 h24 M104 103 v-9" />
            </g>
            <g fill="#fff" opacity=".6"><circle cx="72" cy="86" r="4" /><circle cx="128" cy="80" r="3" /><circle cx="104" cy="122" r="4.5" /></g>
          </g>
        </template>

        <template v-else-if="motif === 'moons'">
          <g fill="#fff">
            <circle cx="98" cy="90" r="28" opacity=".55" />
            <circle cx="146" cy="60" r="13" opacity=".45" />
            <circle cx="58" cy="132" r="9" opacity=".35" />
            <circle cx="150" cy="140" r="6" opacity=".3" />
          </g>
        </template>

        <template v-else-if="motif === 'quilt'">
          <g opacity=".45">
            <path d="M20 130 q22 -20 44 0 t44 0 t44 0 t44 0" fill="none" stroke="#fff" stroke-width="3.4" stroke-linecap="round" />
            <path d="M20 158 q22 -20 44 0 t44 0 t44 0 t44 0" fill="none" stroke="#fff" stroke-width="3" stroke-linecap="round" opacity=".7" />
            <g fill="#fff"><circle cx="70" cy="70" r="2.6" /><circle cx="122" cy="58" r="3" /><circle cx="150" cy="92" r="2.2" /></g>
          </g>
        </template>

        <template v-else-if="motif === 'glow'">
          <g stroke="#fff" stroke-width="3" stroke-linecap="round" opacity=".5">
            <path d="M100 34 v-14" /><path d="M100 180 v-14" /><path d="M34 100 h-14" /><path d="M180 100 h-14" />
            <path d="M54 54 l-10 -10" /><path d="M146 54 l10 -10" /><path d="M54 146 l-10 10" /><path d="M146 146 l10 10" />
          </g>
          <circle cx="100" cy="100" r="30" fill="#fff" opacity=".28" />
        </template>

        <template v-else-if="motif === 'paw'">
          <g fill="#fff" opacity=".5">
            <ellipse cx="100" cy="116" rx="21" ry="17" />
            <circle cx="76" cy="88" r="7.5" /><circle cx="92" cy="78" r="7.5" />
            <circle cx="110" cy="78" r="7.5" /><circle cx="126" cy="90" r="7.5" />
          </g>
        </template>
      </g>

      <!-- ===== 人物 / 猫 / 小怪兽：每个角色一套专属外型 ===== -->
      <g v-if="c.kind === 'person'" class="dv-figure">
        <!-- 后发（长发） -->
        <path v-if="c.hair === 'long'"
              d="M60 96 C58 54 142 54 140 96 L146 176 L120 176 C126 132 74 132 80 176 L54 176 Z"
              :fill="hairColor" />
        <!-- 身体 -->
        <path d="M50 200 C54 152 74 138 100 138 C126 138 146 152 150 200 Z" :fill="c.cloth" />
        <path d="M50 200 C54 152 74 138 100 138 C126 138 146 152 150 200 Z" :fill="`url(#${gid}cloth)`" />
        <path d="M84 138 L100 160 L116 138 Z" :fill="c.cloth" opacity=".75" />
        <rect v-if="c.acc === 'apron'" x="80" y="150" width="40" height="50" rx="7" fill="#fff" opacity=".28" />
        <!-- 脖子 -->
        <rect x="90" y="122" width="20" height="24" rx="6" :fill="c.skin" />
        <!-- 头 -->
        <ellipse cx="100" :cy="headCy" :rx="headRx" :ry="headRy" :fill="c.skin" />
        <ellipse cx="68" :cy="headCy + 2" rx="6" ry="8" :fill="c.skin" />
        <ellipse cx="132" :cy="headCy + 2" rx="6" ry="8" :fill="c.skin" />
        <!-- 头发 -->
        <path v-if="c.hair === 'short' || c.hair === 'white' || c.hair === 'bun' || c.hair === 'topknot'"
              d="M67 100 C67 60 133 60 133 100 C131 74 120 66 100 66 C80 66 69 74 67 100 Z" :fill="hairColor" />
        <circle v-if="c.hair === 'bun'" cx="100" cy="52" r="13" :fill="hairColor" />
        <g v-if="c.hair === 'topknot'">
          <circle cx="100" cy="50" r="10" :fill="hairColor" />
          <rect x="74" y="62" width="52" height="7" rx="3.5" :fill="c.cloth" />
        </g>
        <g v-if="c.hair === 'child'">
          <path d="M64 92 C64 58 136 58 136 92 C132 72 118 64 100 64 C82 64 68 72 64 92 Z" :fill="hairColor" />
          <circle cx="66" cy="72" r="9" :fill="hairColor" />
          <circle cx="134" cy="72" r="9" :fill="hairColor" />
        </g>
        <g v-if="c.hair === 'cap'">
          <path d="M60 84 C60 56 140 56 140 84 Z" :fill="hairColor" />
          <path d="M60 82 L100 34 L142 84 Z" :fill="c.cloth" />
          <circle cx="100" cy="32" r="8" fill="#fff" opacity=".85" />
        </g>
        <!-- 胡须先画，五官压在上面，避免遮住嘴 -->
        <path v-if="c.beard" d="M74 108 C78 140 122 140 126 108 C122 126 78 126 74 108 Z" :fill="hairColor" />
        <!-- 五官 -->
        <g class="dv-face">
          <ellipse cx="88" :cy="eyeCy" rx="3.6" ry="4.6" fill="#3a3550" />
          <ellipse cx="112" :cy="eyeCy" rx="3.6" ry="4.6" fill="#3a3550" />
          <path :d="`M82 ${eyeCy - 11} q6 -5 12 0`" stroke="#3a3550" stroke-width="2" fill="none" stroke-linecap="round" opacity=".7" />
          <path :d="`M106 ${eyeCy - 11} q6 -5 12 0`" stroke="#3a3550" stroke-width="2" fill="none" stroke-linecap="round" opacity=".7" />
          <ellipse class="dv-mouth" cx="100" :cy="mouthCy" rx="5.5" ry="2.8" fill="#b4705f" />
          <ellipse cx="80" :cy="eyeCy + 12" rx="6" ry="4" fill="#e08a8a" opacity=".28" />
          <ellipse cx="120" :cy="eyeCy + 12" rx="6" ry="4" fill="#e08a8a" opacity=".28" />
        </g>
        <!-- 眼镜 -->
        <g v-if="c.glasses" fill="none" stroke="#3a3550" stroke-width="2.4" opacity=".85">
          <circle cx="88" :cy="eyeCy" r="10" /><circle cx="112" :cy="eyeCy" r="10" />
          <path d="M98 98 q2 -3 4 0" /><path d="M78 97 l-6 -3" /><path d="M122 97 l6 -3" />
        </g>
        <!-- 配饰 -->
        <g v-if="c.acc === 'earpiece'">
          <circle cx="136" cy="106" r="4.5" fill="#2f2f42" />
          <path d="M136 106 q-6 4 -8 14" stroke="#2f2f42" stroke-width="2" fill="none" />
        </g>
        <g v-if="c.acc === 'mic'">
          <rect x="128" y="152" width="9" height="20" rx="4.5" fill="#33334a" />
          <path d="M132 172 q-10 6 -14 -6" stroke="#33334a" stroke-width="2.4" fill="none" />
        </g>
      </g>

      <!-- 猫 -->
      <g v-else-if="c.kind === 'cat'" class="dv-figure">
        <path d="M148 176 q34 -6 30 -34" stroke="#fff" stroke-width="0" fill="none" />
        <path d="M142 172 q30 -14 26 -40" :stroke="c.color" stroke-width="14" fill="none" stroke-linecap="round" />
        <ellipse cx="100" cy="164" rx="44" ry="34" :fill="c.color" />
        <path d="M72 74 L62 34 L98 58 Z" :fill="c.color" />
        <path d="M128 74 L138 34 L102 58 Z" :fill="c.color" />
        <path d="M74 70 L68 44 L90 60 Z" fill="#f6c6a4" />
        <path d="M126 70 L132 44 L110 60 Z" fill="#f6c6a4" />
        <circle cx="100" cy="100" r="36" :fill="c.color" />
        <circle cx="100" cy="100" r="36" fill="#fff" opacity=".08" />
        <ellipse cx="86" cy="96" rx="6" ry="8" fill="#2f2f42" />
        <ellipse cx="114" cy="96" rx="6" ry="8" fill="#2f2f42" />
        <ellipse cx="86" cy="93" rx="2" ry="3" fill="#fff" opacity=".85" />
        <ellipse cx="114" cy="93" rx="2" ry="3" fill="#fff" opacity=".85" />
        <path d="M96 112 L104 112 L100 117 Z" fill="#e0808f" />
        <ellipse class="dv-mouth" cx="100" cy="122" rx="7" ry="3" fill="#b4705f" opacity=".8" />
        <g stroke="#fff" stroke-width="2" stroke-linecap="round" opacity=".7">
          <path d="M64 100 l-16 -4" /><path d="M64 108 l-16 2" /><path d="M136 100 l16 -4" /><path d="M136 108 l16 2" />
        </g>
        <g stroke="#fff" stroke-width="3" opacity=".35" stroke-linecap="round">
          <path d="M88 70 l4 8" /><path d="M100 68 l0 9" /><path d="M112 70 l-4 8" />
        </g>
      </g>

      <!-- 会发光的小怪兽 -->
      <g v-else-if="c.kind === 'monster'" class="dv-figure">
        <circle cx="100" cy="108" r="58" fill="#fff" opacity=".22" class="dv-monster-glow" />
        <path d="M74 66 q-6 -22 14 -22 q6 8 2 20 Z" :fill="c.color" />
        <path d="M126 66 q6 -22 -14 -22 q-6 8 -2 20 Z" :fill="c.color" />
        <ellipse cx="100" cy="118" rx="46" ry="42" :fill="c.color" />
        <ellipse cx="100" cy="126" rx="30" ry="26" fill="#fff" opacity=".22" />
        <circle cx="84" cy="106" r="13" fill="#fff" />
        <circle cx="116" cy="106" r="13" fill="#fff" />
        <circle cx="85" cy="107" r="6.5" fill="#3a3550" />
        <circle cx="117" cy="107" r="6.5" fill="#3a3550" />
        <path d="M88 132 q12 12 24 0" stroke="#3a3550" stroke-width="3.4" fill="none" stroke-linecap="round" />
        <ellipse class="dv-mouth" cx="100" cy="134" rx="9" ry="5" fill="#b4705f" opacity=".85" />
        <rect x="70" y="152" width="20" height="14" rx="7" :fill="c.color" />
        <rect x="110" y="152" width="20" height="14" rx="7" :fill="c.color" />
        <g fill="#fff" opacity=".65">
          <circle cx="56" cy="86" r="3" /><circle cx="146" cy="94" r="3.5" /><circle cx="64" cy="146" r="2.6" /><circle cx="140" cy="146" r="2.6" />
        </g>
      </g>

      <!-- 场景（旁白）：地面剪影 + 氛围主体 -->
      <g v-else class="dv-sceneart">
        <path d="M0 168 q50 -18 100 0 t100 0 v34 h-200Z" fill="#fff" opacity=".14" />
        <path d="M0 182 q50 -14 100 0 t100 0 v20 h-200Z" fill="#fff" opacity=".1" />
      </g>
    </svg>
  </div>
</template>

<script setup>
import { computed } from 'vue'

let seq = 0

const props = defineProps({
  visual: { type: Object, required: true },
  speaking: { type: Boolean, default: false },
  size: { type: String, default: 'stage' }   // stage | chip
})

const gid = `dv${++seq}-`
const c = computed(() => ({
  color: '#5a6b8f', skin: '#f2cdb0', cloth: '#5a6b8f', hair: 'short', motif: 'none', kind: 'person', ...props.visual
}))
const motif = computed(() => c.value.motif || 'none')
const isFigure = computed(() => c.value.kind && c.value.kind !== 'scene')

/* 老人白发 / 儿童头型 / 睡帽等在外型参数上的差异 */
const hairColor = computed(() => (c.value.hair === 'white' ? '#dfe0e6' : (c.value.hairColor || '#4a3a38')))
const headCy = computed(() => (c.value.hair === 'child' ? 102 : 100))
const headRx = computed(() => (c.value.hair === 'child' ? 34 : 32))
const headRy = computed(() => (c.value.hair === 'child' ? 33 : 36))
const eyeCy = computed(() => (c.value.hair === 'child' ? 100 : 98))
const mouthCy = computed(() => (c.value.hair === 'child' ? 118 : 116))
</script>

<style scoped>
.dv { position: relative; display: block; }
.dv-art { display: block; width: 100%; height: 100%; }

.dv-stage { width: 100%; height: 100%; border-radius: 14px; overflow: hidden; }
.dv-chip { width: 100%; height: 100%; border-radius: 50%; overflow: hidden; }

.dv-halo { opacity: .5; transition: opacity .5s ease; }
.dv.is-speaking .dv-halo { opacity: .95; animation: dvPulse 2.4s ease-in-out infinite; }
@keyframes dvPulse { 0%, 100% { opacity: .6; } 50% { opacity: 1; } }

/* 角色说话时：整体轻微起伏 + 嘴部开合 */
.dv-figure { transform-box: fill-box; transform-origin: 50% 100%; }
.dv.is-speaking .dv-figure { animation: dvBob 2.6s ease-in-out infinite; }
@keyframes dvBob { 0%, 100% { transform: translateY(0); } 50% { transform: translateY(-3px); } }

.dv-mouth { transform-box: fill-box; transform-origin: 50% 0%; }
.dv.is-speaking .dv-mouth { animation: dvTalk .62s ease-in-out infinite; }
@keyframes dvTalk { 0%, 100% { transform: scaleY(.6); } 50% { transform: scaleY(1.5); } }

.dv-monster-glow { transform-box: fill-box; transform-origin: 50% 50%; animation: dvBreathe 3.6s ease-in-out infinite; }
@keyframes dvBreathe { 0%, 100% { transform: scale(.92); opacity: .18; } 50% { transform: scale(1.08); opacity: .34; } }

/* 氛围元素：人物画面上小且淡，场景画面上大且居中 */
.dv-motif { transform-box: view-box; transform-origin: 50% 50%; transition: transform .4s ease, opacity .4s ease; opacity: .5; }
.dv-motif.is-big { opacity: .95; transform: scale(1.02); }

@media (prefers-reduced-motion: reduce) {
  .dv.is-speaking .dv-figure,
  .dv.is-speaking .dv-mouth,
  .dv.is-speaking .dv-halo,
  .dv-monster-glow { animation: none; }
}
</style>
