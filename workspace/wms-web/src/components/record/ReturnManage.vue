<template>
  <div class="return-manage-container">
    <!-- 顶部搜索区 -->
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="退货单号">
          <el-input
            v-model="searchForm.returnNo"
            placeholder="请输入退货单号"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        
        <el-form-item label="状态">
          <el-select
            v-model="searchForm.status"
            placeholder="请选择状态"
            clearable
            style="width: 150px"
          >
            <el-option label="待退款" :value="0" />
            <el-option label="已退款" :value="1" />
          </el-select>
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
        <el-table-column prop="returnNo" label="退货单号" width="200" />
        <el-table-column prop="salesOrderNum" label="原销售单号" width="180" />
        <el-table-column prop="returnAmount" label="退款总额" width="120">
          <template #default="{ row }">
            ¥{{ formatAmount(row.returnAmount) }}
          </template>
        </el-table-column>
        <el-table-column prop="returnReason" label="退货原因" min-width="200" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="cashierName" label="原收银员" width="120" />
        <el-table-column prop="createTime" label="申请时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="180" align="center">
          <template #default="{ row }">
            <el-button
              type="primary"
              size="small"
              @click="handleViewDetails(row)"
            >
              查看明细
            </el-button>
            <el-button
              v-if="row.status === 0"
              type="danger"
              size="small"
              @click="handleConfirmRefund(row)"
            >
              确认退款
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

    <!-- 退货明细弹窗 -->
    <el-dialog
      v-model="detailDialogVisible"
      :title="`退货明细 - ${currentReturnNo}`"
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
        <el-table-column prop="price" label="退货单价" width="120">
          <template #default="{ row }">
            ¥{{ formatAmount(row.price) }}
          </template>
        </el-table-column>
        <el-table-column prop="returnCount" label="退货数量" width="120" align="center" />
        <el-table-column label="小计金额" width="120">
          <template #default="{ row }">
            ¥{{ formatAmount(row.subtotal) }}
          </template>
        </el-table-column>
      </el-table>

      <!-- 总金额汇总 -->
      <div class="detail-summary">
        <el-divider />
        <div class="summary-text">
          退款总额：<span class="total-amount">¥{{ formatAmount(currentTotalAmount) }}</span>
        </div>
      </div>

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
import { ElMessage, ElMessageBox } from 'element-plus'

const { proxy } = getCurrentInstance()
const httpUrl = proxy.$httpUrl
const $message = ElMessage

// ==================== 数据定义 ====================

// 搜索表单
const searchForm = ref({
  returnNo: '',
  status: null
})

// 表格数据
const tableData = ref([])
const loading = ref(false)
const pageSize = ref(10)
const pageNum = ref(1)
const total = ref(0)

// 明细弹窗
const detailDialogVisible = ref(false)
const detailList = ref([])
const currentReturnNo = ref('')
const currentTotalAmount = ref(0)

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
 * 获取状态标签类型
 */
const getStatusType = (status) => {
  const typeMap = {
    0: 'warning',  // 待退款 - 黄色
    1: 'success'   // 已退款 - 绿色
  }
  return typeMap[status] || 'info'
}

/**
 * 获取状态文本
 */
const getStatusText = (status) => {
  const textMap = {
    0: '待退款',
    1: '已退款'
  }
  return textMap[status] || '未知'
}

/**
 * 加载退货单列表
 */
const loadReturnList = () => {
  loading.value = true
  
  // 构建请求参数
  const params = {
    page: pageNum.value,
    limit: pageSize.value
  }
  
  // 添加退货单号（如果存在）
  if (searchForm.value.returnNo) {
    params.returnNo = searchForm.value.returnNo
  }
  
  // 添加状态（如果存在）
  if (searchForm.value.status !== null && searchForm.value.status !== undefined) {
    params.status = searchForm.value.status
  }
  
  axios.get(httpUrl + '/return/listPage', { params })
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
  loadReturnList()
}

/**
 * 重置按钮点击
 */
const handleReset = () => {
  searchForm.value.returnNo = ''
  searchForm.value.status = null
  pageNum.value = 1
  loadReturnList()
}

/**
 * 分页大小改变
 */
const handleSizeChange = (val) => {
  pageSize.value = val
  pageNum.value = 1
  loadReturnList()
}

/**
 * 页码改变
 */
const handleCurrentChange = (val) => {
  pageNum.value = val
  loadReturnList()
}

/**
 * 查看退货明细
 */
const handleViewDetails = (row) => {
  currentReturnNo.value = row.returnNo || '未知单号'
  currentTotalAmount.value = row.returnAmount || 0
  
  // TODO: 需要后端提供查询退货明细的接口
  // 这里暂时模拟数据，实际应该调用后端接口
  // axios.get(httpUrl + '/return/getDetails', { params: { returnId: row.id } })
  
  // 模拟数据（临时）
  detailList.value = []
  detailDialogVisible.value = true
  
  // 实际使用时取消下面的注释并删除上面的模拟代码
  /*
  axios.get(httpUrl + '/return/getDetails', {
    params: { returnId: row.id }
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
  */
}

/**
 * 确认退款
 */
const handleConfirmRefund = (row) => {
  const refundAmount = formatAmount(row.returnAmount)
  
  ElMessageBox.confirm(
    `确定已将 ¥${refundAmount} 退还给顾客吗？退款后将自动回滚商品库存。`,
    '确认退款',
    {
      confirmButtonText: '确定退款',
      cancelButtonText: '取消',
      type: 'warning',
    }
  )
    .then(() => {
      // 调用后端确认退款接口
      axios.post(httpUrl + '/return/confirm', null, {
        params: { returnId: row.id }
      })
        .then(res => {
          const result = res.data
          if (result.code === 200) {
            $message.success('确认退款成功，库存已回滚')
            // 刷新列表
            loadReturnList()
          } else {
            $message.error(result.msg || '确认退款失败')
          }
        })
        .catch(error => {
          console.error('请求失败:', error)
          $message.error('请求失败')
        })
    })
    .catch(() => {
      // 用户取消操作
      $message.info('已取消退款')
    })
}

// ==================== 生命周期 ====================

onMounted(() => {
  // 加载退货单列表
  loadReturnList()
})
</script>

<style scoped>
.return-manage-container {
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
