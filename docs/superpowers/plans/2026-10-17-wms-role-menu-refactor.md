# WMS 角色命名与菜单权限重构 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将WMS系统重构为3角色（店长/库存管理员/收银员）+ 6大功能模块（用户管理/商品管理/采购管理/库存管理/销售管理/统计分析），统一角色命名、菜单结构和前后端权限控制。

**Architecture:** 采用前端菜单动态加载（数据库menu表驱动）+ 后端@RequireRole注解鉴权 + 前端路由守卫三层权限控制。SQL脚本重建菜单树，前端组件更新角色判断逻辑，后端Controller同步调整权限注解。

**Tech Stack:** Java Spring Boot, MyBatis-Plus, Vue 3, Element Plus, Vue Router, Vuex

---

## Task 1: 创建数据库菜单迁移脚本

**Files:**
- Create: `wms/src/main/resources/sql/refactor_role_menu_structure.sql`

**说明:** 当前菜单结构（7个一级模块）需要重构为新的6大模块。menu表通过menuRight字段控制角色权限（逗号分隔的roleId字符串）。新脚本需：清空旧菜单 → 插入新的6大模块及子菜单 → 隐藏操作日志和仓库管理。

- [ ] **Step 1: 创建SQL迁移脚本**

```sql
-- ============================================
-- WMS 角色命名与菜单结构重构脚本
-- 执行时间: 2026-10-17
-- 目标: 3角色(店长0/库存管理员1/收银员2) + 6大模块
-- ============================================

-- 1. 清空现有菜单数据
DELETE FROM menu;

-- 2. 插入新的一级菜单（6大模块）
INSERT INTO menu (id, menuCode, menuName, menuLevel, menuParentCode, menuClick, menuRight, menuComponent, menuIcon) VALUES
-- 一级菜单
(1,  'usermgmt',   '用户管理', '1', '0', '', '0,1,2', 'Index', 'User'),
(2,  'goods',      '商品管理', '1', '0', '', '1',     'Index', 'Shopping'),
(3,  'purchase',   '采购管理', '1', '0', '', '0,1',   'Index', 'ShoppingCart'),
(4,  'inventory',  '库存管理', '1', '0', '', '0,1',   'Index', 'Box'),
(5,  'sales',      '销售管理', '1', '0', '', '0,1,2', 'Index', 'Money'),
(6,  'stats',      '统计分析', '1', '0', '', '0',     'Index', 'DataAnalysis'),

-- 二级菜单：用户管理
(10, 'user',       '用户账号管理',   '2', 'usermgmt', '/User',        '0',     'user/UserManage',       'User'),
(11, 'admin',      '管理员账号管理', '2', 'usermgmt', '/Admin',       '0',     'admin/AdminManage',     'UserFilled'),

-- 二级菜单：商品管理（仅库存管理员）
(20, 'goodslist',  '商品信息',       '2', 'goods',    '/Goods',       '1',     'goods/GoodsManage',     'Goods'),
(21, 'category',   '商品分类',       '2', 'goods',    '/Goodstype',   '1',     'goodstype/GoodstypeManage', 'Grid'),

-- 二级菜单：采购管理
(30, 'supplier',   '供应商信息维护', '2', 'purchase', '/Supplier',    '1',     'supplier/SupplierManage', 'Van'),
(31, 'purchaselist','采购入库',       '2', 'purchase', '/PurchaseManage','0,1',  'purchase/PurchaseManage', 'ShoppingCart'),
(32, 'purchasereturn','采购退回',     '2', 'purchase', '/ReturnManage','0,1',   'record/ReturnManage',   'RefreshLeft'),

-- 二级菜单：库存管理
(40, 'check',      '库存盘点',       '2', 'inventory','/Check',       '1',     'inventory/Check',       'DocumentChecked'),
(41, 'transfer',   '库存调拨',       '2', 'inventory','/Transfer',    '1',     'inventory/Transfer',    'Sort'),
(42, 'warning',    '库存预警',       '2', 'inventory','/WarningList', '0,1',   'stats/RestockSuggest',  'WarningFilled'),
(43, 'restock',    '补货建议报表',   '2', 'inventory','/RestockSuggest','0',    'stats/RestockSuggest',  'Present'),

-- 二级菜单：销售管理
(50, 'cashierdesk','收银结算',       '2', 'sales',    '/CashierDesk', '2',     'cashinerdesk/CashierDesk','Money'),
(51, 'salesrecord','销售记录查询',   '2', 'sales',    '/SalesRecord', '0,2',   'record/SalesRecord',    'Sell'),
(52, 'return',     '退换货处理',     '2', 'sales',    '/ReturnManage','0,2',   'record/ReturnManage',   'RefreshLeft'),

-- 二级菜单：统计分析（仅店长）
(60, 'dashboard',  '销售统计',       '2', 'stats',    '/Dashboard',   '0',     'dashboard/Dashboard',   'Monitor');
```

- [ ] **Step 2: 更新complete_test_data.sql中的菜单数据**

修改 `wms/src/main/resources/sql/complete_test_data.sql` 第97-134行的菜单INSERT语句，使其与新结构一致。同时更新用户数据中的角色名称：

```sql
-- 用户表中 roleId=0 的名称改为"店长"
UPDATE user SET name = '店长' WHERE id = 1;
```

---

## Task 2: 更新前端路由权限配置

**Files:**
- Modify: `workspace/wms-web/src/router/index.js:166-181`

- [ ] **Step 1: 更新roleRouteMap**

将第166-181行的角色-路由映射更新为新的权限矩阵：

```javascript
// ==================== 角色-路由映射 ====================
// 0=店长 1=库存管理员 2=收银员
const roleRouteMap = {
  // 用户管理 - 店长管理账号，所有角色可访问个人中心(Home)
  '/Admin':          [0],           // 管理员账号管理 - 仅店长
  '/User':           [0],           // 用户账号管理 - 仅店长

  // 商品管理 - 仅库存管理员
  '/Goods':          [1],           // 商品信息
  '/Goodstype':      [1],           // 商品分类

  // 采购管理
  '/Supplier':       [1],           // 供应商信息维护 - 仅库存管理员
  '/PurchaseManage': [0, 1],        // 采购入库 - 店长+库存管理员
  '/ReturnManage':   [0, 2],        // 退换货(含采购退货) - 店长+收银员

  // 库存管理
  '/Check':          [1],           // 库存盘点 - 仅库存管理员
  '/Transfer':       [1],           // 库存调拨 - 仅库存管理员
  '/WarningList':    [0, 1],        // 库存预警 - 店长+库存管理员
  '/RestockSuggest': [0],           // 补货建议报表 - 仅店长

  // 销售管理
  '/CashierDesk':    [2],           // 收银结算 - 仅收银员
  '/SalesRecord':    [0, 2],        // 销售记录 - 店长+收银员

  // 统计分析 - 仅店长
  '/Dashboard':      [0],           // 销售统计
}
```

---

## Task 3: 更新前端角色名称显示

**Files:**
- Modify: `workspace/wms-web/src/components/Home.vue:39-41`
- Modify: `workspace/wms-web/src/components/Header.vue:199-204`
- Modify: `workspace/wms-web/src/components/admin/AdminManage.vue:335-336`
- Modify: `workspace/wms-web/src/components/Main.vue:311-312`
- Modify: `workspace/wms-web/src/components/user/SelectUser.vue:341-342`

- [ ] **Step 1: 更新Home.vue角色标签**

将第39行的"系统管理员"改为"店长"：

```html
<!-- 第39行: 系统管理员 → 店长 -->
<el-tag v-if="user.roleId === 0" type="danger" size="small">店长</el-tag>
<!-- 第40行保持不变: 库存管理员 -->
<!-- 第41行保持不变: 收银员 -->
```

- [ ] **Step 2: 更新Header.vue角色标签**

将第203行的角色名称统一：

```html
{{ user.roleId === 0 ? '店长' : (user.roleId === 1 ? '库存管理员' : '收银员') }}
```

- [ ] **Step 3: 更新AdminManage.vue角色标签**

将第335-336行的标签文字统一（当前显示"系统管理员/管理员/用户"）：

```html
:type="scope.row.roleId=== 0 ? 'danger' : (scope.row.roleId=== 1 ? 'primary' : 'success')"
disable-transitions>{{scope.row.roleId=== 0 ? '店长' : (scope.row.roleId=== 1 ? '库存管理员' : '收银员')}}</el-tag>
```

- [ ] **Step 4: 更新Main.vue角色标签**

将第311-312行的标签文字统一（当前显示"系统管理员/库存管理员/收银员"，需要把第一个改为"店长"）：

```html
:type="scope.row.roleId=== 0 ? 'danger' : (scope.row.roleId=== 1 ? 'primary' : 'success')"
disable-transitions>{{scope.row.roleId=== 0 ? '店长' : (scope.row.roleId=== 1 ? '库存管理员' : '收银员')}}</el-tag>
```

- [ ] **Step 5: 更新SelectUser.vue角色标签**

将第341-342行的标签文字统一（当前显示"系统管理员/库存管理员/收银员"）：

```html
:type="scope.row.roleId=== 0 ? 'danger' : (scope.row.roleId=== 1 ? 'primary' : 'success')"
disable-transitions>{{scope.row.roleId=== 0 ? '店长' : (scope.row.roleId=== 1 ? '库存管理员' : '收银员')}}</el-tag>
```

---

## Task 4: 更新GoodsManage.vue商品管理权限

**Files:**
- Modify: `workspace/wms-web/src/components/goods/GoodsManage.vue:446,457,485,545`

**说明:** 当前商品管理允许 roleId!=2（即店长和库存管理员）操作。新需求要求商品管理**仅库存管理员(1)**可操作。需要将 `v-if="user.roleId!=2"` 改为 `v-if="user.roleId===1"`。

- [ ] **Step 1: 更新新增按钮权限**

第446行：
```html
<el-button type="primary" style="margin-left: 10px;" @click="add" v-if="user.roleId===1">新增</el-button>
```

- [ ] **Step 2: 更新新增批次按钮权限**

第457行：
```html
<el-button v-if="user.roleId===1" type="primary" size="small" style="margin-left: 12px;" @click="openBatchAdd(row)">
```

- [ ] **Step 3: 更新展开行操作列权限**

第485行：
```html
<el-table-column v-if="user.roleId===1" label="操作" width="140" align="center">
```

- [ ] **Step 4: 更新表格操作列权限**

第545行：
```html
<el-table-column label="操作" width="130" align="center" v-if="user.roleId===1">
```

---

## Task 5: 更新ReturnManage.vue退换货权限

**Files:**
- Modify: `workspace/wms-web/src/components/record/ReturnManage.vue:6,282`

**说明:** 当前采购退货tab对roleId!==2可见（即店长+库存管理员），新需求要求退换货处理归**店长(0)+收银员(2)**，采购退货归**店长(0)+库存管理员(1)**。需要拆分权限判断逻辑。

- [ ] **Step 1: 更新采购退货Tab可见性**

第6行 — 采购退货tab仅对店长和库存管理员可见（保持不变，因为收银员不应看到采购退货）：

```html
<el-tab-pane v-if="user.roleId !== 2" label="采购退货" name="purchase" />
```

实际上这个逻辑是正确的：roleId !== 2 表示店长(0)和库存管理员(1)可以看到采购退货tab。收银员(2)看不到。

- [ ] **Step 2: 更新销售退货数据过滤**

第282行 — 销售退货列表：收银员只看自己的，店长看全部。库存管理员不应看到销售退货（已在菜单层面隐藏）：

```javascript
// 收银员只看自己的退换货记录
if (user.roleId === 2) {
  params.userId = user.id
}
```

这个逻辑也已经是正确的，不需要修改。因为菜单层面已经通过menuRight控制了：退换货处理的menuRight为"0,2"（店长+收银员），库存管理员根本看不到这个菜单。

**总结：ReturnManage.vue无需修改代码逻辑，权限控制通过菜单menuRight实现。**

---

## Task 6: 更新LossReport.vue损耗管理权限

**Files:**
- Modify: `workspace/wms-web/src/components/inventory/LossReport.vue:11,65,112`

**说明:** 当前损耗管理对所有角色可见（后端@RequireRole({0,1,2})），且收银员(2)只能上报销售损耗、不能审核库存损耗。新需求中损耗管理未被明确列为6大模块之一，但保留现有逻辑。如果需要移除此菜单入口，在SQL脚本中不插入即可。

**决策：损耗管理保留在库存管理模块下，但不在新菜单结构中显式列出。如需保留，可在Task 1的SQL中添加。此处暂不修改LossReport.vue代码。**

---

## Task 7: 更新后端Controller权限注解

**Files:**
- Modify: `wms/src/main/java/com/wms/controller/GoodsController.java:35-36`
- Modify: `wms/src/main/java/com/wms/controller/GoodstypeController.java:28-29`
- Modify: `wms/src/main/java/com/wms/controller/SupplierController.java:29-30`
- Modify: `wms/src/main/java/com/wms/controller/SalesReturnController.java:19-21`
- Modify: `wms/src/main/java/com/wms/controller/InventoryCheckController.java:33-35`
- Modify: `wms/src/main/java/com/wms/controller/InventoryTransferController.java:18-20`
- Modify: `wms/src/main/java/com/wms/controller/PurchaseReturnController.java:16-17`
- Modify: `wms/src/main/java/com/wms/controller/StatsController.java:128,142`
- Keep: `wms/src/main/java/com/wms/controller/UserController.java:38` (不变)
- Keep: `wms/src/main/java/com/wms/controller/PurchaseController.java:35` (不变)
- Keep: `wms/src/main/java/com/wms/controller/SalesController.java:27` (不变)

- [ ] **Step 1: 为GoodsController添加权限注解**

在类定义上方添加 `@RequireRole({1})`（仅库存管理员可操作商品）：

```java
@RestController
@RequestMapping("/goods")
@RequireRole({1}) // 仅库存管理员可操作商品
public class GoodsController {
```

- [ ] **Step 2: 为GoodstypeController添加权限注解**

```java
@RestController
@RequestMapping("/goodstype")
@RequireRole({1}) // 仅库存管理员可管理商品分类
public class GoodstypeController {
```

- [ ] **Step 3: 为SupplierController添加权限注解**

```java
@RestController
@RequestMapping("/supplier")
@RequireRole({1}) // 仅库存管理员可管理供应商
public class SupplierController {
```

- [ ] **Step 4: 为SalesReturnController添加权限注解**

退换货处理：店长+收银员可访问：

```java
@RestController
@RequestMapping("/return")
@RequireRole({0, 2}) // 店长+收银员可处理退换货
public class SalesReturnController {
```

- [ ] **Step 5: 更新InventoryCheckController权限**

库存盘点仅库存管理员：

```java
@RequestMapping("/inventoryCheck")
@RequireRole({1}) // 仅库存管理员可进行库存盘点
public class InventoryCheckController {
```

- [ ] **Step 6: 更新InventoryTransferController权限**

库存调拨仅库存管理员：

```java
@RequestMapping("/inventoryTransfer")
@RequireRole({1}) // 仅库存管理员可进行库存调拨
public class InventoryTransferController {
```

- [ ] **Step 7: 为PurchaseReturnController添加权限注解**

采购退回：店长+库存管理员：

```java
@RestController
@RequestMapping("/purchaseReturn")
@RequireRole({0, 1}) // 店长+库存管理员可处理采购退回
public class PurchaseReturnController {
```

- [ ] **Step 8: 更新StatsController中的补货建议权限**

将第128行的 `@RequireRole({0, 1})` 改为 `@RequireRole({0})`（补货建议仅店长）：

```java
@RequireRole({0}) // 补货建议仅店长可查看
@GetMapping("/suggestRestock")
```

将第142行的 `@RequireRole({0, 1})` 保持不变（库存预警店长+库存管理员均可查看）。

---

## Task 8: 更新SalesRecord.vue销售记录权限

**Files:**
- Modify: `workspace/wms-web/src/components/record/SalesRecord.vue:465`

**说明:** 当前收银员(2)只能看自己的销售记录。新需求中销售记录对**店长(0)+收银员(2)**可见。库存管理员(1)不再有权限（通过菜单menuRight控制）。代码逻辑无需修改，因为：
- 店长(0)看全部记录（现有逻辑不变）
- 收银员(2)只看自己的记录（现有逻辑不变）
- 库存管理员(1)在菜单层面已被排除

**无需修改代码。**

---

## Task 9: 更新测试数据中的用户角色名称

**Files:**
- Modify: `wms/src/main/resources/sql/complete_test_data.sql:11`

- [ ] **Step 1: 更新测试数据中店长的名称**

将第11行的"系统管理员"改为"店长"：

```sql
(1, 'admin', '店长', '123456', 30, 1, '13800000001', 0, 'Y'),
```

---

## Task 10: 更新CLAUDE.md项目文档

**Files:**
- Modify: `CLAUDE.md`

- [ ] **Step 1: 更新CLAUDE.md中的角色与模块说明**

在CLAUDE.md末尾追加最新的角色定义和功能模块说明：

```markdown
## 角色定义
- roleId = 0：店长（原系统管理员）
- roleId = 1：库存管理员
- roleId = 2：收银员

## 功能模块与权限矩阵
| 模块 | 子功能 | 店长(0) | 库存管理员(1) | 收银员(2) |
|------|--------|---------|---------------|-----------|
| 用户管理 | 用户账号管理 | ✅ | ❌ | ❌ |
| 用户管理 | 管理员账号管理 | ✅ | ❌ | ❌ |
| 用户管理 | 个人信息管理 | ✅ | ✅ | ✅ |
| 商品管理 | 商品信息 | ❌ | ✅ | ❌ |
| 商品管理 | 商品分类 | ❌ | ✅ | ❌ |
| 采购管理 | 供应商信息维护 | ❌ | ✅ | ❌ |
| 采购管理 | 采购入库 | ✅ | ✅ | ❌ |
| 采购管理 | 采购退回 | ✅ | ✅ | ❌ |
| 库存管理 | 库存盘点 | ❌ | ✅ | ❌ |
| 库存管理 | 库存调拨 | ❌ | ✅ | ❌ |
| 库存管理 | 库存预警 | ✅ | ✅ | ❌ |
| 库存管理 | 补货建议报表 | ✅ | ❌ | ❌ |
| 销售管理 | 收银结算 | ❌ | ❌ | ✅ |
| 销售管理 | 销售记录查询 | ✅ | ❌ | ✅ |
| 销售管理 | 退换货处理 | ✅ | ❌ | ✅ |
| 统计分析 | 销售统计 | ✅ | ❌ | ❌ |
```

---

## 执行顺序与依赖关系

```
Task 1 (SQL脚本) ──→ Task 2 (路由) ──→ Task 3 (角色标签)
                                      ──→ Task 4 (商品权限)
                                      ──→ Task 5 (退换货权限)
                                      ──→ Task 8 (销售记录)
Task 7 (后端注解) ──→ Task 9 (测试数据)
                    ──→ Task 10 (文档)
```

建议按 Task 1 → 2 → 3 → 4 → 7 → 9 → 10 的顺序执行。Task 5、6、8 无需代码修改。

---

## 验证检查清单

- [ ] 店长登录后能看到：用户管理（用户账号管理、管理员账号管理）、采购管理（采购入库、采购退回）、库存管理（库存预警、补货建议报表）、销售管理（销售记录查询、退换货处理）、统计分析（销售统计）
- [ ] 库存管理员登录后能看到：商品管理（商品信息、商品分类）、采购管理（供应商信息维护、采购入库、采购退回）、库存管理（库存盘点、库存调拨、库存预警）
- [ ] 收银员登录后能看到：销售管理（收银结算、销售记录查询、退换货处理）
- [ ] 所有角色登录后都能看到：首页 + 个人中心
- [ ] 各页面的角色标签统一显示为"店长/库存管理员/收银员"
- [ ] 操作日志和仓库管理菜单不再出现在导航栏