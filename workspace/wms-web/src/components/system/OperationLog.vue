<script setup>
import { ref, onMounted, getCurrentInstance } from 'vue'
import { Search, Refresh, View } from '@element-plus/icons-vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const { proxy } = getCurrentInstance()
const httpUrl = proxy.$httpUrl
const $message = ElMessage

// 搜索条件
const searchForm = ref({
  username: '',
  startDate: '',
  endDate: ''
})

// 初始化时间范围为今天
const initDateRange = () => {
  const today = new Date()
  const year = today.getFullYear()
  const month = String(today.getMonth() + 1).padStart(2, '0')
  const day = String(today.getDate()).padStart(2, '0')
  
  searchForm.value.startDate = `${year}-${month}-${day}`
  searchForm.value.endDate = `${year}-${month}-${day}`
}

// 表格数据
const tableData = ref([])
const loading = ref(false)
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

// 详情对话框
const detailDialogVisible = ref(false)
const currentLog = ref({})

// 查询日志列表
const loadLogs = () => {
  loading.value = true
  
  // 处理时间范围
  let startTime = ''
  let endTime = ''
  if (searchForm.value.startDate) {
    startTime = searchForm.value.startDate + ' 00:00:00'
  }
  if (searchForm.value.endDate) {
    endTime = searchForm.value.endDate + ' 23:59:59'
  }
  
  axios.get(httpUrl + '/log/listPage', {
    params: {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      username: searchForm.value.username || undefined,
      startTime: startTime || undefined,
      endTime: endTime || undefined
    }
  }).then(res => {
    const result = res.data
    if (result.code === 200) {
      tableData.value = result.data || []
      total.value = result.total || 0
    } else {
      $message.error('获取日志失败')
    }
  }).catch(error => {
    console.error('请求失败:', error)
    $message.error('请求失败：' + error.message)
  }).finally(() => {
    loading.value = false
  })
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return ''
  const d = new Date(date)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

// 禁用开始日期：不能选择未来日期
const disabledStartDate = (time) => {
  return time.getTime() > Date.now()
}

// 禁用结束日期：不能选择未来日期，且不能小于开始日期
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

// 格式化日期时间显示
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

// 格式化JSON参数
const formatJson = (jsonStr) => {
  if (!jsonStr) return '-'
  try {
    const obj = JSON.parse(jsonStr)
    return JSON.stringify(obj, null, 2)
  } catch (e) {
    return jsonStr
  }
}

// 查看详情
const viewDetail = (row) => {
  currentLog.value = { ...row }
  detailDialogVisible.value = true
}

// 查询按钮
const handleSearch = () => {
  pageNum.value = 1
  loadLogs()
}

// 重置按钮
const handleReset = () => {
  searchForm.value.username = ''
  initDateRange()
  pageNum.value = 1
  loadLogs()
}

// 分页大小改变
const handleSizeChange = (val) => {
  pageSize.value = val
  pageNum.value = 1
  loadLogs()
}

// 当前页改变
const handleCurrentChange = (val) => {
  pageNum.value = val
  loadLogs()
}

// 组件挂载时加载数据
onMounted(() => {
  initDateRange()
  loadLogs()
})
</script>

<template>
  <div class="operation-log-container">
    <!-- 顶部搜索区 -->
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="操作人">
          <el-input
            v-model="searchForm.username"
            placeholder="请输入操作人"
            clearable
            style="width: 200px"
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        
        <el-form-item label="操作时间">
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
          <el-button :icon="Refresh" @click="handleReset">
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 主表格 -->
    <el-card class="table-card" shadow="never">
      <el-table
        :data="tableData"
        style="width: 100%"
        :header-cell-style="{ background: '#f5f7fa', color: '#606266' }"
        border
        stripe
        v-loading="loading"
      >
        <el-table-column prop="id" label="ID" width="80" align="center" />
        
        <el-table-column prop="username" label="操作人" width="120" align="center" />
        
        <el-table-column prop="operation" label="操作描述" width="150" show-overflow-tooltip />
        
        <el-table-column prop="method" label="请求方法" min-width="250" show-overflow-tooltip>
          <template #default="scope">
            <span class="method-text">{{ scope.row.method }}</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="params" label="请求参数" width="150" align="center">
          <template #default="scope">
            <el-button
              v-if="scope.row.params && scope.row.params !== '[]'"
              type="primary"
              link
              :icon="View"
              @click="viewDetail(scope.row)"
            >
              查看详情
            </el-button>
            <span v-else>-</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="ip" label="操作IP" width="140" align="center" />
        
        <el-table-column prop="executionTime" label="执行耗时" width="100" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.executionTime > 1000 ? 'danger' : scope.row.executionTime > 500 ? 'warning' : 'success'">
              {{ scope.row.executionTime }} ms
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="createTime" label="操作时间" width="180" align="center">
          <template #default="scope">
            {{ formatDateTime(scope.row.createTime) }}
          </template>
        </el-table-column>
      </el-table>

      <!-- 底部分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 查看详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="请求参数详情"
      width="60%"
      :close-on-click-modal="false"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="操作人">
          {{ currentLog.username }}
        </el-descriptions-item>
        <el-descriptions-item label="操作描述">
          {{ currentLog.operation }}
        </el-descriptions-item>
        <el-descriptions-item label="请求方法" :span="2">
          {{ currentLog.method }}
        </el-descriptions-item>
        <el-descriptions-item label="操作IP">
          {{ currentLog.ip }}
        </el-descriptions-item>
        <el-descriptions-item label="执行耗时">
          {{ currentLog.executionTime }} ms
        </el-descriptions-item>
        <el-descriptions-item label="操作时间" :span="2">
          {{ formatDateTime(currentLog.createTime) }}
        </el-descriptions-item>
      </el-descriptions>
      
      <div class="params-section">
        <div class="params-title">请求参数：</div>
        <pre class="json-content">{{ formatJson(currentLog.params) }}</pre>
      </div>
      
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.operation-log-container {
  padding: 20px;
}

.search-card {
  margin-bottom: 20px;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
}

.table-card {
  min-height: 500px;
}

.method-text {
  font-family: 'Courier New', monospace;
  font-size: 12px;
  color: #409eff;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.params-section {
  margin-top: 20px;
}

.params-title {
  font-weight: bold;
  margin-bottom: 10px;
  color: #606266;
}

.json-content {
  background-color: #f5f7fa;
  padding: 15px;
  border-radius: 4px;
  font-family: 'Courier New', monospace;
  font-size: 13px;
  line-height: 1.6;
  max-height: 400px;
  overflow-y: auto;
  white-space: pre-wrap;
  word-wrap: break-word;
  border: 1px solid #dcdfe6;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .search-form {
    flex-direction: column;
  }
  
  .search-form :deep(.el-form-item) {
    margin-right: 0;
    width: 100%;
  }
  
  .search-form :deep(.el-input),
  .search-form :deep(.el-date-picker) {
    width: 100% !important;
  }
}
</style>
