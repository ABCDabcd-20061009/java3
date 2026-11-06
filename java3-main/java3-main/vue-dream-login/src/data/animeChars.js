// 首页「动漫人物」展示数据：替换原「梦境精选」的十二场梦。
// 每条沿用梦境卡片的格式（tag / title / summary / author / views / likes），
// 但用 img 字段承载角色图片，渲染为图片封面而非文字首字。
// 顺序与用户提供的 15 张图片一一对应。

const BASE = '/anime-chars/'

export const animeChars = [
  {
    id: -1101, anime: true, img: BASE + 'anime-01.webp',
    title: '夏日树荫下的棒冰时光',
    summary: '白衬衫与草帽下的粉衣少年，棒冰将化未化，小狗和 geese 在脚边打盹，风里都是暑假的味道。',
    content: '白衬衫与草帽下的粉衣少年，棒冰将化未化，小狗和鹅在脚边打盹，风里都是暑假的味道。',
    category: '治愈', categoryCode: 'essay', tags: ['治愈', '夏', '同人'],
    authorName: '同人绘', avatarText: '绘', views: 412, likes: 128, liked: false, timeText: ''
  },
  {
    id: -1102, anime: true, img: BASE + 'anime-02.webp',
    title: '指尖轻点唇角的少年',
    summary: '蓝白条纹衬衫半敞，他眨着眼把食指抵在唇边，像一句没说出口的悄悄话。',
    content: '蓝白条纹衬衫半敞，他眨着眼把食指抵在唇边，像一句没说出口的悄悄话。',
    category: '童话', categoryCode: 'essay', tags: ['童话', '俏皮', '同人'],
    authorName: '同人绘', avatarText: '绘', views: 388, likes: 110, liked: false, timeText: ''
  },
  {
    id: -1103, anime: true, img: BASE + 'anime-03.jpeg',
    title: '旧厂房里的午后',
    summary: '白T配破洞牛仔、黑背心与绿连帽，三个少年在生锈的机器间抽烟发呆，时间慢得能听见回响。',
    content: '白T配破洞牛仔、黑背心与绿连帽，三个少年在生锈的机器间抽烟发呆，时间慢得能听见回响。',
    category: '回忆', categoryCode: 'essay', tags: ['回忆', '日常', '同人'],
    authorName: '同人绘', avatarText: '绘', views: 356, likes: 97, liked: false, timeText: ''
  },
  {
    id: -1104, anime: true, img: BASE + 'anime-04.jpeg',
    title: '玻璃碗里的小小梦',
    summary: '四个蓝衣黄帽的小不点在玻璃碗里排排坐，揉眼睛打哈欠，像是刚从午睡里被轻轻捞起。',
    content: '四个蓝衣黄帽的小不点在玻璃碗里排排坐，揉眼睛打哈欠，像是刚从午睡里被轻轻捞起。',
    category: '童话', categoryCode: 'essay', tags: ['童话', 'Q版', '同人'],
    authorName: '同人绘', avatarText: '绘', views: 502, likes: 173, liked: false, timeText: ''
  },
  {
    id: -1105, anime: true, img: BASE + 'anime-05.jpg',
    title: '单车后座的夏天',
    summary: '灰帽少年载着绿衣同伴穿过向阳的小路，燕子在头顶掠过，两个人都笑得比风还轻。',
    content: '灰帽少年载着绿衣同伴穿过向阳的小路，燕子在头顶掠过，两个人都笑得比风还轻。',
    category: '旅途', categoryCode: 'essay', tags: ['旅途', '夏', '同人'],
    authorName: '同人绘', avatarText: '绘', views: 431, likes: 142, liked: false, timeText: ''
  },
  {
    id: -1106, anime: true, img: BASE + 'anime-06.jpg',
    title: '西装与头纱之间',
    summary: '深蓝西装与灰西装隔着一层面纱相视，镜头压得极低，把那一瞬的温柔框成了电影。',
    content: '深蓝西装与灰西装隔着一层面纱相视，镜头压得极低，把那一瞬的温柔框成了电影。',
    category: '奇幻', categoryCode: 'essay', tags: ['奇幻', '婚礼', 'JINX'],
    authorName: 'JINX', avatarText: 'J', views: 689, likes: 264, liked: false, timeText: ''
  },
  {
    id: -1107, anime: true, img: BASE + 'anime-07.jpg',
    title: '蓝紫色光晕里的相拥',
    summary: '蓝发与戴帽的他在梦幻的蓝紫散景中紧紧相拥，世界退成模糊的光，只剩彼此的温度。',
    content: '蓝发与戴帽的他在梦幻的蓝紫散景中紧紧相拥，世界退成模糊的光，只剩彼此的温度。',
    category: '静谧', categoryCode: 'essay', tags: ['静谧', '同人', '治愈'],
    authorName: '同人绘', avatarText: '绘', views: 547, likes: 201, liked: false, timeText: ''
  },
  {
    id: -1108, anime: true, img: BASE + 'anime-08.jpg',
    title: '白衬衫上的旧时光',
    summary: '棕发少年穿着短袖白衬衫望向别处，眉眼间落着一层薄薄的怅惘，像没说完的那句别来无恙。',
    content: '棕发少年穿着短袖白衬衫望向别处，眉眼间落着一层薄薄的怅惘，像没说完的那句别来无恙。',
    category: '回忆', categoryCode: 'essay', tags: ['回忆', '别来无恙', '同人'],
    authorName: '别来无恙', avatarText: '恙', views: 398, likes: 119, liked: false, timeText: ''
  },
  {
    id: -1109, anime: true, img: BASE + 'anime-09.jpg',
    title: '花蝶环绕的婚典',
    summary: '黑发新郎与白裳新娘在花与蝶之间相视而笑，连空气都变得柔软而圣洁。',
    content: '黑发新郎与白裳新娘在花与蝶之间相视而笑，连空气都变得柔软而圣洁。',
    category: '奇幻', categoryCode: 'essay', tags: ['奇幻', '婚礼', '同人'],
    authorName: '同人绘', avatarText: '绘', views: 731, likes: 298, liked: false, timeText: ''
  },
  {
    id: -1110, anime: true, img: BASE + 'anime-10.webp',
    title: '晴日街角的并肩',
    summary: '黑发少年白T配紫裤、棕发少年浅蓝衫搭熊仔T，两人在洒满阳光的街角静静站着，像刚聊完一个长篇。',
    content: '黑发少年白T配紫裤、棕发少年浅蓝衫搭熊仔T，两人在洒满阳光的街角静静站着，像刚聊完一个长篇。',
    category: '旅途', categoryCode: 'essay', tags: ['旅途', '日常', '同人'],
    authorName: '同人绘', avatarText: '绘', views: 365, likes: 108, liked: false, timeText: ''
  },
  {
    id: -1111, anime: true, img: BASE + 'anime-11.webp',
    title: '黑色西装里的从容',
    summary: '黑西装白衬衫配暗纹领带，他微微勾起嘴角，那份不动声色的自信比台词更有分量。',
    content: '黑西装白衬衫配暗纹领带，他微微勾起嘴角，那份不动声色的自信比台词更有分量。',
    category: '静谧', categoryCode: 'essay', tags: ['静谧', '同人', '西装'],
    authorName: '同人绘', avatarText: '绘', views: 374, likes: 115, liked: false, timeText: ''
  },
  {
    id: -1112, anime: true, img: BASE + 'anime-12.jpg',
    title: '灰调里的撒野',
    summary: '黑帽少年阴郁、耳钉少年随性，两人在去色的小城里彼此较劲又彼此依存——这是只属于他们的撒野。',
    content: '黑帽少年阴郁、耳钉少年随性，两人在去色的小城里彼此较劲又彼此依存——这是只属于他们的撒野。',
    category: '回忆', categoryCode: 'essay', tags: ['回忆', '撒野', '同人'],
    authorName: '撒野', avatarText: '野', views: 612, likes: 233, liked: false, timeText: ''
  },
  {
    id: -1113, anime: true, img: BASE + 'anime-13.jpg',
    title: '机场落日的人设',
    summary: '蓝外套口罩拿咖啡、灰大衣墨镜掩面，两人在登机口的余晖里各自从容，人设拿捏得刚刚好。',
    content: '蓝外套口罩拿咖啡、灰大衣墨镜掩面，两人在登机口的余晖里各自从容，人设拿捏得刚刚好。',
    category: '旅途', categoryCode: 'essay', tags: ['旅途', '人设', '同人'],
    authorName: '我只喜欢你的人设', avatarText: '设', views: 569, likes: 212, liked: false, timeText: ''
  },
  {
    id: -1114, anime: true, img: BASE + 'anime-14.jpg',
    title: '花嫁之舞',
    summary: '白衫持花的他与暗色礼服的他在花瓣中旋身，一笑便胜过人间万千灯火。',
    content: '白衫持花的他与暗色礼服的他在花瓣中旋身，一笑便胜过人间万千灯火。',
    category: '奇幻', categoryCode: 'essay', tags: ['奇幻', '天官赐福', '婚礼'],
    authorName: '天官赐福', avatarText: '赐', views: 808, likes: 341, liked: false, timeText: ''
  },
  {
    id: -1115, anime: true, img: BASE + 'anime-15.jpg',
    title: '蓝天下的举臂欢呼',
    summary: '两个少年背对镜头举起手臂，黄衫与蓝帽在云朵里定格成最纯粹的欢喜，像赢了整片天空。',
    content: '两个少年背对镜头举起手臂，黄衫与蓝帽在云朵里定格成最纯粹的欢喜，像赢了整片天空。',
    category: '旅途', categoryCode: 'essay', tags: ['旅途', '同人', '晴空'],
    authorName: '同人绘', avatarText: '绘', views: 446, likes: 151, liked: false, timeText: ''
  }
]

export default animeChars
