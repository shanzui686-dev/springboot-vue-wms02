<template>
  <div class="cashier-today-sales">
    <!-- 顶部统计卡片（仿店长统计页风格） -->
    <el-row :gutter="20" class="stats-cards">
      <el-col :span="8">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon order-icon">
              <el-icon :size="40"><Tickets /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">{{ searchDate ? '今日销售笔数' : '销售笔数' }}</div>
              <div class="stat-value">
                <count-to :end-val="summary.count" :duration="2000" :decimals="0" />
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon sales-icon">
              <el-icon :size="40"><Money /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">{{ searchDate ? '今日应收总额' : '应收总额' }}</div>
              <div class="stat-value">
                <count-to :end-val="summary.totalAmount" :duration="2000" prefix="¥" />
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon net-icon">
              <el-icon :size="40"><Coin /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">{{ searchDate ? '今日实收总额' : '实收总额' }}</div>
              <div class="stat-value">
                <count-to :end-val="summary.realAmount" :duration="2000" prefix="¥" />
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 销售记录 -->
    <el-row :gutter="20" class="tables-section">
      <el-col :span="24">
        <el-card class="table-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>{{ searchDate ? '今日销售记录' : '全部销售记录' }}</span>
              <!-- 查询区 -->
              <div class="header-search">
                <el-date-picker
                  v-model="searchDate"
                  type="date"
                  placeholder="选择日期"
                  value-format="YYYY-MM-DD"
                  clearable
                  style="width: 150px"
                />
                <el-input
                  v-model="searchOrderNum"
                  placeholder="流水单号"
                  clearable
                  style="width: 170px"
                />
                <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
                <el-button @click="handleReset">重置</el-button>
              </div>
            </div>
          </template>

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
            <el-table-column label="退货状态" width="100" align="center">
              <template #default="{ row }">
                <el-tag v-if="row.returnStatus === 2" type="success">已退货</el-tag>
                <el-tag v-else-if="row.returnStatus === 1" type="warning">待退款</el-tag>
                <el-tag v-else type="info">未退货</el-tag>
              </template>
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
      </el-col>
    </el-row>

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
        <el-table-column label="明细状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'danger' : row.status === 2 ? 'warning' : 'success'">
              {{ row.status === 1 ? '退货' : row.status === 2 ? '换货' : '正常' }}
            </el-tag>
          </template>
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
import { Search, Money, Coin, Tickets } from '@element-plus/icons-vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const { proxy } = getCurrentInstance()
const httpUrl = proxy.$httpUrl

// 数字动画组件（与店长统计页 Dashboard.vue 同款）
const CountTo = {
  props: {
    endVal: { type: Number, default: 0 },
    duration: { type: Number, default: 2000 },
    prefix: { type: String, default: '' },
    decimals: { type: Number, default: 2 }
  },
  data() {
    return {
      displayValue: 0
    }
  },
  mounted() {
    this.animateNumber()
  },
  watch: {
    endVal() {
      this.displayValue = 0
      this.animateNumber()
    }
  },
  methods: {
    animateNumber() {
      const startTime = performance.now()
      const startValue = 0
      const endValue = this.endVal
      const duration = this.duration

      const step = (currentTime) => {
        const elapsed = currentTime - startTime
        const progress = Math.min(elapsed / duration, 1)

        // 使用缓动函数
        const easeOutQuart = 1 - Math.pow(1 - progress, 4)
        this.displayValue = startValue + (endValue - startValue) * easeOutQuart

        if (progress < 1) {
          requestAnimationFrame(step)
        } else {
          this.displayValue = endValue
        }
      }

      requestAnimationFrame(step)
    }
  },
  render() {
    const value = this.displayValue.toFixed(this.decimals)
    return `${this.prefix}${value}`
  }
}

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
  background-color: #f0f2f5;
  min-height: calc(100vh - 60px);
}

/* 统计卡片样式（与店长统计页一致） */
.stats-cards {
  margin-bottom: 20px;
}

.stat-card {
  border-radius: 8px;
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 20px;
}

.stat-icon {
  width: 70px;
  height: 70px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.sales-icon {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.net-icon {
  background: linear-gradient(135deg, #a8edea 0%, #fed6e3 100%);
  color: #409EFF;
}

.order-icon {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.stat-info {
  flex: 1;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
}

/* 表格区域样式 */
.tables-section {
  margin-bottom: 20px;
}

.table-card {
  border-radius: 8px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
  font-size: 16px;
  flex-wrap: wrap;
  gap: 10px;
}

.header-search {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: normal;
}

.pagination {
  margin-top: 12px;
}

/* 响应式调整 */
@media (max-width: 1200px) {
  .stat-value {
    font-size: 24px;
  }
}
</style>
