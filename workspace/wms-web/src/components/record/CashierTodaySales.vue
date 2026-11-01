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
