# Phase 0 Research - 本地生活 O2O 网站

## 决策 1：后端采用 Spring Boot 单体分层架构
- **Decision**: 使用 Spring Boot 3.x + Java 17 + Maven 构建单体 Web 服务，按 controller / service / mapper / entity / dto 分层。
- **Rationale**: 当前首期功能虽然覆盖 8 个模块，但共享用户、商户、优惠券、内容与社交数据，单体结构更利于快速交付、事务边界控制、统一鉴权与测试。
- **Alternatives considered**: 微服务拆分；被拒绝，因为首期会明显增加部署、联调、链路追踪与一致性成本，不利于“可验证的最小交付”。

## 决策 2：商户详情与列表采用缓存旁路模式
- **Decision**: 商户详情使用 Redis 缓存旁路（Cache-Aside），空值缓存防穿透，互斥锁或逻辑过期防击穿，商户列表热点页可做短 TTL 缓存。
- **Rationale**: 商户详情是高频读场景，缓存可显著降低 MySQL 压力，并满足详情查询 p95 < 300ms、首屏加载 p95 < 2s 的目标。
- **Alternatives considered**: 纯数据库查询；被拒绝，因为热点商户被重复访问时容易触发明显性能退化。全量预热缓存；被拒绝，因为商户规模增长后缓存成本更高且更新复杂。

## 决策 3：附近商户使用 Redis GEO
- **Decision**: 将商户经纬度索引写入 Redis GEO，按用户坐标进行半径检索和距离排序，再回表 MySQL/缓存补充商户详情。
- **Rationale**: GEO 原生支持按距离搜索和排序，适合“附近商户”场景，能避免纯 SQL 地理计算带来的复杂度与性能压力。
- **Alternatives considered**: MySQL 地理函数；被拒绝，因为首期无需引入更重的 GIS 能力，Redis GEO 更贴合当前规模与栈。

## 决策 4：优惠券秒杀采用 Lua 原子预检 + 数据库兜底
- **Decision**: 秒杀请求先执行 Redis Lua 脚本，原子完成“活动状态校验、库存是否足够、用户是否已领”的预检与扣减；通过后再异步或同步写入订单表，并用数据库唯一索引 `(user_id, voucher_id)` 兜底防重。
- **Rationale**: Lua 能把多步 Redis 操作收敛为单次原子执行，适合高并发一人一券与防超卖场景；数据库唯一索引保证最终一致性。
- **Alternatives considered**: 仅数据库乐观锁；被拒绝，因为高峰秒杀下数据库冲突与压力更大。仅 Redisson 分布式锁；被拒绝，因为锁粒度更粗，吞吐不如 Lua 原子校验。

## 决策 5：Redisson 仅用于少量分布式互斥与重建保护
- **Decision**: Redisson 用于缓存重建互斥、可能的订单串行保护等少数需要 JVM 级友好封装的分布式锁场景，不替代 Lua 的高频原子预检。
- **Rationale**: Redisson API 易用，适合在 Spring 服务层实现可读性较强的临界区控制；但不适合把所有高并发路径都锁住。
- **Alternatives considered**: 全部自行维护原生 Redis 锁；被拒绝，因为可读性、可维护性与可靠性更差。

## 决策 6：签到与 UV 分别使用 Bitmap / HyperLogLog
- **Decision**: 用户签到按“用户维度 + 月份”使用 Redis Bitmap 记录每日签到位；站点 UV 统计按“日期维度”使用 HyperLogLog 去重统计独立访客。
- **Rationale**: Bitmap 对每日签到的空间效率极高，且便于计算连续签到；HyperLogLog 适合近似 UV 去重统计，内存成本低。
- **Alternatives considered**: 全量签到明细实时查 MySQL；被拒绝，因为读写与统计成本更高。UV 使用 Set；被拒绝，因为高访问量下内存占用更大。

## 决策 7：点赞排行与共同关注分别使用 ZSet / Set
- **Decision**: 笔记点赞关系以 MySQL 为业务真值，Redis Set 保存用户点赞集合，Redis ZSet 维护笔记热度排行；关注关系以 MySQL 为真值，Redis Set 维护用户关注集合，并通过交集计算共同关注。
- **Rationale**: Set/ZSet 非常契合点赞状态查询、排行榜和共同关注交集运算，响应快且实现直接。
- **Alternatives considered**: 全部依赖 SQL 聚合与联表；被拒绝，因为互动功能需要更低时延和更高读并发承载。

## 决策 8：探店内容流采用滚动分页
- **Decision**: 内容流接口采用基于游标的滚动分页，主要排序键为发布时间或热度时间戳，翻页参数使用 `lastId` + `offset`。
- **Rationale**: 相比 offset/limit，滚动分页在高并发追加内容下更稳定，能减少重复项、漏项和错序。
- **Alternatives considered**: 普通页码分页；被拒绝，因为内容流滚动场景下稳定性较差。

## 决策 9：测试策略采用后端集成优先、前端 E2E 补主路径
- **Decision**: 后端使用 JUnit 5 + Spring Boot Test + Testcontainers 验证 MySQL/Redis 真实集成；前端使用 Vitest 做组件单测，Playwright 覆盖匿名浏览、短信登录、浏览商户、抢券、签到、发笔记、点赞、关注等主路径。
- **Rationale**: 本项目核心价值集中在缓存、并发控制、Redis 数据结构和 HTTP 交互，单纯 mock 无法证明行为正确；真实容器集成更符合宪章中的“测试先行与防回归”。
- **Alternatives considered**: 全部 mock 测试；被拒绝，因为不能有效覆盖 Lua、Redis GEO、Bitmap、HyperLogLog 等关键行为。

## 决策 10：前端采用 Vue 3 + Pinia + Vue Router + Nginx 部署
- **Decision**: 前端使用 Vue 3 构建 SPA，Pinia 管理登录态与局部业务状态，Vue Router 组织页面，打包产物由 Nginx 静态托管并转发 `/api` 到 Spring Boot。
- **Rationale**: 与用户要求完全匹配，部署链路简单，便于快速交付中文界面网站。
- **Alternatives considered**: Nuxt SSR；被拒绝，因为当前首期没有明确 SEO/SSR 刚性要求，SPA + API 更轻量。

## 决策 11：采用匿名只读访问 + 登录后交互的鉴权边界
- **Decision**: 首页、商户列表/详情、附近商户、优惠券活动列表、探店内容流和点赞排行允许匿名访问；抢券、签到、发布笔记、点赞、关注和共同关注查询必须登录后才能执行。
- **Rationale**: 该边界同时满足低门槛浏览和核心互动防滥用需求，能减少首次访问阻力，又不会放松对用户权益、互动数据和社交关系的身份约束。
- **Alternatives considered**: 全站强制登录；被拒绝，因为会抬高浏览门槛。所有页面和操作都匿名开放；被拒绝，因为会破坏用户行为归属、权益控制和风控基础。
