# 收银员首页当日销售记录与明细查询 — 设计文档

日期：2026-11-01
状态：已确认

## 背景与目标

收银员（roleId = 2）登录后的首页目前是"欢迎语 + 个人信息卡"，不符合实际工作需求。本次修改将收银员首页**完全替换**为该收银员当日销售记录与明细查询页面。

**范围约束：只改收银员，其他一律不动。** 店长/库存管理员首页、销售记录查询页、退换货页等全部保持现状。

## 需求摘要

- 收银员首页展示**该收银员自己**的销售记录列表，支持按日期（默认今天）和流水号查询
- 每条记录支持"查看明细"弹窗，展示商品级明细
- 列表上方展示汇总统计：销售笔数 / 应收总额 / 实收总额
- 汇总口径与列表一致，**含已退款单**（保证笔数 = 列表总条数，表格内可见退款标记）

## 一、前端

### 1. 新组件 `workspace/wms-web/src/components/record/CashierTodaySales.vue`

结构（自上而下）：

1. **搜索区**
   - 日期选择器：单日选择，默认今天；清空则不传日期参数（查全部历史）
   - 流水单号输入框（模糊匹配，可空）
   - 查询 / 重置按钮
2. **汇总卡片**：销售笔数 / 应收总额 / 实收总额（随查询条件联动刷新）
3. **记录表格**，列：
   | 列 | 来源 |
   |---|---|
   | 流水单号 | `orderNum` |
   | 应收总额 | `totalAmount` |
   | 实收金额 | `realAmount` |
   | 找零 | `changeAmount` |
   | 支付方式 | `paymentMethod`（1=现金，否则扫码） |
   | 销售状态 | `status`（1=已退款，否则正常） |
   | 结账时间 | `createTime` |
   | 操作 | 【查看明细】按钮 |

   不含"申请退货"按钮（退货走"退换货处理"页）。
4. **分页**：10/20/50/100，与现有页面一致
5. **明细弹窗**：点击"查看明细"调用 `/sales/getDetails?salesId=...`，展示商品名称、单价、数量、小计

数据请求（复用现有接口）：

- 列表：`GET /sales/listPage`，参数 `startDate`（`YYYY-MM-DD 00:00:00`）/`endDate`（`YYYY-MM-DD 23:59:59`，与现有 SalesRecord.vue 格式一致）/`orderNum`/`userId=当前登录人 id`/分页参数
- 汇总：`GET /sales/summary`，筛选参数同上（不分页）
- 明细：`GET /sales/getDetails`

只做查询展示，无写操作。

### 2. 修改 `workspace/wms-web/src/components/Home.vue`

- `user.roleId === 2` 时，渲染 `<CashierTodaySales />`（完全替换欢迎语、个人信息卡、DateUtils）
- 其他角色渲染现有内容，不动

## 二、后端（最小改动）

### 1. 新增汇总接口 `GET /sales/summary`

链路：`SalesController` → `ISalesService` → `SalesServiceImpl` → `SalesMapper`（`SalesMapper.xml` 新增 `selectSalesSummary`）。

- 参数与 `listPage` 的筛选一致：`startDate`、`endDate`、`orderNum`、`userId`（均可空）
- 返回 `Result.data = { count, totalAmount, realAmount }`
  - `count`：`COUNT(*)`
  - `totalAmount`：`IFNULL(SUM(total_amount), 0)`
  - `realAmount`：`IFNULL(SUM(real_amount), 0)`
- SQL 的 WHERE 条件与 `selectSalesPage` 完全一致（`order_num` 模糊匹配含 `CONCAT('SO', LPAD(id, 8, '0'))` 兜底、`create_time` 范围、`user_id` 过滤），保证汇总与列表同口径
- 权限：类级 `@RequireRole({0, 2})` 覆盖，无需额外注解

### 2. 不改任何现有接口

`/sales/listPage`、`/sales/getDetails` 原样复用。收银员由前端传自己的 `userId`（与现有 SalesRecord.vue 做法一致）。本次不做后端越权加固。

## 三、边界与错误处理

- 无记录：表格空态展示
- 金额：保留 2 位小数
- 日期清空：不传日期参数，查全部历史，汇总同步
- 接口异常：`ElMessage` 提示，不阻塞页面

## 四、验证方式

1. 收银员登录 → 首页直接是当日销售记录页（无欢迎语/个人信息卡）；店长/库管首页不变
2. 默认查今天；改日期、输流水号查询、重置均正常；清空日期可查历史
3. 汇总笔数 = 表格总条数；应收/实收总额与所查记录合计一致（含已退款单）
4. "查看明细"弹窗展示该单商品明细
5. 列表只显示该收银员自己的单据

## 明确的取舍

| 决策 | 选择 | 理由 |
|---|---|---|
| 首页布局 | 完全替换（非追加区块） | 用户明确：欢迎页对收银员是不对的 |
| 汇总口径 | 含已退款单 | 与列表总条数一致，避免歧义；退款状态表格内可见 |
| 日期控件 | 单日选择器，默认今天 | 贴合"当日"主诉求；清空可回看历史 |
| 明细展示 | 弹窗 | 复用现有 `/sales/getDetails`，实现简单 |
| 后端加固 | 不做 | 用户限定范围：只管收银员、最小改动 |
