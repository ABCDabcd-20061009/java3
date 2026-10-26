// 临时预览入口：一次性渲染全部广播剧角色画面，用于人工核对立绘效果
import { createApp, h } from 'vue'
import DramaVisual from './components/DramaVisual.vue'
import { DRAMA_SCRIPTS, castOf } from './data/dramaScripts'

const App = {
  setup() {
    return () =>
      h('div', {}, Object.keys(DRAMA_SCRIPTS).map(t =>
        h('section', {}, [
          h('h3', {}, t),
          h('div', { class: 'grid' }, castOf(t).map(p =>
            h('div', { class: 'cell' }, [
              h('div', { class: 'av' }, [h(DramaVisual, { visual: p, speaking: false, size: 'stage' })]),
              h('p', {}, [p.name, h('small', {}, p.role ? ' · ' + p.role : '')])
            ])
          ))
        ])
      ))
  }
}

createApp(App).mount('#app')
