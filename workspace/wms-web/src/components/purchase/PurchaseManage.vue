<script setup>
import {onBeforeMount, ref, getCurrentInstance} from 'vue'
import { Search } from '@element-plus/icons-vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import PurchaseCreate from './PurchaseCreate.vue'

const { proxy } = getCurrentInstance()
const httpUrl = proxy.$httpUrl
const $message = ElMessage
const user = JSON.parse(sessionStorage.getItem('user') || '{}')

// 数据定义
const tableData = ref([])
const supplierData = ref([])
const pageSize = ref(10)
const pageNum = ref(1)
const total = ref(0)
const purchaseNo = ref('')
const supplierId = ref('')
const status = ref('')

// 对话框控制
const detailDialogVisible = ref(false) // 查看明细
const detailList = ref([]) // 明细列表
const currentPurchase = ref({}) // 当前操作的采购单

// 新建采购单组件引用
const purchaseCreateRef = ref(null)

/**
 * 格式化供应商名称
 */
const formatSupplierName = (row) => {
  return row.supplierName || '-'
}

/**
 * 格式化采购员姓名
 */
const formatUserName = (row) => {
  return row.userName || '-'
}

/**
 * 格式化状态标签类型
 */
const getStatusType = (status) => {
  const typeMap = {
    0: 'info',     // 待审核 - 灰色
    1: 'warning',  // 待入库 - 橙色
    2: 'success',  // 已入库 - 绿色
    3: 'danger',   // 已取消 - 红色
    4: 'danger',   // 已拒绝 - 红色
    5: 'warning',  // 待退回 - 橙色
    6: 'success',  // 已退回 - 绿色
    7: 'info'      // 不退回 - 灰色
  }
  return typeMap[status] || 'info'
}

/**
 * 格式化状态文本
 */
const getStatusText = (status) => {
  const textMap = {
    0: '待审核',
    1: '待入库',
    2: '已入库',
    3: '已取消',
    4: '已拒绝',
    5: '待退回',
    6: '已退回',
    7: '不退回'
  }
  return textMap[status] || '未知'
}

/**
 * 格式化金额
 */
const formatAmount = (amount) => {
  if (!amount && amount !== 0) return '-'
  return '¥' + Number(amount).toFixed(2)
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
  return `${year}-${month}-${day} ${hours}:${minutes}`
}

/**
 * 分页大小改变
 */
const handleSizeChange = (val) => {
  pageNum.value = 1
  pageSize.value = val
  loadPost()
}

/**
 * 页码改变
 */
const handleCurrentChange = (val) => {
  pageNum.value = val
  loadPost()
}

/**
 * 重置搜索条件
 */
const resetParam = () => {
  purchaseNo.value = ''
  supplierId.value = ''
  status.value = ''
  loadPost()
}

/**
 * 加载采购单列表
 */
const loadPost = () => {
  const param = {
    purchaseNo: purchaseNo.value || undefined,
  }
  if (supplierId.value !== '' && supplierId.value !== null && supplierId.value !== undefined) {
    param.supplierId = supplierId.value
  }
  if (status.value !== '' && status.value !== null && status.value !== undefined) {
    param.status = status.value
  }

  axios.post(httpUrl + '/purchase/listPage', {
    pagesize: pageSize.value,
    pagenum: pageNum.value,
    param: param
  }).then(res => {
    const result = res.data
    if (result.code === 200) {
      const newData = Array.isArray(result.data) ? result.data : []
      tableData.value = newData
      total.value = result.total || 0
    } else {
      $message.error('获取数据失败')
    }
  }).catch(error => {
    console.error('请求失败:', error)
    $message.error('请求失败')
  })
}

/**
 * 加载供应商列表
 */
const loadSupplier = () => {
  axios.get(httpUrl + '/supplier/list').then(res => {
    const result = res.data
    if (result.code === 200) {
      const newData = Array.isArray(result.data) ? result.data : []
      supplierData.value = newData
    }
  }).catch(error => {
    console.error('请求失败:', error)
  })
}

/**
 * 打开新建采购单对话框
 */
const add = () => {
  if (purchaseCreateRef.value) {
    purchaseCreateRef.value.open()
  }
}

/**
 * 新建采购单成功回调
 */
const handleCreateSuccess = () => {
  loadPost() // 刷新列表
}

/**
 * 生成采购单号
 */
const generatePurchaseNo = () => {
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  const random = String(Math.floor(Math.random() * 1000)).padStart(3, '0')
  return `PO${year}${month}${day}${random}`
}

// ===== 入库对话框相关 =====
const inboundDialogVisible = ref(false)
const inboundPurchaseId = ref(null)
const inboundItems = ref([])

/**
 * 打开入库对话框（加载明细，默认数量=采购数量）
 */
const openInboundDialog = (row) => {
  inboundPurchaseId.value = row.id
  axios.get(httpUrl + '/purchase/detail/list', { params: { purchaseId: row.id } }).then(res => {
    if (res.data.code === 200) {
      inboundItems.value = (res.data.data || []).map(d => ({
        ...d,
        actualCount: d.count
      }))
      inboundDialogVisible.value = true
    }
  })
}

/**
 * 确认入库（提交 InboundDTO）
 */
const handleInbound = (row) => {
  openInboundDialog(row)
}

const confirmInbound = () => {
  const dto = {
    purchaseId: inboundPurchaseId.value,
    storageId: 1, // 默认使用主仓库
    items: inboundItems.value.map(item => ({
      detailId: item.id,
      actualCount: Number(item.actualCount)
    }))
  }
  axios.post(httpUrl + '/purchase/inbound', dto).then(res => {
    if (res.data.code === 200) {
      $message.success('入库成功!')
      inboundDialogVisible.value = false
      loadPost()
    } else {
      $message.error(res.data.msg || '入库失败!')
    }
  }).catch(error => {
    $message.error('入库失败：' + (error.response?.data?.msg || error.message))
  })
}

// ===== 采购退回对话框相关 =====
const returnDialogVisible = ref(false)
const returnPurchaseId = ref(null)
const returnItems = ref([])
const returnReason = ref('')
const returnSubmitting = ref(false)

/**
 * 打开采购退回对话框
 */
const openReturnDialog = (row) => {
  returnPurchaseId.value = row.id
  returnReason.value = ''
  returnSubmitting.value = false
  const purchaseStatus = row.status // 1=待入库, 2=已入库

  // 加载采购明细
  axios.get(httpUrl + '/purchase/detail/list', { params: { purchaseId: row.id } }).then(res => {
    if (res.data.code === 200) {
      const details = res.data.data || []

      // 为每个明细项加载批次库存和总库存
      const goodsIds = [...new Set(details.map(d => d.goodsId))]
      Promise.all([
        // 获取批次库存
        Promise.all(goodsIds.map(gid =>
          axios.get(httpUrl + '/goods/batchStock', { params: { goodsId: gid } }).then(r =>
            (r.data.code === 200 && r.data.data) ? r.data.data : []
          )
        )),
        // 获取商品总库存
        axios.get(httpUrl + '/goods/list').then(r =>
          (r.data.code === 200 && r.data.data) ? r.data.data : []
        )
      ]).then(([batchResults, goodsList]) => {
        const stockMap = {}
        batchResults.forEach(arr => arr.forEach(s => { stockMap[s.batchNo] = s }))

        const goodsMap = {}
        goodsList.forEach(g => { goodsMap[g.id] = g })

        returnItems.value = details.map(d => {
          const stock = stockMap[d.batchNo] || {}
          const batchStock = stock.currentCount || 0
          const goods = goodsMap[d.goodsId] || {}
          const totalStock = goods.count || 0
          return {
            detailId: d.id,
            goodsId: d.goodsId,
            goodsName: d.goodsName,
            batchNo: d.batchNo,
            purchaseCount: d.count,
            batchStock: batchStock,
            totalStock: totalStock,
            batchId: stock.id || null,
            returnCount: 0,
            maxReturn: purchaseStatus === 2 ? batchStock : d.count,
            isInbound: purchaseStatus === 2
          }
        })
        returnDialogVisible.value = true
      })
    }
  })
}

/**
 * 提交采购退回申请
 */
const submitReturn = async () => {
  // 确保 returnCount 是数字类型
  const items = returnItems.value.filter(b => Number(b.returnCount) > 0)
  if (items.length === 0) {
    $message.warning('请至少填写一个商品的退回数量')
    return
  }

  // 校验退回数量不超过可退回数量
  for (const b of items) {
    if (Number(b.returnCount) > b.maxReturn) {
      $message.warning(`商品【${b.goodsName}】退回数量超过可退回数量`)
      return
    }
  }

  returnSubmitting.value = true

  try {
    // 逐个商品提交退回申请
    for (const b of items) {
      const requestData = {
        purchaseId: returnPurchaseId.value,
        goodsId: b.goodsId,
        batchId: b.batchId || null,
        batchNo: b.batchNo,
        returnCount: Number(b.returnCount),
        reason: returnReason.value,
        creatorId: user.id
      }
      console.log('提交退回申请:', requestData)
      const res = await axios.post(httpUrl + '/purchaseReturn/submit', requestData)
      console.log('退回申请响应:', res.data)
      if (res.data.code !== 200) {
        throw new Error(res.data.msg || '提交失败')
      }
    }
    $message.success({
      message: `已提交 ${items.length} 笔退回申请，等待店长审核! 请在"退换货处理"页面的"采购退回审核"标签页查看`,
      duration: 5000
    })
    returnDialogVisible.value = false
    loadPost()
  } catch (error) {
    console.error('退回申请失败:', error)
    $message.error('提交失败：' + (error.response?.data?.msg || error.message))
  } finally {
    returnSubmitting.value = false
  }
}

/**
 * 查看明细
 */
const viewDetails = (row) => {
  currentPurchase.value = row
  
  // 查询该采购单的明细
  axios.get(httpUrl + '/purchase/detail/list', {
    params: {
      purchaseId: row.id
    }
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

/**
 * 格式化明细中的商品名称
 */
const formatGoodsName = (row) => {
  return row.goodsName || '-'
}

/**
 * 初始化加载
 */
onBeforeMount(() => {
  loadSupplier()
  loadPost()
})
</script>

<template>
  <div>
    <!-- 顶部搜索栏 -->
    <div style="margin-bottom: 10px;">
      <el-input 
        v-model="purchaseNo" 
        @keyup.enter="loadPost" 
        placeholder="请输入采购单号" 
        :suffix-icon="Search" 
        style="width: 200px;"
      ></el-input>
      
      <el-select 
        v-model="supplierId" 
        placeholder="请选择供应商" 
        clearable
        style="width: 200px; margin-left: 10px"
      >
        <el-option
          v-for="item in supplierData"
          :key="item.id"
          :label="item.name"
          :value="item.id"
        >
        </el-option>
      </el-select>
      
      <el-select 
        v-model="status" 
        placeholder="请选择状态" 
        clearable
        style="width: 150px; margin-left: 10px"
      >
        <el-option label="待入库" :value="1"></el-option>
        <el-option label="已入库" :value="2"></el-option>
        <el-option label="已取消" :value="3"></el-option>
        <el-option label="已拒绝" :value="4"></el-option>
        <el-option label="待退回" :value="5"></el-option>
        <el-option label="已退回" :value="6"></el-option>
        <el-option label="不退回" :value="7"></el-option>
      </el-select>
      
      <el-button type="primary" style="margin-left: 10px;" @click="loadPost">查询</el-button>
      <el-button type="success" @click="resetParam">重置</el-button>
      
      <!-- 右上角新建按钮 -->
      <el-button type="primary" style="float: right;" @click="add">
        <el-icon style="margin-right: 5px;"><Plus /></el-icon>
        新建采购单
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
      <el-table-column prop="purchaseNo" label="采购单号" width="180"></el-table-column>
      <el-table-column prop="supplierName" label="供应商" width="150" :formatter="formatSupplierName"></el-table-column>
      <el-table-column prop="userName" label="采购员" width="120" :formatter="formatUserName"></el-table-column>
      <el-table-column prop="totalAmount" label="总金额" width="120">
        <template #default="scope">
          {{ formatAmount(scope.row.totalAmount) }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="scope">
          <el-tag :type="getStatusType(scope.row.status)">
            {{ getStatusText(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="purchaseDate" label="创建时间" width="180">
        <template #default="scope">
          {{ formatDateTime(scope.row.purchaseDate) }}
        </template>
      </el-table-column>
      <el-table-column prop="remark" label="备注" min-width="150"></el-table-column>
      <el-table-column prop="operate" label="操作" width="350" fixed="right">
        <template #default="scope">
          <el-button size="small" type="primary" @click="viewDetails(scope.row)">查看明细</el-button>

          <!-- 待入库 → 确认入库 -->
          <el-button
            v-if="scope.row.status === 1"
            size="small"
            type="success"
            @click="handleInbound(scope.row)"
          >
            确认入库
          </el-button>

          <!-- 待入库或已入库 → 采购退回（待退回、已退回、不退回状态不显示） -->
          <el-button
            v-if="(scope.row.status === 1 || scope.row.status === 2) && scope.row.status !== 5 && scope.row.status !== 6 && scope.row.status !== 7"
            size="small"
            type="warning"
            @click="openReturnDialog(scope.row)"
          >
            采购退回
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

    <!-- 查看明细对话框 -->
    <el-dialog
      title="采购明细"
      v-model="detailDialogVisible"
      width="70%"
      center
    >
      <div style="margin-bottom: 15px;">
        <strong>采购单号：</strong>{{ currentPurchase.purchaseNo }} |
        <strong>供应商：</strong>{{ currentPurchase.supplierName }} |
        <strong>总金额：</strong>{{ formatAmount(currentPurchase.totalAmount) }}
      </div>
      
      <el-table :data="detailList" border style="width: 100%">
        <el-table-column prop="goodsName" label="商品名称" width="200" :formatter="formatGoodsName"></el-table-column>
        <el-table-column prop="count" label="数量" width="100"></el-table-column>
        <el-table-column prop="price" label="单价" width="120">
          <template #default="scope">
            {{ formatAmount(scope.row.price) }}
          </template>
        </el-table-column>
        <el-table-column prop="subtotal" label="小计" width="120">
          <template #default="scope">
            {{ formatAmount(scope.row.subtotal) }}
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="150"></el-table-column>
      </el-table>
    </el-dialog>

    <!-- 入库对话框（填写实际入库数量） -->
    <el-dialog title="确认入库" v-model="inboundDialogVisible" width="50%" center>
      <el-form label-width="100px">
        <el-table :data="inboundItems" border style="width: 100%">
          <el-table-column prop="goodsName" label="商品" width="150"></el-table-column>
          <el-table-column prop="batchNo" label="批次号" width="140"></el-table-column>
          <el-table-column prop="count" label="采购数量" width="100"></el-table-column>
          <el-table-column label="实际入库数量" width="150">
            <template #default="scope">
              <el-input-number v-model="scope.row.actualCount" :min="1" :max="scope.row.count" :step="1"></el-input-number>
            </template>
          </el-table-column>
        </el-table>
      </el-form>
      <template #footer>
        <el-button @click="inboundDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmInbound">确认入库</el-button>
      </template>
    </el-dialog>

    <!-- 采购退回对话框 -->
    <el-dialog title="采购退回申请" v-model="returnDialogVisible" width="75%" center>
      <el-table :data="returnItems" border style="width: 100%">
        <el-table-column prop="goodsName" label="商品" width="120"></el-table-column>
        <el-table-column prop="batchNo" label="批次号" width="140"></el-table-column>
        <el-table-column prop="purchaseCount" label="采购数量" width="90" align="center"></el-table-column>
        <!-- 已入库显示批次库存 -->
        <el-table-column v-if="returnItems.length > 0 && returnItems[0].isInbound" label="批次库存" width="90" align="center">
          <template #default="{ row }">
            <span :style="{ color: row.batchStock > 0 ? '#303133' : '#f56c6c' }">{{ row.batchStock }}</span>
          </template>
        </el-table-column>
        <!-- 总库存（已入库和未入库都显示） -->
        <el-table-column label="总库存" width="90" align="center">
          <template #default="{ row }">
            <span>{{ row.totalStock }}</span>
          </template>
        </el-table-column>
        <el-table-column label="退回数量" width="160" align="center">
          <template #default="{ row }">
            <el-input-number v-model="row.returnCount" :min="0" :max="row.maxReturn" :step="1" size="small" style="width: 130px" :placeholder="`最大${row.maxReturn}`"></el-input-number>
          </template>
        </el-table-column>
      </el-table>
      <el-form label-width="100px" style="margin-top: 15px;">
        <el-form-item label="退回原因">
          <el-input type="textarea" v-model="returnReason" :rows="2" placeholder="请输入退回原因（选填）"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="returnDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReturn" :loading="returnSubmitting">
          提交申请（{{ returnItems.filter(b => Number(b.returnCount) > 0).length }} 个商品）
        </el-button>
      </template>
    </el-dialog>

    <!-- 新建采购单组件 -->
    <PurchaseCreate ref="purchaseCreateRef" @success="handleCreateSuccess" />
  </div>
</template>

<style scoped>
:deep(.el-pagination) {
  margin-top: 10px;
}
</style>
