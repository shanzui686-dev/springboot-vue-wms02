<template>
  <div class="purchase-return-review-container">
    <div style="margin-bottom: 10px; font-size: 16px; font-weight: bold;">采购退回审核</div>

    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="退回单号">
          <el-input v-model="searchForm.returnNo" placeholder="请输入退回单号" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 150px">
            <el-option label="待退回" :value="1" />
            <el-option label="已退回" :value="2" />
            <el-option label="不退回" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table :data="tableData" border stripe v-loading="loading" style="width: 100%">
        <el-table-column prop="returnNo" label="退回单号" width="180" />
        <el-table-column prop="purchaseNo" label="原采购单号" width="180" />
        <el-table-column prop="goodsName" label="商品名" width="120" />
        <el-table-column prop="batchNo" label="批次号" width="150" />
        <el-table-column prop="returnCount" label="退回数量" width="100" align="center" />
        <el-table-column prop="reason" label="退回原因" min-width="160" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="creatorName" label="申请人" width="100" />
        <el-table-column prop="auditorName" label="审核人" width="100" />
        <el-table-column prop="createTime" label="申请时间" width="170">
          <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="180" align="center">
          <template #default="{ row }">
            <el-button v-if="row.status === 1" type="success" size="small" @click="handleApprove(row)">审核通过</el-button>
            <el-button v-if="row.status === 1" type="danger" size="small" @click="handleReject(row)">审核拒绝</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="pageNum" v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]" :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange" @current-change="handleCurrentChange" class="pagination"
      />
    </el-card>
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
const user = JSON.parse(sessionStorage.getItem('user') || '{}')

// ==================== 数据定义 ====================

const searchForm = ref({ returnNo: '', status: null })
const tableData = ref([])
const loading = ref(false)
const pageSize = ref(10)
const pageNum = ref(1)
const total = ref(0)

// ==================== 方法定义 ====================

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

const getStatusType = (s) => ({ 1: 'warning', 2: 'success', 3: 'danger' }[s] || 'info')
const getStatusText = (s) => ({ 1: '待退回', 2: '已退回', 3: '不退回' }[s] || '未知')

const loadList = () => {
  loading.value = true
  console.log('加载采购退回列表...')
  axios.post(httpUrl + '/purchaseReturn/listPage', {
    pagenum: pageNum.value,
    pagesize: pageSize.value,
    param: {
      returnNo: searchForm.value.returnNo || undefined,
      status: searchForm.value.status
    }
  }).then(res => {
    console.log('采购退回列表响应:', res.data)
    if (res.data.code === 200) {
      tableData.value = res.data.data || []
      total.value = res.data.total || 0
      console.log('加载到', tableData.value.length, '条记录')
    } else {
      $message.error(res.data.msg || '获取数据失败')
    }
  }).catch(error => {
    console.error('加载失败:', error)
    $message.error('加载失败')
  }).finally(() => {
    loading.value = false
  })
}

const handleSearch = () => {
  pageNum.value = 1
  loadList()
}

const handleReset = () => {
  searchForm.value.returnNo = ''
  searchForm.value.status = null
  pageNum.value = 1
  loadList()
}

const handleSizeChange = (val) => {
  pageSize.value = val
  pageNum.value = 1
  loadList()
}

const handleCurrentChange = (val) => {
  pageNum.value = val
  loadList()
}

const handleApprove = (row) => {
  ElMessageBox.confirm(`确定审核通过退回单「${row.returnNo}」吗？`, '审核采购退回', { type: 'warning' })
    .then(() => {
      console.log('审核通过:', row.id, user.id)
      axios.post(httpUrl + '/purchaseReturn/approve', null, { params: { returnId: row.id, auditorId: user.id } })
        .then(res => {
          console.log('审核通过响应:', res.data)
          if (res.data.code === 200) {
            $message.success('审核通过，采购单状态已更新')
            loadList()
          } else {
            $message.error(res.data.msg || '操作失败')
          }
        }).catch(error => {
          console.error('审核通过失败:', error)
          $message.error('请求失败：' + (error.response?.data?.msg || error.message))
        })
    }).catch(() => {})
}

const handleReject = (row) => {
  ElMessageBox.confirm(`确定拒绝退回单「${row.returnNo}」吗？`, '审核拒绝', { type: 'warning' })
    .then(() => {
      console.log('审核拒绝:', row.id, user.id)
      axios.post(httpUrl + '/purchaseReturn/reject', null, { params: { returnId: row.id, auditorId: user.id } })
        .then(res => {
          console.log('审核拒绝响应:', res.data)
          if (res.data.code === 200) {
            $message.success('已拒绝，采购单状态已更新')
            loadList()
          } else {
            $message.error(res.data.msg || '操作失败')
          }
        }).catch(error => {
          console.error('审核拒绝失败:', error)
          $message.error('请求失败：' + (error.response?.data?.msg || error.message))
        })
    }).catch(() => {})
}

// ==================== 生命周期 ====================

onMounted(() => {
  loadList()
})
</script>

<style scoped>
.purchase-return-review-container {
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
</style>
