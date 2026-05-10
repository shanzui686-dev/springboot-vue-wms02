<script setup>
import {onBeforeMount, ref, getCurrentInstance} from 'vue'
import { Search } from '@element-plus/icons-vue'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import PurchaseCreate from './PurchaseCreate.vue'

const { proxy } = getCurrentInstance()
const httpUrl = proxy.$httpUrl
const $message = ElMessage

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
    0: 'primary',  // 待入库 - 蓝色
    1: 'success',  // 已入库 - 绿色
    2: 'danger'    // 已退货 - 红色
  }
  return typeMap[status] || 'info'
}

/**
 * 格式化状态文本
 */
const getStatusText = (status) => {
  const textMap = {
    0: '待入库',
    1: '已入库',
    2: '已退货'
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

/**
 * 确认入库
 */
const handleInbound = (row) => {
  ElMessageBox.confirm(
    `确定要对采购单【${row.purchaseNo}】进行入库操作吗？入库后将增加对应商品的库存。`,
    '确认入库',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    axios.post(httpUrl + '/purchase/inbound', null, {
      params: {
        purchaseId: row.id
      }
    }).then(res => {
      if (res.data.code === 200) {
        $message.success('入库成功!')
        loadPost()
      } else {
        $message.error(res.data.msg || '入库失败!')
      }
    }).catch(error => {
      console.error('入库失败:', error)
      $message.error('入库失败：' + (error.response?.data?.msg || error.message))
    })
  }).catch(() => {
    // 用户取消操作
  })
}

/**
 * 采购退货
 */
const handleReturn = (row) => {
  ElMessageBox.confirm(
    `确定要对采购单【${row.purchaseNo}】进行退货操作吗？退货后将扣减对应商品的库存。`,
    '确认退货',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    axios.post(httpUrl + '/purchase/returnGoods', null, {
      params: {
        purchaseId: row.id
      }
    }).then(res => {
      if (res.data.code === 200) {
        $message.success('退货成功!')
        loadPost()
      } else {
        $message.error(res.data.msg || '退货失败!')
      }
    }).catch(error => {
      console.error('退货失败:', error)
      $message.error('退货失败：' + (error.response?.data?.msg || error.message))
    })
  }).catch(() => {
    // 用户取消操作
  })
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
        <el-option label="待入库" :value="0"></el-option>
        <el-option label="已入库" :value="1"></el-option>
        <el-option label="已退货" :value="2"></el-option>
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
      <el-table-column prop="operate" label="操作" width="280" fixed="right">
        <template #default="scope">
          <el-button size="small" type="primary" @click="viewDetails(scope.row)">查看明细</el-button>
          
          <!-- 待入库状态显示确认入库按钮 -->
          <el-button 
            v-if="scope.row.status === 0" 
            size="small" 
            type="success" 
            @click="handleInbound(scope.row)"
          >
            确认入库
          </el-button>
          
          <!-- 已入库状态显示退货按钮 -->
          <el-button 
            v-if="scope.row.status === 1" 
            size="small" 
            type="danger" 
            @click="handleReturn(scope.row)"
          >
            退货
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

    <!-- 新建采购单组件 -->
    <PurchaseCreate ref="purchaseCreateRef" @success="handleCreateSuccess" />
  </div>
</template>

<style scoped>
:deep(.el-pagination) {
  margin-top: 10px;
}
</style>
