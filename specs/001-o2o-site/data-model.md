# Data Model - 本地生活 O2O 网站

## 1. 用户 User
- **Purpose**: 表示平台账户主体。
- **Core Fields**:
  - `id` bigint
  - `phone` varchar(11)，唯一
  - `password` varchar(128)
  - `nick_name` varchar(64)
  - `icon` varchar(255)
  - `create_time` datetime
  - `update_time` datetime
- **Validation**:
  - 手机号必须符合中国大陆手机号格式。
  - 用户昵称允许中文、英文、数字和常见符号，长度 1-64。
- **Relationships**:
  - 1:N `Blog`
  - 1:N `VoucherOrder`
  - 1:N `SignRecord`
  - N:N `UserFollow`（关注 / 被关注）

## 2. 短信验证码 LoginCode
- **Purpose**: 支撑手机号验证码登录。
- **Storage**: Redis 为主，不强制落 MySQL。
- **Redis Key**: `login:code:{phone}`
- **Fields**:
  - `code` string
  - `expireAt` timestamp
  - `attempts` int
- **Validation**:
  - TTL 到期自动失效。
  - 超过错误尝试阈值后拒绝继续验证。

## 3. 登录会话 UserSession
- **Purpose**: 保存登录态与轻量用户信息。
- **Storage**: Redis Hash
- **Redis Key**: `login:token:{token}`
- **Fields**:
  - `userId`
  - `nickName`
  - `icon`
  - `lastAccessTime`
- **Validation**:
  - 未携带合法 token 的请求不得执行需登录操作。
  - 未登录用户允许访问声明为只读公开的页面与查询接口。

## 4. 商户 Shop
- **Purpose**: 表示本地生活商户。
- **Core Fields**:
  - `id` bigint
  - `name` varchar(128)
  - `type_id` bigint
  - `address` varchar(255)
  - `x` decimal(10,6)
  - `y` decimal(10,6)
  - `images` text
  - `avg_price` decimal(10,2)
  - `comments` int
  - `score` decimal(3,2)
  - `open_hours` varchar(128)
  - `create_time` datetime
  - `update_time` datetime
- **Redis Structures**:
  - 缓存 Key：`cache:shop:{id}`
  - GEO Key：`shop:geo:{typeId}`
- **Validation**:
  - 经纬度不能为空，供附近商户检索使用。
  - 商户列表、详情和附近查询属于匿名可读资源。

## 5. 商户分类 ShopType
- **Purpose**: 表示美食、丽人、酒店等类别。
- **Fields**:
  - `id` bigint
  - `name` varchar(64)
  - `sort` int
  - `icon` varchar(255)

## 6. 优惠券活动 Voucher
- **Purpose**: 表示限时优惠资源及库存。
- **Core Fields**:
  - `id` bigint
  - `shop_id` bigint
  - `title` varchar(128)
  - `sub_title` varchar(255)
  - `pay_value` bigint
  - `actual_value` bigint
  - `stock` int
  - `begin_time` datetime
  - `end_time` datetime
  - `status` tinyint
  - `create_time` datetime
  - `update_time` datetime
- **Redis Keys**:
  - `seckill:stock:{voucherId}`
  - `seckill:order:{voucherId}`（已领取用户集合）
- **State Transitions**:
  - 未开始 → 进行中 → 已结束 / 已售罄
- **Validation**:
  - `begin_time < end_time`
  - `stock >= 0`
  - 活动列表可匿名浏览，但领取动作必须绑定登录用户。

## 7. 优惠券订单 VoucherOrder
- **Purpose**: 表示用户成功领取的优惠券记录。
- **Core Fields**:
  - `id` bigint
  - `user_id` bigint
  - `voucher_id` bigint
  - `status` tinyint
  - `create_time` datetime
  - `update_time` datetime
- **Constraints**:
  - 唯一索引：`uk_user_voucher(user_id, voucher_id)`
- **State Transitions**:
  - 已创建 → 已核销 / 已过期 / 已取消

## 8. 签到记录 SignRecord
- **Purpose**: 表示用户签到事实与展示信息。
- **Storage Strategy**:
  - Redis Bitmap：按月保存每日签到位
  - MySQL 可选：仅在需要审计或离线分析时落明细
- **Redis Key**: `sign:{userId}:{yyyyMM}`
- **Derived Fields**:
  - `signedToday`
  - `consecutiveDays`
- **Validation**:
  - 同一用户同一天只能签到一次。
  - 仅登录用户可执行签到。

## 9. 访问统计 VisitStat
- **Purpose**: 统计站点 UV。
- **Storage**: Redis HyperLogLog
- **Redis Key**: `uv:{yyyyMMdd}`
- **Fields**:
  - `date`
  - `uvCount`（聚合读出）
- **Validation**:
  - 同一访客在同一统计周期只计一次。
  - 匿名访问和登录访问都计入 UV。

## 10. 探店笔记 Blog
- **Purpose**: 表示用户发布的探店内容。
- **Core Fields**:
  - `id` bigint
  - `user_id` bigint
  - `shop_id` bigint
  - `title` varchar(128)
  - `content` text
  - `images` text
  - `liked` int
  - `status` tinyint
  - `create_time` datetime
  - `update_time` datetime
- **Validation**:
  - 标题不能为空，长度建议 1-128。
  - 正文不能为空。
  - 内容需支持中文、emoji 与英文品牌词正常展示。
  - 内容流与点赞排行可匿名浏览，但发布和互动动作必须登录。
- **State Transitions**:
  - 草稿（可选）→ 已发布 → 已下线

## 11. 点赞记录 BlogLike
- **Purpose**: 表示用户对笔记的点赞关系。
- **Core Fields**:
  - `id` bigint
  - `blog_id` bigint
  - `user_id` bigint
  - `create_time` datetime
- **Redis Structures**:
  - `blog:liked:{blogId}` → ZSet，member 为 `userId`，score 为时间戳
  - `user:liked:{userId}` → Set，可选
- **Validation**:
  - 同一用户对同一笔记同时最多存在一条有效点赞关系。
  - 点赞与取消点赞必须登录后执行。

## 12. 关注关系 UserFollow
- **Purpose**: 表示用户之间的关注/取关关系。
- **Core Fields**:
  - `id` bigint
  - `user_id` bigint
  - `follow_user_id` bigint
  - `create_time` datetime
- **Constraints**:
  - 唯一索引：`uk_user_follow(user_id, follow_user_id)`
- **Redis Structures**:
  - `follows:{userId}` → Set，成员为关注目标用户 ID
- **Validation**:
  - 不能关注自己。
  - 关注、取关和共同关注查询必须登录后执行。

## 13. 关键关系概览
- User 1:N Blog
- User 1:N VoucherOrder
- User N:N User（通过 UserFollow）
- Shop 1:N Voucher
- Shop 1:N Blog
- Voucher 1:N VoucherOrder
- Blog 1:N BlogLike

## 14. Redis Key 约定
- `login:code:{phone}`
- `login:token:{token}`
- `cache:shop:{shopId}`
- `shop:geo:{typeId}`
- `seckill:stock:{voucherId}`
- `seckill:order:{voucherId}`
- `sign:{userId}:{yyyyMM}`
- `uv:{yyyyMMdd}`
- `blog:liked:{blogId}`
- `follows:{userId}`

## 15. 数据一致性原则
- MySQL 是用户、商户、优惠券、订单、笔记、点赞、关注的最终业务真值。
- Redis 承担高频查询、实时状态、去重、排行和原子预检职责。
- 所有 Redis 派生状态在 MySQL 写入失败时必须有补偿或回滚策略，至少要保证不会出现重复领券。