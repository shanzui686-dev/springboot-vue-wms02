<template>
  <div class="loss-report-container">
    <!-- 搜索栏 -->
    <div class="search-section">
      <el-input v-model="search.lossNo" placeholder="损耗单号" clearable style="width: 200px;" @keyup.enter="loadList" />
      <el-select v-model="search.status" placeholder="状态" clearable style="width: 130px; margin-left: 10px;">
        <el-option label="待审核" :value="0" />
        <el-option label="已处理" :value="1" />
        <el-option label="已拒绝" :value="2" />
      </el-select>
      <el-select v-if="user.roleId !== 2" v-model="search.lossType" placeholder="损耗类型" clearable style="width: 130px; margin-left: 10px;">
        <el-option label="库存损耗" :value="1" />
        <el-option label="销售损耗" :value="2" />
      </el-select>
      <el-button type="primary" style="margin-left: 10px;" @click="loadList">查询</el-button>
      <el-button @click="resetSearch">重置</el-button>
      <el-button type="danger" style="float: right;" @click="openReportDialog">
        <el-icon><Warning /></el-icon>
        损耗上报
      </el-button>
    </div>

    <!-- 表格 -->
    <el-card class="table-card" shadow="never">
      <template #header>
        <div class="card-header">损耗记录</div>
      </template>

      <el-table :data="tableData" stripe border v-loading="loading">
        <el-table-column prop="lossNo" label="损耗单号" width="180" />
        <el-table-column label="商品" min-width="120">
          <template #default="{ row }">{{ row.goodsName || '-' }}</template>
        </el-table-column>
        <el-table-column prop="batchNo" label="批次号" width="150" />
        <el-table-column prop="lossCount" label="损耗数量" width="90" align="center">
          <template #default="{ row }">
            <el-tag type="danger">{{ row.lossCount }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.lossType === 1 ? 'danger' : 'warning'">{{ row.lossType === 1 ? '库存损耗' : '销售损耗' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="损耗金额" width="110" align="center">
          <template #default="{ row }">
            <span style="color: #F56C6C; font-weight: bold;">{{ row.lossAmount ? '¥' + row.lossAmount : '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="上报人" width="100">
          <template #default="{ row }">{{ row.reporterName || '-' }}</template>
        </el-table-column>
        <el-table-column label="审核人" width="100">
          <template #default="{ row }">{{ row.auditorName || '-' }}</template>
        </el-table-column>
        <el-table-column prop="reason" label="原因" min-width="120" show-overflow-tooltip />
        <el-table-column label="上报时间" width="170">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column v-if="user.roleId !== 2" label="操作" width="160" fixed="right">
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
        @size-change="loadList"
        @current-change="loadList"
      />
    </el-card>

    <!-- 损耗上报弹窗 -->
    <el-dialog v-model="reportVisible" title="损耗上报" width="550px" :close-on-click-modal="false" @close="handleReportClose">
      <el-form ref="reportFormRef" :model="reportForm" :rules="reportRules" label-width="100px">
        <el-form-item label="选择商品" prop="goodsId">
          <el-select v-model="reportForm.goodsId" placeholder="请选择商品" style="width: 100%" filterable @change="handleReportGoodsChange">
            <el-option v-for="g in goodsList" :key="g.id" :label="`${g.name} (库存:${g.count || 0})`" :value="g.id" />
          </el-select>
        </el-form-item>

        <el-form-item label="选择批次" prop="batchId">
          <el-select v-model="reportForm.batchId" placeholder="请先选择商品" style="width: 100%" :disabled="!reportForm.goodsId" @change="handleBatchChange">
            <el-option v-for="b in batchList" :key="b.id" :label="`${b.batchNo} (剩余:${b.currentCount})`" :value="b.id" />
          </el-select>
        </el-form-item>

        <el-form-item label="损耗数量" prop="lossCount">
          <el-input-number v-model="reportForm.lossCount" :min="1" :max="maxLossCount" :step="1" style="width: 100%" :disabled="!reportForm.batchId" />
          <div v-if="selectedBatch" class="stock-info">
            <el-text type="info">批次剩余：{{ selectedBatch.currentCount }}</el-text>
          </div>
        </el-form-item>

        <el-form-item label="损耗类型" prop="lossType">
          <el-radio-group v-model="reportForm.lossType">
            <el-radio v-if="user.roleId !== 2" :value="1">库存损耗</el-radio>
            <el-radio :value="2">销售损耗</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="损耗原因" prop="reason">
          <el-input v-model="reportForm.reason" type="textarea" :rows="3" placeholder="请填写损耗原因" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="reportVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitReport" :loading="submitting">提交上报</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
export default { name: 'LossReport' }
</script>

<script setup>
import { ref, reactive, onMounted, onActivated } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Warning } from '@element-plus/icons-vue'
import request from '@/utils/request'

const user = JSON.parse(sessionStorage.getItem('user') || '{}')
const loading = ref(false)
const submitting = ref(false)
const reportVisible = ref(false)
const tableData = ref([])
const total = ref(0)
const goodsList = ref([])
const batchList = ref([])
const selectedBatch = ref(null)
const maxLossCount = ref(999999)

const page = reactive({ pageNum: 1, pageSize: 10 })
const search = reactive({ lossNo: '', status: '', lossType: '' })

const reportFormRef = ref(null)
const reportForm = reactive({
  goodsId: null,
  batchId: null,
  lossCount: 1,
  lossType: 1,
  reason: ''
})

const reportRules = {
  goodsId: [{ required: true, message: '请选择商品', trigger: 'change' }],
  batchId: [{ required: true, message: '请选择批次', trigger: 'change' }],
  lossCount: [{ required: true, message: '请输入损耗数量', trigger: 'blur' }],
  lossType: [{ required: true, message: '请选择损耗类型', trigger: 'change' }],
  reason: [{ required: true, message: '请填写损耗原因', trigger: 'blur' }]
}

const statusType = (s) => ({ 0: 'warning', 1: 'success', 2: 'danger' }[s] || 'info')
const statusText = (s) => ({ 0: '待审核', 1: '已处理', 2: '已拒绝' }[s] || '未知')
const formatTime = (t) => t ? new Date(t).toLocaleString('zh-CN') : '-'

const resetSearch = () => {
  search.lossNo = ''
  search.status = ''
  search.lossType = ''
  loadList()
}

onMounted(() => { loadList(); loadGoodsList() })

onActivated(() => { loadList(); loadGoodsList() })

const loadList = async () => {
  loading.value = true
  try {
    const param = {}
    if (search.lossNo) param.lossNo = search.lossNo
    if (search.status !== '' && search.status !== null && search.status !== undefined) param.status = search.status
    if (search.lossType !== '' && search.lossType !== null && search.lossType !== undefined) param.lossType = search.lossType
    // 收银员只能看销售损耗和自己的记录
    if (user.roleId === 2) {
      param.lossType = 2
      param.reporterId = user.id
    }
    const res = await request.post('/lossReport/listPage', { pagenum: page.pageNum, pagesize: page.pageSize, param })
    if (res.data.code === 0 || res.data.code === 200) {
      tableData.value = res.data.data?.records || res.data.data || []
      total.value = res.data.data?.total || res.data.total || 0
    } else {
      ElMessage.error(res.data.msg || '加载失败')
    }
  } catch (e) { ElMessage.error('加载失败') }
  finally { loading.value = false }
}

const loadGoodsList = async () => {
  try {
    const res = await request.post('/goods/listPage', { pagenum: 1, pagesize: 1000, param: {} })
    if (res.data.code === 0 || res.data.code === 200) {
      goodsList.value = (res.data.data?.records || res.data.data || []).filter(g => g.count > 0)
    }
  } catch (e) { /* ignore */ }
}

const handleReportGoodsChange = async () => {
  reportForm.batchId = null
  reportForm.lossCount = 1
  batchList.value = []
  selectedBatch.value = null
  maxLossCount.value = 999999
  if (!reportForm.goodsId) return
  try {
    const res = await request.get('/goods/batchStock', { params: { goodsId: reportForm.goodsId } })
    // batchStock 可能直接返回 data 或 data.data
    let batches = res.data
    if (res.data.code === 0 || res.data.code === 200) {
      batches = res.data.data || []
    }
    batchList.value = (Array.isArray(batches) ? batches : []).filter(b => b.currentCount > 0)
  } catch (e) { ElMessage.error('加载批次失败') }
}

const handleReportClose = () => {
  reportFormRef.value?.resetFields()
  reportForm.goodsId = null
  reportForm.batchId = null
  reportForm.lossCount = 1
  reportForm.lossType = 1
  reportForm.reason = ''
  batchList.value = []
  selectedBatch.value = null
  maxLossCount.value = 999999
}

const handleBatchChange = () => {
  selectedBatch.value = batchList.value.find(b => b.id === reportForm.batchId)
  if (selectedBatch.value) {
    maxLossCount.value = selectedBatch.value.currentCount
    reportForm.lossCount = 1
  }
}

const openReportDialog = () => {
  reportVisible.value = true
  if (user.roleId === 2) reportForm.lossType = 2
  loadGoodsList()
}

const handleSubmitReport = async () => {
  try { await reportFormRef.value.validate() } catch { return }
  submitting.value = true
  try {
    const user = JSON.parse(sessionStorage.getItem('user') || '{}')
    const res = await request.post('/lossReport/report', {
      goodsId: reportForm.goodsId,
      batchId: reportForm.batchId,
      lossCount: reportForm.lossCount,
      lossType: reportForm.lossType,
      reason: reportForm.reason,
      reporterId: user.id || 1
    })
    if (res.data.code === 0 || res.data.code === 200) {
      ElMessage.success('损耗单已提交，等待审核')
      reportVisible.value = false
      loadList()
    } else {
      ElMessage.error(res.data.msg || '提交失败')
    }
  } catch (e) { ElMessage.error(e.response?.data?.msg || '提交失败') }
  finally { submitting.value = false }
}

const handleAudit = (row) => {
  const typeLabel = row.lossType === 1 ? '库存损耗' : '销售损耗'
  const amountInfo = row.lossAmount ? `，损耗金额：¥${row.lossAmount}` : ''
  const tip = row.lossType === 1
    ? `确认审核通过？将扣减批次库存并记录损耗费用。`
    : `确认审核通过？将记录销售损耗。`
  ElMessageBox.confirm(`${tip}${amountInfo}`, `审核${typeLabel}单 ${row.lossNo}`, {
    confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
  }).then(async () => {
    const user = JSON.parse(sessionStorage.getItem('user') || '{}')
    try {
      const res = await request.post('/lossReport/audit', null, { params: { lossId: row.id, auditorId: user.id || 1 } })
      if (res.data.code === 0 || res.data.code === 200) {
        ElMessage.success('审核通过')
        loadList()
      } else {
        ElMessage.error(res.data.msg || '审核失败')
      }
    } catch (e) { ElMessage.error(e.response?.data?.msg || '审核失败') }
  }).catch(() => {})
}

const handleReject = (row) => {
  ElMessageBox.confirm(`确认拒绝损耗单 ${row.lossNo}？`, '审核拒绝', {
    confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
  }).then(async () => {
    const user = JSON.parse(sessionStorage.getItem('user') || '{}')
    try {
      const res = await request.post('/lossReport/reject', null, { params: { lossId: row.id, auditorId: user.id || 1 } })
      if (res.data.code === 0 || res.data.code === 200) {
        ElMessage.success('已拒绝')
        loadList()
      } else {
        ElMessage.error(res.data.msg || '操作失败')
      }
    } catch (e) { ElMessage.error(e.response?.data?.msg || '操作失败') }
  }).catch(() => {})
}
</script>

<style scoped>
.loss-report-container { padding: 20px; }
.search-section { margin-bottom: 15px; }
.table-card { border-radius: 8px; }
.card-header { font-size: 16px; font-weight: 600; }
.pagination { margin-top: 20px; display: flex; justify-content: center; }
.stock-info { margin-top: 8px; }
</style>
