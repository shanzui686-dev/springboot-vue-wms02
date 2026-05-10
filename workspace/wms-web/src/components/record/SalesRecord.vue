<template>
  <div class="sales-record-container">
    <!-- 顶部搜索区 -->
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="流水单号">
          <el-input
            v-model="searchForm.orderNum"
            placeholder="请输入流水单号"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="searchForm.startDate"
            type="date"
            placeholder="开始日期"
            value-format="YYYY-MM-DD"
            :disabled-date="disabledStartDate"
            style="width: 150px"
          />
          <span style="margin: 0 8px">至</span>
          <el-date-picker
            v-model="searchForm.endDate"
            type="date"
            placeholder="结束日期"
            value-format="YYYY-MM-DD"
            :disabled-date="disabledEndDate"
            style="width: 150px"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">
            查询
          </el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 主数据表格 -->
    <el-card class="table-card" shadow="never">
      <el-table
        :data="tableData"
        border
        stripe
        v-loading="loading"
        style="width: 100%"
      >
        <el-table-column prop="orderNum" label="流水单号" width="180" />
        <el-table-column prop="cashierName" label="收银员" width="120" />
        <el-table-column prop="totalAmount" label="应收总额" width="120">
          <template #default="{ row }">
            ¥{{ formatAmount(row.totalAmount) }}
          </template>
        </el-table-column>
        <el-table-column prop="realAmount" label="实收金额" width="120">
          <template #default="{ row }">
            ¥{{ formatAmount(row.realAmount) }}
          </template>
        </el-table-column>
        <el-table-column prop="changeAmount" label="找零" width="100">
          <template #default="{ row }">
            ¥{{ formatAmount(row.changeAmount) }}
          </template>
        </el-table-column>
        <el-table-column label="支付方式" width="120">
          <template #default="{ row }">
            <el-tag type="success">现金</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="结账时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="退货状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.returnStatus === 2" type="success">已退货</el-tag>
            <el-tag v-else-if="row.returnStatus === 1" type="warning">待退款</el-tag>
            <el-tag v-else type="info">未退货</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="220">
          <template #default="{ row }">
            <el-button
              type="primary"
              size="small"
              @click="handleViewDetails(row)"
            >
              查看明细
            </el-button>
            <el-button
              type="danger"
              size="small"
              :disabled="row.returnStatus !== 0"
              @click="handleOpenReturnDialog(row)"
            >
              申请退货
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页组件 -->
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
      width="700px"
      :close-on-click-modal="false"
    >
      <el-table
        :data="detailList"
        border
        stripe
        style="width: 100%"
      >
        <el-table-column prop="goodsName" label="商品名称" min-width="150" />
        <el-table-column prop="price" label="单价" width="100">
          <template #default="{ row }">
            ¥{{ formatAmount(row.price) }}
          </template>
        </el-table-column>
        <el-table-column prop="count" label="数量" width="100" align="center" />
        <el-table-column prop="subtotal" label="小计金额" width="120">
          <template #default="{ row }">
            ¥{{ formatAmount(row.subtotal) }}
          </template>
        </el-table-column>
      </el-table>

      <!-- 总金额汇总 -->
      <div class="detail-summary">
        <el-divider />
        <div class="summary-text">
          总金额：<span class="total-amount">¥{{ formatAmount(currentTotalAmount) }}</span>
        </div>
      </div>

      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 申请退货弹窗 -->
    <el-dialog
      v-model="returnDialogVisible"
      :title="`申请退货 - ${returnOrderNum}`"
      width="800px"
      :close-on-click-modal="false"
    >
      <el-alert
        title="提示：请输入本次退货数量，点击提交后将生成退货申请单"
        type="info"
        :closable="false"
        style="margin-bottom: 15px"
      />

      <!-- 退货商品明细表格 -->
      <el-table
        :data="returnDetailList"
        border
        stripe
        style="width: 100%"
        max-height="400"
      >
        <el-table-column prop="goodsName" label="商品名称" min-width="150" />
        <el-table-column prop="price" label="单价" width="100">
          <template #default="{ row }">
            ¥{{ formatAmount(row.price) }}
          </template>
        </el-table-column>
        <el-table-column prop="count" label="原购买数量" width="120" align="center" />
        <el-table-column label="本次退货数量" width="180" align="center">
          <template #default="{ row }">
            <el-input-number
              v-model="row.returnCount"
              :min="0"
              :max="row.count"
              :precision="0"
              size="small"
              style="width: 100%"
            />
          </template>
        </el-table-column>
        <el-table-column label="小计" width="120" align="right">
          <template #default="{ row }">
            ¥{{ formatAmount(row.price * (row.returnCount || 0)) }}
          </template>
        </el-table-column>
      </el-table>

      <!-- 预计退款总额 -->
      <div class="return-summary">
        <el-divider />
        <div class="summary-text">
          预计退款总额：<span class="total-amount">¥{{ formatAmount(predictedRefundAmount) }}</span>
        </div>
      </div>

      <!-- 退货原因输入框 -->
      <el-form :model="returnForm" style="margin-top: 20px">
        <el-form-item label="退货原因" label-width="100px">
          <el-input
            v-model="returnForm.returnReason"
            type="textarea"
            :rows="3"
            placeholder="请输入退货原因（必填）"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="returnDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitReturn" :loading="returnLoading">
          提交申请
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onActivated, getCurrentInstance, computed } from 'vue'
import { Search } from '@element-plus/icons-vue'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'

defineOptions({
  name: 'SalesRecord'
})

const { proxy } = getCurrentInstance()
const httpUrl = proxy.$httpUrl
const $message = ElMessage

// ==================== 数据定义 ====================

// 搜索表单
const searchForm = ref({
  orderNum: '',
  startDate: '',
  endDate: ''
})

// 默认时间范围（今天 00:00:00 到 23:59:59）
const defaultTime = [
  new Date(2000, 1, 1, 0, 0, 0), // 开始时间 00:00:00
  new Date(2000, 1, 1, 23, 59, 59) // 结束时间 23:59:59
]

/**
 * 禁用开始日期：不能选择未来日期
 */
const disabledStartDate = (time) => {
  return time.getTime() > Date.now()
}

/**
 * 禁用结束日期：不能选择未来日期，且不能小于开始日期
 */
const disabledEndDate = (time) => {
  const now = Date.now()
  const startDate = searchForm.value.startDate
  
  // 禁用未来日期
  if (time.getTime() > now) {
    return true
  }
  
  // 如果已选择开始日期，禁用开始日期之前的日期
  if (startDate) {
    const startTimestamp = new Date(startDate).getTime()
    return time.getTime() < startTimestamp
  }
  
  return false
}
const tableData = ref([])
const loading = ref(false)
const pageSize = ref(10)
const pageNum = ref(1)
const total = ref(0)

// 明细弹窗
const detailDialogVisible = ref(false)
const detailList = ref([])
const currentOrderNum = ref('')
const currentTotalAmount = ref(0)

// 退货弹窗
const returnDialogVisible = ref(false)
const returnOrderNum = ref('')
const returnDetailList = ref([])
const returnLoading = ref(false)
const currentSalesId = ref(null)

// 退货表单
const returnForm = ref({
  returnReason: ''
})

// ==================== 计算属性 ====================

/**
 * 计算预计退款总额
 */
const predictedRefundAmount = computed(() => {
  return returnDetailList.value.reduce((total, item) => {
    const count = item.returnCount || 0
    return total + (item.price * count)
  }, 0)
})

// ==================== 方法定义 ====================

/**
 * 格式化金额（保留两位小数）
 */
const formatAmount = (amount) => {
  if (!amount && amount !== 0) return '0.00'
  return Number(amount).toFixed(2)
}

/**
 * 格式化日期时间
 */
const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  const date = new Date(dateTime)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

/**
 * 初始化时间范围为今天
 */
const initDateRange = () => {
  const today = new Date()
  const year = today.getFullYear()
  const month = String(today.getMonth() + 1).padStart(2, '0')
  const day = String(today.getDate()).padStart(2, '0')
  
  // 设置为今天的日期
  searchForm.value.startDate = `${year}-${month}-${day}`
  searchForm.value.endDate = `${year}-${month}-${day}`
}

/**
 * 加载销售单列表
 */
const loadSalesList = () => {
  loading.value = true
  
  // 构建请求参数
  const params = {
    page: pageNum.value,
    limit: pageSize.value
  }
  
  // 添加流水单号（如果存在）
  if (searchForm.value.orderNum) {
    params.orderNum = searchForm.value.orderNum
  }
  
  // 添加时间范围（如果存在）
  if (searchForm.value.startDate) {
    params.startDate = searchForm.value.startDate + ' 00:00:00'
  }
  if (searchForm.value.endDate) {
    params.endDate = searchForm.value.endDate + ' 23:59:59'
  }
  
  axios.get(httpUrl + '/sales/listPage', { params })
    .then(res => {
      const result = res.data
      if (result.code === 200) {
        tableData.value = result.data.records || []
        total.value = result.data.total || 0
      } else {
        $message.error(result.msg || '获取数据失败')
      }
    })
    .catch(error => {
      console.error('请求失败:', error)
      $message.error('请求失败')
    })
    .finally(() => {
      loading.value = false
    })
}

/**
 * 查询按钮点击
 */
const handleSearch = () => {
  pageNum.value = 1
  loadSalesList()
}

/**
 * 重置按钮点击
 */
const handleReset = () => {
  searchForm.value.orderNum = ''
  initDateRange() // 重置为今天的日期范围
  pageNum.value = 1
  loadSalesList()
}

/**
 * 分页大小改变
 */
const handleSizeChange = (val) => {
  pageSize.value = val
  pageNum.value = 1
  loadSalesList()
}

/**
 * 页码改变
 */
const handleCurrentChange = (val) => {
  pageNum.value = val
  loadSalesList()
}

/**
 * 查看明细
 */
const handleViewDetails = (row) => {
  currentOrderNum.value = row.orderNum || '未知单号'
  currentTotalAmount.value = row.totalAmount || 0
  
  // 调用接口获取明细
  axios.get(httpUrl + '/sales/getDetails', {
    params: { salesId: row.id }
  })
    .then(res => {
      const result = res.data
      if (result.code === 200) {
        detailList.value = result.data || []
        detailDialogVisible.value = true
      } else {
        $message.error(result.msg || '获取明细失败')
      }
    })
    .catch(error => {
      console.error('请求失败:', error)
      $message.error('请求失败')
    })
}

/**
 * 打开退货申请弹窗
 */
const handleOpenReturnDialog = (row) => {
  currentSalesId.value = row.id
  returnOrderNum.value = row.orderNum || '未知单号'
  
  // 调用接口获取明细
  axios.get(httpUrl + '/sales/getDetails', {
    params: { salesId: row.id }
  })
    .then(res => {
      const result = res.data
      if (result.code === 200) {
        // 初始化退货数量字段
        returnDetailList.value = (result.data || []).map(item => ({
          ...item,
          returnCount: 0 // 默认退货数量为 0
        }))
        // 重置表单
        returnForm.value.returnReason = ''
        returnDialogVisible.value = true
      } else {
        $message.error(result.msg || '获取明细失败')
      }
    })
    .catch(error => {
      console.error('请求失败:', error)
      $message.error('请求失败')
    })
}

/**
 * 提交退货申请
 */
const handleSubmitReturn = () => {
  // 校验退货原因
  if (!returnForm.value.returnReason || returnForm.value.returnReason.trim() === '') {
    $message.warning('请输入退货原因')
    return
  }
  
  // 过滤出退货数量大于 0 的商品
  const items = returnDetailList.value
    .filter(item => item.returnCount > 0)
    .map(item => ({
      goodsId: item.goodsId,
      returnCount: item.returnCount
    }))
  
  if (items.length === 0) {
    $message.warning('请至少选择一个商品并输入退货数量')
    return
  }
  
  // 构建请求数据
  const requestData = {
    salesId: currentSalesId.value,
    returnReason: returnForm.value.returnReason,
    items: items
  }
  
  returnLoading.value = true
  
  // 调用后端接口
  axios.post(httpUrl + '/return/apply', requestData)
    .then(res => {
      const result = res.data
      if (result.code === 200) {
        $message.success('退货申请提交成功')
        returnDialogVisible.value = false
        // 可选：刷新列表
        loadSalesList()
      } else {
        $message.error(result.msg || '退货申请失败')
      }
    })
    .catch(error => {
      console.error('请求失败:', error)
      $message.error('请求失败')
    })
    .finally(() => {
      returnLoading.value = false
    })
}

// ==================== 生命周期 ====================

onMounted(() => {
  // 初始化时间范围为今天
  initDateRange()
  // 加载销售单列表
  loadSalesList()
})

// keep-alive 激活时触发（切换回页面时）
onActivated(() => {
  // 如果表格数据为空，重新加载数据
  if (tableData.value.length === 0 && !loading.value) {
    loadSalesList()
  }
})
</script>

<style scoped>
.sales-record-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.search-card {
  margin-bottom: 20px;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
}

.table-card {
  min-height: 500px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.detail-summary {
  margin-top: 20px;
}

.summary-text {
  text-align: right;
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.total-amount {
  color: #f56c6c;
  font-size: 18px;
}
</style>
