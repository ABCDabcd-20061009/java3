// 首页栏目配置：默认值 + 接口加载 + 快捷入口预置图标
// 约定：/api/home/config 返回 data = { navMenu, shortcut, platform, text }
// 接口不可用（后端未启动等）时回落到内置默认值，首页不会开天窗。

import { fetchHomeConfig } from './api'

/** 站点文案内置默认值（与后端 SiteConfigInit 种子保持一致） */
export const DEFAULT_CONFIG = {
  navMenu: [
    { key: 'square', label: '动漫人物', target: 'square' },
    { key: 'favorites', label: '我的收藏', target: 'favorites' },
    { key: 'profile', label: '我的梦境', target: 'profile' },
    { key: 'drama', label: '广播剧', target: 'drama' },
    { key: 'help', label: '帮助中心', target: 'help' }
  ],
  shortcut: [
    { key: 'write_dream', title: '写一场梦', desc: '把昨夜的梦记录下来', iconKey: 'edit', target: 'profile' },
    { key: 'go_favorites', title: '我的收藏', desc: '回头看看收藏的梦', iconKey: 'heart', target: 'favorites' },
    { key: 'go_drama', title: '广播剧', desc: '戴上耳机听一场梦', iconKey: 'play', target: 'drama' },
    { key: 'go_square', title: '动漫人物', desc: '看看喜欢的动漫角色', iconKey: 'star', target: 'square' },
    { key: 'go_profile', title: '个人中心', desc: '我的创作与数据', iconKey: 'user', target: 'profile' }
  ],
  platform: ['喜马拉雅', '猫耳FM', '晋江文学城', '蜻蜓FM', '凯叔讲故事'],
  text: {
    brand_name: '粉金梦境',
    hero_sub: '记录、分享、聆听 —— 每一场梦都值得被温柔收藏',
    footer: '粉金梦境 · 愿你夜夜好梦 ✦'
  }
}

/** 已拉取缓存：进入首页后通常拉一次就够（loadHomeConfig(true) 可强制刷新） */
let cached = null

/**
 * 拉取首页栏目配置。
 * @param {boolean} force true 时跳过缓存强制拉取（管理员刚在后台改过配置的场景）
 * @returns {Promise<{navMenu:Array, shortcut:Array, platform:Array, text:Object}>}
 *          失败时返回内置默认值，绝不抛出异常
 */
export async function loadHomeConfig(force = false) {
  if (cached && !force) {
    return cached
  }
  try {
    const res = await fetchHomeConfig()
    const data = (res && res.data) || {}
    // 逐块校验：后端某一块缺失时用默认值补齐，避免页面局部空白
    const cfg = {
      navMenu: Array.isArray(data.navMenu) && data.navMenu.length ? data.navMenu : DEFAULT_CONFIG.navMenu,
      shortcut: Array.isArray(data.shortcut) && data.shortcut.length ? data.shortcut : DEFAULT_CONFIG.shortcut,
      platform: Array.isArray(data.platform) ? data.platform : DEFAULT_CONFIG.platform,
      text: { ...DEFAULT_CONFIG.text, ...(data.text || {}) }
    }
    // 站点文案的固定键必须有值，防止后台误停用后首页留白
    for (const k of Object.keys(DEFAULT_CONFIG.text)) {
      if (!cfg.text[k]) {
        cfg.text[k] = DEFAULT_CONFIG.text[k]
      }
    }
    cached = cfg
    return cfg
  } catch (_) {
    // 接口异常：回落内置默认值，首页照常渲染
    return { ...DEFAULT_CONFIG, text: { ...DEFAULT_CONFIG.text } }
  }
}

/* ==================== 快捷入口预置图标 ==================== */

const ICONS = {
  star: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M12 3l2.2 4.6L19 8.4l-3.6 3.3.9 4.9L12 14.4 7.7 16.6l.9-4.9L5 8.4l4.8-.8z"/></svg>',
  moon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M21 12.8A9 9 0 1 1 11.2 3a7 7 0 0 0 9.8 9.8Z"/></svg>',
  heart: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M12 20s-7.5-4.6-7.5-9.4A4.1 4.1 0 0 1 12 8.1a4.1 4.1 0 0 1 7.5 2.5C19.5 15.4 12 20 12 20Z"/></svg>',
  book: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20V4H6.5A2.5 2.5 0 0 0 4 6.5v13z"/><path d="M4 19.5A2.5 2.5 0 0 0 6.5 22H20v-5"/></svg>',
  edit: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M12 20h9"/><path d="M16.5 3.5a2.1 2.1 0 0 1 3 3L7 19l-4 1 1-4Z"/></svg>',
  play: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="9"/><path d="M10 8.5l6 3.5-6 3.5z"/></svg>',
  music: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M9 18V5l12-2v13"/><circle cx="6" cy="18" r="3"/><circle cx="18" cy="16" r="3"/></svg>',
  search: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><circle cx="11" cy="11" r="7"/><path d="M20 20l-3.5-3.5"/></svg>',
  user: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="8" r="4"/><path d="M4 21c0-4 3.6-7 8-7s8 3 8 7"/></svg>',
  cloud: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M17.5 19a4.5 4.5 0 0 0 .4-9A7 7 0 0 0 4.3 12.7 3.9 3.9 0 0 0 6 19.5z"/></svg>',
  fire: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M12 22c4.4 0 7-2.8 7-6.5 0-4.5-4-6-4.5-9.5C13 7 11 8 11 10.5 9.7 9.8 9 8.5 9 7c-1.6 1.6-4 4.4-4 8.5C5 19.2 7.6 22 12 22Z"/></svg>'
}

/** 按后台配置的 iconKey 取预置图标，未知 key 回落「星光」 */
export function iconSvg(key) {
  return ICONS[key] || ICONS.star
}
