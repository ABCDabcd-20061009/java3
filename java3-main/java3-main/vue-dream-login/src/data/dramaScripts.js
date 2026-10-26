// 广播剧剧本：每部剧一段原创小说片段，供「声画剧场」逐句朗读
// key 用剧名，与 t_drama.title / 演示库 DRAMA_LIB 的 title 对应
// 匹配不到剧本时，播放器退回视频/音频模式
//
// 数据结构：
//   scene  — 本剧主场景氛围（旁白句使用）
//   cast   — 角色表：key 为说话人名字，value 为「该角色专属画面」的绘制参数
//            kind : scene(旁白场景) / person(人物) / cat(猫) / monster(小怪兽)
//            motif: 画面氛围元素（海浪 / 信封 / 星光 / 雨 / 钟 / 灯笼 / 月 / 被窝 / 光）
//            build: adult(成年) / petite(纤细) / child(孩童) / elder(老人) —— 决定身高与头身比
//            hair : short / long / bun / topknot / child / cap / ponytail / wavy
//            outfit: round(圆领) / v(V 领) / coat(风衣翻领) / cross(交领·古风) / uniform(制服) / pjs(睡衣)
//            prop : 随身道具 watch / letter / lantern / pen / cup / mic
//            acc  : 配饰 apron(围裙) / earpiece(耳机) / glasses(眼镜) / beard(胡须)
//   lines  — { speaker: 角色名（对应 cast 的 key）, text: 正文 }

/* ---- 角色画面构造 helper：填默认值，避免每处重复 ---- */
const person = o => ({
  kind: 'person', build: 'adult', skin: '#f2cdb0', hair: 'short', hairColor: '#4a3a38',
  cloth: '#5a6b8f', color: '#5a6b8f', outfit: 'round', prop: 'none',
  motif: 'none', acc: '', beard: false, glasses: false, wrinkles: false, ...o
})
const narrator = (motif, desc) => ({ kind: 'scene', role: '旁白', motif, color: '#4a5578', desc })
const creature = (kind, o) => ({ kind, color: '#e8a34a', motif: 'none', build: 'petite', ...o })

export const DRAMA_SCRIPTS = {
  '雾港谜案': {
    scene: { motif: 'sea', color: '#2c3e6b', desc: '退潮的码头' },
    cast: {
      '旁白': narrator('sea', '退潮后的第九级石阶'),
      '沈屿': person({ role: '查潮人', build: 'adult', hair: 'short', hairColor: '#33291f', outfit: 'coat', prop: 'pen', cloth: '#3f5d8f', color: '#4a6fa5', motif: 'wave', desc: '每晚记录潮水退到哪里' }),
      '阿婆': person({ role: '港边阿婆', build: 'elder', hair: 'bun', hairColor: '#cfd2d8', glasses: true, wrinkles: true, skin: '#eac7a4', outfit: 'cross', cloth: '#7e8b6b', color: '#8a9b7a', motif: 'wave', desc: '在港边住了一辈子' })
    },
    lines: [
      { speaker: '旁白', text: '潮水退到最低的时候，码头第九级石阶露了出来。' },
      { speaker: '旁白', text: '石阶上放着一枚旧怀表，黄铜壳子被海水泡得发暗。' },
      { speaker: '沈屿', text: '表盖内侧刻着一行小字：给阿宁，一九九七。' },
      { speaker: '沈屿', text: '这块表，你见过吗？' },
      { speaker: '阿婆', text: '港里的人，谁没丢过东西。' },
      { speaker: '沈屿', text: '可这表链上缠着一根红线，红线的另一头，在水里。' },
      { speaker: '旁白', text: '第三天夜里，潮水又退了。' },
      { speaker: '旁白', text: '这一次，第九级石阶上多了一只鞋。' },
      { speaker: '旁白', text: '沈屿翻开记录本，一九九七年那个秋天，也下过这样大的退潮。' },
      { speaker: '沈屿', text: '……原来退潮的时候，海底会还给岸上一点东西。' }
    ]
  },

  '晚风寄来的情书': {
    scene: { motif: 'letter', color: '#c96f97', desc: '三个抽屉，十年' },
    cast: {
      '旁白': narrator('letter', '一封没有邮票的信'),
      '陈叙': person({ role: '收信人', build: 'adult', hair: 'short', hairColor: '#3b3026', outfit: 'v', prop: 'cup', cloth: '#6b8fb5', color: '#6b8fb5', motif: 'letter', desc: '劝她别寄了的那个人' }),
      '周晚': person({ role: '寄信人', build: 'petite', hair: 'wavy', hairColor: '#53372f', outfit: 'round', prop: 'letter', cloth: '#d98aa8', color: '#d98aa8', motif: 'letter', desc: '把信折好塞进抽屉的人' })
    },
    lines: [
      { speaker: '旁白', text: '那封信没有邮票，所以邮局退了回来。' },
      { speaker: '旁白', text: '退回到周晚手里的时候，信封已经磨得起毛。' },
      { speaker: '陈叙', text: '要不就别寄了。' },
      { speaker: '旁白', text: '周晚没说话，只是把信重新折好，塞进抽屉最里面。' },
      { speaker: '旁白', text: '十年里，这封信换过三个抽屉。' },
      { speaker: '旁白', text: '第一个是学校宿舍的铁皮柜，钥匙丢在了毕业那天。' },
      { speaker: '旁白', text: '第二个是租来的小屋，搬家时压在了箱子底。' },
      { speaker: '旁白', text: '第三个，是陈叙家的书桌——他们结婚那年搬进来的。' },
      { speaker: '旁白', text: '去年冬天整理旧物，周晚又翻出了它。' },
      { speaker: '旁白', text: '信封上的字迹已经淡了，可收件人的名字还看得清。' },
      { speaker: '周晚', text: '……如果你也记得那个夏天，就来找我。' }
    ]
  },

  '星轨之外': {
    scene: { motif: 'stars', color: '#2b3a6b', desc: '最后一艘方舟' },
    cast: {
      '旁白': narrator('stars', '舷窗外的地球'),
      '祁昭': person({ role: '离舰登记员', build: 'adult', hair: 'short', hairColor: '#2e2a3c', outfit: 'uniform', acc: 'earpiece', cloth: '#2f3f7a', color: '#6f8cff', motif: 'stars', desc: '负责登记每一个离开的人' }),
      '归档员': person({ role: '归档员', build: 'adult', hair: 'short', hairColor: '#6b6257', glasses: true, outfit: 'uniform', prop: 'pen', cloth: '#5a6486', color: '#9aa7c7', motif: 'stars', desc: '只登记有名字的人' })
    },
    lines: [
      { speaker: '旁白', text: '最后一艘方舟离开轨道时，地球还在自转。' },
      { speaker: '旁白', text: '舷窗外，云层缓慢地合拢，像合上一本书。' },
      { speaker: '祁昭', text: '编号 A-7，登记离舰人员。' },
      { speaker: '归档员', text: '登记完毕，共一万人。' },
      { speaker: '祁昭', text: '最后一位呢？' },
      { speaker: '归档员', text: '最后一位拒绝登记。' },
      { speaker: '旁白', text: '祁昭走回舷窗，玻璃上有一道浅浅的划痕。' },
      { speaker: '旁白', text: '那是她用指甲刻下的，一个名字，不是她自己的。' },
      { speaker: '祁昭', text: '把他也记上吧。' },
      { speaker: '归档员', text: '他没有名字可记。' },
      { speaker: '祁昭', text: '那就记：替她留下来的人。' }
    ]
  },

  '猫咪咖啡馆的午后': {
    scene: { motif: 'rain', color: '#8a6a55', desc: '下雨的咖啡馆' },
    cast: {
      '旁白': narrator('rain', '窗台上背对着雨的猫'),
      '店主': person({ role: '店主', build: 'adult', hair: 'short', hairColor: '#4b3626', outfit: 'v', acc: 'apron', prop: 'cup', cloth: '#c58b5b', color: '#c58b5b', motif: 'rain', desc: '和猫一起开了这家店' }),
      '客人': person({ role: '客人', build: 'petite', hair: 'ponytail', hairColor: '#2f2a33', outfit: 'coat', cloth: '#7c9b8e', color: '#7c9b8e', motif: 'rain', desc: '推门进来躲雨的人' }),
      '阿布': creature('cat', { role: '橘猫', color: '#e8a34a', motif: 'paw', desc: '不爱说话，只会看着你' })
    },
    lines: [
      { speaker: '旁白', text: '推门的时候，风铃响了一声。' },
      { speaker: '旁白', text: '那只橘猫抬了下眼皮，又闭上。' },
      { speaker: '店主', text: '它叫阿布，不太爱说话。' },
      { speaker: '客人', text: '猫本来就不说话。' },
      { speaker: '店主', text: '不，它是真的不爱说。别的猫会叫，它只会看着你。' },
      { speaker: '旁白', text: '窗外的雨下得很慢，像有人在天上慢慢地倒水。' },
      { speaker: '旁白', text: '阿布跳上窗台，背对着雨，面朝着人。' },
      { speaker: '店主', text: '那天我在桥洞下捡到它，我自己也刚丢掉工作。' },
      { speaker: '店主', text: '我们俩都没地方去，就一起开了这家店。' },
      { speaker: '旁白', text: '雨停的时候，阿布终于叫了一声，很轻，像是谢谢。' }
    ]
  },

  '长安月明': {
    scene: { motif: 'lantern', color: '#a8763c', desc: '上元夜的西市' },
    cast: {
      '旁白': narrator('lantern', '一整面墙的月亮'),
      '裴昀': person({ role: '画师', build: 'adult', hair: 'topknot', hairColor: '#33261c', outfit: 'cross', prop: 'pen', cloth: '#b8894a', color: '#c8913f', motif: 'moons', desc: '在没有月亮的夜里画月亮' }),
      '小童': person({ role: '小童', build: 'child', hair: 'child', hairColor: '#3a2c22', outfit: 'cross', prop: 'lantern', cloth: '#e0a55c', color: '#e0a55c', motif: 'lantern', desc: '提着灯要看先生画画' })
    },
    lines: [
      { speaker: '旁白', text: '上元夜的长安，街上全是灯。' },
      { speaker: '旁白', text: '裴昀提着笔，站在西市那面新砌的墙前。' },
      { speaker: '小童', text: '先生要画什么？' },
      { speaker: '裴昀', text: '画月亮。' },
      { speaker: '小童', text: '可今夜没有月亮。' },
      { speaker: '裴昀', text: '那就画一个。' },
      { speaker: '旁白', text: '他蘸了墨，从墙的这一头画到那一头。' },
      { speaker: '旁白', text: '一整面墙，画满了月亮，有的圆，有的缺，有的被云遮了一半。' },
      { speaker: '小童', text: '为什么是三十七个？' },
      { speaker: '旁白', text: '裴昀没回答，只是在最后一个月亮的边上，写了两个字。' },
      { speaker: '旁白', text: '灯灭的时候，那两个字被照得很亮：阿窈。' }
    ]
  },

  '会发光的小怪兽': {
    scene: { motif: 'quilt', color: '#5f4b8b', desc: '关灯后的被窝' },
    cast: {
      '旁白': narrator('quilt', '被窝里亮起来的光'),
      '豆豆': person({ role: '怕黑的小孩', build: 'child', hair: 'cap', hairColor: '#5c4030', outfit: 'pjs', cloth: '#ffb3c9', color: '#ff9ebb', motif: 'quilt', desc: '睡觉一定要开着灯' }),
      '噜噜': creature('monster', { role: '会发光的小怪兽', color: '#ffd166', motif: 'glow', desc: '躲在被子里的那团光' })
    },
    lines: [
      { speaker: '旁白', text: '豆豆怕黑，所以睡觉总要开着灯。' },
      { speaker: '旁白', text: '有一天，被窝里亮了起来。' },
      { speaker: '豆豆', text: '你是谁？' },
      { speaker: '噜噜', text: '我是噜噜，我会发光。' },
      { speaker: '豆豆', text: '那你为什么躲在被子里？' },
      { speaker: '噜噜', text: '因为……外面也很黑。' },
      { speaker: '豆豆', text: '那你发光是给自己看的呀。' },
      { speaker: '噜噜', text: '不是，是给你看的。' },
      { speaker: '旁白', text: '那天晚上，豆豆第一次关了灯。' },
      { speaker: '噜噜', text: '你睡不着的时候，我就一直亮着。' },
      { speaker: '旁白', text: '后来豆豆不怕黑了，可噜噜还是每天晚上都来，亮着。' }
    ]
  },

  '第七次钟声': {
    scene: { motif: 'clock', color: '#3d4a52', desc: '响了七次的钟楼' },
    cast: {
      '旁白': narrator('clock', '一天只响一次的钟'),
      '老周': person({ role: '守钟人', build: 'elder', hair: 'white', hairColor: '#dfe0e6', beard: true, glasses: true, wrinkles: true, skin: '#e9c3a0', outfit: 'round', cloth: '#8a8578', color: '#8a8578', motif: 'clock', desc: '数了四十年钟声' }),
      '记者': person({ role: '记者', build: 'petite', hair: 'long', hairColor: '#332b3a', outfit: 'coat', prop: 'mic', cloth: '#5f7f8f', color: '#5f7f8f', motif: 'clock', desc: '来问今天发生了什么' })
    },
    lines: [
      { speaker: '旁白', text: '钟楼的钟，一天只响一次。' },
      { speaker: '旁白', text: '可今天，它响了第七次。' },
      { speaker: '老周', text: '我数得很清楚，因为我数了四十年。' },
      { speaker: '旁白', text: '每响一次，街上就有一个人停下脚步。' },
      { speaker: '旁白', text: '然后那个人会问：我刚才要做什么来着？' },
      { speaker: '记者', text: '您记得今天发生了什么吗？' },
      { speaker: '老周', text: '我记得早上买了豆浆。' },
      { speaker: '记者', text: '那您还记得喝了吗？' },
      { speaker: '旁白', text: '老周愣住了，钟声又响了。' },
      { speaker: '记者', text: '我们刚才在说什么？' },
      { speaker: '老周', text: '在说，钟每响一次，就有人忘记一件事。' },
      { speaker: '记者', text: '那第七次呢？' },
      { speaker: '老周', text: '第七次，是钟自己忘了。' }
    ]
  },

  '时间旅人的信': {
    scene: { motif: 'letter', color: '#4a4270', desc: '走反了的时间' },
    cast: {
      '旁白': narrator('letter', '投进邮筒的那天'),
      '收信人': person({ role: '收信人', build: 'adult', hair: 'short', hairColor: '#2b2634', outfit: 'v', prop: 'letter', cloth: '#7b6ca8', color: '#7b6ca8', motif: 'letter', desc: '提前十年收到信的人' }),
      '旅人': person({ role: '旅人', build: 'adult', hair: 'wavy', hairColor: '#6a4a33', outfit: 'coat', prop: 'watch', cloth: '#4a5b8f', color: '#5b6ea8', motif: 'clock', desc: '想把信寄给十年前的自己' })
    },
    lines: [
      { speaker: '旁白', text: '旅人把信投进邮筒的时候，是二〇二六年。' },
      { speaker: '旁白', text: '他想寄给二〇一六年的自己，可信先到了二〇三六年。' },
      { speaker: '收信人', text: '如果你收到了这封信，说明时间走反了。' },
      { speaker: '收信人', text: '第一件事：不要相信二〇二六年秋天的那场雨。' },
      { speaker: '收信人', text: '第二件事：三月十七号那天，别上那班地铁。' },
      { speaker: '收信人', text: '第三件事：如果有人在楼下等了你一整夜，请上楼。' },
      { speaker: '旁白', text: '收信人读完，抬头看了看日历，今天是一月九号。' },
      { speaker: '旁白', text: '他还剩下六十八天。' },
      { speaker: '旁白', text: '他把信折好，投进了自己的邮筒，写上了新的日期。' },
      { speaker: '旁白', text: '第二天清晨，二〇一六年的门口，躺着两封信。' },
      { speaker: '旁白', text: '一封是旅人写的，另一封，是他自己写的。' }
    ]
  },

  '海边的旧信封': {
    scene: { motif: 'sea', color: '#3f6f8f', desc: '退潮后的沙滩' },
    cast: {
      '旁白': narrator('sea', '沙滩上的玻璃瓶'),
      '老人': person({ role: '老人', build: 'elder', hair: 'white', hairColor: '#e2e3e8', beard: true, wrinkles: true, skin: '#e9c3a0', outfit: 'v', cloth: '#6f8fa8', color: '#6f8fa8', motif: 'sea', desc: '等了三十年的那句回答' }),
      '孙女': person({ role: '孙女', build: 'child', hair: 'child', hairColor: '#4a3326', outfit: 'round', cloth: '#f2a2a2', color: '#f2a2a2', motif: 'sea', desc: '以为是藏宝图的小孩' })
    },
    lines: [
      { speaker: '旁白', text: '退潮之后，沙滩上躺着一个玻璃瓶。' },
      { speaker: '孙女', text: '爷爷，是藏宝图吗？' },
      { speaker: '旁白', text: '老人接过瓶子，手有点抖，花了很久才把纸展开。' },
      { speaker: '老人', text: '如果你捡到这个，请替我看一眼今天的海。' },
      { speaker: '旁白', text: '落款是一九八九年。' },
      { speaker: '孙女', text: '这是谁写的？' },
      { speaker: '旁白', text: '老人没有回答，只是蹲下来，看着远处。' },
      { speaker: '旁白', text: '海和三十年前一样，还是那样蓝。' },
      { speaker: '孙女', text: '爷爷，你哭了吗？' },
      { speaker: '老人', text: '没有，是海风太大。' },
      { speaker: '旁白', text: '那天晚上，老人也写了一封信，卷好，塞进瓶子。' },
      { speaker: '老人', text: '如果你捡到这个，请替我告诉一九八九年的我：她后来看到了。' }
    ]
  }
}

/* 取某部剧的剧本行；没有则返回空数组（播放器会退回视频模式） */
export function scriptOf(title) {
  if (!title) return []
  const s = DRAMA_SCRIPTS[String(title).trim()]
  return s && Array.isArray(s.lines) ? s.lines : []
}

/* 取某部剧的角色表：[{ name, ...画面参数 }]，按出场顺序排列 */
export function castOf(title) {
  const s = DRAMA_SCRIPTS[String(title).trim()]
  if (!s || !s.cast) return []
  const order = []
  ;(s.lines || []).forEach(l => {
    const n = l && l.speaker
    if (n && s.cast[n] && !order.includes(n)) order.push(n)
  })
  Object.keys(s.cast).forEach(n => { if (!order.includes(n)) order.push(n) })
  return order.map(n => ({ name: n, ...s.cast[n] }))
}

/* 取某部剧的主场景氛围（旁白句使用） */
export function sceneOf(title) {
  const s = DRAMA_SCRIPTS[String(title).trim()]
  return (s && s.scene) || { motif: 'none', color: '#4a5578', desc: '' }
}

/* 取某一句对应的画面配置：有说话人 → 该角色专属画面；旁白 → 本剧场景画面 */
export function visualOf(title, line) {
  const s = DRAMA_SCRIPTS[String(title).trim()]
  const name = line && line.speaker
  if (s && s.cast && s.cast[name]) return { name, ...s.cast[name] }
  const sc = (s && s.scene) || { motif: 'none', color: '#4a5578', desc: '' }
  return { name: name || '旁白', kind: 'scene', role: '旁白', motif: sc.motif, color: sc.color, desc: sc.desc }
}

/* 舞台上要显示的角色（旁白不算人物，它用场景画面表示） */
export function figuresOf(title) {
  return castOf(title).filter(p => p.kind && p.kind !== 'scene')
}
