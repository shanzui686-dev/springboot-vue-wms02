<script setup>
import { onMounted, onActivated, ref, computed, getCurrentInstance } from 'vue'
import { Search, Plus } from '@element-plus/icons-vue'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'

const { proxy } = getCurrentInstance()
const httpUrl = proxy.$httpUrl
const $message = ElMessage

const user = JSON.parse(sessionStorage.getItem('user') || '{}')

// ==================== 数据定义 ====================
const tableData = ref([])
const pageSize = ref(10)
const pageNum = ref(1)
const total = ref(0)
const checkNo = ref('')
const status = ref('')

// 对话框控制
const inputDialogVisible = ref(false)  // 录入实盘数据对话框
const detailDialogVisible = ref(false) // 查看详情对话框
const currentCheck = ref({})           // 当前操作的盘点单
const detailList = ref([])             // 盘点明细列表

// 录入表单
const inputDetails = ref([])           // 用户填写的实盘数据
const inputPageNum = ref(1)            // 录入对话框-当前页
const inputPageSize = ref(10)          // 录入对话框-每页条数

// ==================== 状态显示 ====================
const getStatusType = (status) => {
  const typeMap = {
    0: 'info',     // 待录入 - 灰色
    1: 'warning',  // 待审核 - 橙色
    2: 'success'   // 已完成 - 绿色
  }
  return typeMap[status] || 'info'
}

const getStatusText = (status) => {
  const textMap = {
    0: '待录入',
    1: '待审核',
    2: '已完成'
  }
  return textMap[status] || '未知'
}

const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  const date = new Date(dateTime)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}`
}

// ==================== 计算属性 ====================
// 实时计算盈亏数量（实盘 - 账面）
const summaryProfitLoss = computed(() => {
  return inputDetails.value.reduce((sum, item) => {
    return sum + (item.actualCount || 0) - (item.expectedCount || 0)
  }, 0)
})

// 录入对话框分页（前端分页）
const pagedInputDetails = computed(() => {
  const start = (inputPageNum.value - 1) * inputPageSize.value
  return inputDetails.value.slice(start, start + inputPageSize.value)
})

const handleInputSizeChange = (val) => {
  inputPageNum.value = 1
  inputPageSize.value = val
}

const handleInputCurrentChange = (val) => {
  inputPageNum.value = val
}

// ==================== 分页 ====================
const handleSizeChange = (val) => {
  pageNum.value = 1
  pageSize.value = val
  loadPost()
}

const handleCurrentChange = (val) => {
  pageNum.value = val
  loadPost()
}

// ==================== 搜索与重置 ====================
const resetParam = () => {
  checkNo.value = ''
  status.value = ''
  loadPost()
}

// ==================== 加载数据 ====================
const loadPost = () => {
  const param = {}
  if (checkNo.value && checkNo.value !== 'null') {
    param.checkNo = checkNo.value
  }
  if (status.value !== '' && status.value !== null && status.value !== undefined) {
    param.status = status.value
  }

  axios.post(httpUrl + '/inventoryCheck/listPage', {
    pagesize: pageSize.value,
    pagenum: pageNum.value,
    param: param
  }).then(res => {
    const result = res.data
    if (result.code === 200) {
      tableData.value = Array.isArray(result.data) ? result.data : []
      total.value = result.total || 0
    } else {
      $message.error('获取数据失败')
    }
  }).catch(error => {
    console.error('请求失败:', error)
    $message.error('请求失败')
  })
}

// ==================== 发起盘点 ====================
// 整个系统只有一个仓库（主仓库），发起盘点无需选择仓库
const handleCreate = () => {
  ElMessageBox.confirm(
    '将对主仓库全部商品生成账面库存快照，确认发起盘点？',
    '发起盘点',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    axios.post(httpUrl + '/inventoryCheck/create', {
      creatorId: user.id || 1,
      remark: '常规盘点'
    }).then(res => {
      if (res.data.code === 200) {
        $message.success('盘点单创建成功！')
        loadPost()
      } else {
        $message.error(res.data.msg || '创建失败')
      }
    }).catch(error => {
      console.error('创建失败:', error)
      $message.error('创建失败：' + (error.response?.data?.msg || error.message))
    })
  }).catch(() => {})
}

// ==================== 录入实盘数据 ====================
const handleInput = (row) => {
  currentCheck.value = row
  
  // 查询该盘点单的明细
  axios.get(httpUrl + '/inventoryCheck/detail/list', {
    params: { checkId: row.id }
  }).then(res => {
    if (res.data.code === 200) {
      detailList.value = Array.isArray(res.data.data) ? res.data.data : []
      // 初始化实盘数据（复制明细数据，实际数量默认为账面数量）
      inputDetails.value = detailList.value.map(item => ({
        id: item.id,
        goodsId: item.goodsId,
        barcode: item.barcode,
        goodsName: item.goodsName,
        expectedCount: item.expectedCount,
        actualCount: item.actualCount || item.expectedCount // 默认实盘等于账面
      }))
      inputDialogVisible.value = true
      inputPageNum.value = 1
    } else {
      $message.error('获取明细失败')
    }
  }).catch(error => {
    console.error('获取明细失败:', error)
    $message.error('获取明细失败')
  })
}

// 提交实盘数据
const submitInput = () => {
  const details = inputDetails.value.map(item => ({
    detailId: item.id,
    actualCount: item.actualCount
  }))
  
  axios.post(httpUrl + '/inventoryCheck/input', {
    checkId: currentCheck.value.id,
    details: details
  }).then(res => {
    if (res.data.code === 200) {
      $message.success('录入成功！')
      inputDialogVisible.value = false
      loadPost()
    } else {
      $message.error(res.data.msg || '录入失败')
    }
  }).catch(error => {
    console.error('录入失败:', error)
    $message.error('录入失败：' + (error.response?.data?.msg || error.message))
  })
}

// ==================== 审核盘点单 ====================
const handleAudit = (row) => {
  currentCheck.value = row
  
  ElMessageBox.confirm(
    '确认账面库存与实盘一致，并将盈亏结果入账吗？',
    '确认审核',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    axios.post(httpUrl + '/inventoryCheck/audit', null, {
      params: { checkId: row.id }
    }).then(res => {
      if (res.data.code === 200) {
        $message.success('审核成功，盈亏已入账！')
        loadPost()
      } else {
        $message.error(res.data.msg || '审核失败')
      }
    }).catch(error => {
      console.error('审核失败:', error)
      $message.error('审核失败：' + (error.response?.data?.msg || error.message))
    })
  }).catch(() => {})
}

// ==================== 查看详情 ====================
const viewDetails = (row) => {
  currentCheck.value = row
  
  axios.get(httpUrl + '/inventoryCheck/detail/list', {
    params: { checkId: row.id }
  }).then(res => {
    if (res.data.code === 200) {
      detailList.value = Array.isArray(res.data.data) ? res.data.data : []
      detailDialogVisible.value = true
    } else {
      $message.error('获取明细失败')
    }
  }).catch(error => {
    console.error('获取明细失败:', error)
    $message.error('获取明细失败')
  })
}

// 计算盈亏数量（用于详情展示）
const totalDiffCount = computed(() => {
  return detailList.value.reduce((sum, item) => {
    return sum + (item.diffCount || 0)
  }, 0)
})

// 格式化商品名称
const formatGoodsName = (row) => {
  return row.goodsName || '-'
}

// ==================== 初始化 ====================
const initPage = () => {
  loadPost()
}

onMounted(() => { initPage() })
onActivated(() => { initPage() })
</script>

<template>
  <div>
    <!-- 顶部搜索栏 -->
    <div style="margin-bottom: 10px;">
      <el-input 
        v-model="checkNo" 
        @keyup.enter="loadPost" 
        placeholder="请输入盘点单号" 
        :suffix-icon="Search" 
        style="width: 200px;"
      ></el-input>
      
      <el-select 
        v-model="status" 
        placeholder="请选择状态" 
        clearable
        style="width: 150px; margin-left: 10px"
      >
        <el-option label="待录入" :value="0"></el-option>
        <el-option label="待审核" :value="1"></el-option>
        <el-option label="已完成" :value="2"></el-option>
      </el-select>
      
      <el-button type="primary" style="margin-left: 10px;" @click="loadPost">查询</el-button>
      <el-button type="success" @click="resetParam">重置</el-button>
      
      <!-- 发起盘点按钮 -->
      <el-button type="primary" style="float: right;" @click="handleCreate">
        <el-icon style="margin-right: 5px;"><Plus /></el-icon>
        发起盘点
      </el-button>
    </div>

    <!-- 主表格 -->
    <el-table 
      :data="tableData" 
      style="width: 100%;"
      :header-cell-style="{background:'#f2f5fc',color:'#555'}"
      border
    >
      <el-table-column prop="id" label="ID" width="80"></el-table-column>
      <el-table-column prop="checkNo" label="盘点单号" width="200"></el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="scope">
          <el-tag :type="getStatusType(scope.row.status)">
            {{ getStatusText(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180">
        <template #default="scope">
          {{ formatDateTime(scope.row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column prop="remark" label="备注" min-width="150"></el-table-column>
      <el-table-column prop="operate" label="操作" width="150" fixed="right">
        <template #default="scope">
          <!-- 状态0：待录入 -->
          <el-button 
            v-if="scope.row.status === 0" 
            size="small" 
            type="primary" 
            @click="handleInput(scope.row)"
          >
            录入
          </el-button>
          
          <!-- 状态1：待审核 -->
          <el-button 
            v-if="scope.row.status === 1" 
            size="small" 
            type="danger" 
            @click="handleAudit(scope.row)"
          >
            审核
          </el-button>
          
          <!-- 状态2：已完成 -->
          <el-button 
            v-if="scope.row.status === 2" 
            size="small" 
            @click="viewDetails(scope.row)"
          >
            查看详情
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <el-pagination
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
      :current-page="pageNum"
      :page-sizes="[5, 10, 20, 50]"
      :page-size="pageSize"
      layout="total, sizes, prev, pager, next, jumper"
      :total="total"
      :pager-count="5"
      prev-text="上一页"
      next-text="下一页"
    >
    </el-pagination>

    <!-- 录入实盘数据对话框 -->
    <el-dialog
      title="录入实盘数据"
      v-model="inputDialogVisible"
      width="80%"
      center
    >
      <div style="margin-bottom: 15px;">
        <strong>盘点单号：</strong>{{ currentCheck.checkNo }} |
        <strong>仓库：</strong>主仓库
      </div>

      <el-table :data="pagedInputDetails" border style="width: 100%">
        <el-table-column label="ID" width="60">
          <template #default="scope">
            {{ scope.row.goodsId }}
          </template>
        </el-table-column>
        <el-table-column label="商品条码" width="130">
          <template #default="scope">
            {{ scope.row.barcode || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="商品名称" min-width="120">
          <template #default="scope">
            {{ scope.row.goodsName || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="账面库存" width="100">
          <template #default="scope">
            <span style="color: #409EFF;">{{ scope.row.expectedCount }}</span>
          </template>
        </el-table-column>
        <el-table-column label="实盘数量" width="180">
          <template #default="scope">
            <el-input-number 
              v-model="scope.row.actualCount" 
              :min="0" 
              :precision="0"
              controls-position="right"
              style="width: 100%;"
            />
          </template>
        </el-table-column>
        <el-table-column label="盈亏数量" width="120">
          <template #default="scope">
            <span :style="{
              color: (scope.row.actualCount - scope.row.expectedCount) > 0 ? '#67C23A' : 
                     (scope.row.actualCount - scope.row.expectedCount) < 0 ? '#F56C6C' : '#909399',
              fontWeight: 'bold'
            }">
              {{ (scope.row.actualCount - scope.row.expectedCount) > 0 ? '+' : '' }}
              {{ scope.row.actualCount - scope.row.expectedCount }}
            </span>
          </template>
        </el-table-column>
      </el-table>

      <!-- 录入对话框分页 -->
      <el-pagination
        style="margin-top: 10px; justify-content: flex-end;"
        @size-change="handleInputSizeChange"
        @current-change="handleInputCurrentChange"
        :current-page="inputPageNum"
        :page-sizes="[5, 10, 20, 50]"
        :page-size="inputPageSize"
        layout="total, sizes, prev, pager, next, jumper"
        :total="inputDetails.length"
        :pager-count="5"
        prev-text="上一页"
        next-text="下一页"
      >
      </el-pagination>

      <div style="margin-top: 15px; text-align: right; font-size: 16px; font-weight: bold;">
        总盈亏：
        <span :style="{
          color: summaryProfitLoss > 0 ? '#67C23A' : 
                 summaryProfitLoss < 0 ? '#F56C6C' : '#909399'
        }">
          {{ summaryProfitLoss > 0 ? '+' : '' }}{{ summaryProfitLoss }}
        </span>
      </div>
      
      <template #footer>
        <el-button @click="inputDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitInput">确认提交</el-button>
      </template>
    </el-dialog>

    <!-- 查看详情对话框（只读） -->
    <el-dialog
      title="盘点结果详情"
      v-model="detailDialogVisible"
      width="80%"
      center
    >
      <div style="margin-bottom: 15px;">
        <strong>盘点单号：</strong>{{ currentCheck.checkNo }} |
        <strong>仓库：</strong>主仓库 |
        <strong>状态：</strong>
        <el-tag :type="getStatusType(currentCheck.status)">
          {{ getStatusText(currentCheck.status) }}
        </el-tag>
      </div>
      
      <el-table :data="detailList" border style="width: 100%">
        <el-table-column label="ID" width="60">
          <template #default="scope">
            {{ scope.row.goodsId }}
          </template>
        </el-table-column>
        <el-table-column label="商品条码" width="130">
          <template #default="scope">
            {{ scope.row.barcode || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="商品名称" min-width="120">
          <template #default="scope">
            {{ scope.row.goodsName || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="账面库存" width="120">
          <template #default="scope">
            {{ scope.row.expectedCount }}
          </template>
        </el-table-column>
        <el-table-column label="实盘库存" width="120">
          <template #default="scope">
            {{ scope.row.actualCount }}
          </template>
        </el-table-column>
        <el-table-column label="盈亏数量" width="120">
          <template #default="scope">
            <span :style="{
              color: scope.row.diffCount > 0 ? '#67C23A' : 
                     scope.row.diffCount < 0 ? '#F56C6C' : '#909399',
              fontWeight: 'bold'
            }">
              {{ scope.row.diffCount > 0 ? '+' : '' }}{{ scope.row.diffCount }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="150"></el-table-column>
      </el-table>
      
      <div style="margin-top: 15px; text-align: right; font-size: 16px; font-weight: bold;">
        总盈亏：
        <span :style="{
          color: totalDiffCount > 0 ? '#67C23A' : 
                 totalDiffCount < 0 ? '#F56C6C' : '#909399'
        }">
          {{ totalDiffCount > 0 ? '+' : '' }}{{ totalDiffCount }}
        </span>
      </div>
    </el-dialog>
  </div>
</template>

<style scoped>
:deep(.el-pagination) {
  margin-top: 10px;
}
</style>
