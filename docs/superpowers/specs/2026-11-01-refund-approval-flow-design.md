# 退货退款审批流设计（收银台申请 → 店长审核）

日期：2026-11-01
状态：已确认（方案一，用户要求快速推进）

## 需求

1. 收银结算的"退货退款"不再直接退款，改为**提交退款申请**（整单，类型=退货、状态=待退款）。
2. 退换货处理中，店长对待退款退货单可**确认退款**（退款+回滚库存）或**拒绝退款**（→已拒绝）；操作仅店长可见/可用。
3. 新状态**已拒绝**（复用状态码 2，前端已有未使用的映射）。
4. 对应关系：一个销售单 → 多个退货单（可重复申请、被拒后可重新申请）；一个退货单 → 一个销售单。
5. **换货**（type=2）：不申请、不退款、无状态，提交即执行商品互换；退换货处理中只读展示（仅查明细）。

## 状态机

### 退货（type=1）
```
提交申请（收银台整单 / 销售记录选商品）
   → 待退款(0)
       ├─ 店长确认退款 → 已退款(1)：退款＋回滚库存（不可二次销售则生成损耗单）＋操作日志
       └─ 店长拒绝退款 → 已拒绝(2)：不退款、不动库存（终态）
```
已拒绝/已退款后可再次发起申请，生成新退货单。仅待退款(0)可被确认/拒绝。

### 换货（type=2，即时执行）
- 提交即执行：退回商品入库（可二次销售→恢复批次库存；不可→损耗单）＋换出商品 FIFO 出库＋操作日志。
- 落一条 sales_return 记录仅供查询（status 存 1=已完成；UI 不显示状态、状态筛选不包含它）。
- 销售明细 detail.status=2（换货）。无审核、无退款、无状态流转。

## 核心规则

- **数量防超退**：剩余可退数量 = 销售数量 − 该销售单所有未拒绝退货单（status ∈ 0/1）已申请数量（已拒绝不计）。申请数量 ≤ 剩余；收银台整单申请只提交剩余部分，全部退完提示"无可退商品"。
- **确认退款联动**：库存回滚/损耗单 → 明细 detail.status=1（退货）→ 若该销售单全部明细均已退货/换货则 sales.status=1（已退款）→ 记 refundTime。换货路径不改销售单状态（换货无退款）。
- **状态校验**：确认/拒绝仅对 status=0 生效；退货单仅 type=1 可确认/拒绝。
- **权限**：/return/confirm、/return/reject 仅店长（@RequireRole({0})）；apply/listPage/getDetails 保持店长+收银员。

## 后端变更

| 文件 | 变更 |
|------|------|
| SalesReturnServiceImpl | applyReturn：删除"一单一退货"拦截、加数量防超退、type=2 即时执行；confirmRefund：仅 type=1 且 status=0、补销售单/明细联动；新增 rejectRefund；库存操作抽取公共方法 |
| ISalesReturnService | 新增 rejectRefund(Integer returnId) |
| SalesReturnController | 新增 POST /return/reject；confirm/reject 加 @RequireRole({0}) |
| SalesController + ISalesService + SalesServiceImpl | 移除 /sales/refund 直接退款接口（被申请流取代） |
| SalesMapper.xml selectSalesPage | LEFT JOIN 改聚合子查询（防一对多行重复）；return_status 只统计 type=1：有 status=0→1(退货中)，否则有 status=1→2(已退货)，否则 0 |
| SalesReturnMapper.xml | listPage 状态筛选只匹配 type=1 记录 |

新增接口：`POST /return/applyWholeOrder?orderNum=xxx` —— 收银台整单申请：按单号查销售单，算剩余可退明细，创建 type=1 待退款退货单（退货原因自动填"收银台退货退款申请"）。

## 前端变更

| 文件 | 变更 |
|------|------|
| CashierDesk.vue | 按钮"确认退款"→"提交退款申请"；确认弹窗改为申请语义；调用 /return/applyWholeOrder |
| ReturnManage.vue | 状态列 type=2 显示"-"；操作列：查看明细全员，确认退款/拒绝退款仅 `roleId===0 && status===0 && type!==2`；新增拒绝退款 handler（POST /return/reject） |

## 边界处理

1. 重复申请允许，数量校验防超退（已拒绝不占用额度）。
2. 确认/拒绝对非 status=0 报错提示。
3. 换货目标商品缺货由 FIFO 扣减抛错，事务整体回滚。
4. 一对多后销售列表查询行重复 → 聚合子查询修复。

## 验收路径

1. 收银台提交申请 → 退换货处理出现待退款退货单；库存/销售单不变。
2. 店长确认退款 → 已退款、库存回滚（或损耗单）、明细更新；收银员看不到操作按钮。
3. 店长拒绝退款 → 已拒绝、无库存动作；可再次申请。
4. 换货提交即完成库存互换，退换货处理只读展示（无状态、无按钮）。
5. 多次申请数量之和不超销售数量。
6. 收银员调 /return/confirm、/return/reject 返回 403。
