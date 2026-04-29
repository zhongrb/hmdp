# Tasks: 本地生活 O2O 网站

**Input**: Design documents from `/specs/001-o2o-site/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/api.yaml, quickstart.md

**Tests**: Tests are REQUIRED. 本特性规格已明确要求为登录、商户浏览、秒杀、签到、笔记发布、点赞、关注等核心行为提供自动化验证，并验证性能与中文文案。

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g. US1, US2, US3)
- 所有任务都包含精确文件路径
- 所有用户故事阶段都先写测试，再写实现，再做性能/中文验收

## Path Conventions

- 后端：`backend/src/main/java/com/hmdp/`、`backend/src/main/resources/`、`backend/src/test/java/com/hmdp/`
- 前端：`frontend/src/`、`frontend/tests/`
- 部署：`deploy/nginx/`
- 规格与验收：`specs/001-o2o-site/`

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: 初始化前后端工程、部署骨架和统一规范

- [X] T001 初始化后端 Maven 工程和前端 Vue 工程清单于 `backend/pom.xml`、`frontend/package.json`
- [X] T002 [P] 配置后端基础运行参数与本地环境示例于 `backend/src/main/resources/application.yml`、`backend/src/main/resources/application-local.yml`
- [X] T003 [P] 配置前端构建、单测、E2E 与 lint 脚本于 `frontend/vite.config.ts`、`frontend/vitest.config.ts`、`frontend/playwright.config.ts`、`frontend/eslint.config.js`
- [X] T004 [P] 配置 Nginx 静态托管和 `/api` 反向代理于 `deploy/nginx/default.conf`
- [X] T005 建立全站中文文案常量与前端基础路由定义于 `frontend/src/constants/copy.ts`、`frontend/src/router/routes.ts`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: 所有用户故事共享的阻塞性基础设施

**⚠️ CRITICAL**: 本阶段完成前，不得开始任何用户故事实现

- [X] T006 创建首期核心表结构与初始化脚本于 `backend/src/main/resources/sql/schema.sql`、`backend/src/main/resources/sql/data.sql`
- [X] T007 [P] 配置 Redis、Redisson 与 Lua 脚本加载支持于 `backend/src/main/java/com/hmdp/config/RedisConfig.java`、`backend/src/main/java/com/hmdp/config/RedissonConfig.java`、`backend/src/main/java/com/hmdp/config/LuaScriptConfig.java`
- [X] T008 [P] 实现统一返回体、全局异常处理和中文错误映射于 `backend/src/main/java/com/hmdp/dto/Result.java`、`backend/src/main/java/com/hmdp/exception/GlobalExceptionHandler.java`
- [X] T009 实现登录态刷新、公开路由白名单和登录拦截器于 `backend/src/main/java/com/hmdp/utils/UserHolder.java`、`backend/src/main/java/com/hmdp/interceptor/RefreshTokenInterceptor.java`、`backend/src/main/java/com/hmdp/interceptor/LoginInterceptor.java`、`backend/src/main/java/com/hmdp/config/WebMvcConfig.java`
- [X] T010 [P] 实现前端统一 HTTP 客户端、鉴权状态和路由守卫于 `frontend/src/api/http.ts`、`frontend/src/stores/auth.ts`、`frontend/src/router/guards.ts`
- [X] T011 [P] 实现滚动分页、用户会话和地理查询共享 DTO 于 `backend/src/main/java/com/hmdp/dto/ScrollResult.java`、`backend/src/main/java/com/hmdp/dto/UserDTO.java`、`backend/src/main/java/com/hmdp/dto/ShopGeoResult.java`
- [X] T012 配置后端 Testcontainers 基类和前端 E2E 夹具于 `backend/src/test/java/com/hmdp/integration/BaseIntegrationTest.java`、`frontend/tests/e2e/fixtures/auth.ts`

**Checkpoint**: Foundation ready - user story implementation can now begin

---

## Phase 3: User Story 1 - 匿名浏览并完成登录 (Priority: P1) 🎯 MVP

**Goal**: 支持匿名访问只读页面、短信登录、商户详情缓存和附近商户浏览

**Independent Test**: 用户未登录时可访问首页、商户列表/详情、附近商户、活动列表、内容流和点赞排行；尝试抢券、签到、发布、点赞、关注时会被引导登录；登录后可正常完成商户浏览。

### Tests for User Story 1 ⚠️

- [X] T013 [P] [US1] 编写公开路由白名单和验证码校验单元测试于 `backend/src/test/java/com/hmdp/unit/AuthGuardTest.java`
- [X] T014 [P] [US1] 编写匿名浏览、短信登录、商户缓存和附近商户集成测试于 `backend/src/test/java/com/hmdp/integration/AuthAndShopFlowIntegrationTest.java`
- [X] T015 [P] [US1] 编写匿名浏览与登录引导 E2E 测试于 `frontend/tests/e2e/us1-anonymous-browse-and-login.spec.ts`

### Implementation for User Story 1

- [X] T016 [P] [US1] 实现登录表单 DTO、短信验证码发送/校验和登录控制器于 `backend/src/main/java/com/hmdp/dto/LoginFormDTO.java`、`backend/src/main/java/com/hmdp/service/impl/UserServiceImpl.java`、`backend/src/main/java/com/hmdp/controller/UserController.java`
- [X] T017 [P] [US1] 实现用户、商户、商户分类实体与 Mapper 于 `backend/src/main/java/com/hmdp/entity/User.java`、`backend/src/main/java/com/hmdp/entity/Shop.java`、`backend/src/main/java/com/hmdp/entity/ShopType.java`、`backend/src/main/java/com/hmdp/mapper/UserMapper.java`、`backend/src/main/java/com/hmdp/mapper/ShopMapper.java`、`backend/src/main/java/com/hmdp/mapper/ShopTypeMapper.java`
- [X] T018 [US1] 实现商户详情缓存、防穿透/击穿和公开查询服务于 `backend/src/main/java/com/hmdp/utils/CacheClient.java`、`backend/src/main/java/com/hmdp/service/impl/ShopServiceImpl.java`
- [X] T019 [US1] 实现商户列表、详情和附近商户公开接口于 `backend/src/main/java/com/hmdp/controller/ShopController.java`
- [X] T020 [P] [US1] 实现匿名首页、商户列表、商户详情和登录页于 `frontend/src/views/HomeView.vue`、`frontend/src/views/ShopListView.vue`、`frontend/src/views/ShopDetailView.vue`、`frontend/src/views/LoginView.vue`
- [X] T021 [P] [US1] 实现附近商户页、登录引导弹层和中文空状态于 `frontend/src/views/NearbyShopView.vue`、`frontend/src/components/LoginPromptModal.vue`、`frontend/src/constants/copy.ts`
- [X] T022 [US1] 在 `specs/001-o2o-site/quickstart.md` 记录商户列表/详情/附近商户的性能验证步骤与目标结果

**Checkpoint**: User Story 1 应可单独演示为 MVP

---

## Phase 4: User Story 2 - 抢购优惠并完成日常活跃行为 (Priority: P2)

**Goal**: 支持优惠券活动浏览、秒杀领券、每日签到与 UV 统计

**Independent Test**: 已登录用户可查看活动状态、参与秒杀且不会重复领券；每日只能签到一次；同一访客重复访问不会被重复计为多个 UV。

### Tests for User Story 2 ⚠️

- [X] T023 [P] [US2] 编写秒杀 Lua 返回码和签到位图逻辑单元测试于 `backend/src/test/java/com/hmdp/unit/VoucherSeckillServiceTest.java`、`backend/src/test/java/com/hmdp/unit/SignServiceTest.java`
- [ ] T024 [P] [US2] 编写活动列表、抢券防重、签到和 UV 统计集成测试于 `backend/src/test/java/com/hmdp/integration/VoucherAndSignFlowIntegrationTest.java`
- [X] T025 [P] [US2] 编写抢券与签到主路径 E2E 测试于 `frontend/tests/e2e/us2-voucher-and-sign.spec.ts`

### Implementation for User Story 2

- [X] T026 [P] [US2] 实现优惠券、秒杀券和券订单实体与 Mapper 于 `backend/src/main/java/com/hmdp/entity/Voucher.java`、`backend/src/main/java/com/hmdp/entity/VoucherOrder.java`、`backend/src/main/java/com/hmdp/mapper/VoucherMapper.java`、`backend/src/main/java/com/hmdp/mapper/VoucherOrderMapper.java`
- [X] T027 [P] [US2] 实现秒杀 Lua 脚本和抢券服务于 `backend/src/main/resources/lua/seckill.lua`、`backend/src/main/java/com/hmdp/service/impl/VoucherOrderServiceImpl.java`
- [X] T028 [US2] 实现活动列表、抢券、签到和 UV 统计接口于 `backend/src/main/java/com/hmdp/controller/VoucherController.java`、`backend/src/main/java/com/hmdp/controller/SignController.java`、`backend/src/main/java/com/hmdp/filter/UvRecordFilter.java`
- [X] T029 [P] [US2] 实现活动列表页、抢券卡片和活动状态中文展示于 `frontend/src/views/VoucherListView.vue`、`frontend/src/components/VoucherCard.vue`、`frontend/src/api/voucher.ts`
- [X] T030 [P] [US2] 实现签到面板、活动反馈状态和登录后操作提示于 `frontend/src/components/SignPanel.vue`、`frontend/src/stores/activity.ts`、`frontend/src/api/sign.ts`
- [X] T031 [US2] 在 `specs/001-o2o-site/quickstart.md` 记录秒杀延迟、重复领券拦截和签到校验的验收结果

**Checkpoint**: User Story 2 完成后，应能独立验证交易与活跃行为

---

## Phase 5: User Story 3 - 发布探店内容并建立社交互动 (Priority: P3)

**Goal**: 支持探店笔记发布、滚动分页、点赞排行、关注与共同关注

**Independent Test**: 已登录用户可发布笔记、滚动浏览内容流、点赞并查看排行榜、关注/取关用户并查看共同关注；匿名用户仍可浏览内容流和点赞排行。

### Tests for User Story 3 ⚠️

- [X] T032 [P] [US3] 编写滚动分页游标、点赞切换和共同关注集合运算单元测试于 `backend/src/test/java/com/hmdp/unit/BlogInteractionServiceTest.java`
- [ ] T033 [P] [US3] 编写笔记发布、内容流、热榜、关注和共同关注集成测试于 `backend/src/test/java/com/hmdp/integration/BlogAndFollowFlowIntegrationTest.java`
- [X] T034 [P] [US3] 编写笔记发布、点赞、热榜和关注主路径 E2E 测试于 `frontend/tests/e2e/us3-blog-and-social.spec.ts`

### Implementation for User Story 3

- [X] T035 [P] [US3] 实现笔记、点赞、关注实体与 Mapper 于 `backend/src/main/java/com/hmdp/entity/Blog.java`、`backend/src/main/java/com/hmdp/entity/BlogLike.java`、`backend/src/main/java/com/hmdp/entity/Follow.java`、`backend/src/main/java/com/hmdp/mapper/BlogMapper.java`、`backend/src/main/java/com/hmdp/mapper/FollowMapper.java`
- [X] T036 [P] [US3] 实现笔记发布、滚动分页和热榜服务于 `backend/src/main/java/com/hmdp/service/impl/BlogServiceImpl.java`
- [X] T037 [US3] 实现点赞切换、关注/取关和共同关注接口与服务于 `backend/src/main/java/com/hmdp/service/impl/FollowServiceImpl.java`、`backend/src/main/java/com/hmdp/controller/BlogController.java`、`backend/src/main/java/com/hmdp/controller/FollowController.java`
- [X] T038 [P] [US3] 实现内容流、笔记发布页和博客 API 调用于 `frontend/src/views/BlogFeedView.vue`、`frontend/src/views/BlogPublishView.vue`、`frontend/src/api/blog.ts`
- [X] T039 [P] [US3] 实现热榜列表、点赞按钮和关注按钮组件于 `frontend/src/components/HotBlogList.vue`、`frontend/src/components/LikeButton.vue`、`frontend/src/components/FollowButton.vue`
- [X] T040 [P] [US3] 实现共同关注页、关注 API 和中文空状态于 `frontend/src/views/CommonFollowView.vue`、`frontend/src/api/follow.ts`、`frontend/src/constants/copy.ts`
- [X] T041 [US3] 在 `specs/001-o2o-site/quickstart.md` 记录内容流稳定性、热榜正确性和社交交互验收结果

**Checkpoint**: User Story 3 完成后，内容与社交能力应可独立演示

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: 收尾、回归、跨故事一致性与最终验收

- [X] T042 [P] 为公开接口与鉴权接口补充契约回归测试于 `backend/src/test/java/com/hmdp/contract/PublicAndProtectedApiContractTest.java`
- [X] T043 [P] 为公开页文案、空状态和登录引导补充前端回归测试于 `frontend/tests/unit/public-copy.spec.ts`、`frontend/tests/e2e/public-empty-states.spec.ts`
- [X] T044 复查并精简重复的缓存、鉴权和社交交互逻辑于 `backend/src/main/java/com/hmdp/service/impl/`、`frontend/src/composables/`
- [X] T045 在 `specs/001-o2o-site/quickstart.md`、`specs/001-o2o-site/checklists/requirements.md` 记录端到端验证结果、性能证据与剩余风险
- [X] T046 统一复核中文术语、错误提示与默认文案于 `frontend/src/constants/copy.ts`、`specs/001-o2o-site/spec.md`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1: Setup** → 无依赖，可立即开始
- **Phase 2: Foundational** → 依赖 Phase 1，阻塞所有用户故事
- **Phase 3: US1** → 依赖 Phase 2，构成 MVP
- **Phase 4: US2** → 依赖 Phase 2，可在 US1 后顺序推进，也可在基础完成后并行推进
- **Phase 5: US3** → 依赖 Phase 2，可在 US1 后顺序推进，也可在基础完成后并行推进
- **Phase 6: Polish** → 依赖所有目标用户故事完成

### User Story Dependencies

- **US1 (P1)**: 无其它用户故事依赖，是 MVP
- **US2 (P2)**: 依赖 Foundational，但不依赖 US3；会复用 US1 的登录与公开页边界
- **US3 (P3)**: 依赖 Foundational，但不依赖 US2；会复用 US1 的登录与公开页边界

### Within Each User Story

- 测试任务必须先写并先失败
- 实体/Mapper 先于 Service
- Service 先于 Controller / API / 页面集成
- 中文文案、性能验证和独立验收必须在故事结束前完成

### Parallel Opportunities

- Phase 1 中标记 `[P]` 的任务可并行
- Phase 2 中 Redis/Lua、异常处理、前端客户端、共享 DTO 可并行
- Foundational 完成后，US2 和 US3 可由不同成员并行开发
- 同一用户故事中的后端测试与前端 E2E 测试可并行
- 不同页面组件任务和独立 API 模块任务可并行，只要不写同一文件

---

## Parallel Example: User Story 1

```bash
# 可并行启动的 US1 任务示例
T013 backend/src/test/java/com/hmdp/unit/AuthGuardTest.java
T014 backend/src/test/java/com/hmdp/integration/AuthAndShopFlowIntegrationTest.java
T015 frontend/tests/e2e/us1-anonymous-browse-and-login.spec.ts
T017 backend/src/main/java/com/hmdp/entity/User.java + Shop.java + ShopType.java + mapper/
T020 frontend/src/views/HomeView.vue + ShopListView.vue + ShopDetailView.vue + LoginView.vue
```

## Parallel Example: User Story 2

```bash
# 可并行启动的 US2 任务示例
T023 backend/src/test/java/com/hmdp/unit/VoucherSeckillServiceTest.java + SignServiceTest.java
T024 backend/src/test/java/com/hmdp/integration/VoucherAndSignFlowIntegrationTest.java
T025 frontend/tests/e2e/us2-voucher-and-sign.spec.ts
T026 backend/src/main/java/com/hmdp/entity/Voucher.java + VoucherOrder.java + mapper/
T029 frontend/src/views/VoucherListView.vue + VoucherCard.vue + api/voucher.ts
```

## Parallel Example: User Story 3

```bash
# 可并行启动的 US3 任务示例
T032 backend/src/test/java/com/hmdp/unit/BlogInteractionServiceTest.java
T033 backend/src/test/java/com/hmdp/integration/BlogAndFollowFlowIntegrationTest.java
T034 frontend/tests/e2e/us3-blog-and-social.spec.ts
T035 backend/src/main/java/com/hmdp/service/impl/BlogServiceImpl.java
T038 frontend/src/views/BlogFeedView.vue + BlogPublishView.vue + api/blog.ts
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. 完成 Phase 1: Setup
2. 完成 Phase 2: Foundational
3. 完成 Phase 3: US1
4. 停止并验证匿名浏览、短信登录、商户缓存和附近商户体验
5. 若通过，则可先演示/交付 MVP

### Incremental Delivery

1. Setup + Foundational 完成后，先交付 US1
2. 在不破坏 US1 的前提下交付 US2
3. 再交付 US3
4. 最后执行 Polish 和全链路回归

### Parallel Team Strategy

1. 一名成员负责 Phase 1/2 的基础设施
2. 基础完成后：
   - 成员 A：US1 登录与商户链路
   - 成员 B：US2 秒杀与签到链路
   - 成员 C：US3 内容与社交链路
3. 最后合流执行 Phase 6 回归与性能验收

---

## Notes

- `[P]` 任务表示在不同文件上、且不依赖未完成任务时可并行执行
- `[US1]`、`[US2]`、`[US3]` 任务与对应用户故事一一映射
- 每个用户故事都能独立完成、独立测试、独立演示
- 所有用户可见默认文案必须保持中文一致
- 严禁把跨故事的大任务混成一个不可验收的步骤
- `data-model.md` 中用户实体已包含 `password` 字段，任务实现时应保留该字段但不扩展出密码登录流程