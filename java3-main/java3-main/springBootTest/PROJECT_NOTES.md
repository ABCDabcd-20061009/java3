# 粉金梦境 登录注册系统 · 项目笔记（给 AI 助手看的速查手册）

> 下次会话先读本文件，不要重新探索。电脑每次开机会重置：`node_modules`、`target` 会被删除，按本文「快速启动」执行即可。

## 一、项目组成

| 模块 | 路径 | 端口 | 技术栈 |
|---|---|---|---|
| 后端 | `C:\Users\Administrator\Desktop\java3-main\java3-main\springBootTest`（旧的 `D:\workSpace\springBootTest` 是更早的副本，以本目录为准） | 8081 | Spring Boot 3.4.5 + MyBatis-Plus 3.5.7 + MySQL 8.0，Java 17（本机装的 Zulu 21 可跑） |
| 前端 | `C:\Users\Administrator\Desktop\java3-main\java3-main\vue-dream-login` | 5173 | Vue 3.5 + Vite 6 |
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

前端无后端时会自动进入「演示模式」：`api.js` 里网络错误带 `isNetwork=true` 标记，仅此时降级本地生成验证码。

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

## 五、前端要点

- `src/api.js` — 接口封装，`BASE = /api`（可用 `.env` 的 `VITE_API_BASE` 覆盖）
- `src/components/LoginCard.vue` — 登录/注册双表单：字段级内联校验、密码强度条、验证码倒计时、用户协议勾选；登录成功 `emit('login-success', {user, remember})`
- `src/components/HomeCard.vue` — 登录后的欢迎主页（用户名、登录时间、退出登录）
- `src/App.vue` — 会话管理：记住我 → localStorage，否则 → sessionStorage（键 `dream-user`），刷新自动恢复，退出清除
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

## 七、已知缺口（用户可能让你继续做的）

- ~~首页搜索后端~~（已完成：`/api/search`、`/api/home/*`、`/api/dream/*` 全部就绪）
- ~~接口 JWT 鉴权~~（已完成：登录发 token，点赞/收藏/发布/编辑/删除按 token 身份）
- ~~收藏功能~~（已完成：`t_dream_favorite` + `/api/dream/favorite` + `/api/user/favorites`，前端「我的收藏」已接通）
- ~~无单元测试~~（已完成：`DreamServiceImplTest` 20 例 + `JwtUtilTest` 5 例 = 26 例，`mvn.cmd -o test` 全绿）
- ~~创作闭环 + 个人中心后端~~（已完成：`/api/dream/update`、`/api/dream/delete`、`/api/user/dreams`、`/api/user/profile`，含「只能改/删自己内容」的归属校验）
- ~~「梦境手记 / 个人中心」占位页~~（已完成：前端发布/编辑弹层 + 个人中心视图，见前端 `PROJECT_NOTES.md`）
- 剩余占位：「帮助中心」页；导航里的「梦境广场」目前等价于首页精选流
- token 没有刷新/注销机制（改密码或想强制下线只能换 `dream.jwt.secret`）；`dream.jwt.secret` 是开发默认值，上线前必须换成随机长串
- 忘记密码、第三方登录是占位
- 验证码未接真实短信/邮件（可配 spring-boot-starter-mail）
- 密码哈希可升级 BCrypt（`PasswordUtil` 目前是 SHA-256 + 固定盐）
- 演示数据里 `t_dream.likes` 是历史累计值，`t_dream_like` 只记录新产生的点赞，两者不是严格对账关系（种子数据故意如此）
- 自测留下的小尾巴：`t_user` 里有个空账号 `123`（id=4）；`t_dream_search_log` 有些自测搜索词，都是正常业务数据，不影响展示
