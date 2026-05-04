# 设计说明：voucher-update

## 概览
本次改造围绕“秒杀异步化”一条主线展开：

1. 秒杀主链路由“Lua 预校验 + 同步落库”调整为“Lua 预校验 + RabbitMQ 消息投递 + 后台异步落库”。
2. 订单主键由数据库自增切换为业务侧雪花算法生成，支持抢券成功后立即返回订单 ID。

---

## 当前状态
### 已有能力
- `VoucherController.java` 提供秒杀活动列表查询与抢券入口。
- `VoucherOrderServiceImpl.java` 通过 Lua 脚本在 Redis 中进行库存预校验和一人一券判定。
- `VoucherStockRedisWarmUp.java` 启动时会把未结束优惠券库存预热到 Redis。
- `tb_voucher_order` 已存在 `(user_id, voucher_id)` 唯一索引，可作为数据库最终兜底。

### 现存问题
- 抢券接口仍在主请求线程同步落库，数据库写入延迟直接影响响应时长。
- 订单 ID 依赖数据库插入结果，无法在异步方案中提前返回给前端。
- Lua 脚本无法直接投递 RabbitMQ，Redis 与 MQ 之间存在非原子窗口。

---

## 目标架构

```text
前端抢券请求
   ↓
VoucherController.claimSeckillVoucher()
   ↓
VoucherOrderService.claimSeckillVoucher()
   ↓
生成雪花算法 orderId
   ↓
执行 Lua 脚本
   ├─ 校验库存 > 0
   ├─ 校验用户未下单
   ├─ 扣减 Redis 库存
   └─ 记录用户下单资格
   ↓
发送 RabbitMQ 下单消息
   ↓
返回 orderId

RabbitMQ 消费者线程
   ↓
读取 orderId / userId / voucherId
   ↓
加 Redisson 用户锁
   ↓
数据库原子扣库存
   ↓
创建订单记录
   ↓
ACK
```

---

## 详细设计

## 1. 订单 ID 生成
### 方案
新增雪花算法 ID 生成器，例如  `SnowflakeIdWorker`，在抢券入口生成 Long 型订单号。

### 设计要点
- 输出为 `Long`，直接写入 `VoucherOrder.id`。
- 采用时间戳 + 机器位 + 序列号组合。
- 需要明确起始时间戳与机器标识来源。
- 如果项目部署为单实例，可先使用固定 workerId；如未来扩容，再从配置或环境变量读取。

### 原因
RabbitMQ 异步落库下，前端必须在消息消费前就拿到订单号，自增主键不再适合作为唯一来源。

---

## 2. Lua 脚本职责调整
### 保留职责
Lua 仍负责 Redis 原子校验：
- 判断库存是否充足。
- 判断用户是否已领券。
- 扣减库存。
- 记录用户已下单资格。

### 不承担职责
Lua 不再负责入队，因为 RabbitMQ 无法在 Lua 中直接操作。

### 输入建议
- `voucherId`
- `userId`

### 返回码建议
- `0`：成功
- `1`：库存不足
- `2`：重复下单
- `3`：活动未开始或已结束
- `4`：库存 key 缺失或活动未预热

---

## 3. RabbitMQ 设计
### 交换机与队列
建议使用以下命名：

- Exchange：`seckill.order.exchange`
- Queue：`seckill.order.queue`
- Routing Key：`seckill.order`
- Dead Letter Exchange：`seckill.order.dlx`
- Dead Letter Queue：`seckill.order.dlq`

### 消息结构
使用最小消息体：

```json
{
  "orderId": 123456789012345678,
  "userId": 1001,
  "voucherId": 10
}
```

### 生产者行为
Lua 成功后，由 Java 代码发送 RabbitMQ 消息。

### 发送可靠性
第一版至少启用：
- publisher confirm
- return callback

### 失败处理策略
第一版采取保守策略：
- 如果 MQ 发送失败，记录中文错误日志，返回“系统繁忙，请稍后重试”。
- 后续如需增强，可再补消息补偿或人工修复策略。

---

## 4. 消费者设计
### 消费方式
- 使用 Spring AMQP 监听队列。
- 使用手动 ACK 模式。

### 消费步骤
1. 反序列化消息，得到 `orderId/userId/voucherId`。
2. 获取 `lock:order:{userId}` 的 Redisson 分布式锁。
3. 执行数据库库存扣减。
4. 创建订单记录。
5. 成功后 ACK。
6. 可重试异常按配置重试，持续失败进入死信队列。

### 为什么保留 Redisson 锁
虽然数据库唯一索引和 Lua 校验已提供兜底，但保留用户粒度锁可以减少同一用户重复消息并发落库时的冲突噪音，提升日志可读性和消费稳定性。

---

## 5. 数据库层设计
### 订单表
现有 `tb_voucher_order` 已有 `(user_id, voucher_id)` 唯一索引，可保留。

### 订单 ID
- 改为业务生成并显式插入。
- 实体类中的 `id` 不再依赖数据库自增结果。

### 库存扣减 SQL
需要新增原子扣库存语句：

```sql
update tb_voucher
set stock = stock - 1
where id = #{voucherId}
  and stock > 0
```

### 原因
即使 Redis 已做预扣，数据库仍需在最终落库时再次防超卖，避免消费重试或异常链路下出现不一致。

---

## 6. 前端影响
### 保持不变
- `POST /vouchers/seckill/{voucherId}/claim` 仍返回 `number` 类型订单号。
- 活动列表查询接口保持不变。

### 交互语义
前端可继续展示“抢券成功”，但从系统语义上更准确的定义是“抢券资格确认成功且已进入异步落库流程”。第一版不强制增加订单状态查询接口。

---

## 7. 风险与缓解
### 风险 1：Redis 预扣成功但 RabbitMQ 发送失败
- 缓解：启用 confirm/return；记录详细日志；第一版先不自动回滚 Redis。

### 风险 2：消息重复消费
- 缓解：数据库唯一索引 + 用户级 Redisson 锁 + 幂等插入策略。

### 风险 3：数据库库存与 Redis 库存短时不一致
- 缓解：数据库原子扣减作为最终真值；必要时可在后续补充库存修复任务。

---

## 8. 决策汇总
| 主题 | 选型 |
|---|---|
| 消息队列 | RabbitMQ |
| 订单 ID | 雪花算法 Long |
| 抢券接口返回 | 直接返回 orderId |
| Lua 是否入队 | 否，由 Java 发送 MQ |
| 消费确认 | 手动 ACK |
| 失败消息处理 | 死信队列 |
| 用户防重 | Redis Set + 数据库唯一索引 |
| 消费端锁 | 保留 Redisson 用户锁 |
| 活动接口范围 | 仅保留现有活动查询与抢券入口 |
| 订单状态查询 | 第一版不新增 |
| Redis 失败补偿 | 第一版不自动回滚 |
| 关键目标 | 一人一券、库存不超卖、快速返回订单号 |
