# Implementation Plan: 本地生活 O2O 网站

**Branch**: `001-o2o-site` | **Date**: 2026-04-27 | **Spec**: [specs/001-o2o-site/spec.md](./spec.md)
**Input**: Feature specification from `/specs/001-o2o-site/spec.md`

## Summary

构建一个前后端分离的本地生活 O2O 网站：后端采用 Spring Boot + MyBatis-Plus + MySQL + Redis/Redisson/Lua，前端采用 Vue 并由 Nginx 部署。首期按“匿名浏览与登录 → 秒杀与签到 → 内容与社交”三段最小增量推进，优先保证首页、商户列表/详情、附近商户、优惠券活动列表、探店内容流和点赞排行支持匿名只读访问，同时在抢券、签到、发布笔记、点赞、关注和共同关注查询等交互动作上统一登录拦截，并为高频查询与高并发抢券提前纳入缓存、GEO、Bitmap、HyperLogLog、Set/ZSet 与 Lua 原子校验设计。

## Technical Context

**Language/Version**: Java 17（后端），JavaScript ES2022（前端）  
**Primary Dependencies**: Spring Boot 3.x，MyBatis-Plus，MySQL 8，Redis 7，Redisson，Lua，Maven 3.9+，Lombok，Vue 3，Vite，Vue Router，Pinia，Nginx  
**Storage**: MySQL 8（业务主数据），Redis 7（缓存、GEO、Bitmap、HyperLogLog、Set、ZSet、Lua 原子校验）  
**Testing**: JUnit 5，Spring Boot Test，MockMvc，Testcontainers（MySQL/Redis），Vitest，Vue Test Utils，Playwright  
**Target Platform**: Linux 服务器，Nginx 静态托管 + 反向代理，现代桌面/移动浏览器  
**Project Type**: 前后端分离 Web 应用  
**Performance Goals**: 商户列表/详情/附近商户/内容流首屏 p95 < 2s；缓存命中的商户详情接口 p95 < 300ms；秒杀请求结果返回 p95 < 3s；签到/点赞/关注/共同关注接口 p95 < 500ms；滚动分页重复项与明显错序率 < 1%  
**Constraints**: 默认中文界面；首页、商户列表/详情、附近商户、优惠券活动列表、探店内容流和点赞排行允许匿名只读访问；未登录禁止参与抢券、签到、发布笔记、点赞、关注和共同关注查询等需鉴权操作；同一用户同一优惠券只能领取一次；秒杀必须防超卖与重复领取；附近商户依赖经纬度数据；优先使用现有技术栈，不在首期引入额外消息中间件；所有关键行为必须可自动化验证  
**Scale/Scope**: 首期覆盖 8 大业务模块，目标支撑 10 万注册用户、1 万商户、单场秒杀峰值约 1000 req/s、日常内容流与互动并发访问

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- **代码质量优先**: PASS。后端使用统一分层（controller/service/repository/mapper）、Spotless/Checkstyle 风格约束、MyBatis-Plus 负责通用 CRUD、复杂查询仅保留在少量 mapper XML；前端使用 ESLint + Prettier，避免重复请求与重复状态管理；关键业务流程补充必要中文注释与便于排障的中文日志。
- **测试先行与防回归**: PASS。每个核心流程先定义失败场景；后端为匿名浏览、登录、秒杀、防重、签到、共同关注、滚动分页提供单元/集成测试；前端为匿名浏览、登录、附近商户、抢券、笔记发布与点赞提供组件/E2E 验证。
- **性能预算不可后补**: PASS。已为商户查询、缓存命中、秒杀、互动接口、滚动分页定义 p95 预算，并要求在实现阶段补充压测与复验记录。
- **中文一致性**: PASS。默认页面文案、错误提示、空状态、帮助信息、表单提示全部使用中文，接口错误消息也统一中文化，新增注释与关键日志默认使用中文。
- **可验证的最小交付**: PASS。按三段增量拆分：认证与商户、运营与活跃、内容与社交；每段均有独立输入输出、自动化验证与人工验收主路径。

## Project Structure

### Documentation (this feature)

```text
specs/001-o2o-site/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── api.yaml
└── tasks.md
```

### Source Code (repository root)

```text
backend/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/com/hmdp/
│   │   │   ├── config/
│   │   │   ├── controller/
│   │   │   ├── service/
│   │   │   ├── service/impl/
│   │   │   ├── mapper/
│   │   │   ├── entity/
│   │   │   ├── dto/
│   │   │   └── utils/
│   │   └── resources/
│   │       ├── mapper/
│   │       ├── lua/
│   │       └── application.yml
│   └── test/
│       └── java/com/hmdp/
│           ├── unit/
│           ├── integration/
│           └── contract/

frontend/
├── package.json
├── src/
│   ├── api/
│   ├── components/
│   ├── views/
│   ├── router/
│   ├── stores/
│   ├── composables/
│   └── assets/
└── tests/
    ├── unit/
    └── e2e/

deploy/
└── nginx/
    └── default.conf
```

**Structure Decision**: 采用前后端分离结构。`backend/` 承载 Spring Boot 单体业务服务与 Redis/Lua 集成，`frontend/` 承载 Vue 网站，`deploy/nginx/` 保存 Nginx 部署配置。该结构最符合用户明确给出的 Java + Vue + Nginx 技术栈，同时允许围绕缓存、秒杀和社交关系分别做后端集成测试与前端体验验证。

## Phase 0: Research Output Summary

- 运行时基线确定为 Java 17 + Spring Boot 3.x + MySQL 8 + Redis 7 + Vue 3 + Nginx。
- 商户详情采用缓存旁路模式，热点 Key 使用逻辑过期或互斥重建，穿透场景使用空值缓存。
- 附近商户采用 Redis GEO 做按距离检索，商户主数据仍以 MySQL 为准。
- 秒杀采用 Redis Lua 脚本完成库存校验与一人一券原子预检，落库阶段用事务 + 唯一索引兜底，必要时配合 Redisson 细粒度锁控制并发写入。
- 签到采用 Redis Bitmap；UV 统计采用 HyperLogLog；共同关注采用 Redis Set；点赞排行采用 Redis ZSet；业务真值仍落 MySQL。
- 内容流采用基于游标的滚动分页（`lastId + offset`），保证稳定顺序、避免重复返回与漏项。

## Phase 1: Design Artifacts

- `research.md`: 记录技术选型、缓存、秒杀、签到、UV、内容流与测试策略决策。
- `data-model.md`: 细化用户、验证码、商户、优惠券活动、券订单、签到、UV、笔记、点赞、关注等实体与 Redis 结构。
- `contracts/api.yaml`: 定义前后端 HTTP 接口契约。
- `quickstart.md`: 定义本地运行、测试、验收与 Nginx 部署步骤。

## Post-Design Constitution Check

- **代码质量优先**: PASS。已明确后端/前端目录边界、静态检查、复杂查询落点，以及需补充中文注释与关键日志的业务区域。
- **测试先行与防回归**: PASS。已将失败场景、集成测试、契约测试与前端 E2E 纳入交付范围。
- **性能预算不可后补**: PASS。缓存、GEO、Lua、Bitmap、HyperLogLog、ZSet/Set 的使用都与性能目标直接对应。
- **中文一致性**: PASS。契约、快速验收、必要注释与关键日志均要求以中文为默认表达。
- **可验证的最小交付**: PASS。三个增量阶段均可独立演示，无需等待全部模块完成。

## Complexity Tracking

当前无需要申请豁免的宪章例外。