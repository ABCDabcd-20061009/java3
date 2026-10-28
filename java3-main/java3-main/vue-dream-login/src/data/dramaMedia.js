// 广播剧「电视剧式」素材路径映射
// ---------------------------------------------------------------
// 这些文件由 ImageGen（角色/场景剧照）与 VideoGen（每部剧视频）生成后，
// 直接放入对应路径即可自动生效；缺失时 DramaFrame / 视频分支会自动回退到
// 现有 SVG 画面或角色动画蒙太奇，页面照常可用。
//
// 命名规则（生成时请严格按此命名，便于一键回填）：
//   视频    public/drama-media/videos/<slug>.mp4
//   场景图  public/drama-media/scenes/<slug>.png
//   角色图  public/drama-media/characters/<slug>_<id>.png
// ---------------------------------------------------------------

const SLUG = {
  '雾港谜案': 'fog-harbor',
  '晚风寄来的情书': 'love-letter',
  '星轨之外': 'star-track',
  '猫咪咖啡馆的午后': 'cat-cafe',
  '长安月明': 'changan-moon',
  '会发光的小怪兽': 'glow-monster',
  '第七次钟声': 'seventh-bell',
  '时间旅人的信': 'time-letter',
  '海边的旧信封': 'seaside-letter'
}

export function slugOf(title) {
  return SLUG[String(title || '').trim()] || 'drama'
}

const BASE = '/drama-media'

export function sceneImg(title) {
  return `${BASE}/scenes/${slugOf(title)}.png`
}

export function videoFor(title) {
  return `${BASE}/videos/${slugOf(title)}.mp4`
}

// 角色剧照：显式文件名（生成时按此命名）。旁白 / 未知角色回退到场景图。
export const DRAMA_IMG = {
  '雾港谜案':       { '沈屿': 'fog-harbor_shenyu.png',  '阿婆': 'fog-harbor_apo.png' },
  '晚风寄来的情书': { '陈叙': 'love-letter_chenxu.png', '周晚': 'love-letter_zhouwan.png' },
  '星轨之外':       { '祁昭': 'star-track_qizhao.png',  '归档员': 'star-track_guiyuan.png' },
  '猫咪咖啡馆的午后': { '店主': 'cat-cafe_dianzhu.png',  '客人': 'cat-cafe_keren.png', '阿布': 'cat-cafe_abu.png' },
  '长安月明':       { '裴昀': 'changan-moon_peiyun.png', '小童': 'changan-moon_xiaotong.png' },
  '会发光的小怪兽':   { '豆豆': 'glow-monster_doudou.png', '噜噜': 'glow-monster_lulu.png' },
  '第七次钟声':     { '老周': 'seventh-bell_laozhou.png', '记者': 'seventh-bell_jizhe.png' },
  '时间旅人的信':   { '收信人': 'time-letter_shouxinren.png', '旅人': 'time-letter_lvren.png' },
  '海边的旧信封':   { '老人': 'seaside-letter_laoren.png', '孙女': 'seaside-letter_sunnv.png' }
}

export function charImg(title, name) {
  const file = DRAMA_IMG[String(title || '').trim()]?.[String(name || '').trim()]
  if (file) return `${BASE}/characters/${file}`
  return sceneImg(title) // 旁白 / 未登记角色：用场景图兜底
}
