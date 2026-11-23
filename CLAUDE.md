# WMS 项目开发约束

## 1. 探索与扫描限制
- **绝对禁止**：除非我明确要求，否则不允许使用 `find` 命令漫无目的地全盘扫描项目结构。
- **调用限制**：在分析需求或编写代码前，最多只允许查阅 3 到 5 个强相关的核心文件，严禁陷入"探索死循环"。

## 2. 技术栈上下文
- 后端：Java, Spring Boot, MyBatis (代码在 wms 目录下)
- 前端：Vue (代码在 wms-web/src 目录下)

## 3. 角色定义
- roleId = 0：店长（原系统管理员）
- roleId = 1：库存管理员
- roleId = 2：收银员

## 4. 功能模块与权限矩阵
| 模块 | 子功能 | 店长(0) | 库存管理员(1) | 收银员(2) | 备注 |
|------|--------|---------|---------------|-----------|------|
| 用户管理 | 用户账号管理 | ✅ | ❌ | ❌ | |
| 用户管理 | 管理员账号管理 | ✅ | ❌ | ❌ | |
| 用户管理 | 个人信息管理 | ✅ | ✅ | ✅ | |
| 商品管理 | 商品信息 | ✅ | ✅ | ❌ | |
| 商品管理 | 商品分类 | ✅ | ✅ | ❌ | |
| 采购管理 | 供应商信息维护 | ✅ | ✅ | ❌ | |
| 采购管理 | 采购入库 | ✅ | ✅ | ❌ | 创建即待入库，免审核 |
| 采购管理 | 采购退回审核 | ✅ | ❌ | ❌ | 仅店长 |
| 库存管理 | 库存盘点 | ✅ | ✅ | ❌ | |
| 库存管理 | 补货建议报表 | ✅ | ✅ | ❌ | |
| 销售管理 | 收银结算 | ✅ | ❌ | ✅ | |
| 销售管理 | 销售记录查询 | ✅ | ❌ | ✅ | |
| 销售管理 | 退换货处理 | ✅ | ❌ | ✅ | 店长负责审核 |

## 5. 变更记录

### 2026-11-15 采购免审核

#### 一、流程变更
- **采购单创建后无需审核**：创建后直接为"待入库"状态，可立即确认入库
- 移除采购审核环节："待审核/已拒绝"状态流转不再产生（status=0、4 为历史遗留）
- 采购退回审核流程不变（退回申请仍需店长审核）

#### 二、后端变更
- `PurchaseServiceImpl.createPurchase`：初始状态 0(待审核) → 1(待入库)
- **移除** `POST /purchase/audit`、`POST /purchase/reject` 接口（含 IPurchaseService.audit/reject、PurchaseServiceImpl.audit/reject）
- 状态 1 文案由"已审核待入库"统一改为"待入库"

#### 三、前端变更
- `PurchaseManage.vue`：移除"审核通过/审核拒绝"按钮及对应处理函数；状态 1 显示"待入库"；状态下拉移除"待审核"选项

#### 四、数据库
- **新增** `wms/src/main/resources/sql/purchase_skip_audit.sql` — 存量 status=0 采购单迁移为 status=1

### 2026-11-01 退货退款审批流

#### 一、流程变更
- **收银结算**的"退货退款"不再直接退款，改为**提交退款申请**：整单生成待退款退货单（类型=退货、状态=待退款），不退款、不动库存
- **退换货处理**中店长对**待退款**退货单可：**确认退款**（退款+回滚库存/损耗单）或**拒绝退款**（→已拒绝，不退款不回滚）
- 新状态**已拒绝**（状态码2）：终态，不退款；之后可重新发起申请
- 对应关系：一个销售单 → 多个退货单；一个退货单 → 一个销售单（移除"一单一退货"拦截）
- **换货**（type=2）：不申请、不退款、无状态，提交即执行商品互换；退换货处理中只读展示（仅查明细，无状态、无操作按钮）
- 数量防超退：剩余可退 = 销售数量 − 未拒绝退货单（状态0/1）已申请数量；收银台整单申请只提交剩余部分

#### 二、后端变更
- **新增** `POST /return/applyWholeOrder?orderNum=` — 收银台整单提交退款申请（自动填原因"收银台退货退款申请"）
- **新增** `POST /return/reject?returnId=` — 拒绝退款（状态0→2）
- `/return/confirm`、`/return/reject` 加 `@RequireRole({0})`（仅店长）；apply/listPage/getDetails 保持店长+收银员
- **移除** `POST /sales/refund` 收银台直接退款接口（含 ISalesService.refund / SalesServiceImpl.refund）
- `SalesReturnServiceImpl`：applyReturn 加数量防超退校验、type=2 即时执行（库存互换/损耗单/操作日志）；confirmRefund 仅对 type=1 且 status=0 生效，联动销售明细 status=1、全部退完则 sales.status=1；换货不改销售单状态
- `SalesMapper.xml` selectSalesPage：一对多后 LEFT JOIN 改聚合子查询防行重复；return_status 只统计 type=1（有待退款→1退货中，否则有已退款→2已退货）
- `SalesReturnMapper.xml`：listPage 状态筛选只匹配 type=1 记录

#### 三、前端变更
- `CashierDesk.vue`：按钮改"提交退款申请"，调 `/return/applyWholeOrder`
- `ReturnManage.vue`：状态列 type=2 显示"-"；操作列：查看明细全员可见，确认退款/拒绝退款仅 `roleId===0` 且 status=0 的退货单显示；新增拒绝退款操作

#### 四、设计文档
- `docs/superpowers/specs/2026-11-01-refund-approval-flow-design.md`


### 2026-10-17 角色命名与菜单权限重构

#### 一、数据库变更
- **新建** `wms/src/main/resources/sql/refactor_role_menu_structure.sql` — 菜单迁移脚本
  - 清空旧菜单，重建6大一级模块（用户管理/商品管理/采购管理/库存管理/销售管理/统计分析）
  - menuCode 列为 VARCHAR(7)，所有编码≤7字符
  - menuRight 按权限矩阵严格配置
- **更新** `wms/src/main/resources/sql/complete_test_data.sql` — 同步菜单数据 + 用户名"店长"
- **新增** `wms/src/main/resources/sql/remove_purchase_return_and_warning_menu.sql` — 移除采购退回和库存预警菜单项
- **新增** `wms/src/main/resources/sql/update_restock_permission.sql` — 更新补货建议报表权限为店长+库存管理员
- **新增** `wms/src/main/resources/sql/update_store_manager_permissions.sql` — 更新店长权限，店长可访问所有页面
- **新增** `wms/src/main/resources/sql/remove_inventory_transfer_menu.sql` — 移除库存调拨菜单项
- **新增** `wms/src/main/resources/sql/add_purchase_return_review_menu.sql` — 添加采购退回审核菜单

#### 二、后端变更
- **修复** `wms/src/main/java/com/wms/common/JwtInterceptor.java`
  - `writeErrorResponse()` 原本始终返回 HTTP 401，导致前端误判 token 过期
  - 修复为使用实际错误码（403 权限不足不再返回 401）
- **新增/更新** 8个 Controller 的 `@RequireRole` 注解：
  - GoodsController → `{0,1}`（店长+库存管理员）
  - GoodstypeController → `{0,1}`（店长+库存管理员）
  - SupplierController → `{0,1}`（店长+库存管理员）
  - SalesReturnController → `{0,2}`（店长+收银员）
  - InventoryCheckController → `{0,1}`（店长+库存管理员）
  - InventoryTransferController → `{0,1}`（店长+库存管理员）
  - PurchaseReturnController → 已移除（采购退回功能不保留）
  - StatsController suggestRestock → `{0,1}`（店长+库存管理员）

#### 三、前端变更
- **更新** `workspace/wms-web/src/router/index.js` — roleRouteMap 按新权限矩阵配置
- **更新** `workspace/wms-web/src/srtore/index.js` — 修复路由注册逻辑
  - 跳过 menuClick 为空的一级菜单，避免覆盖登录路由
  - 处理 menuClick 已含前导 `/` 的情况，避免双斜杠路径
- **更新** 6个组件的角色标签统一为"店长/库存管理员/收银员"：
  - Home.vue, Header.vue, AdminManage.vue, Main.vue, SelectUser.vue, UserManage.vue
- **更新** `workspace/wms-web/src/components/goods/GoodsManage.vue`
  - 商品操作权限从 `roleId!=2` 改为 `roleId===1`（仅库存管理员）
  - 移除所有仓库相关UI（搜索栏、表单、批次表格、变量、函数）

#### 四、权限矩阵关键变更
| 功能 | 旧权限 | 新权限 |
|------|--------|--------|
| 商品增删改查 | 仅库存管理员 | 店长+库存管理员 |
| 商品分类 | 仅库存管理员 | 店长+库存管理员 |
| 供应商管理 | 仅库存管理员 | 店长+库存管理员 |
| 库存盘点/调拨 | 仅库存管理员 | 店长+库存管理员 |
| 收银台 | 仅收银员 | 店长+收银员 |
| 补货建议 | 仅店长 | 店长+库存管理员 |
| 退换货 | 店长+收银员 | 店长+收银员（店长审核） |

#### 五、已知限制
- 仓库管理模块已从菜单中移除，但数据库 storage 表和后端 StorageController 保留
- 损耗管理(LossReport)未纳入新菜单结构，如需保留可手动添加
- 操作日志菜单已移除
- 采购入库时自动使用主仓库（storageId=1），无需选择仓库
- 库存调拨功能已从菜单中移除，后端代码保留

#### 六、菜单结构说明
- **店长登录后默认首页为销售统计**（/Dashboard）
- 其他角色登录后默认首页为首页（/Home）
- 销售统计不在菜单中显示，店长直接进入

#### 七、采购流程说明
1. **采购单创建**：库存管理员或店长新建采购单，创建后直接为"待入库"状态（免审核）
2. **确认入库**：店长和库存管理员都可以确认入库，状态变为"已入库"
3. **采购退回流程**：
   - 只能对"待入库"或"已入库"的采购单发起退回申请
   - 选择要退回的商品及数量，提交退回申请
   - 只有店长审核通过才能退回
   - 对于已入库的商品：在对应批次上减去退回数量
   - 对于未入库的商品：继续走入库流程
   - 店长拒绝退回申请：未入库的商品继续走入库流程，已入库的商品不处理