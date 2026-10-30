# 粉金梦境 登录注册系统 · 项目笔记（给 AI 助手看的速查手册）

> 下次会话先读本文件，不要重新探索。电脑每次开机会重置：`node_modules`、`target` 会被删除，按本文「快速启动」执行即可。

## 一、项目组成

| 模块 | 路径 | 端口 | 技术栈 |
|---|---|---|---|
| 后端 | `D:\workSpace\java3\java3-main\java3-main\springBootTest`（旧的 `D:\workSpace\springBootTest` 是更早的副本，以本目录为准） | 8081 | Spring Boot 3.4.5 + MyBatis-Plus 3.5.7 + MySQL 8.0，Java 17（本机装的 Zulu 21 可跑） |
| 前端 | `D:\workSpace\java3\java3-main\java3-main\vue-dream-login` | 5173 | Vue 3.5 + Vite 6 |
| 数据库 | 本机 MySQL 8.0 | 3306 | 库 `dream_login`；登录表 `t_user`，首页表 `t_dream` / `t_dream_category` / `t_dream_like` / `t_dream_favorite` / `t_dream_search_log`；账号 `root` / 密码 `123456` |

## 二、快速启动（每次开机后）

1. 双击 `springBootTest\启动后端.bat`（自动检查 Java/MySQL；库不存在执行 `sql\dream_login.sql`，首页 4 张表缺失执行 `sql\dream_home.sql`——两个脚本都幂等，可重复跑；然后启动服务）
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

**鉴权约定（JWT）**

- `POST /api/login`、`POST /api/register` 成功时 `data` 除 `id`/`username` 外多返回 `token`（HS256，默认 7 天，配置项 `dream.jwt.secret` / `dream.jwt.expire-days`）
- 后续请求带 `Authorization: Bearer <token>`；`JwtInterceptor` 解析后写入 `common/UserContext`，业务层据此拿真实身份
- 需要登录的接口：`/api/dream/like`、`/api/dream/favorite`、`/api/dream/publish`、`/api/dream/update`、`/api/dream/delete`、`/api/user-info`、`/api/user/favorites`、`/api/user/dreams`、`/api/user/profile`
  - 缺失/伪造/过期 token → **HTTP 401** + `message`（前端 `api.js` 看到 401 会清会话回登录页）
  - 其余接口（首页/搜索/详情）匿名可访问，带 token 时额外返回 `liked` / `favorited`
- `POST /api/user-info` → `{id, username, createTime}`，用于前端校验 token 是否仍有效
- ⚠️ 点赞/收藏/发布/编辑/删除的**操作人和作者一律取 token 身份**，请求体里的 `userId` 已不再被信任；发布时的 `authorName` 也已废弃（`DreamPublishDTO.authorName` 标了 `@Deprecated`，传了不生效，署名固定为登录用户名，防伪造）

### 首页接口（首页展示 / 搜索 / 点赞，POST + JSON，共用上面的 `{code,message,data}` 约定）

| 接口 | 请求体 | 返回 data |
|---|---|---|
| `POST /api/home/overview` | `{userId?}` | `{stats,categories,hotKeywords,hotDreams,latestDreams}` 首页一次性拿全 |
| `POST /api/home/feed` | `{page,size,keyword?,category?,sort?,userId?}` | `{list,total,page,size,pages,hasMore}` |
| `POST /api/search` | 同上 | 同上，另外把关键词写进搜索日志（热门词统计来源） |
| `POST /api/home/categories` | `{}` | `[{code,name,icon,count}]` |
| `POST /api/home/hot-keywords` | `{}` | `[{keyword,count}]`（近 30 天日志 top8，无数据给默认词） |
| `POST /api/dream/detail` | `{id,userId?}` | 详情 VO，浏览量原子 +1，含 `content` / `liked` / `favorited` |
| `POST /api/dream/like` | `{id,action?}` action=`toggle`(默认)/`like`/`unlike` | `{id,liked,likes}`（需登录） |
| `POST /api/dream/favorite` | `{id,action?}` action=`toggle`(默认)/`favorite`/`unfavorite` | `{id,favorited}`（需登录） |
| `POST /api/user/favorites` | `{page,size}` | `{list,total,...}` 我的收藏，按收藏时间倒序（需登录） |
| `POST /api/user/dreams` | `{page,size,keyword?,sort?}` | `{list,total,...}` 我的梦境，**只返回自己发布的**（强制 `user_id=登录用户`，外部传 `authorId` 无效），**带 `content`** 方便前端直接编辑（需登录） |
| `POST /api/user/profile` | `{}` | `{userId,username,dreamCount,viewCount,likeCount,favoriteCount}` 个人中心统计（需登录） |
| `POST /api/dream/publish` | `{title,summary,content,coverUrl,category,tags}` | `{id,title,category,authorName}`（需登录，署名取登录用户名） |
| `POST /api/dream/update` | `{id,title,summary,content,coverUrl,category,tags}` | `{id,title,category,authorName}`（需登录，**只能改自己的**，否则 400 `只能编辑自己发布的梦境`） |
| `POST /api/dream/delete` | `{id}` | `{id,title}`（需登录，**只能删自己的**，否则 400 `只能删除自己发布的梦境`；逻辑删除） |
| `POST /api/dream/list` | 同 `/api/home/feed` | 同上（旧路径保留，等价） |
| `POST /api/dream/search` | 同 `/api/search` | 同上（等价） |

- 列表项 VO 字段：`id,userId,authorName,avatarText,title,summary,content(仅详情),coverUrl,category,categoryCode,tags[],views,likes,liked,favorited,createTime,timeText`
- `sort`：`newest`（默认，发布时间倒序）/ `hot`（浏览量倒序）；`category` 可传分类名（美梦）或编码（beauty），不传或 `全部` 表示不限
- 点赞/收藏各有唯一键 `(dream_id,user_id)` 防重复；并发重复插入被捕获，计数不会叠加
- 搜索关键词里的 `%` `_` `\` 已做转义，避免通配符注入式全表匹配

### 广播剧接口（首页「广播剧」功能，全部匿名可访问，POST + JSON，复用 `{code,message,data}`）

| 接口 | 请求体 | 返回 data |
|---|---|---|
| `POST /api/drama/list` | `{page,size,keyword?,category?,platform?,sort?}` | `{list,total,page,size,pages,hasMore}` |
| `POST /api/drama/detail` | `{id}` | `DramaVO`（浏览量原子 +1，含 `videoUrl`/`audioUrl`/`posterUrl`/`categoryCode`/`platform`） |
| `POST /api/drama/categories` | `{}` | `[{code,name,icon,count}]` |

- 列表项 `DramaVO` 字段：`id,title,summary,coverUrl,category,categoryCode,tags[],platform,videoUrl,audioUrl,posterUrl,duration,views,likes,createTime,timeText`
- `category` 传分类名（悬疑）或编码（suspense），不传/`全部` 不限；`platform` 传来源平台（喜马拉雅/猫耳FM/晋江文学城/蜻蜓FM/凯叔讲故事），不传/`全部` 不限；`sort`：`newest`(默认)/`hot`
- 广播剧为公开内容，**浏览/详情/分类均匿名可访问**（不在 `JwtInterceptor.AUTH_REQUIRED` 里）
- 播放源：`videoUrl` 为主播放源（有画面也有声音），为空时前端回退 `audioUrl` 纯音频
- **媒体走后端本地托管**：`videoUrl` 一律填 `/media/drama/*.mp4`，`WebConfig` 已注册 `/media/**` 静态映射（依次查 `file:./media/` → `classpath:/static/media/`，支持 Range 断点续传，不经过 JWT 拦截器）
  - ⚠️ 早期种子用的是 `https://commondatastorage.googleapis.com/gtv-videos-bucket/...`，该域名国内不可达 → 播放器黑屏「播放不了」，已全部改成本地地址
  - 已有库升级执行 `sql/dream_drama_media_fix.sql`（幂等）；真实视频直接丢进 `springBootTest/media/drama/` 即可，无需重新打包
- 建表脚本 `sql/dream_drama.sql`（幂等：建 `t_drama` + `t_drama_category` + 9 条种子），启动脚本第 4.5 步已自动执行（t_drama 缺失时）

### 后台管理接口（独立管理员账号，前缀 `/api/admin`，全部 POST + JSON）

- `POST /api/admin/login` 请求 `{username, password}` → `data = {id, username, token}`
  - **唯一匿名入口**；账号体系 `t_admin` 与普通用户 `t_user` **完全隔离**
  - 默认账号 `admin` / `admin123`，由 `config/AdminSchemaInit` 启动时自动建表并植入（幂等，表内非空则跳过）
- ⚠️ **鉴权**：`JwtInterceptor` 对 `/api/admin/**`（`/login` 除外）强制要求 token 里 `role=admin`。
  普通用户的 token 访问任何后台接口一律 **401 `无管理员权限`**
  → **新增后台接口必须放在 `/api/admin/` 前缀下**才会自动受保护；放到别处需手动加进 `JwtInterceptor.AUTH_REQUIRED` 白名单

| 接口 | 请求体 | 返回 data |
|---|---|---|
| `POST /api/admin/info` | `{}` | `{id,username}` |
| `POST /api/admin/overview` | `{}` | `AdminStatsVO` 8 项统计 |
| `POST /api/admin/dreams` | `{page,size,keyword?,category?,status?}` | `{list,total,...}` |
| `POST /api/admin/dream/save` | `AdminDreamDTO`（有 id 则编辑） | 单条 |
| `POST /api/admin/dream/delete` | `{id}` | 逻辑删除 |
| `POST /api/admin/dramas` | `{page,size,keyword?,category?,platform?,status?}` | `{list,total,...}` |
| `POST /api/admin/drama/save` / `delete` | 同上 dream | |
| `POST /api/admin/categories` | `{type:"dream"\|"drama"}` | 分类列表 |
| `POST /api/admin/category/save` / `delete` | `{...,type}` | |
| `POST /api/admin/users` | `{page,size,keyword?}` | 列表（含已注销，前端打「已注销」badge） |
| `POST /api/admin/user/delete` | `{id}` | 注销用户（逻辑删除） |
| `POST /api/admin/search-logs` | `{page,size,keyword?}` | 搜索日志 |
| `POST /api/admin/search-logs/clear` | `{}` | 清空日志 |
| `POST /api/admin/likes` / `favorites` | `{page,size}` | 点赞 / 收藏记录 |

**用户列表会返回已注销用户**（`deleted=1`）：这是**有意设计**，表格里打「已注销」徽标。
原因是 `UserMapper.selectAdminPage` 用的是自定义 `@Select` 注解 SQL —— ⚠️ **MyBatis-Plus 的 `@TableLogic` 对自定义注解 SQL 不生效**，只对 BaseMapper 方法自动追加过滤。以后写原生 SQL 记得自己带 `deleted = 0`。

### 首页配置接口（把首页栏目从前端搬到数据库）

- `POST /api/home/config` — **匿名**可访问 → `data = {navMenu, shortcut, platform, text}`
- 后台维护接口 `/api/admin/home-config/**`（同样自动受 `role=admin` 保护）：

| 接口 | 请求体 | 返回 data |
|---|---|---|
| `POST /api/admin/home-config/list` | `{type}` | `[{id,configType,configKey,label,subLabel,iconKey,target,sortNo,status}]` |
| `POST /api/admin/home-config/save` | `SiteConfigDTO`（有 id 则编辑） | 单条 |
| `POST /api/admin/home-config/delete` | `{id}` | 逻辑删除 |

四类 `config_type`：`nav_menu` 导航菜单 / `shortcut` 快捷入口 / `platform` 广播剧平台 / `site_text` 站点文案（键：`brand_name` / `hero_title` / `hero_sub` / `footer`）。

约束：
- `config_key` 同类型内唯一，留空自动生成 `<type>_<时间戳>`；重复 → 400
- **站点文案禁止删除**（`BizException`）：前端强依赖固定键，删了首页会留白，只能改内容或停用
- `status=0` 停用时前台 `/home/config` 不再返回该项（只筛 `status=1`）
- 返回体手工用 `LinkedHashMap` 组装，不直接抛实体，规避 `LocalDateTime` 被 Jackson 序列化成数组的老问题

前端无后端时会自动进入「演示模式」：`api.js` 里网络错误带 `isNetwork=true` 标记，仅此时降级本地生成验证码 / 本地梦境库 / 本地广播剧库。

## 四、后端代码地图（包 com.fenglin.springboottest）

**登录注册（原有）**
- `controller/AuthController.java` — 三个接口
- `service/UserService.java` + `service/impl/UserServiceImpl.java` — 业务与校验
- `mapper/UserMapper.java` — MyBatis-Plus BaseMapper，零 XML
- `entity/User.java` — 对应 `t_user`（id/username/password/create_time/update_time/deleted 逻辑删除）
- `common/Result.java`、`common/BizException.java`、`common/GlobalExceptionHandler.java`
- `util/PasswordUtil.java` — SHA-256 加盐；`util/CodeStore.java` — 验证码内存存储
- `config/CorsConfig.java` — 放行 5173 跨域（前端同时也走 Vite 代理，双保险）
- 旧的 Thymeleaf 演示 `UserController.java`、`templates/user/login.html` 仍在，未清理，与 API 无关

**鉴权（新增）**
- `util/JwtUtil.java` — 签发/解析（密钥 SHA-256 派生，避免配置太短报错）；`config/JwtInterceptor.java` + `config/WebConfig.java` — 解析 Bearer token、按接口决定是否强制登录、请求结束清理上下文
- `common/UserContext.java` — ThreadLocal 当前登录用户（业务层不用再信请求体）；`common/AuthException.java` — 鉴权失败统一 401
- `/api/user-info` 在 `AuthController`；`/api/user/favorites`、`/api/user/dreams`、`/api/user/profile` 在 `controller/UserApiController.java`（类名避开根包下遗留的 Thymeleaf `UserController`）

**首页展示 / 搜索 / 互动 / 创作（新增，全部零 XML）**
- `controller/HomeController.java` — `/api/home/overview`、`/feed`、`/categories`、`/hot-keywords`
- `controller/SearchController.java` — `/api/search`（对齐前端预留契约）
- `controller/DreamController.java` — `/api/dream/list`、`/search`、`/detail`、`/like`、`/favorite`、`/publish`、`/update`、`/delete`、`/categories`、`/hot-keywords`
- `service/DreamService.java` + `service/impl/DreamServiceImpl.java` — 首页列表/搜索/聚合概览/详情/点赞/收藏/发布/编辑/删除/我的收藏/我的梦境/个人中心统计；批量填充 `liked`/`favorited` 避免 N+1；聚合统计用 `selectMaps` + group by；发布与编辑共用 `fillEditableFields()` 做字段校验归一（标题 2-100 字、正文必填≤5000、摘要空则取正文前 60 字、非法分类回落「随笔」、标签去重≤8）；`requireLogin()` 保证写操作一律用 token 身份
- `mapper/DreamMapper.java`、`DreamCategoryMapper.java`、`DreamLikeMapper.java`、`DreamFavoriteMapper.java`、`DreamSearchLogMapper.java`
- `entity/Dream.java`、`DreamCategory.java`、`DreamLike.java`、`DreamFavorite.java`、`DreamSearchLog.java`
- `dto/DreamQuery.java`（列表/搜索条件：page/size/keyword/category/sort/userId/authorId）、`DreamDetailDTO.java`、`DreamActionDTO.java`（点赞与收藏共用，原 `DreamLikeDTO` 已删）、`DreamPublishDTO.java`、`DreamUpdateDTO.java`
- `vo/DreamVO.java`（列表+详情统一视图）、`PageResult.java`（list/total/page/size/pages/hasMore）、`HomeOverviewVO.java`、`HomeStatsVO.java`、`CategoryVO.java`、`HotKeywordVO.java`
- `config/MybatisPlusConfig.java` — 分页拦截器（缺它 Page 不生效）
- 单元测试：`src/test/java/.../DreamServiceImplTest.java`（**20 个用例**：鉴权、点赞/收藏增删、发布归一、编辑/删除权限、我的梦境强制归属、个人中心统计、分页上限、搜索日志）、`.../util/JwtUtilTest.java`（5 个用例：签发解析、伪造、过期、篡改）→ 合计 **26 个用例全绿**
  - ⚠️ 纯 Mock 单测里 MP 的 lambda 列名缓存是空的，一旦物化 SQL 片段（`getCustomSqlSegment()`）就抛 `can not find lambda cache for this entity`；`@BeforeAll` 里用 `TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), "unit-test"), Dream.class)` 初始化即可。另注意 lambda 条件是**惰性**的：不先物化 SQL 就读 `getParamNameValuePairs()` 会拿到空 map
- `sql/dream_login.sql` — 建库 + `t_user` + 3 个演示账号（梦梦/星星/夜风，密码都是 `abc123456`，哈希与 `PasswordUtil` 的盐一致）
- `sql/dream_home.sql` — 建 `t_dream`（12 条种子）/ `t_dream_category`（4 条）/ `t_dream_like`（5 条）/ `t_dream_search_log`（14 条）/ `t_dream_favorite`（5 条）
- `sql/dream_drama.sql` — 建 `t_drama`（9 条广播剧种子，`videoUrl` 指向本地 `/media/drama/*.mp4`）/ `t_drama_category`（6 条：悬疑/言情/科幻/治愈/古风/儿童）；启动脚本第 4.5 步自动执行（t_drama 缺失时）
- `sql/dream_drama_media_fix.sql` — 幂等修复脚本，把存量库里指向 Google 示例桶的 `video_url` 改成本地 `/media/drama/*.mp4`（老库升级执行一次即可）

**广播剧（新增，首页「广播剧」功能，全部匿名可访问，零 XML）**
- `controller/DramaController.java` — `/api/drama/list`、`/detail`、`/categories`
- `service/DramaService.java` + `service/impl/DramaServiceImpl.java` — 列表（关键词/分类/平台/排序/分页，只取 status=1 未删除）、详情（浏览量原子 +1）、分类导航（含各分类数量）；`applyKeyword`/`applyCategory`/`applyPlatform`/`applySort` 复用梦境那套转义与排序约定；`categoryCode` 由分类字典 name→code 映射（兜底内置映射）
- `mapper/DramaMapper.java`、`DramaCategoryMapper.java`
- `entity/Drama.java`（`platform` 来源平台字段）、`DramaCategory.java`
- `dto/DramaQuery.java`（page/size/keyword/category/platform/sort）、`DramaDetailDTO.java`
- `vo/DramaVO.java`（列表+详情统一视图，含 `videoUrl`/`audioUrl`/`posterUrl`/`categoryCode`/`platform`）

**后台管理系统（新增 2026-10-30）**
- `controller/AdminController.java` — `/api/admin/**` 共 18 个接口（登录/信息/概览/梦境/广播剧/分类/用户/搜索日志/互动数据）
- `service/AdminService.java` + `service/impl/AdminServiceImpl.java` — 后台全部业务逻辑
- `entity/Admin.java`（`t_admin`，与普通用户隔离）、`mapper/AdminMapper.java`、`dto/AdminLoginDTO.java` / `AdminDreamDTO.java` / `AdminDramaDTO.java` / `AdminCategoryDTO.java`
- `vo/AdminStatsVO.java`（概览 8 项统计）、`vo/AdminCategoryVO.java`
- `config/AdminSchemaInit.java` — ApplicationRunner 幂等建 `t_admin` + 植入默认管理员
- `UserMapper` / `DreamLikeMapper` / `DreamFavoriteMapper` 新增 `selectAdminPage` / `selectAdminCount` 自定义 `@Select`（联表带标题与用户名，`DATE_FORMAT` 转时间字符串避免序列化成数组）
- `sql/dream_admin.sql` — 等价手动脚本

**首页配置（新增 2026-10-30）**
- `entity/SiteConfig.java`（`t_site_config`；常量 `TYPE_NAV_MENU` / `TYPE_SHORTCUT` / `TYPE_PLATFORM` / `TYPE_SITE_TEXT`）
- `mapper/SiteConfigMapper.java`、`dto/SiteConfigDTO.java`
- `service/SiteConfigService.java` + `service/impl/SiteConfigServiceImpl.java`
- `controller/SiteConfigController.java` — `/api/admin/home-config/**`；`HomeController` 新增匿名 `POST /api/home/config`
- `config/SiteConfigInit.java` — ApplicationRunner 幂等建表 + 植入 19 条种子（5 导航 + 5 快捷入口 + 5 平台 + 4 文案）。**只补齐缺失项**（按 `config_type + config_key` 判断），不会覆盖人工改动过的数据
- `sql/dream_site_config.sql` — 等价手动脚本

**端到端冒烟脚本**
- `e2e-admin-test.js`（项目根目录）— `node e2e-admin-test.js`，**53 项断言全通过**
  - 覆盖：管理员登录 / JWT role / 错误密码 / 匿名拦截 / **普通用户越权 401** / 14 个后台接口 / 7 个匿名接口 / 首页配置完整性 / **CRUD 往返（新增→前台可见→停用→前台隐藏→删除→前台恢复）** / 参数校验（非法类型、缺 label、重复键、站点文案禁删）
  - **自带清理**：临时注册普通用户做越权校验，结尾由管理员接口注销，可反复运行
  - ⚠️ 跑完会留下**逻辑删除**的临时行（BaseMapper 自动过滤，页面看不到，但 `GROUP BY` 统计仍能看到）。测试残留可用 mysql 客户端按 key 物理清掉

## 五、前端要点

- `src/api.js` — 接口封装，`BASE = /api`（可用 `.env` 的 `VITE_API_BASE` 覆盖）
- `src/components/LoginCard.vue` — 登录/注册双表单：字段级内联校验、密码强度条、验证码倒计时、用户协议勾选；登录成功 `emit('login-success', {user, remember})`
- `src/components/HomeCard.vue` — 登录后的欢迎主页（用户名、登录时间、退出登录）
- `src/App.vue` — 会话管理：记住我 → localStorage，否则 → sessionStorage（键 `dream-user`），刷新自动恢复，退出清除
- **后台管理（新增 2026-10-30）**：会话键独立为 `dream-admin`，与普通用户并存互不干扰，退出后台不会把用户踢下线
  - `src/components/AdminLogin.vue` — 只有账号+密码，**明确不提供注册**
  - `src/components/AdminDashboard.vue` — 8 个菜单：数据概览 / 梦境管理 / 广播剧管理 / 分类管理 / 用户管理 / 搜索日志 / 互动数据 / **首页配置**
  - `src/homeConfig.js`（新增）— 首页配置共享模块：12 个预置图标集、带缓存的 `loadHomeConfig(force)`、失败兜底 `DEFAULT_CONFIG`
  - 详细组件说明见前端 `vue-dream-login/PROJECT_NOTES.md` 第五节
- `vite.config.js` — `/api` 代理到 `http://localhost:8081`

## 六、本机环境坑（都踩过，别再排查）

1. **端口被环境变量覆盖**：本机有环境变量把 `server.port` 顶成 54322（AI 沙箱里是 `SERVER__PORT=53383`，而且那个端口已被沙箱自己的代理占用，直接启动会报 `Port 53383 was already in use`）。启动必须显式传 `--server.port=8081`（脚本已处理）
2. **npm 官方源极慢**：必须用 `--registry=https://registry.npmmirror.com`（约 50 秒装完）
3. **MySQL 客户端不在 PATH**：用 `C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe`；服务名 `MySQL80`
4. **JAVA_HOME**：`C:\Program Files\Zulu\zulu-21`；系统无 mvn，用项目自带 `mvnw.cmd`
5. **Maven 镜像**：`~/.m2/settings.xml` 已配阿里云；若被重置，mvnw 首次下载 Maven 会慢
6. MySQL 里已有测试用户：梦梦 / 星星 / 夜风，密码统一 `abc123456`（注册接口也能自己开号）
7. **别用 git bash 跑 `./mvnw`**：那个 shell 脚本在 Windows 下解析 MAVEN_HOME 会失败（报 `ClassNotFoundException: org.codehaus.plexus.classworlds.launcher.Launcher`）。用 `mvnw.cmd`，或直接调缓存的 Maven：`C:\Users\Administrator\.m2\wrapper\dists\apache-maven-3.9.9-bin\4nf9hui3q3djbarqar9g711ggc\apache-maven-3.9.9\bin\mvn.cmd -o -DskipTests package`
8. `sc.exe` / `cmd.exe` 在本机沙箱里被禁用，查端口用 `netstat -ano | findstr :8081`，杀进程用 PowerShell `Stop-Process -Id <pid> -Force`
9. **打包前必须先停后端**：jar 被运行中的进程占用会导致 `repackage` 报 `Unable to rename ... .jar.original`（而且 thin jar 已经把 fat jar 覆盖掉了，运行起来会缺依赖）
   - 找占用者：`tasklist | grep -i java.exe`，然后 `Stop-Process -Id <pid> -Force`；`Get-Process java` 在沙箱里有时查不到输出，用 `tasklist` 更可靠
   - 判断文件是否真被锁：`mv x.jar y.jar` 报 `Device or resource busy` 即被占用（能 `cp` 说明只是禁止删除/改名）
10. 首次跑测试要联网一次（本地仓库缺 `surefire-junit-platform`），之后 `mvn.cmd -o test` 可离线跑
11. **AI 沙箱里 `curl` 要加 `--noproxy '*'`**：否则 localhost 请求会被代理拦下返回 502；PowerShell 工具的前台 stdout 有时拿不到，需要输出时建议用 `run_in_background` 或改用 git bash 的 `tasklist` / `netstat` / `curl`
12. **mysql 查中文字段必须加 `--default-character-set=utf8mb4`**：否则输出被判定为二进制，报 `Binary file (standard input) matches`
13. **bash 里 `UID` 是只读变量**，不能用作脚本变量名（换成 `TID` 之类）
14. **devtools 会自动重启**：改完 Java 类编译进 `target/classes` 后 running 实例自动生效，一般不用手动重启后端

## 七、已知缺口（用户可能让你继续做的）

- ~~首页搜索后端~~（已完成：`/api/search`、`/api/home/*`、`/api/dream/*` 全部就绪）
- ~~接口 JWT 鉴权~~（已完成：登录发 token，点赞/收藏/发布/编辑/删除按 token 身份）
- ~~后台管理系统~~（已完成：`/api/admin/**` 18 个接口 + 独立管理员账号 + 8 个管理菜单）
- ~~首页硬编码栏目纳管~~（已完成：`t_site_config` + 「首页配置」菜单，覆盖导航菜单/快捷入口/广播剧平台/站点文案）

**仍为前端硬编码、未纳入后台**（如要继续做，参考首页配置的做法）：
- Hero 区搜索框 placeholder「搜索梦境、故事或关键词…」、问候语「今天想找一场怎样的梦？」
- 区块标题与角标文案：如「快捷入口」这个标题本身、「大家都在看」的说明「按浏览量挑选的 6 场梦」
- 后台自己的侧边菜单（`AdminDashboard.vue` 的 `menus` 数组）—— 一般不需要数据化
- ⚠️ 上线前记得改掉默认管理员密码 `admin123`（该账号现在能改全站文案，风险更高）
- ~~收藏功能~~（已完成：`t_dream_favorite` + `/api/dream/favorite` + `/api/user/favorites`，前端「我的收藏」已接通）
- ~~无单元测试~~（已完成：`DreamServiceImplTest` 20 例 + `JwtUtilTest` 5 例 = 26 例，`mvn.cmd -o test` 全绿）
- ~~创作闭环 + 个人中心后端~~（已完成：`/api/dream/update`、`/api/dream/delete`、`/api/user/dreams`、`/api/user/profile`，含「只能改/删自己内容」的归属校验）
- ~~「梦境手记 / 个人中心」占位页~~（已完成：前端发布/编辑弹层 + 个人中心视图，见前端 `PROJECT_NOTES.md`）
- ~~广播剧功能~~（已完成：`t_drama` / `t_drama_category` + `DramaController` / `DramaServiceImpl` + 前端 `DramaPage.vue`，首页导航「广播剧」可挑选并播放，含分类/平台/排序/搜索，播放器 video 有画面也有声音、audio 回退；匿名可访问）
- 剩余占位：「帮助中心」页；导航里的「梦境广场」目前等价于首页精选流
- token 没有刷新/注销机制（改密码或想强制下线只能换 `dream.jwt.secret`）；`dream.jwt.secret` 是开发默认值，上线前必须换成随机长串
- 忘记密码、第三方登录是占位
- 验证码未接真实短信/邮件（可配 spring-boot-starter-mail）
- 密码哈希可升级 BCrypt（`PasswordUtil` 目前是 SHA-256 + 固定盐）
- 演示数据里 `t_dream.likes` 是历史累计值，`t_dream_like` 只记录新产生的点赞，两者不是严格对账关系（种子数据故意如此）
- 自测留下的小尾巴：`t_user` 里有个空账号 `123`（id=4）；`t_dream_search_log` 有些自测搜索词，都是正常业务数据，不影响展示
