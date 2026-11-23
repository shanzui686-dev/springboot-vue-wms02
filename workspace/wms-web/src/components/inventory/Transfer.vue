<template>
  <div class="inventory-transfer-container">
    <!-- 搜索栏 -->
    <div class="search-section">
      <el-input v-model="search.transferNo" placeholder="调拨单号" clearable style="width: 200px;" @keyup.enter="loadTransferList" />
      <el-select v-model="search.status" placeholder="状态" clearable style="width: 140px; margin-left: 10px;">
        <el-option label="待审核" :value="0" />
        <el-option label="已通过" :value="1" />
        <el-option label="已拒绝" :value="2" />
      </el-select>
      <el-button type="primary" style="margin-left: 10px;" @click="loadTransferList">查询</el-button>
      <el-button @click="resetSearch">重置</el-button>
      <el-button type="primary" style="float: right;" @click="openTransferDialog">
        <el-icon><Upload /></el-icon>
        创建调拨申请
      </el-button>
    </div>

    <!-- 表格 -->
    <el-card class="table-card" shadow="never">
      <template #header>
        <div class="card-header">调拨记录</div>
      </template>

      <el-table :data="tableData" stripe border v-loading="loading">
        <el-table-column prop="transferNo" label="调拨单号" width="180" />
        <el-table-column label="商品">
          <template #default="{ row }">
            {{ row.goodsName || '商品' + row.goodsId }}
          </template>
        </el-table-column>
        <el-table-column label="调出仓库" width="120">
          <template #default="{ row }">
            {{ row.fromStorageName || '仓库' + row.fromStorageId }}
          </template>
        </el-table-column>
        <el-table-column label="调入仓库" width="120">
          <template #default="{ row }">
            {{ row.toStorageName || '仓库' + row.toStorageId }}
          </template>
        </el-table-column>
        <el-table-column prop="count" label="调拨数量" width="100" align="center">
          <template #default="{ row }">
            <el-tag type="primary">{{ row.count }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="调拨时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" show-overflow-tooltip />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <template v-if="row.status === 0">
              <el-button size="small" type="success" @click="handleAudit(row)">通过</el-button>
              <el-button size="small" type="danger" @click="handleReject(row)">拒绝</el-button>
            </template>
            <span v-else style="color: #909399;">-</span>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        class="pagination"
        v-model:current-page="page.pageNum"
        v-model:page-size="page.pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadTransferList"
        @current-change="loadTransferList"
      />
    </el-card>

    <!-- 创建调拨弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      title="创建调拨申请"
      width="600px"
      :close-on-click-modal="false"
      @close="handleDialogClose"
    >
      <el-form ref="transferFormRef" :model="transferForm" :rules="rules" label-width="100px">
        <el-form-item label="调出仓库" prop="fromStorageId">
          <el-select v-model="transferForm.fromStorageId" placeholder="请选择调出仓库" style="width: 100%" @change="handleFromStorageChange">
            <el-option v-for="s in storageList" :key="s.id" :label="s.name" :value="s.id" />
          </el-select>
        </el-form-item>

        <el-form-item label="调入仓库" prop="toStorageId">
          <el-select v-model="transferForm.toStorageId" placeholder="请选择调入仓库" style="width: 100%" :disabled="!transferForm.fromStorageId">
            <el-option v-for="s in storageList.filter(s => s.id !== transferForm.fromStorageId)" :key="s.id" :label="s.name" :value="s.id" />
          </el-select>
        </el-form-item>

        <el-form-item label="选择商品" prop="goodsId">
          <el-select v-model="transferForm.goodsId" placeholder="请选择商品" style="width: 100%" :disabled="!transferForm.fromStorageId" @change="handleGoodsChange">
            <el-option v-for="g in availableGoodsList" :key="g.id" :label="`${g.name} (可用: ${g.batchCount || 0})`" :value="g.id" />
          </el-select>
        </el-form-item>

        <el-form-item label="调拨数量" prop="count">
          <el-input-number v-model="transferForm.count" :min="1" :max="maxTransferCount" :step="1" style="width: 100%" :disabled="!transferForm.goodsId" />
          <div v-if="selectedGoods" class="stock-info">
            <el-text type="info">可用库存：{{ selectedGoods.batchCount || 0 }}</el-text>
          </div>
        </el-form-item>

        <el-form-item label="备注">
          <el-input v-model="transferForm.remark" type="textarea" :rows="3" placeholder="选填" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitTransfer" :loading="submitting">提交申请</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onActivated } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Upload } from '@element-plus/icons-vue'
import request from '@/utils/request'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const tableData = ref([])
const total = ref(0)
const storageList = ref([])
const availableGoodsList = ref([])
const selectedGoods = ref(null)
const maxTransferCount = ref(999999)

const page = reactive({ pageNum: 1, pageSize: 10 })
const search = reactive({ transferNo: '', status: '' })

const transferFormRef = ref(null)
const transferForm = reactive({
  fromStorageId: null,
  toStorageId: null,
  goodsId: null,
  count: 1,
  remark: ''
})

const rules = {
  fromStorageId: [{ required: true, message: '请选择调出仓库', trigger: 'change' }],
  toStorageId: [{ required: true, message: '请选择调入仓库', trigger: 'change' }],
  goodsId: [{ required: true, message: '请选择商品', trigger: 'change' }],
  count: [{ required: true, message: '请输入调拨数量', trigger: 'blur' }]
}

const statusType = (s) => ({ 0: 'warning', 1: 'success', 2: 'danger' }[s] || 'info')
const statusText = (s) => ({ 0: '待审核', 1: '已通过', 2: '已拒绝' }[s] || '未知')
const formatTime = (t) => t ? new Date(t).toLocaleString('zh-CN') : '-'

const resetSearch = () => {
  search.transferNo = ''
  search.status = ''
  loadTransferList()
}

onMounted(() => { initPage() })
onActivated(() => { initPage() })

const initPage = () => {
  loadStorageList()
  loadTransferList()
}

const loadStorageList = async () => {
  try {
    const res = await request.post('/storage/listPage', { pagenum: 1, pagesize: 100, param: {} })
    if (res.data.code === 0 || res.data.code === 200) {
      storageList.value = res.data.data?.records || res.data.data || []
    }
  } catch (e) { console.error('加载仓库失败', e) }
}

const handleFromStorageChange = async () => {
  transferForm.toStorageId = null
  transferForm.goodsId = null
  transferForm.count = 1
  availableGoodsList.value = []
  selectedGoods.value = null
  maxTransferCount.value = 999999
  if (!transferForm.fromStorageId) return

  try {
    const res = await request.get('/goods/listByStorage', { params: { storageId: transferForm.fromStorageId } })
    if (res.data.code === 0 || res.data.code === 200) {
      const records = res.data.data || []
      availableGoodsList.value = records.filter(g => g.batchCount > 0)
    }
  } catch (e) { ElMessage.error('加载商品失败') }
}

const handleGoodsChange = () => {
  selectedGoods.value = availableGoodsList.value.find(g => g.id === transferForm.goodsId)
  if (selectedGoods.value) {
    maxTransferCount.value = selectedGoods.value.batchCount || 0
    transferForm.count = 1
  }
}

const loadTransferList = async () => {
  loading.value = true
  try {
    const param = {}
    if (search.transferNo) param.transferNo = search.transferNo
    if (search.status !== '' && search.status !== null && search.status !== undefined) param.status = search.status
    const res = await request.post('/inventoryTransfer/listPage', { pagenum: page.pageNum, pagesize: page.pageSize, param })
    if (res.data.code === 0 || res.data.code === 200) {
      tableData.value = res.data.data?.records || res.data.data || []
      total.value = res.data.data?.total || res.data.total || 0
    } else {
      ElMessage.error(res.data.msg || '加载失败')
    }
  } catch (e) { ElMessage.error('加载失败') }
  finally { loading.value = false }
}

const openTransferDialog = () => { dialogVisible.value = true; loadStorageList() }

const handleDialogClose = () => {
  transferFormRef.value?.resetFields()
  transferForm.fromStorageId = null
  transferForm.toStorageId = null
  transferForm.goodsId = null
  transferForm.count = 1
  transferForm.remark = ''
  availableGoodsList.value = []
  selectedGoods.value = null
  maxTransferCount.value = 999999
}

const handleSubmitTransfer = async () => {
  try { await transferFormRef.value.validate() } catch { return }
  if (transferForm.fromStorageId === transferForm.toStorageId) {
    ElMessage.warning('调出仓库和调入仓库不能相同')
    return
  }
  submitting.value = true
  try {
    const user = JSON.parse(localStorage.getItem('user') || '{}')
    const res = await request.post('/inventoryTransfer/create', {
      fromStorageId: transferForm.fromStorageId,
      toStorageId: transferForm.toStorageId,
      goodsId: transferForm.goodsId,
      count: transferForm.count,
      creatorId: user.id || 1,
      remark: transferForm.remark
    })
    if (res.data.code === 0 || res.data.code === 200) {
      ElMessage.success('调拨申请已提交，等待审核')
      dialogVisible.value = false
      loadTransferList()
    } else {
      ElMessage.error(res.data.msg || '提交失败')
    }
  } catch (e) { ElMessage.error(e.response?.data?.msg || '提交失败') }
  finally { submitting.value = false }
}

const handleAudit = (row) => {
  ElMessageBox.confirm(`确认通过调拨单 ${row.transferNo}？将执行实际库存转移。`, '审核通过', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' })
    .then(async () => {
      const user = JSON.parse(localStorage.getItem('user') || '{}')
      try {
        const res = await request.post('/inventoryTransfer/audit', null, { params: { transferId: row.id, auditorId: user.id || 1 } })
        if (res.data.code === 0 || res.data.code === 200) {
          ElMessage.success('调拨已执行')
          loadTransferList()
        } else {
          ElMessage.error(res.data.msg || '审核失败')
        }
      } catch (e) { ElMessage.error(e.response?.data?.msg || '审核失败') }
    }).catch(() => {})
}

const handleReject = (row) => {
  ElMessageBox.confirm(`确认拒绝调拨单 ${row.transferNo}？`, '审核拒绝', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' })
    .then(async () => {
      const user = JSON.parse(localStorage.getItem('user') || '{}')
      try {
        const res = await request.post('/inventoryTransfer/reject', null, { params: { transferId: row.id, auditorId: user.id || 1 } })
        if (res.data.code === 0 || res.data.code === 200) {
          ElMessage.success('已拒绝')
          loadTransferList()
        } else {
          ElMessage.error(res.data.msg || '操作失败')
        }
      } catch (e) { ElMessage.error(e.response?.data?.msg || '操作失败') }
    }).catch(() => {})
}
</script>

<style scoped>
.inventory-transfer-container { padding: 20px; }
.search-section { margin-bottom: 15px; }
.table-card { border-radius: 8px; }
.card-header { font-size: 16px; font-weight: 600; }
.pagination { margin-top: 20px; display: flex; justify-content: center; }
.stock-info { margin-top: 8px; }
</style>
