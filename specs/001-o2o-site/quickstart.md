# Quickstart - 本地生活 O2O 网站

## 1. 运行环境
- JDK 17
- Maven 3.9+
- Node.js 20+
- MySQL 8
- Redis 7
- Nginx 1.24+

## 2. 建议目录
```text
backend/
frontend/
deploy/nginx/
```

## 3. 本地启动步骤

### 3.1 启动基础设施
1. 启动 MySQL 8，创建业务库 `hmdp`。
2. 启动 Redis 7。
3. 初始化商户、优惠券、用户测试数据。

### 3.2 启动后端
1. 在 `backend/` 下配置 `application-local.yml`：
   - MySQL 连接
   - Redis 连接
   - 短信服务占位配置
2. 执行：
   - `mvn clean test`
   - `mvn spring-boot:run -Dspring-boot.run.profiles=local`
3. 验证：访问健康检查接口或打开接口文档页。

### 3.3 启动前端
1. 在 `frontend/` 下安装依赖：`npm install`
2. 启动开发服务器：`npm run dev`
3. 在前端环境变量中配置 API 基地址，例如 `/api` 或本地后端地址。

## 4. 测试策略

### 4.1 后端自动化验证
- 单元测试：
  - 验证码校验失败场景
  - 商户缓存命中/空值缓存/互斥重建逻辑
  - 秒杀 Lua 返回码映射
  - 点赞、关注、共同关注服务逻辑
  - 匿名访问白名单与需登录操作拦截逻辑
- 集成测试：
  - 基于 Testcontainers 启动 MySQL + Redis
  - 验证匿名浏览首页、商户详情、附近商户、活动列表、内容流、点赞排行
  - 验证短信登录、秒杀、防重、签到、笔记发布、点赞、共同关注接口
- 契约测试：
  - 按 `contracts/api.yaml` 校验关键接口字段、状态码与公开/鉴权边界

### 4.2 前端自动化验证
- Vitest：登录表单、商户列表、笔记卡片、关注按钮等组件行为
- Playwright：
  - 匿名浏览首页与商户页
  - 匿名浏览内容流和点赞排行
  - 未登录触发抢券/签到/点赞/关注时跳转或弹出登录引导
  - 短信登录主路径
  - 浏览附近商户
  - 抢优惠券
  - 每日签到
  - 发布探店笔记
  - 点赞与关注

## 5. 人工验收清单
- 所有默认页面文案和错误提示为中文。
- 新增关键业务代码已补充必要中文注释，且未用注释重复显而易见的实现。
- 关键业务链路、外部依赖调用、异常分支与状态迁移存在可检索中文日志。
- 首页、商户列表/详情、附近商户、优惠券活动列表、探店内容流和点赞排行可在未登录时直接访问。
- 未登录时，抢券、签到、发布、点赞、关注和共同关注查询会先引导登录。
- 拒绝位置权限时，附近商户页面能展示降级提示或默认商户列表。
- 连续滚动内容流时，无明显重复项或跳序。
- 同一用户重复抢同一优惠券时，提示明确且不会重复生成订单。

## 5.1 注释与日志检查要点
- 中文注释仅用于说明业务约束、边界条件、缓存/并发策略、登录拦截边界等不易从代码直接看出的意图。
- 中文日志至少覆盖登录校验失败、缓存重建/降级、秒杀校验结果、签到结果、关注关系变化、内容发布失败等关键节点。
- 日志中不得输出验证码、令牌、完整手机号、数据库密钥等敏感信息。
- 抽样验证日志字段是否足以支持定位请求结果、失败原因与关键业务对象。


## 5.2 US1 商户与登录链路性能验证记录
- 商户列表首屏目标：p95 < 2s；当前状态：待执行本地压测并补记录。
- 商户详情缓存命中目标：p95 < 300ms；当前状态：待执行缓存命中复验并补记录。
- 附近商户查询目标：p95 < 2s；当前状态：待执行 GEO 查询复验并补记录。
- 验证步骤建议：先启动 MySQL/Redis 与前后端服务，再用浏览器访问首页、商户列表、商户详情、附近商户；随后对 `/api/shops/{id}` 做连续请求，分别记录冷启动与缓存命中耗时。

## 5.3 US2 秒杀与签到验收记录
- 后端单元测试：`VoucherSeckillServiceTest`、`SignServiceTest` 已通过，已覆盖秒杀 Lua 返回码映射、重复领券拦截、签到位图与连续签到统计。
- 后端集成测试：`AuthAndShopFlowIntegrationTest`、`VoucherAndSignFlowIntegrationTest` 当前被 Testcontainers 阻塞；本机缺少可用 Docker 环境，错误为 `Could not find a valid Docker environment`。
- 前端构建与单测：`npm run build` 与 `npm test` 已通过；当前 shell 会额外输出 MobaXterm cwd 日志缺失噪声，但不影响 Vite/Vitest 实际结果。
- 前端 E2E：`us2-voucher-and-sign.spec.ts` 已编写，当前被 Playwright 浏览器缺失阻塞；需先执行 `npx playwright install`。
- 当前人工验收建议：登录后访问 `/vouchers`，验证活动列表加载、抢券成功提示、重复领取报错、签到成功提示与连续签到天数展示。
- 秒杀接口延迟目标：p95 < 3s；当前状态：待在 Docker 与浏览器环境补齐后做端到端复验并补充数据。
- 重复领券拦截目标：同一用户重复请求只能成功一次；当前状态：已由单元测试验证，待集成测试复验。
- 签到校验目标：当日只能签到一次且可返回连续签到天数；当前状态：已由单元测试验证，待集成/E2E 复验。

## 5.4 US3 内容与社交验收记录
- 后端单元测试：`BlogInteractionServiceTest` 已通过，已覆盖点赞状态判断、共同关注集合交集、自关注拦截和滚动分页游标构造的关键断言。
- 后端集成测试：`BlogAndFollowFlowIntegrationTest` 已编写，当前被 Testcontainers 阻塞；本机缺少可用 Docker 环境，错误为 `Could not find a valid Docker environment`。
- 前端构建与单测：`npm run build` 与 `npm test` 已通过；当前 shell 仍会附带 MobaXterm cwd 日志缺失噪声，但不影响 Vite/Vitest 的实际结果。
- 前端 E2E：`us3-blog-and-social.spec.ts` 已编写，当前被 Playwright 浏览器缺失阻塞；需先执行 `npx playwright install`。
- 当前人工验收建议：登录后访问 `/blogs`、`/blogs/publish`、`/blogs/hot`、`/follows/common/2`，验证内容流加载、发布成功提示、点赞切换、共同关注列表展示，以及匿名访问热榜/内容流、受保护动作弹登录引导。
- 内容流稳定性目标：滚动浏览无明显重复项或跳序；当前状态：页面与接口已实现，待 Docker 与浏览器环境补齐后做端到端复验。
- 热榜正确性目标：点赞后热榜状态与文案同步更新；当前状态：前后端已接通，待集成/E2E 复验。
- 社交交互目标：关注/取关与共同关注查询保持中文提示且遵守登录边界；当前状态：后端接口与前端页面已完成，待集成测试补齐验证。

## 5.5 Phase 6 收尾验收记录
- 前端单元测试：`frontend/tests/unit/public-copy.spec.ts` 已补充导航、公开浏览、空状态与登录引导相关中文文案断言，并通过 `npm test` 验证。
- 前端 E2E：`frontend/tests/e2e/public-empty-states.spec.ts` 已补充公开页空状态与未登录点赞触发登录引导的主路径；当前仍因 Playwright 浏览器缺失未实际执行，需先执行 `npx playwright install`。
- 前端构建与 lint：`npm run build` 已通过；`npm run lint` 已执行且未输出 ESLint 违规，当前 shell 仍会附带 MobaXterm cwd 日志缺失噪声并返回非零退出码。
- 后端单元测试：`BlogInteractionServiceTest` 已补充内容流游标分页回归断言，并通过 `mvn -Dtest=BlogInteractionServiceTest test` 验证；当前实现已改为按 `lastId` 游标继续加载，避免“继续浏览”重复返回上一页内容。
- 后端契约测试：`backend/src/test/java/com/hmdp/contract/PublicAndProtectedApiContractTest.java` 已新增公开接口字段与鉴权边界回归覆盖；当前仍因 Testcontainers 依赖 Docker，未能在本机完成执行，错误为 `Could not find a valid Docker environment`。
- 当前剩余自动化验证缺口：需在具备 Playwright 浏览器与 Docker 的环境中补跑 `us1`、`us2`、`us3`、`public-empty-states` 与 `PublicAndProtectedApiContractTest`，完成公开页、鉴权边界、契约字段和登录引导的自动化回归闭环。
- 当前剩余风险：短信网关仍为占位实现；Playwright 浏览器与 Docker 环境未就绪导致 E2E / 集成 / 契约测试尚未在本机闭环；内容流当前以 `id` 倒序游标为准，若后续切换到“同时间戳多条内容”排序模型，需要再扩展 `lastId + offset` 的完整游标协议。

## 6. 性能验收基线
- 商户详情缓存命中 p95 < 300ms
- 商户列表、附近商户、内容流首屏 p95 < 2s
- 秒杀接口结果返回 p95 < 3s
- 点赞/关注/共同关注/签到接口 p95 < 500ms

## 7. Nginx 部署说明
1. 前端执行 `npm run build`，生成静态资源。
2. Nginx 托管 `frontend/dist`。
3. Nginx 反向代理 `/api` 到 Spring Boot 服务。
4. 为 SPA 配置 `try_files`，确保前端路由刷新可回到 `index.html`。
5. 仅前端路由控制公开页与登录页展示，后端仍必须对需登录接口执行鉴权校验。

## 8. 发布前必须确认
- 后端测试、前端测试、契约测试全部通过。
- 至少完成一次秒杀压测与一次缓存热点复验。
- 中文文案抽样检查通过。
- 公开页与需登录操作的边界已通过人工与自动化双重验证。
- 记录剩余风险，例如短信网关、图片上传、内容审核等暂未实现部分。