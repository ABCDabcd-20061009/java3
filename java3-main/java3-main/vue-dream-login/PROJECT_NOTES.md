# 粉金梦境 登录注册系统 · 项目笔记（给 AI 助手看的速查手册）

> 下次会话先读本文件，不要重新探索。电脑每次开机会重置：`node_modules`、`target` 会被删除，按本文「快速启动」执行即可。

## 一、项目组成

| 模块 | 路径 | 端口 | 技术栈 |
|---|---|---|---|
| 后端 | `D:\workSpace\java3\java3-main\java3-main\springBootTest`（旧的 `D:\workSpace\springBootTest` 是更早的副本，以本目录为准） | 8081 | Spring Boot 3.4.5 + MyBatis-Plus 3.5.7 + MySQL 8.0，Java 17（本机装的 Zulu 21 可跑） |
| 前端 | `D:\workSpace\java3\java3-main\java3-main\vue-dream-login` | 5173 | Vue 3.5 + Vite 6 |
| 数据库 | 本机 MySQL 8.0 | 3306 | 库 `dream_login`；登录表 `t_user`，首页表 `t_dream` / `t_dream_category` / `t_dream_like` / `t_dream_favorite` / `t_dream_search_log`；账号 `root` / 密码 `123456` |

## 二、快速启动（每次开机后）

1. 双击 `springBootTest\启动后端.bat`（自动检查 Java/MySQL；库不存在执行 `sql\dream_login.sql`，首页 4 张表缺失执行 `sql\dream_home.sql`——都幂等；然后启动服务）
2. 双击 `vue-dream-login\启动前端.bat`（`node_modules` 不存在则自动用 npmmirror 安装）
3. 停止：对应目录下的 `停止后端.bat` / `停止前端.bat`（按端口杀进程）
4. 脚本均为 GBK 编码 + CRLF，相对路径（`%~dp0`），不依赖 `node_modules`/`target` 预先存在

手工命令（脚本失效时的备选）：
- 后端：`mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--server.port=8081"`
- 前端：`npm install --registry=https://registry.npmmirror.com` 然后 `npm run dev`

## 三、API 契约（前后端约定，改接口时两边同步）

统一前缀 `/api`，POST + JSON。响应统一 `{code, message, data}`：`code=0` 成功；业务失败返回 HTTP 400 + `message`（前端 `api.js` 会抛出该 message 并 toast 展示）。

- `POST /api/send-code` 请求 `{account: 用户名, type: "register"}` → `data.devCode` 为演示验证码（未接短信/邮件服务，同时打印在后端控制台）
- `POST /api/register` 请求 `{username, password, code}`
- `POST /api/login` 请求 `{account: 用户名, password}`

校验规则：用户名 2-20 字符；密码 1-18 位不含空格；验证码 6 位数字、5 分钟有效、一次性（`util/CodeStore` 内存存储）。注册时用户名唯一。

前端无后端时会自动进入「演示模式」：`api.js` 里网络错误带 `isNetwork=true` 标记，仅此时降级本地生成验证码 / 本地梦境库。

### 首页接口（后端已实现，前端已接通）

| 接口 | 请求体 | 返回 data |
|---|---|---|
| `POST /api/home/overview` | `{userId?}` | `{stats,categories,hotKeywords,hotDreams,latestDreams}` |
| `POST /api/home/feed` | `{page,size,category?,sort?,userId?}` | `{list,total,page,size,pages,hasMore}` |
| `POST /api/search` | `{keyword,page,size,category?,sort?,userId?}` | 同上 |
| `POST /api/home/categories` | `{}` | `[{code,name,icon,count}]` |
| `POST /api/home/hot-keywords` | `{}` | `[{keyword,count}]` |
| `POST /api/dream/detail` | `{id,userId?}` | 单条 VO（浏览量 +1，含 `content` / `liked` / `favorited`） |
| `POST /api/dream/like` | `{id,action?}` | `{id,liked,likes}`（需登录） |
| `POST /api/dream/favorite` | `{id,action?}` | `{id,favorited}`（需登录） |
| `POST /api/user/favorites` | `{page,size}` | `{list,total,...}` 我的收藏（需登录） |
| `POST /api/dream/publish` | `{...梦境字段}` | `{id,title,category,authorName}`（需登录） |
| `POST /api/user-info` | `{}` | `{id,username,createTime}`（需登录，用于校验 token） |

列表项字段：`id,userId,authorName,avatarText,title,summary,content(仅详情),coverUrl,category,categoryCode,tags[],views,likes,liked,favorited,createTime,timeText`。`sort` = `newest`|`hot`；`category` 传分类名或编码，`全部` 表示不限。

**鉴权**：`/api/login`、`/api/register` 返回的 `data.token` 会随会话一起存进 localStorage/sessionStorage，`api.js` 自动加 `Authorization: Bearer`。需登录接口未登录/过期统一返回 **401**，`api.js` 标记 `isUnauthorized`，页面收到就清理会话回登录页。
⚠️ 旧的 localStorage 会话（没有 token 字段）会点不动赞/收藏 —— 重新登录一次即可。

## 四、后端代码地图（包 com.fenglin.springboottest）

- `controller/AuthController.java` — 三个接口
- `service/UserService.java` + `service/impl/UserServiceImpl.java` — 业务与校验
- `mapper/UserMapper.java` — MyBatis-Plus BaseMapper，零 XML
- `entity/User.java` — 对应 `t_user`（id/username/password/create_time/update_time/deleted 逻辑删除）
- `common/Result.java`、`common/BizException.java`、`common/GlobalExceptionHandler.java`
- `util/PasswordUtil.java` — SHA-256 加盐；`util/CodeStore.java` — 验证码内存存储
- `config/CorsConfig.java` — 放行 5173 跨域（前端同时也走 Vite 代理，双保险）
- `sql/dream_login.sql` — 建库 + `t_user` + 3 个演示账号（梦梦/星星/夜风，密码 `abc123456`）；`sql/dream_home.sql` — 首页 4 张表 + 种子数据（两个脚本都幂等）
- 首页新增后端类：`controller/HomeController`、`SearchController`、`DreamController`、`UserApiController`；`service/impl/DreamServiceImpl`；`vo/DreamVO`、`PageResult`、`HomeOverviewVO`；`entity/Dream`、`DreamCategory`、`DreamLike`、`DreamFavorite`、`DreamSearchLog`
- 鉴权相关：`util/JwtUtil`、`config/JwtInterceptor` + `WebConfig`、`common/UserContext`、`common/AuthException`（鉴权失败 → HTTP 401）
- 旧的 Thymeleaf 演示 `UserController.java`、`templates/user/login.html` 仍在，未清理，与 API 无关

## 五、前端要点

- `src/api.js` — 接口封装，`BASE = /api`（可用 `.env` 的 `VITE_API_BASE` 覆盖）。首页相关：`fetchHomeOverview` / `fetchHomeFeed` / `searchDreams`（`searchAll` 是它的别名）/ `fetchCategories` / `fetchHotKeywords` / `fetchDreamDetail` / `toggleDreamLike` / `toggleDreamFavorite` / `fetchMyFavorites` / `publishDream`；**广播剧相关**：`fetchDramaList` / `fetchDramaDetail` / `fetchDramaCategories`（调 `/api/drama/*`，全部匿名）；登录态相关会自动带 `Authorization: Bearer <token>`（后端已校验，401 会清会话）
- `src/components/LoginCard.vue` — 登录/注册双表单：字段级内联校验、密码强度条、验证码倒计时、用户协议勾选；登录成功 `emit('login-success', {user, remember})`
- `src/components/DreamBackground.vue` — 全站背景动画层：粉金光晕漂移 + 满天星星闪烁（小星点 + 四角星光，随机节奏 twinkle）+ 气泡缓慢上升流动；App.vue 背景为粉金色渐变（日间 #ffdcea→#fff1dd，夜空深紫底 + 粉金光晕）
  - ⚠️ 坑：星星/气泡是 JS `document.createElement` 动态创建的，**没有** scoped 的 `data-v` 属性，样式选择器必须写成 `.stars :deep(.star)`、`.bubbles :deep(.bubble)`（编译成 `.stars[data-v] .star` 后代选择器）。直接写 `.star {}` 会被编译成 `.star[data-v]` 导致样式完全不生效、元素不可见——旧版花瓣/气泡长期不可见就是这个原因
- `src/components/HomePage.vue` — 登录后的首页，已全部接通后端：
  - 顶部：悬浮胶囊导航栏（品牌、菜单占位、头像/用户名、退出登录）
  - 搜索区：大搜索框 + 热门关键词（来自 `/api/home/hot-keywords`，带热度数字），走 `searchDreams` → `POST /api/search`
  - 统计条：梦境内容 / 累计浏览 / 收获点赞 / 造梦者（`/api/home/overview` 的 `stats`）
  - 梦境精选：分类 tab（带各分类数量）+ 「最新/最热」排序切换 + 卡片网格 + 「加载更多」分页（`/api/home/feed`）
  - 卡片：无封面图时按 `categoryCode` 配渐变色块 + 首字水印 + 分类标签 + 标签 + 作者/时间/浏览/点赞/收藏（点赞走 `/api/dream/like`，收藏走 `/api/dream/favorite`，都需登录）
  - 「大家都在看」热门推荐栏（`overview.hotDreams` 按浏览量 top6，仅在全部+最新排序时显示）
  - 点卡片打开详情弹层（`POST /api/dream/detail`，浏览量 +1 后回写列表数字），正文按 `\n` 分段；弹层里也能点赞/收藏
  - 快捷入口「我的收藏」切到真实收藏列表（`fetchMyFavorites`，独立分页与「加载更多」，在收藏列表里取消收藏会即时移出该条）；「梦境广场」切回精选；`view` 状态在 `feed` / `favorites` 间切换，主列表区两种视图复用同一套卡片模板（`displayList` / `displayLoading` / `displayHasMore` 三个 computed）
  - 互动接口 401 会自动 `emit('logout')` 回登录页
  - 后端不可用时自动降级演示模式（本地 `DREAM_LIB`，卡片 id 取负数以便区分；演示模式下收藏不可用）
- `src/components/HomeCard.vue` — 旧版登录后欢迎卡（已被 HomePage 取代，保留未删）
- `src/components/DramaPage.vue` — **广播剧（首页导航「广播剧」接管主区域时的视图）**：
  - 头部：标题 + 广播剧搜索框 + 「热门平台」筛选 chips（全部/喜马拉雅/猫耳FM/晋江文学城/蜻蜓FM/凯叔讲故事）
  - 列表区：分类 tab（含数量，来自 `/api/drama/categories`）+ 「最新/最热」排序 + 卡片网格 + 「加载更多」分页（`/api/drama/list`，支持 keyword/category/platform/sort）
  - 卡片：无封面时按 `categoryCode` 配渐变色块 + 首字水印 + 分类标签 + **「播放」角标** + **来源平台角标** + 时长/浏览/点赞；点卡片打开**播放器弹层**
  - 播放器弹层：主播放源 `<video :src="videoUrl" controls autoplay playsinline>`（**有画面也有声音**，弹层出现尝试自动播放，被拦截时交还控件）；`videoUrl` 为空回退 `<audio :src="audioUrl">`；都无则提示无媒体；详情走 `/api/drama/detail`（浏览量 +1 回写列表）
  - 后端不可用时自动降级演示模式（本地 `DRAMA_LIB` 9 部，卡片 id 取负；媒体用公开示例视频，保证可播放可演示）
- `src/App.vue` — 会话管理：记住我 → localStorage，否则 → sessionStorage（键 `dream-user`），刷新自动恢复，退出清除；同时定义全站主题变量（`:root` 日间 / `.app-root.dark` 夜空），新增 `--panel-bg` 给弹层做不透明底色（scoped 样式里写 `:global(.app-root.dark) xxx` **无效**，会被编译器丢掉，主题差异一律走变量）
- `vite.config.js` — `/api` 代理到 `http://localhost:8081`

## 六、本机环境坑（都踩过，别再排查）

1. **端口被环境变量覆盖**：本机有环境变量把 `server.port` 顶成 54322，启动必须显式传 `--server.port=8081`（脚本已处理）
2. **npm 官方源极慢**：必须用 `--registry=https://registry.npmmirror.com`（约 50 秒装完）
3. **MySQL 客户端不在 PATH**：用 `C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe`；服务名 `MySQL80`
4. **JAVA_HOME**：`C:\Program Files\Zulu\zulu-21`；系统无 mvn，用项目自带 `mvnw.cmd`
5. **Maven 镜像**：`~/.m2/settings.xml` 已配阿里云；若被重置，mvnw 首次下载 Maven 会慢
6. MySQL 里已有测试用户：梦梦 / 星星 / 夜风，密码统一 `abc123456`
7. **别用 git bash 跑后端 `./mvnw`**：Windows 下会报 `ClassNotFoundException: org.codehaus.plexus.classworlds.launcher.Launcher`。用 `mvnw.cmd`，或直接调缓存 Maven `C:\Users\Administrator\.m2\wrapper\dists\apache-maven-3.9.9-bin\4nf9hui3q3djbarqar9g711ggc\apache-maven-3.9.9\bin\mvn.cmd -o -DskipTests package`
8. `sc.exe` / `cmd.exe` 在本机沙箱里被禁用：查端口用 `netstat -ano | findstr :8081`，杀进程用 PowerShell `Stop-Process -Id <pid> -Force`
9. 前端构建校验：`node node_modules/vite/bin/vite.js build`（不需要 npm run）

## 七、已知缺口（用户可能让你继续做的）

- ~~登录成功后的主页~~（已完成 HomePage：导航栏 + 搜索栏 + 首页展示流）
- ~~首页搜索后端接口~~（已完成：`POST /api/search`、`/api/home/*`、`/api/dream/*`，已做接口自测）
- ~~接口无鉴权 / 收藏功能~~（后端 JWT 已落地，点赞与收藏需登录；前端「我的收藏」已接通真实接口）
- ~~广播剧功能~~（已完成：首页导航「广播剧」→ `DramaPage.vue`，可挑选并播放热门平台小说广播剧，`fetchDramaList`/`fetchDramaDetail`/`fetchDramaCategories` 已封装）
- 「梦境广场 / 梦境手记 / 个人中心」仍是占位页；`publishDream()` 已封装但还没有发布页 UI
- token 无刷新机制，过期只能重新登录（默认 7 天）
- 忘记密码、第三方登录是占位
- 验证码未接真实短信/邮件（可配 spring-boot-starter-mail）
