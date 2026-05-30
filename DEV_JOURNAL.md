# 开发日志 — 2026-05-22 / 2026-05-23

## 重构目标

基于《课题.docx》需求文档，对"小型超市进销存管理系统"进行底层架构升级和业务模块改造，覆盖商品管理、采购管理、采购退货、库存查询四个模块。

---

## 一、数据库变更

### 1.1 goods 表 — 新增预占数量

```sql
ALTER TABLE goods ADD COLUMN reserved_count INT DEFAULT 0 COMMENT '预占数量' AFTER count;
```

### 1.2 新建 goods_batch 表 — 批次库存（解决批次+多仓库两个问题）

```sql
CREATE TABLE goods_batch (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    goods_id        INT NOT NULL COMMENT '商品ID',
    batch_no        VARCHAR(50) NOT NULL COMMENT '批次号 yyyyMMddHHmm',
    supplier_id     INT COMMENT '供应商ID',
    storage_id      INT NOT NULL COMMENT '仓库ID',
    purchase_price  DECIMAL(10,2) DEFAULT 0 COMMENT '成本价',
    initial_count   INT NOT NULL DEFAULT 0 COMMENT '原始入库数量',
    current_count   INT NOT NULL DEFAULT 0 COMMENT '当前剩余数量',
    purchase_id     INT COMMENT '来源采购单ID',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_goods_batch (goods_id, batch_no),
    INDEX idx_goods_storage (goods_id, storage_id),
    INDEX idx_batch_available (goods_id, storage_id, current_count)
);
```

### 1.3 purchase_detail 表 — 新增批次号+入库仓库

```sql
ALTER TABLE purchase_detail 
  ADD COLUMN batch_no VARCHAR(50) COMMENT '批次号' AFTER subtotal,
  ADD COLUMN storage_id INT COMMENT '入库仓库ID' AFTER batch_no;
```

### 1.4 新建 purchase_return 表 — 采购退货独立工作流

```sql
CREATE TABLE purchase_return (
    id INT AUTO_INCREMENT PRIMARY KEY,
    return_no VARCHAR(50) NOT NULL COMMENT '退货单号',
    purchase_id INT NOT NULL COMMENT '原采购单ID',
    goods_id INT NOT NULL COMMENT '商品ID',
    batch_id INT COMMENT 'goods_batch ID',
    batch_no VARCHAR(50) COMMENT '批次号',
    return_count INT NOT NULL COMMENT '退货数量',
    reason VARCHAR(500) COMMENT '退货原因',
    status INT DEFAULT 1 COMMENT '1:待审核 2:已完成 3:已拒绝',
    creator_id INT COMMENT '创建人（采购员）',
    auditor_id INT COMMENT '审核人（店长）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    audit_time DATETIME
);
```

---

## 二、核心逻辑变更

### 2.1 商品管理 — 预占数量 + 批次库存体系 + 批次下拉联动

| 变更点 | 说明 |
|--------|------|
| `Goods.reservedCount` | 新增预占数量字段，与 count 区分：可用库存 = count - reservedCount |
| `Goods.batchNo` | 新增 transient 字段（`@TableField(exist = false)`），新增/编辑商品时选填 |
| `GoodsBatch` 实体 | 以"批次+仓库"为粒度，每一条记录追踪一个批次的真实库存 |
| `deductStockFIFO()` | 销售出库时按 create_time ASC 逐批扣减 current_count |
| `getBatchStock()` | 查询某商品在各仓库的批次库存分布 |
| `getBatchList()` | 跨商品批次总览：JOIN goods_batch+goods+supplier+storage，含仓库名/供应商名 |
| **批次下拉联动（新增）** | 商品列表每行内嵌批次选择下拉框，选中后仓库/供应商/进价/库存数量四列实时切换为批次数据 |
| **初始批次自动创建（新增）** | `GoodsController.save()`：batchNo 非空 + count>0 → 自动创建 goods_batch；`update()`：count 增量>0 + batchNo 非空 → 按增量创建新批次 |
| 补货建议公式修正 | `need = avgDaily×cycleDays - (count - reservedCount)` |
| 库存预警公式修正 | 触发条件：`(count - reservedCount) <= min_count` |
| 前端新增列 | 商品列表新增"预占数量""可用库存""批次（下拉）"列，预警标红基于可用库存 |

### 2.2 采购管理 — 状态流+批次号+仓库入库

| 变更点 | 旧流程 | 新流程 |
|--------|--------|--------|
| 状态定义 | 0=待入库, 1=已入库, 2=已退货 | 0=待审核, 1=已审核待入库, 2=已入库, 3=已取消 |
| 审核环节 | 无 | 新增 `audit()`：店长审核 0→1 |
| 批次号生成 | 无 | `createPurchase()` 自动生成：yyyyMMddHHmm + 序号 |
| 入库流程 | `inbound(purchaseId)` 全量入库 | `inbound(InboundDTO)`：选仓库 + 每条明细填实际入库数量 → 创建 goods_batch |
| 退货流程 | 直接扣 goods.count | 按 batchNo 定位 goods_batch，FIFO 逐批扣减 |

### 2.3 采购退货 — 独立工作流

| 变更点 | 说明 |
|--------|------|
| 新建 `PurchaseReturn` 实体 | 独立于 Purchase 的退货单体系 |
| 审批链 | 采购员 submit（展示批次剩余数量）→ 店长 approve（FIFO 扣 batch+goods）→ 完成；或 reject |
| 前端 | 入库对话框（仓库选择+数量调整）、退货申请对话框（批次选择+数量+原因） |

### 2.4 库存查询 — 供应商筛选 + 批次总览

| 变更点 | 说明 |
|--------|------|
| `listPage` 加 supplierId | 商品列表支持按供应商筛选 |
| 新增 `POST /goods/batchList` | 跨商品批次库存总览：JOIN goods_batch+goods+supplier+storage，支持按 goodsName/supplierId/batchNo 筛选 |
| 前端新增供应商下拉框 | GoodsManage.vue 筛选栏新增供应商选择 |

### 2.5 Bug 修复 — keep-alive 组件缓存导致页面数据不刷新

| 问题 | 根因 | 修复 |
|------|------|------|
| 退换货管理/损耗管理页面须刷新才能显示 | `Index.vue` 的 `<keep-alive :include="['SalesRecord']">` 对 `<script setup>` 异步组件名称匹配失败，导致未在白名单的组件也被缓存。缓存后切换回来只触发 `onActivated`，但组件未实现该钩子 | `ReturnManage.vue`、`LossReport.vue`、`GoodsManage.vue` 均添加 `onActivated` 钩子重新加载数据 |
| 商品管理下拉数据（仓库/供应商/分类）不刷新 | `onActivated` 只调了 `loadPost()` 未重新加载下拉数据 | 提取 `initPage()` 统一入口，`onMounted` 和 `onActivated` 均并发加载分类+仓库+供应商后刷新表格 |

---

## 三、新增/修改文件清单

### 新建文件（9个）

| 文件 | 说明 |
|------|------|
| `entity/GoodsBatch.java` | 批次库存实体 |
| `entity/InboundDTO.java` | 入库请求 DTO（仓库+明细数量） |
| `entity/PurchaseReturn.java` | 采购退货实体 |
| `entity/PurchaseReturnDTO.java` | 退货申请 DTO |
| `mapper/GoodsBatchMapper.java` | 批次库存 Mapper |
| `mapper/PurchaseReturnMapper.java` | 退货 Mapper |
| `service/IPurchaseReturnService.java` | 退货服务接口 |
| `service/impl/PurchaseReturnServiceImpl.java` | 退货服务实现 |
| `controller/PurchaseReturnController.java` | 退货 REST 端点 |

### 修改文件（14个后端 + 7个前端）

| 文件 | 关键改动 |
|------|---------|
| `entity/Goods.java` | +reservedCount, +batchNo(transient) |
| `entity/GoodsVO.java` | +reservedCount |
| `entity/Purchase.java` | 状态注释更新 |
| `entity/PurchaseDetail.java` | +batchNo, +storageId |
| `entity/PurchaseDTO.java` | PurchaseDetailItem +batchNo |
| `mapper/GoodsMapper.java` | +supplierId, +selectBatchList, +selectBatchesByGoodsId, +selectAvailableBatchesForSale |
| `mapper/xml/GoodsMapper.xml` | +reservedCount列, +supplierId筛选, +3个新SQL（批次查询） |
| `resources/mapper/StatsMapper.xml` | getRestockSuggestions/getWarningList 改用可用库存 |
| `service/IGoodsService.java` | +getBatchStock, +deductStockFIFO, +getBatchList |
| `service/IPurchaseService.java` | +audit, +inbound(InboundDTO) |
| `service/impl/GoodsServiceImpl.java` | suggestRestock公式, FIFO扣减, getBatchStock, getBatchList |
| `service/impl/PurchaseServiceImpl.java` | 批次号生成, audit(), inbound重写, returnGoods重写 |
| `controller/GoodsController.java` | +batchStock, +batchList, save/update 自动创建批次, listPage+supplierId, 注入 GoodsBatchMapper |
| `controller/PurchaseController.java` | +audit端点, inbound改InboundDTO, detail返回batchNo |
| `PurchaseManage.vue` | 状态值修正, 审核/入库对话框, 退货申请对话框 |
| `GoodsManage.vue` | **重写**：批次下拉框联动（仓库/供应商/进价/数量）、表单批次号字段、onActivated修复、initPage统一入口 |
| `ReturnManage.vue` | +onActivated 修复缓存不刷新 |
| `LossReport.vue` | +onActivated 修复缓存不刷新 |
| `Aside.vue` | 菜单图标+路由更新 |
| `Dashboard.vue` | 数据大屏更新 |
| `router/index.js` | 新增库存盘点/调拨/损耗管理路由 + 角色权限映射 |

---

## 四、下一步 TODO

### 4.1 待完成功能模块

| 模块 | 状态 | 说明 |
|------|------|------|
| **JWT 认证** | 文件已创建，待集成 | `JwtInterceptor`, `JwtUtils`, `RequireRole`, `LoginDTO` 已编写，需联调前后端登录流程 |
| **库存盘点** | 后端已创建，待联调 | `InventoryCheckController/Service/Mapper/Entity` 全套 + 前端 `Check.vue`，需验证业务流程 |
| **库存调拨** | 后端已创建，待联调 | `InventoryTransferController/Service/Mapper/Entity` + 前端 `Transfer.vue`，需验证 |
| **损耗管理** | 后端已创建，前端已完成 | `LossReportController/Service/Mapper/Entity` + 前端 `LossReport.vue`（已修复缓存刷新），需端到端测试 |
| **统计分析增强** | 实体已创建 | `ProductSalesVO`, `TodaySummaryVO`, `RestockExportVO` 已定义，需对接前端 Dashboard |

### 4.2 待验证/修复

| 项 | 说明 |
|----|------|
| 采购退货端到端流程 | `PurchaseReturnController` + `PurchaseReturnServiceImpl` 需与前端 `PurchaseManage.vue` 退货对话框联调 |
| SQL 初始化脚本 | 多个测试数据/菜单修复 SQL 已编写（`wms/src/main/resources/sql/` 目录），需整理执行顺序并验证 |
| 菜单数据一致性 | `fix_menu_hierarchy.sql` 等脚本需确保与 `router/index.js` 路由和角色映射一致 |
| 前端样式统一 | `GoodsManage.vue` 操作列宽度需根据"编辑+删除"两按钮调整（原 `width="130"`，去掉批次按钮后可能偏紧） |

### 4.3 技术债务

| 项 | 说明 |
|----|------|
| `getCurrentInstance()` 用法 | `ReturnManage.vue` 和 `GoodsManage.vue` 通过 `proxy.$httpUrl` 获取 baseURL，建议统一迁移到 `@/utils/request` |
| 重复代码 | `GoodsManage.vue` 的 `save()` 中新增/编辑分支重复度较高，可提取公共方法 |
| 批次下拉性能 | 当前 `loadAllBatches()` 一次拉取 10000 条批次数据，批次量大时需改为分页或按需加载 |

---

# 开发日志 — 2026-05-26

## 重构目标

继续推进 DEV_JOURNAL.md 四、TODO 任务，重点完成：
1. 商品管理页面 UI 重构（批次展开行 + 批次 CRUD）
2. 库存盘点端到端联调
3. 多个页面 keep-alive 缓存修复
4. 管理员管理页面 bug 修复

---

## 一、数据库变更

### 1.1 inventory_check_detail 表 — 补漏 remark 列

```sql
-- 建表 DDL 漏了 remark 列，导致 MyBatis Plus 查询映射失败返回 500
ALTER TABLE inventory_check_detail ADD COLUMN remark VARCHAR(255) DEFAULT NULL COMMENT '备注';
```

> 已同步修正 `create_inventory_tables.sql` DDL。

---

## 二、核心逻辑变更

### 2.1 商品管理 — 批次下拉 → 可展开行 + 批次内联 CRUD

| 变更点 | 旧设计 | 新设计 |
|--------|--------|--------|
| 批次查看方式 | 每行内嵌 el-select 下拉框选择批次 | ID 列前加 `type="expand"` 折叠箭头，展开显示批次子表 |
| 主表列精简 | 仓库/供应商/进价/库存数量 随下拉联动切换 | **删掉**仓库/供应商/进价/库存数量四列，全部移入展开行 |
| 可用库存公式 | `count - reservedCount` | **有批次时** = 各批次 `current_count` 之和；无批次时 = `count - reservedCount` |
| 预警判断 | 基于 `count - reservedCount` | 基于 `getAvailableCount(row)`（批次总和优先） |
| 批次操作 | 无 CRUD | 展开行内嵌**新增/编辑/删除**按钮 + 独立批次维护对话框 |
| 批次号 | 手动输入 | **留空自动生成**：`yyyyMMddHHmm` + 2位递增序号，查同分钟内最大序号保证唯一 |
| 批次子表分页 | 无 | 按 goodsId 独立维护分页状态，每页 5 条，超一页显示 prev/pager/next |

### 2.2 后端 — 新增批次 CRUD 端点

| 端点 | 功能 | 说明 |
|------|------|------|
| `POST /goods/batch/save` | 新增批次 | batchNo 留空时自动生成唯一批次号 |
| `POST /goods/batch/update` | 更新批次 | 修改批次号/仓库/进价/库存等 |
| `GET /goods/batch/del?id=` | 删除批次 | 物理删除 |

### 2.3 库存盘点 — 端到端联调修复

| 变更点 | 说明 |
|--------|------|
| `Check.vue` keep-alive | 添加 `onActivated` → `initPage()` |
| `Check.vue` creatorId | `user.id \|\| 1` 从 sessionStorage 读取，不再硬编码 |
| `Check.vue` Plus 图标 | 补导入 `@element-plus/icons-vue` |
| `Check.vue` 发起盘点 | `ElMessageBox.prompt` 替换为仓库选择对话框（`el-select` + `filterable` 搜索） |
| `InventoryCheckController.getDetailList()` | 返回数据增加 `goodsName` + `barcode`，JOIN goods 表查询 |
| 录入/详情对话框表格 | 列变为：ID → 商品条码 → 商品名称 → 账面库存 → 实盘数量 → 盈亏数量 |
| `create_inventory_tables.sql` | DDL 补漏 `remark VARCHAR(255)` |
| `Aside.vue` | 补库存管理菜单图标：`Box`, `DocumentChecked`, `Sort` |

### 2.4 管理员管理 — Bug 修复

| 问题 | 根因 | 修复 |
|------|------|------|
| 确定按钮一直转圈无法点击 | `:loading="centerDialogVisible"` 绑定到对话框显示状态 | 新增 `saving` ref，请求开始设 true，`finally` 中设 false |
| 点击新增无反应 | `resetForm()` 中 `formRef.value` 在对话框关闭时为 null，调 `.resetFields()` 抛 TypeError | 改为 `formRef.value?.resetFields()` 可选链 |
| 新增时表单残留旧数据 | `add()` 只打开对话框不重置表单 | `add()` 中先 `resetForm()` 再手动清空 form |

### 2.5 Keep-alive 缓存修复（本轮新增）

| 页面 | 问题 | 修复 |
|------|------|------|
| `Dashboard.vue` | 切回数据大屏空白，需手动刷新 | 添加 `onActivated` → `loadAllData()`；echarts 每次 init 前 `dispose()` 旧实例 |
| `Check.vue` | 同上 | 添加 `onActivated` → `initPage()` |

---

## 三、新增/修改文件清单

### 本次修改文件（3个后端 + 5个前端）

| 文件 | 关键改动 |
|------|---------|
| `controller/GoodsController.java` | +3个批次CRUD端点，`batchSave` 自动生成唯一批次号 |
| `controller/InventoryCheckController.java` | `getDetailList()` 注入 GoodsMapper，返回 goodsName + barcode |
| `sql/create_inventory_tables.sql` | DDL 补 `remark` 列 |
| `GoodsManage.vue` | **重写展开行体系**：折叠箭头 + 批次子表 + 内联CRUD + 子表分页 + 批次号自动生成 + 可用库存改批次总和；删仓库/供应商/进价/库存数量列；`saveBatch` 改手动校验 |
| `Dashboard.vue` | +onActivated + echarts dispose |
| `Check.vue` | +onActivated + 仓库选择对话框 + 商品条码列 + Plus图标导入 + creatorId动态化 |
| `AdminManage.vue` | :loading 改 saving ref + resetForm 可选链 + add() 表单重置 |
| `Aside.vue` | 补 Box/DocumentChecked/Sort 图标 |

---

## 四、下一步 TODO

### 4.1 待完成功能模块

| 模块 | 状态 | 说明 |
|------|------|------|
| **JWT 认证** | 文件已创建，待集成 | `JwtInterceptor`, `JwtUtils`, `RequireRole`, `LoginDTO` 已编写，需联调前后端登录流程 |
| **库存盘点** | ✅ 联调完成 | 前后端全链路验证通过，发起→录入→审核三阶段可用。**05-27 修复：录入对话框商品条码/名称显示为"-"的 bug（`inputDetails` 映射漏字段）** |
| **库存调拨** | ✅ 联调完成 | `InventoryTransferController/Service/Mapper/Entity` + 前端 `Transfer.vue`。**05-27 修复：商品选择改为按 goods_batch.storage_id 查批次库存（新增 `GET /goods/listByStorage` 端点），修复 onActivated 缓存** |
| **损耗管理** | 后端已创建，前端已完成 | `LossReportController/Service/Mapper/Entity` + 前端 `LossReport.vue`（已修复缓存刷新），需端到端测试 |
| **统计分析增强** | 实体已创建 | `ProductSalesVO`, `TodaySummaryVO`, `RestockExportVO` 已定义，Dashboard 已修复缓存 |

### 4.2 待验证/修复

| 项 | 说明 |
|----|------|
| 采购退货端到端流程 | `PurchaseReturnController` + `PurchaseReturnServiceImpl` 需与前端 `PurchaseManage.vue` 退货对话框联调 |
| SQL 初始化脚本 | 多个测试数据/菜单修复 SQL 已编写（`wms/src/main/resources/sql/` 目录），需整理执行顺序并验证 |
| 菜单数据一致性 | `add_inventory_menu.sql` 需确保已执行，菜单表存在 id=999/9990/9991 记录 |
| 库存调拨 Transfer.vue | 缺少 `onActivated` 钩子，切换 tab 后可能不刷新 |

### 4.3 技术债务

| 项 | 说明 |
|----|------|
| `getCurrentInstance()` 用法 | `ReturnManage.vue` 和 `GoodsManage.vue` 通过 `proxy.$httpUrl` 获取 baseURL，建议统一迁移到 `@/utils/request` |
| 重复代码 | `GoodsManage.vue` 的 `save()` 中新增/编辑分支重复度较高，可提取公共方法 |
| 子表展开性能 | 展开行每次渲染调用 `getBatchesForGoods()` 过滤全量数据，批次量大时建议加缓存或按需查询 |
| Transfer.vue 缓存 | ✅ 已修复 | `onActivated` → `initPage()`，与调拨联调同期完成 |
| GoodsManage 展开行批次号不显示 | 🔧 排查中 | 批次子表仓库/供应商/进价/库存均正常，仅批次号列为空，已加 console.log 调试，需查看浏览器控制台确认后端返回字段名 |
| 调拨生成不同ID商品 | ✅ 已修复 | 05-27：`auditTransfer()` 不再查找/创建目标仓 goods 记录，全程使用同一 goodsId，调拨变为纯批次级操作（同商品不同仓库的批次增减） |

---

# 开发日志 — 2026-05-27

## 重构目标

继续推进 TODO 任务，重点完成：
1. 库存盘点录入对话框 bug 修复（商品条码/名称显示为"-"）
2. 库存调拨端到端联调（商品选择基于批次库存 + onActivated 缓存修复）
3. 商品管理展开行批次号列不显示问题排查

---

## 一、数据库变更

无新增 DDL。本轮未涉及数据库结构变更。

---

## 二、核心逻辑变更

### 2.1 库存盘点 — 录入实盘数据对话框修复

| 问题 | 根因 | 修复 |
|------|------|------|
| 录入实盘数据页面商品条码和商品名称显示为 `- -` | `Check.vue` `handleInput()` 中从 `detailList` 映射到 `inputDetails` 时只复制了 `id/goodsId/expectedCount/actualCount` 四个字段，**漏掉了 `barcode` 和 `goodsName`** | 在 `inputDetails` 映射中补上 `barcode: item.barcode` 和 `goodsName: item.goodsName` |

> 后端 `InventoryCheckController.getDetailList()` 已正确 JOIN goods 表返回 goodsName + barcode（05-26 完成），本次纯前端修复。

### 2.2 库存调拨 — 端到端联调 + 商品选择逻辑修正

| 变更点 | 旧逻辑 | 新逻辑 |
|--------|--------|--------|
| 商品选择数据源 | `POST /goods/listPage` 传 `storage` 参数，后端按 `goods.storage`（商品默认仓库）过滤 | 新增 `GET /goods/listByStorage?storageId=`，查 `goods_batch` 中 `storage_id = ? AND current_count > 0` 的商品 |
| 可用库存展示 | `g.count - g.reservedCount`（总库存扣除预占） | `g.batchCount`（`SUM(gb.current_count)` 在该仓库的批次库存之和） |
| 最大调拨数量 | `max = count - reservedCount` | `max = batchCount` |
| Keep-alive 缓存 | 仅 `onMounted`，切 tab 后不刷新 | 补 `onActivated` → `initPage()` |

**后端新增文件变动：**

| 文件 | 改动 |
|------|------|
| `mapper/GoodsMapper.java` | 新增 `selectGoodsByStorageId(@Param("storageId") Integer storageId)` 方法 |
| `mapper/xml/GoodsMapper.xml` | 新增 SQL：`JOIN goods_batch gb ON g.id = gb.goods_id` + `WHERE gb.storage_id = #{storageId} AND gb.current_count > 0` + `GROUP BY g.id` + `SUM(gb.current_count) AS batchCount` |
| `controller/GoodsController.java` | 新增 `GET /goods/listByStorage?storageId=` 端点 |

**前端修改：**

| 文件 | 改动 |
|------|------|
| `Transfer.vue` | `handleFromStorageChange` 调用新端点；`handleGoodsChange` 用 `batchCount` 计算上限；商品下拉标签显示 `batchCount`；补 `onActivated` |

### 2.3 商品管理 — 批次号列不显示（排查中）

| 问题 | 已尝试修复 | 状态 |
|------|-----------|------|
| 展开行批次子表中**仓库/供应商/进价/当前库存/原始入库**均正常显示，唯独**批次号**列为空 | (1) 将 `prop="batchNo"` 改为显式模板 `{{ b.batchNum \|\| '-' }}`；(2) SQL 别名从 `batchNo` 改为 `batchNum`（避免与 MyBatis `#{batchNo}` 参数名冲突）；(3) 添加 `console.log(Object.keys(allBatches[0]))` 调试日志 | 🔧 待查看浏览器控制台确认后端返回字段名 |

---

## 三、新增/修改文件清单

### 本次修改文件（3个后端 + 2个前端）

| 文件 | 关键改动 |
|------|---------|
| `mapper/GoodsMapper.java` | +selectGoodsByStorageId 方法 |
| `mapper/xml/GoodsMapper.xml` | +selectGoodsByStorageId SQL；selectBatchList 别名 batchNo→batchNum |
| `controller/GoodsController.java` | +GET /goods/listByStorage 端点 |
| `Check.vue` | handleInput() 映射 inputDetails 补 barcode + goodsName |
| `Transfer.vue` | 商品选择改调 /goods/listByStorage；batchCount 替换 count-reservedCount；补 onActivated；补 Upload 图标导入 |

---

## 四、下一步 TODO

### 4.1 待完成功能模块

| 模块 | 状态 | 说明 |
|------|------|------|
| **JWT 认证** | 文件已创建，待集成 | `JwtInterceptor`, `JwtUtils`, `RequireRole`, `LoginDTO` 已编写，需联调前后端登录流程 |
| **库存盘点** | ✅ 已完成 | 05-26 联调完成；05-27 修复录入对话框商品条码/名称 bug |
| **库存调拨** | ✅ 已完成 | 05-27 联调：批次库存商品选择 + onActivated + 创建→审核流程 |
| **损耗管理** | 前端已完成，待端到端测试 | `LossReportController/Service/Mapper/Entity` + 前端 `LossReport.vue`（已修复缓存刷新） |
| **统计分析增强** | 实体已创建 | `ProductSalesVO`, `TodaySummaryVO`, `RestockExportVO` 已定义，Dashboard 已修复缓存 |

### 4.2 待验证/修复

| 项 | 说明 |
|----|------|
| **GoodsManage 批次号列不显示** | 🔧 排查中，需查看浏览器控制台确认后端 `/goods/batchList` 返回数据的实际字段名 |
| 采购退货端到端流程 | `PurchaseReturnController` + `PurchaseReturnServiceImpl` 需与前端 `PurchaseManage.vue` 退货对话框联调 |
| SQL 初始化脚本 | 多个测试数据/菜单修复 SQL 已编写（`wms/src/main/resources/sql/` 目录），需整理执行顺序并验证 |
| 菜单数据一致性 | `add_inventory_menu.sql` 需确保已执行，菜单表存在 id=999/9990/9991 记录 |

### 4.3 技术债务

| 项 | 说明 |
|----|------|
| `getCurrentInstance()` 用法 | `ReturnManage.vue` 和 `GoodsManage.vue` 通过 `proxy.$httpUrl` 获取 baseURL，建议统一迁移到 `@/utils/request` |
| 重复代码 | `GoodsManage.vue` 的 `save()` 中新增/编辑分支重复度较高，可提取公共方法 |
| 子表展开性能 | 展开行每次渲染调用 `getBatchesForGoods()` 过滤全量数据，批次量大时建议加缓存或按需查询 |

---

# 开发日志 — 2026-05-27（下）/ 2026-05-28

## 重构目标

继续推进 TODO 任务，重点完成：
1. 销售换货全流程联调（后端接口修复 + 前端下拉框交互 + 明细展示）
2. 数据大屏统计修复（数字动画 + SQL JOIN 乘法 bug）
3. 智能补货建议 → 快捷采购 → 采购管理 → 入库 → 记录管理全链路打通
4. 采购退货端到端联调（独立 tab + 批次号查找 + 审核扣减 + 记录流水）
5. 批次库存扣减修复（销售超卖兜底 + 批次列表展示）
6. 损耗管理上报人修复

---

## 一、数据库变更

### 1.1 sales_return_detail 表 — 补 exchange_goods_id 列

```sql
ALTER TABLE sales_return_detail
  ADD COLUMN exchange_goods_id INT DEFAULT NULL COMMENT '换货目标商品ID' AFTER subtotal;
```

---

## 二、核心逻辑变更

### 2.1 销售换货 — 全流程联调

| 问题 | 根因 | 修复 |
|------|------|------|
| 换货下拉框无商品可选 | `SalesRecord.vue` `loadExchangeGoods()` 调 `GET /goods/list`，后端要求必传 `barcode`，不传就返回"条码不能为空" | `GoodsController.java` `/goods/list` 的 `barcode` 改为 `required=false`，不传则分页返回全部商品 |
| 换货下拉框无法点击 | `el-select` 有 `:disabled="!row.returnCount \|\| row.returnCount <= 0"`，必须先填退货数量才能选换货商品 | 去掉 `:disabled`，提交时已有校验 |
| 换货可选范围太广 | 所有商品都能选 | 新增 `getExchangeOptions(row)` 方法：按 `row.goodsId` 查原商品分类和售价，只显示**同分类 + 同零售价**的商品 |
| 换货明细不显示换货商品名 | `ReturnDetailVO` 无换货商品字段 | 后端 VO +SQL LEFT JOIN 补 `exchangeGoodsName`；前端明细弹窗换货时显示"换货商品"列，隐藏退款金额，底部显示"换货无需退款" |
| 换货记录仍显示"确认退款"按钮 | 操作列只判断 `status===0` | 加 `row.type !== 2` 条件，换货不显示退款按钮 |

### 2.2 数据大屏 — 统计数字不显示 / 与趋势图不一致

| 问题 | 根因 | 修复 |
|------|------|------|
| 六个统计卡片始终为 0 | `CountTo` 组件只在 `mounted()` 执行动画，数据异步加载完成后 `endVal` 变了但不重新动画 | 加 `watch: { endVal() { this.animateNumber() } }` |
| 今日销售额与近7天趋势图中当天值不一致 | `getTodaySummary` SQL：`FROM sales s LEFT JOIN sales_detail sd` → `SUM(s.total_amount)` 被明细行数翻倍 | `todaySales` 和 `todayOrders` 改为独立子查询直接查 `sales` 表 |

### 2.3 快捷采购 — 端到端打通

| 问题 | 根因 | 修复 |
|------|------|------|
| 点了"快捷采购"提示成功，但采购管理没有记录 | `quickPurchase` 只写 sessionStorage + 触发无人监听的自定义事件，从未调后端 | 改为直接 `POST /purchase/create`，构建完整 PurchaseDTO |
| 采购单创建报 `purchase_no` 无默认值 | `createPurchase()` 未生成采购单号 | 加自动生成：`yyyyMMddHHmmss` |
| 快捷采购缺少供应商/进价 | `RestockSuggestionVO` 无 supplierId/purchasePrice | VO +2 字段，SQL 加 `g.supplier_id`、`g.purchase_price` |
| 批次号可能跨采购单重复 | `createPurchase` 只用 `前缀 + i+1`，同分钟两个采购单首批都是 `xxx01` | 先查 DB 当前分钟已有最大序号，从 `maxSeq+1` 递增 |

### 2.4 采购入库 — 同步记录管理

| 问题 | 根因 | 修复 |
|------|------|------|
| 已入库信息在记录管理看不到 | `inbound()` 创建 Record 时漏设 `userId`（申请人），采购员角色过滤条件 `a.userId={id}` 匹配不到 NULL | 加 `record.setUserId(purchase.getUserId())` |

### 2.5 损耗管理 — 上报人名称不显示

| 问题 | 根因 | 修复 |
|------|------|------|
| 上报人列始终显示 "-" | `LossReport.vue` 从 `localStorage` 读取用户信息，但整个项目登录时存在 `sessionStorage` | 全部 3 处改为 `sessionStorage` |

### 2.6 采购退货 — 端到端联调 + 退货管理 tab

| 变更点 | 说明 |
|--------|------|
| **ReturnManage.vue 加 tab** | "销售退货/换货" + "采购退货"双 tab，采购退货列表含：退货单号、商品名、批次号、数量、金额、原因、状态、申请人、审核人、时间 |
| **审核通过/拒绝** | 采购退货 tab 内嵌审核按钮，调 `POST /purchaseReturn/approve\|reject` |
| **批次查找修正** | `submit()` 不再信任前端 batchId（实际是 purchase_detail.id），改用 `batchNo + goodsId` 查 goods_batch |
| **approve 兜底** | `approve()` 也用 `batchNo + goodsId` 兜底，防止旧记录 batchId 是 purchase_detail.id 扣错批次 |
| **审批人/金额显示** | 新建 `PurchaseReturnVO` + `PurchaseReturnMapper.xml`，LEFT JOIN user+goods+goods_batch 查姓名和金额 |
| **文件变动** | 新建 `PurchaseReturnVO.java`、`PurchaseReturnMapper.xml`；改 `PurchaseReturnMapper.java`、`PurchaseReturnServiceImpl.java`、`ReturnManage.vue` |

### 2.7 批次库存扣减修复

| 问题 | 根因 | 修复 |
|------|------|------|
| 销售后原批次没扣减，多了 `SALE{timestamp}` 负库存记录 | `SalesServiceImpl.checkout()` FIFO 消耗完所有批次后 `remaining>0`，兜底逻辑先"借用"已有批次扣减 → 把已扣完的批次变成负数 | 兜底改为**始终新建 SALE 批次**（`initialCount=0, currentCount=-remaining`），不污染已有批次 |
| 扣完的批次从列表中消失 | `selectBatchList` SQL 有 `WHERE gb.current_count > 0`，消耗完的批次被过滤 | 移除该条件 + 补 `initialCount` 列 |

---

## 三、新增/修改文件清单

### 新建文件（3个）

| 文件 | 说明 |
|------|------|
| `entity/PurchaseReturnVO.java` | 采购退货 VO，含 creatorName/auditorName/goodsName/returnAmount |
| `resources/mapper/PurchaseReturnMapper.xml` | 自定义 SQL：LEFT JOIN user+goods+goods_batch |

### 修改文件（11个后端 + 5个前端）

| 文件 | 关键改动 |
|------|---------|
| `controller/GoodsController.java` | `/goods/list` barcode 改可选；不传返回全部商品分页 |
| `entity/RestockSuggestionVO.java` | +supplierId, +purchasePrice |
| `entity/ReturnDetailVO.java` | +exchangeGoodsId, +exchangeGoodsName |
| `mapper/PurchaseReturnMapper.java` | +selectPage 自定义方法 |
| `mapper/xml/GoodsMapper.xml` | selectBatchList：去 current_count>0 过滤 + 补 initialCount |
| `resources/mapper/StatsMapper.xml` | getTodaySummary: todaySales/todayOrders 改子查询；getRestockSuggestions: +supplierId/purchasePrice |
| `service/impl/PurchaseServiceImpl.java` | createPurchase: +purchaseNo 自动生成；批次号全局唯一 + userId 补充 |
| `service/impl/PurchaseReturnServiceImpl.java` | submit: batchNo+goodsId 查批次；approve: userId 补充 + 批次兜底；listPage 改用自定义 SQL |
| `service/impl/SalesServiceImpl.java` | checkout 兜底：借批次→新建 SALE 批次 |
| `SalesRecord.vue` | 换货下拉框去 disabled + 同分类同售价过滤 |
| `ReturnManage.vue` | **重写**：双 tab（销售/采购退货）；采购退货含审核操作+金额+申请人列 |
| `Dashboard.vue` | CountTo 加 watch endVal |
| `LossReport.vue` | localStorage→sessionStorage（3处） |
| `RestockSuggest.vue` | 快捷采购真正调 POST /purchase/create |

---

## 四、下一步 TODO

### 4.1 待完成功能模块

| 模块 | 状态 | 说明 |
|------|------|------|
| **JWT 认证** | 文件已创建，待集成 | `JwtInterceptor`, `JwtUtils`, `RequireRole`, `LoginDTO` 已编写，需联调前后端登录流程 |
| **库存盘点** | ✅ 已完成 | 05-26 联调完成；05-27 修复录入对话框商品条码/名称 bug |
| **库存调拨** | ✅ 已完成 | 05-27 联调：批次库存商品选择 + onActivated + 创建→审核流程 |
| **损耗管理** | ✅ 已完成 | 05-28 修复上报人名称（localStorage→sessionStorage） |
| **采购退货** | ✅ 已完成 | 05-28 端到端联调：双 tab + 批次查找 + 审核 + 金额显示 |
| **统计分析增强** | ✅ 已完成 | 05-28 修复 Dashboard 数字动画 + getTodaySummary SQL JOIN bug |

### 4.2 待验证/修复

| 项 | 说明 |
|----|------|
| 采购退货旧记录 batchId 错误 | 旧退货记录的 `batch_id` 存的是 `purchase_detail.id`，会导致审核扣错批次。新建退货已修复，旧记录需手动清理或重新提交 |
| SALE 负库存遗留数据 | 旧版 checkout 兜底产生的 `SALE{timestamp}` 负库存记录，新销售不再产生，已有脏数据建议 `DELETE FROM goods_batch WHERE batch_no LIKE 'SALE%'` 清理 |
| SQL 初始化脚本 | 多个测试数据/菜单修复 SQL 已编写（`wms/src/main/resources/sql/` 目录），需整理执行顺序并验证 |
| 菜单数据一致性 | `add_inventory_menu.sql` 需确保已执行，菜单表存在 id=999/9990/9991 记录 |

### 4.3 技术债务

| 项 | 说明 |
|----|------|
| `getCurrentInstance()` 用法 | `ReturnManage.vue` 和 `GoodsManage.vue` 通过 `proxy.$httpUrl` 获取 baseURL，建议统一迁移到 `@/utils/request` |
| 重复代码 | `GoodsManage.vue` 的 `save()` 中新增/编辑分支重复度较高，可提取公共方法 |
| 子表展开性能 | 展开行每次渲染调用 `getBatchesForGoods()` 过滤全量数据，批次量大时建议加缓存或按需查询 |
| PurchaseManage.vue 退货对话框 | `openReturnDialog` 加载 `purchase/detail/list`（purchase_detail 数据），其中的 `batchId` 是 purchase_detail.id 非 goods_batch.id。虽然后端已修正，前端最好改为加载 goods_batch 列表 |