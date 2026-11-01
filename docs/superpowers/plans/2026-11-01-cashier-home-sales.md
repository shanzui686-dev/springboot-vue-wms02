# 收银员首页当日销售记录与明细查询 实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 收银员（roleId=2）登录后首页完全替换为"当日销售记录 + 明细查询 + 汇总统计"页面，其他角色首页不变。

**Architecture:** 后端在销售单链路上新增一个汇总聚合查询（Mapper → Service → Controller），与现有 `listPage` 同口径；前端新建 `CashierTodaySales.vue` 组件，复用现有 `/sales/listPage`、`/sales/getDetails` 接口，由 `Home.vue` 按角色挂载。设计文档：`docs/superpowers/specs/2026-11-01-cashier-home-sales-design.md`。

**Tech Stack:** Spring Boot + MyBatis（MyBatis-Plus BaseMapper）、Vue 3 `<script setup>` + Element Plus、axios。

**测试策略说明:** 本项目无单元测试基础设施（仅默认生成的 `WmsApplicationTests`），且范围限定为"只改收银员、最小改动"，故不引入测试框架。每任务以 `mvn compile` / `vue-cli-service lint` 作编译级校验，Task 6 做端到端手动验证。

**⚠️ 提交纪律:** 工作区存在大量用户在途改动。**所有 `git add` 必须显式指定文件路径，严禁 `git add .` / `git add -A`。**

---

### Task 1: 汇总查询 Mapper 层（SalesSummaryVO + SalesMapper + XML）

**Files:**
- Create: `wms/src/main/java/com/wms/entity/SalesSummaryVO.java`
- Modify: `wms/src/main/java/com/wms/mapper/SalesMapper.java`
- Modify: `wms/src/main/resources/mapper/SalesMapper.xml`

- [ ] **Step 1: 新建 SalesSummaryVO**

创建 `wms/src/main/java/com/wms/entity/SalesSummaryVO.java`，完整内容：

```java
package com.wms.entity;

import java.math.BigDecimal;

/**
 * 销售汇总 VO（销售笔数 / 应收总额 / 实收总额）
 */
public class SalesSummaryVO {

    /** 销售笔数 */
    private Long count;

    /** 应收总额 */
    private BigDecimal totalAmount;

    /** 实收总额 */
    private BigDecimal realAmount;

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getRealAmount() {
        return realAmount;
    }

    public void setRealAmount(BigDecimal realAmount) {
        this.realAmount = realAmount;
    }
}
```

- [ ] **Step 2: SalesMapper.java 增加接口方法**

在 `wms/src/main/java/com/wms/mapper/SalesMapper.java` 的 `selectSalesPage` 方法后新增方法，并在 import 区（`import com.wms.entity.SalesVO;` 之后）加 `import com.wms.entity.SalesSummaryVO;`：

```java
    /**
     * 查询销售汇总（销售笔数 / 应收总额 / 实收总额，与 selectSalesPage 同口径）
     * @param orderNum 订单流水号（模糊匹配）
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param userId 收银员ID
     * @return 销售汇总
     */
    SalesSummaryVO selectSalesSummary(@Param("orderNum") String orderNum,
                                      @Param("startDate") String startDate,
                                      @Param("endDate") String endDate,
                                      @Param("userId") Integer userId);
```

- [ ] **Step 3: SalesMapper.xml 增加 resultMap 与 SQL**

在 `wms/src/main/resources/mapper/SalesMapper.xml` 中，先在 `SalesVOResultMap` 的 `</resultMap>` 之后新增：

```xml
    <!-- 销售汇总结果映射 -->
    <resultMap id="SalesSummaryVOResultMap" type="com.wms.entity.SalesSummaryVO">
        <result column="cnt" property="count" />
        <result column="total_amount" property="totalAmount" />
        <result column="real_amount" property="realAmount" />
    </resultMap>
```

再在 `selectSalesPage` 的 `</select>` 之后新增（WHERE 条件与 `selectSalesPage` 完全一致，保证同口径；含已退款单）：

```xml
    <!-- 销售汇总（口径与 selectSalesPage 一致：模糊单号含SO补零兜底、时间范围、收银员，含已退款单） -->
    <select id="selectSalesSummary" resultMap="SalesSummaryVOResultMap">
        SELECT
            COUNT(*) AS cnt,
            IFNULL(SUM(s.total_amount), 0) AS total_amount,
            IFNULL(SUM(s.real_amount), 0) AS real_amount
        FROM sales s
        <where>
            <if test="orderNum != null and orderNum != ''">
                AND (s.order_num LIKE CONCAT('%', #{orderNum}, '%')
                     OR CONCAT('SO', LPAD(s.id, 8, '0')) LIKE CONCAT('%', #{orderNum}, '%'))
            </if>
            <if test="startDate != null and startDate != ''">
                AND s.create_time &gt;= #{startDate}
            </if>
            <if test="endDate != null and endDate != ''">
                AND s.create_time &lt;= #{endDate}
            </if>
            <if test="userId != null">
                AND s.user_id = #{userId}
            </if>
        </where>
    </select>
```

- [ ] **Step 4: 编译校验**

Run: `cd "E:/javaEE/springboot-vue-wms02/wms" && mvn compile`
Expected: `BUILD SUCCESS`

- [ ] **Step 5: Commit**

```bash
git add wms/src/main/java/com/wms/entity/SalesSummaryVO.java wms/src/main/java/com/wms/mapper/SalesMapper.java wms/src/main/resources/mapper/SalesMapper.xml
git commit -m "feat: 新增销售汇总聚合查询（Mapper层）

Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>"
```

---

### Task 2: 汇总查询 Service 层

**Files:**
- Modify: `wms/src/main/java/com/wms/service/ISalesService.java`
- Modify: `wms/src/main/java/com/wms/service/impl/SalesServiceImpl.java`

- [ ] **Step 1: ISalesService 增加接口方法**

在 `wms/src/main/java/com/wms/service/ISalesService.java` 的 `getDetails` 方法之后新增，并在 import 区（`import com.wms.entity.SalesVO;` 之后）加 `import com.wms.entity.SalesSummaryVO;`：

```java
    /**
     * 查询销售汇总（销售笔数 / 应收总额 / 实收总额）
     * @param orderNum 订单流水号（模糊匹配）
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param userId 收银员ID
     * @return 销售汇总
     */
    SalesSummaryVO summary(String orderNum, String startDate, String endDate, Integer userId);
```

- [ ] **Step 2: SalesServiceImpl 增加实现**

在 `wms/src/main/java/com/wms/service/impl/SalesServiceImpl.java` 的 `getDetails` 实现之后新增（`salesMapper` 字段已存在，无需新增）：

```java
    @Override
    public SalesSummaryVO summary(String orderNum, String startDate, String endDate, Integer userId) {
        return salesMapper.selectSalesSummary(orderNum, startDate, endDate, userId);
    }
```

在 import 区加 `import com.wms.entity.SalesSummaryVO;`。

- [ ] **Step 3: 编译校验**

Run: `cd "E:/javaEE/springboot-vue-wms02/wms" && mvn compile`
Expected: `BUILD SUCCESS`

- [ ] **Step 4: Commit**

```bash
git add wms/src/main/java/com/wms/service/ISalesService.java wms/src/main/java/com/wms/service/impl/SalesServiceImpl.java
git commit -m "feat: 新增销售汇总查询Service层

Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>"
```

---

### Task 3: 汇总查询 Controller 接口

**Files:**
- Modify: `wms/src/main/java/com/wms/controller/SalesController.java`

- [ ] **Step 1: 新增 GET /sales/summary 接口**

在 `SalesController.java` 的 `getDetails` 方法之后新增（类级 `@RequireRole({0, 2})` 自动生效，无需方法级注解；`Result` 已 import）：

```java
    /**
     * 查询销售汇总（销售笔数 / 应收总额 / 实收总额）
     * @param orderNum 订单流水号（模糊匹配）
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param userId 收银员ID
     * @return 汇总结果
     */
    @GetMapping("/summary")
    public Result summary(@RequestParam(required = false) String orderNum,
                          @RequestParam(required = false) String startDate,
                          @RequestParam(required = false) String endDate,
                          @RequestParam(required = false) Integer userId) {
        return Result.suc(salesService.summary(orderNum, startDate, endDate, userId));
    }
```

- [ ] **Step 2: 编译校验**

Run: `cd "E:/javaEE/springboot-vue-wms02/wms" && mvn compile`
Expected: `BUILD SUCCESS`

- [ ] **Step 3: Commit**

```bash
git add wms/src/main/java/com/wms/controller/SalesController.java
git commit -m "feat: 新增销售汇总查询接口 GET /sales/summary

Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>"
```

---

### Task 4: 前端 CashierTodaySales.vue 组件

**Files:**
- Create: `workspace/wms-web/src/components/record/CashierTodaySales.vue`

- [ ] **Step 1: 创建组件**

创建 `workspace/wms-web/src/components/record/CashierTodaySales.vue`，完整内容（结构与写法对齐同目录 `SalesRecord.vue`）：

```vue
<template>
  <div class="cashier-today-sales">
    <!-- 搜索区 -->
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" class="search-form">
        <el-form-item label="日期">
          <el-date-picker
            v-model="searchDate"
            type="date"
            placeholder="选择日期"
            value-format="YYYY-MM-DD"
            clearable
            style="width: 160px"
          />
        </el-form-item>
        <el-form-item label="流水单号">
          <el-input
            v-model="searchOrderNum"
            placeholder="请输入流水单号"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 汇总卡片 -->
    <el-row :gutter="20" class="summary-row">
      <el-col :span="8">
        <el-card shadow="never" class="summary-card">
          <div class="summary-label">销售笔数</div>
          <div class="summary-value">{{ summary.count }}</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never" class="summary-card">
          <div class="summary-label">应收总额</div>
          <div class="summary-value">¥{{ formatAmount(summary.totalAmount) }}</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never" class="summary-card">
          <div class="summary-label">实收总额</div>
          <div class="summary-value">¥{{ formatAmount(summary.realAmount) }}</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 记录表格 -->
    <el-card class="table-card" shadow="never">
      <el-table :data="tableData" border stripe v-loading="loading" style="width: 100%">
        <el-table-column prop="orderNum" label="流水单号" width="180" />
        <el-table-column prop="totalAmount" label="应收总额" width="120">
          <template #default="{ row }">¥{{ formatAmount(row.totalAmount) }}</template>
        </el-table-column>
        <el-table-column prop="realAmount" label="实收金额" width="120">
          <template #default="{ row }">¥{{ formatAmount(row.realAmount) }}</template>
        </el-table-column>
        <el-table-column prop="changeAmount" label="找零" width="100">
          <template #default="{ row }">¥{{ formatAmount(row.changeAmount) }}</template>
        </el-table-column>
        <el-table-column label="支付方式" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.paymentMethod === 1 ? 'success' : 'primary'">
              {{ row.paymentMethod === 1 ? '现金' : '扫码' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="销售状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'danger' : 'success'">
              {{ row.status === 1 ? '已退款' : '正常' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="结账时间" width="180">
          <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="120">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleViewDetails(row)">查看明细</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        class="pagination"
      />
    </el-card>

    <!-- 销售明细弹窗 -->
    <el-dialog
      v-model="detailDialogVisible"
      :title="`销售单明细 - ${currentOrderNum}`"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-table :data="detailList" border stripe style="width: 100%">
        <el-table-column prop="goodsName" label="商品名称" min-width="150" />
        <el-table-column prop="price" label="单价" width="100">
          <template #default="{ row }">¥{{ formatAmount(row.price) }}</template>
        </el-table-column>
        <el-table-column prop="count" label="数量" width="100" align="center" />
        <el-table-column prop="subtotal" label="小计" width="120">
          <template #default="{ row }">¥{{ formatAmount(row.subtotal) }}</template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, getCurrentInstance } from 'vue'
import { Search } from '@element-plus/icons-vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const { proxy } = getCurrentInstance()
const httpUrl = proxy.$httpUrl

// 用户信息：7天免登录存 localStorage，普通登录存 sessionStorage
const user = JSON.parse(localStorage.getItem('user') || sessionStorage.getItem('user') || '{}')

// 今天日期 YYYY-MM-DD
const getToday = () => {
  const d = new Date()
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

// 搜索条件（日期默认今天，清空可查全部历史）
const searchDate = ref(getToday())
const searchOrderNum = ref('')

// 列表数据
const tableData = ref([])
const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 汇总数据
const summary = ref({ count: 0, totalAmount: 0, realAmount: 0 })

// 明细弹窗
const detailDialogVisible = ref(false)
const detailList = ref([])
const currentOrderNum = ref('')

const formatAmount = (val) => Number(val || 0).toFixed(2)

const formatDateTime = (dateTime) => {
  if (!dateTime) return ''
  const d = new Date(dateTime)
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}

// 构建查询参数（收银员固定查自己；日期含 00:00:00 ~ 23:59:59）
const buildParams = () => {
  const params = { userId: user.id }
  if (searchOrderNum.value) {
    params.orderNum = searchOrderNum.value
  }
  if (searchDate.value) {
    params.startDate = searchDate.value + ' 00:00:00'
    params.endDate = searchDate.value + ' 23:59:59'
  }
  return params
}

// 加载销售记录列表
const loadSalesList = () => {
  loading.value = true
  const params = { ...buildParams(), page: pageNum.value, limit: pageSize.value }
  axios.get(httpUrl + '/sales/listPage', { params })
    .then(res => {
      const result = res.data
      if (result.code === 200) {
        tableData.value = result.data.records || []
        total.value = result.data.total || 0
      } else {
        ElMessage.error(result.msg || '获取数据失败')
      }
    })
    .catch(() => {
      ElMessage.error('请求失败')
    })
    .finally(() => {
      loading.value = false
    })
}

// 加载汇总统计（与列表同参数，跨页准确）
const loadSummary = () => {
  axios.get(httpUrl + '/sales/summary', { params: buildParams() })
    .then(res => {
      const result = res.data
      if (result.code === 200 && result.data) {
        summary.value = result.data
      }
    })
    .catch(() => {
      ElMessage.error('汇总加载失败')
    })
}

// 查询按钮
const handleSearch = () => {
  pageNum.value = 1
  loadSalesList()
  loadSummary()
}

// 重置（恢复默认：日期=今天、流水号清空，并重新加载）
const handleReset = () => {
  searchDate.value = getToday()
  searchOrderNum.value = ''
  pageNum.value = 1
  loadSalesList()
  loadSummary()
}

// 查看明细
const handleViewDetails = (row) => {
  currentOrderNum.value = row.orderNum
  axios.get(httpUrl + '/sales/getDetails', { params: { salesId: row.id } })
    .then(res => {
      const result = res.data
      if (result.code === 200) {
        detailList.value = result.data || []
        detailDialogVisible.value = true
      } else {
        ElMessage.error(result.msg || '获取明细失败')
      }
    })
    .catch(() => {
      ElMessage.error('请求失败')
    })
}

const handleSizeChange = () => {
  pageNum.value = 1
  loadSalesList()
}

const handleCurrentChange = () => {
  loadSalesList()
}

onMounted(() => {
  loadSalesList()
  loadSummary()
})
</script>

<style scoped>
.cashier-today-sales {
  padding: 20px;
}
.search-card {
  margin-bottom: 16px;
}
.summary-row {
  margin-bottom: 16px;
}
.summary-card {
  text-align: center;
}
.summary-label {
  color: #909399;
  font-size: 13px;
  margin-bottom: 8px;
}
.summary-value {
  color: #303133;
  font-size: 22px;
  font-weight: 600;
}
.pagination {
  margin-top: 12px;
}
</style>
```

- [ ] **Step 2: Lint 校验**

Run: `cd "E:/javaEE/springboot-vue-wms02/workspace/wms-web" && npx vue-cli-service lint --no-fix src/components/record/CashierTodaySales.vue`
Expected: 无 error（warning 可接受）

- [ ] **Step 3: Commit**

```bash
git add workspace/wms-web/src/components/record/CashierTodaySales.vue
git commit -m "feat: 新增收银员今日销售记录组件（列表+汇总+明细弹窗）

Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>"
```

---

### Task 5: Home.vue 按角色挂载

**Files:**
- Modify: `workspace/wms-web/src/components/Home.vue`

- [ ] **Step 1: 重写 Home.vue**

用以下完整内容替换 `workspace/wms-web/src/components/Home.vue`（收银员渲染 `CashierTodaySales`，其他角色保留原欢迎页；用户信息改为 setup 同步读取，避免收银员闪现欢迎页）：

```vue
<template>
  <!-- 收银员：首页直接是当日销售记录页 -->
  <CashierTodaySales v-if="user.roleId === 2" />
  <!-- 其他角色：欢迎页 + 个人中心 -->
  <div v-else style="padding: 20px;">
    <h1 style="text-align: center; color: #409EFF;">{{'欢迎你！' + user.name }}</h1>
    <el-descriptions :title="'个人中心'" :column="2" size="large" border>
      <el-descriptions-item>
        <template #label>
          <i class="el-icon-user"></i>
          账号
        </template>
        {{ user.no }}
      </el-descriptions-item>
      <el-descriptions-item>
        <template #label>
          <i class="el-icon-mobile-phone"></i>
          电话
        </template>
        {{ user.phone }}
      </el-descriptions-item>
      <el-descriptions-item>
        <template #label>
          <i class="el-icon-location-outline"></i>
          性别
        </template>
        <el-tag v-if="user.sex === 0" type="danger" size="small">男</el-tag>
        <el-tag v-else-if="user.sex === 1" type="success" size="small">女</el-tag>
      </el-descriptions-item>
      <el-descriptions-item>
        <template #label>
          <i class="el-icon-timer"></i>
          年龄
        </template>
        {{ user.age }}
      </el-descriptions-item>
      <el-descriptions-item>
        <template #label>
          <i class="el-icon-setting"></i>
          角色
        </template>
        <el-tag v-if="user.roleId === 0" type="danger" size="small">店长</el-tag>
        <el-tag v-else-if="user.roleId === 1" type="primary" size="small">库存管理员</el-tag>
        <el-tag v-else type="success" size="small">收银员</el-tag>
      </el-descriptions-item>
    </el-descriptions>
    <div style="margin-top: 30px; text-align: center;">
      <DateUtils />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import DateUtils from './DateUtils.vue'
import CashierTodaySales from './record/CashierTodaySales.vue'

const router = useRouter()

// 优先从localStorage获取用户信息（7天免登录），其次从sessionStorage获取（普通登录）
// setup 同步读取，保证收银员首屏直接渲染销售记录页、不闪现欢迎页
const user = ref(JSON.parse(localStorage.getItem('user') || sessionStorage.getItem('user') || '{}'))

onMounted(() => {
  // 如果没有用户信息，说明已退出登录，跳转到登录页
  if (!user.value.id) {
    router.replace('/')
  }
})
</script>

<style scoped>
</style>
```

- [ ] **Step 2: Lint 校验**

Run: `cd "E:/javaEE/springboot-vue-wms02/workspace/wms-web" && npx vue-cli-service lint --no-fix src/components/Home.vue`
Expected: 无 error（warning 可接受）

- [ ] **Step 3: Commit**

```bash
git add workspace/wms-web/src/components/Home.vue
git commit -m "feat: 收银员首页替换为当日销售记录页，其他角色不变

Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>"
```

---

### Task 6: 端到端验证

**Files:** 无新改动（只验证；发现问题则修复后重跑对应校验并提交）

- [ ] **Step 1: 后端整体编译**

Run: `cd "E:/javaEE/springboot-vue-wms02/wms" && mvn compile`
Expected: `BUILD SUCCESS`

- [ ] **Step 2: 前端整体构建**

Run: `cd "E:/javaEE/springboot-vue-wms02/workspace/wms-web" && npm run build`
Expected: 构建成功，无编译错误

- [ ] **Step 3: 启动应用并手动验证**

启动后端（IDE 或 `mvn spring-boot:run`）与前端（`npm run serve`），用收银员账号登录，逐项核对：

| # | 操作 | 预期 |
|---|------|------|
| 1 | 收银员登录后进首页 | 直接是"当日销售记录"页，无欢迎语/个人信息卡 |
| 2 | 首屏加载 | 默认查今天，汇总卡片与表格数据一致（笔数=表格总条数） |
| 3 | 清空日期后查询 | 显示该收银员全部历史记录，汇总同步变化 |
| 4 | 输入流水号查询 / 重置 | 按单号过滤正常；重置恢复"日期=今天+单号清空"并重新加载 |
| 5 | 点击【查看明细】 | 弹窗展示该单商品名称/单价/数量/小计 |
| 6 | 翻页、改每页条数 | 分页正常 |
| 7 | 换店长或库存管理员登录进首页 | 仍是"欢迎你 + 个人中心"，无销售区块 |
| 8 | 店长打开"销售记录查询"页 | 功能正常（未被本次改动影响） |

- [ ] **Step 4: 处理发现的问题（如有）**

如有不符合项，修复对应文件后重跑该文件所属 Task 的校验命令，并单独提交修复（`git add` 显式指定路径）。

---

## Self-Review 记录

- **Spec 覆盖**：首页完全替换（Task 5）、搜索区日期+流水号+重置语义（Task 4）、汇总卡片含已退款口径（Task 1 SQL 注释 + Task 4）、表格列（Task 4）、明细弹窗四列（Task 4）、分页 10/20/50/100（Task 4）、后端 summary 同口径聚合（Task 1–3）、不改现有接口/其他角色（计划无此类改动）——全部覆盖。
- **占位符扫描**：无 TBD/TODO/略写步骤；所有代码步骤含完整代码。
- **类型一致性**：`SalesSummaryVO` 字段 `count/totalAmount/realAmount` 与 XML resultMap、Service/Controller 签名、前端 `summary.value` 字段一致；接口路径 `/sales/summary`、`/sales/listPage`、`/sales/getDetails` 与现有 Controller 实际路径一致。
