<script setup>
import {onBeforeMount, ref, getCurrentInstance} from 'vue'
import { Search, Download } from '@element-plus/icons-vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const { proxy } = getCurrentInstance()
const httpUrl = proxy.$httpUrl
const $message = ElMessage

const user = JSON.parse(sessionStorage.getItem('user') || '{}')
const goodstypeData = ref([])
const storageData = ref([])
const tableData = ref([])
const pageSize = ref(10)
const pageNum = ref(1)
const total = ref(0)
const name = ref('')
const storage=ref('')
const goodstype = ref('')
const operationType = ref('')
const startDate = ref('')
const endDate = ref('')
const centerDialogVisible = ref(false)
const formRef = ref()
const resetForm = () => {
  formRef.value.resetFields();
}

const form = ref({
  id:'',
  name: '',
  storage:'',
  goodstype:'',
  count:'',
  remark: '',

})
const formatStorage = (row) =>{
  let temp=storageData.value.find(item => {
     return item.id === row.storage
  })
  return temp ? temp.name : row.storage
}
const formatGoodsType = (row) =>{
  let temp=goodstypeData.value.find(item => {
    return item.id === row.goodsType
  })
  return temp ? temp.name : row.goodsType
}
const formatGoodsTypeDisplay = (row, column, cellValue, index) => {
  const goodsType = row.goodsType || row.goodstype
  const temp = goodstypeData.value.find(item => item.id === goodsType)
  // 如果找不到分类，显示 ID 和提示信息
  if (!temp && goodsType) {
    return `分类${goodsType}(未找到)`
  }
  return temp ? temp.name : (goodsType || '')
}

/**
 * 格式化日期时间显示
 */
const formatDateTime = (row) => {
  if (!row.createtime) return ''
  // 处理 LocalDateTime 格式: 2026-04-01T19:43:06
  let timeStr = row.createtime.replace('T', ' ')
  return timeStr
}

/**
 * 禁用开始日期（只能选择今天及之前的日期）
 */
const disabledStartDate = (time) => {
  return time.getTime() > Date.now()
}

/**
 * 禁用结束日期（不能早于开始日期，也不能晚于今天）
 */
const disabledEndDate = (time) => {
  const now = Date.now()
  const start = startDate.value ? new Date(startDate.value).getTime() : null
  
  if (start && time.getTime() < start) {
    return true // 不能早于开始日期
  }
  
  return time.getTime() > now // 不能晚于今天
}

/**
 * 格式化状态显示
 */
const formatStatus = (status) => {
  const statusMap = {
    0: { text: '待审核', type: 'primary' },
    1: { text: '已完成', type: 'success' },
    2: { text: '已拒绝', type: 'danger' }
  }
  return statusMap[status] || { text: '未知', type: 'info' }
}

/**
 * 获取操作类型的标签颜色
 */
const getOperationTypeTag = (operationType) => {
  const typeMap = {
    '采购入库': 'success',      // 绿色
    '销售出库': 'primary',      // 蓝色
    '采购退货': 'danger',       // 红色
    '销售退货': 'danger',       // 红色
    '退货入库': 'warning',      // 黄色
    '盘点盈亏': 'warning',      // 黄色
    '其他': 'info'
  }
  return typeMap[operationType] || 'info'
}

/**
 * 复制关联单号到剪贴板
 */
const copyOrderNum = (orderNum) => {
  if (!orderNum) return
  
  navigator.clipboard.writeText(orderNum).then(() => {
    $message.success(`单号 ${orderNum} 已复制到剪贴板`)
  }).catch(err => {
    // 降级方案：使用传统方法复制
    const textarea = document.createElement('textarea')
    textarea.value = orderNum
    textarea.style.position = 'fixed'
    textarea.style.opacity = '0'
    document.body.appendChild(textarea)
    textarea.select()
    
    try {
      document.execCommand('copy')
      $message.success(`单号 ${orderNum} 已复制到剪贴板`)
    } catch (e) {
      $message.error('复制失败，请手动复制')
    }
    
    document.body.removeChild(textarea)
  })
}


const handleSizeChange = (val) => {
  console.log(`每页 ${val} 条`)
  pageNum.value = 1
  pageSize.value = val
  loadPost()
}

const handleCurrentChange = (val) => {
  console.log(`当前页：${val}`)
  pageNum.value = val
  loadPost()
}

/**
 * 重置查询条件
 */
const resetParam = () => {
  name.value = ''
  storage.value = ''
  goodstype.value = ''
  operationType.value = ''
  startDate.value = ''
  endDate.value = ''
  loadPost()
}
const loadStorage = () => {
  console.log('开始查询，参数:', {
    pagesize:pageSize.value,
    pagenum:pageNum.value,
    param: {
      name:name.value
    }
  })
  axios.get(httpUrl+'/storage/list').then(res => {
    console.log('axios 响应:', res)
    const result = res.data  // 获取后端返回的 Result 对象
    console.log('后端返回的 Result:', result)
    console.log('result.code:', result.code)
    console.log('result.data:', result.data)
    console.log('result.data 类型:', Array.isArray(result.data) ? '数组' : '非数组')
    if(result.code==200){
      // 直接使用 result.data，并确保是数组
      const newData = Array.isArray(result.data) ? result.data : []
      console.log('准备更新仓库数据:', newData)
      storageData.value = newData
    }else {
      alert('获取数据失败')
    }
  }).catch(error => {
    console.error('请求失败:', error)
  })
}
const loadGoodsType = () => {
  console.log('开始查询，参数:', {
    pagesize:pageSize.value,
    pagenum:pageNum.value,
    param: {
      name:name.value
    }
  })
  axios.get(httpUrl+'/goodstype/list').then(res => {
    console.log('axios 响应:', res)
    const result = res.data  // 获取后端返回的 Result 对象
    console.log('后端返回的 Result:', result)
    console.log('result.code:', result.code)
    console.log('result.data:', result.data)
    console.log('result.data 类型:', Array.isArray(result.data) ? '数组' : '非数组')
    if(result.code==200){
      // 直接使用 result.data，并确保是数组
      const newData = Array.isArray(result.data) ? result.data : []
      console.log('准备更新分类数据:', newData)
      goodstypeData.value = newData
    }else {
      alert('获取数据失败')
    }
  }).catch(error => {
    console.error('请求失败:', error)
  })
}
/**
 * 加载记录列表数据
 */
const loadPost = () => {
  console.log('开始查询，参数:', {
    pagesize:pageSize.value,
    pagenum:pageNum.value,
    param: {
      name:name.value,
      storage:storage.value,
      goodstype:goodstype.value,
      operationType:operationType.value,
      startDate:startDate.value,
      endDate:endDate.value,
      roleId:user.roleId,
      userId:user.id,
    }
  })
  axios.post(httpUrl+'/record/listPageCC',{
    pagesize:pageSize.value,
    pagenum:pageNum.value,
    param: {
      name:name.value,
      storage:storage.value,
      goodstype:goodstype.value,
      operationType:operationType.value,
      startDate:startDate.value,
      endDate:endDate.value,
      roleId:user.roleId,
      userId:user.id
    }
  }).then(res => {
    console.log('axios 响应:', res)
    const result = res.data  // 获取后端返回的 Result 对象
    console.log('后端返回的 Result:', result)
    console.log('result.code:', result.code)
    console.log('result.data:', result.data)
    console.log('result.total:', result.total)
    console.log('result.data 类型:', Array.isArray(result.data) ? '数组' : '非数组')
    if(result.code==200){
      // 直接使用 result.data，并确保是数组
      const newData = Array.isArray(result.data) ? result.data : []
      console.log('准备更新表格数据:', newData)
      tableData.value = newData
      total.value = result.total || 0
      console.log('表格数据已更新:', tableData.value, '总数:', total.value)
    }else {
      alert('获取数据失败')
    }
  }).catch(error => {
    console.error('请求失败:', error)
  })
}

/**
 * 导出Excel
 */
const handleExport = () => {
  console.log('开始导出，参数:', {
    pagesize:pageSize.value,
    pagenum:pageNum.value,
    param: {
      name:name.value,
      storage:storage.value,
      goodstype:goodstype.value,
      operationType:operationType.value,
      startDate:startDate.value,
      endDate:endDate.value,
      roleId:user.roleId,
      userId:user.id,
    }
  })
  
  axios.post(httpUrl+'/record/export',{
    pagesize:pageSize.value,
    pagenum:pageNum.value,
    param: {
      name:name.value,
      storage:storage.value,
      goodstype:goodstype.value,
      operationType:operationType.value,
      startDate:startDate.value,
      endDate:endDate.value,
      roleId:user.roleId,
      userId:user.id
    }
  }, {
    responseType: 'blob' // 重要：设置响应类型为 blob
  }).then(res => {
    // 创建下载链接
    const url = window.URL.createObjectURL(new Blob([res.data]))
    const link = document.createElement('a')
    link.href = url
    
    // 从响应头中获取文件名，如果没有则使用默认名称
    const contentDisposition = res.headers['content-disposition']
    let fileName = '出入库记录.xlsx'
    if (contentDisposition) {
      const fileNameMatch = contentDisposition.match(/filename\*=utf-8''(.+)/)
      if (fileNameMatch && fileNameMatch[1]) {
        fileName = decodeURIComponent(fileNameMatch[1])
      }
    }
    
    link.setAttribute('download', fileName)
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    
    $message.success('导出成功')
  }).catch(error => {
    console.error('导出失败:', error)
    $message.error('导出失败：' + (error.message || '未知错误'))
  })
}

onBeforeMount(() => {
  loadGoodsType()
  loadStorage()
  loadPost()

})
</script>

<template>
  <div>
    <!-- 顶部搜索区 -->
    <div style="margin-bottom: 10px;">
      <el-input v-model="name" @keyup.enter="loadPost" placeholder="请输入商品名" :suffix-icon="Search" style="width: 200px;"></el-input>
      <el-select v-model="storage" placeholder="请选择仓库" style="width: 200px; margin-left: 10px">
        <el-option
            v-for="item in storageData"
            :key="item.id"
            :label="item.name"
            :value="item.id">
        </el-option>
      </el-select>
      <el-select v-model="goodstype" placeholder="请选择分类" style="width: 200px;margin-left: 10px">
        <el-option
            v-for="item in goodstypeData"
            :key="item.id"
            :label="item.name"
            :value="item.id">
        </el-option>
      </el-select>
      <el-select v-model="operationType" placeholder="请选择操作类型" style="width: 200px;margin-left: 10px">
        <el-option label="全部" value=""></el-option>
        <el-option label="采购入库" value="采购入库"></el-option>
        <el-option label="销售出库" value="销售出库"></el-option>
        <el-option label="采购退货" value="采购退货"></el-option>
        <el-option label="销售退货" value="销售退货"></el-option>
        <el-option label="退货入库" value="退货入库"></el-option>
        <el-option label="盘点盈亏" value="盘点盈亏"></el-option>
        <el-option label="其他" value="其他"></el-option>
      </el-select>
      <el-date-picker
          v-model="startDate"
          type="date"
          placeholder="开始日期"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
          :disabled-date="disabledStartDate"
          style="width: 180px;margin-left: 10px">
      </el-date-picker>
      <el-date-picker
          v-model="endDate"
          type="date"
          placeholder="结束日期"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
          :disabled-date="disabledEndDate"
          style="width: 180px;margin-left: 10px">
      </el-date-picker>
      <el-button type="primary" style="margin-left: 10px;" @click="loadPost">查询</el-button>
      <el-button type="success" @click="resetParam" >重置</el-button>
      <el-button type="warning" @click="handleExport" :icon="Download">导出 Excel</el-button>
    </div>
    
    <!-- 数据表格 -->
    <el-table :data="tableData" style="width: 100%;"
              :header-cell-style="{background:'#f2f5fc',color:'#555'}"
              border
    >
      <el-table-column prop="id" label="记录ID" width="100"></el-table-column>
      <el-table-column prop="goodsname" label="商品名" width="150"></el-table-column>
      <el-table-column prop="storagename" label="仓库" width="120"></el-table-column>
      <el-table-column prop="goodstypename" label="分类" width="120"></el-table-column>
      <el-table-column prop="operationType" label="操作类型" width="120">
        <template #default="{row}">
          <el-tag :type="getOperationTypeTag(row.operationType)" size="small">
            {{ row.operationType || '未分类' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="refOrderNum" label="关联单号" width="180">
        <template #default="{row}">
          <el-link 
            v-if="row.refOrderNum" 
            type="primary" 
            :underline="false"
            @click="copyOrderNum(row.refOrderNum)"
            style="cursor: pointer;"
          >
            {{ row.refOrderNum }}
            <el-icon style="margin-left: 4px;"><CopyDocument /></el-icon>
          </el-link>
          <span v-else style="color: #909399;">-</span>
        </template>
      </el-table-column>
      <el-table-column prop="username" label="申请人" width="120"></el-table-column>
      <el-table-column prop="adminname" label="操作人" width="120"></el-table-column>
      <el-table-column prop="count" label="数量" width="120">
        <template #default="{row}">
          <span :style="{ color: row.count > 0 ? '#67c23a' : '#f56c6c', fontWeight: 'bold' }">
            {{ row.count > 0 ? '+' + row.count : row.count }}
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="createtime" label="操作时间" width="180">
        <template #default="{row}">
          {{ formatDateTime(row) }}
        </template>
      </el-table-column>
      <el-table-column prop="remark" label="备注" min-width="150"></el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{row}">
          <el-tag :type="formatStatus(row.status).type" size="small">
            {{ formatStatus(row.status).text }}
          </el-tag>
        </template>
      </el-table-column>
    </el-table>
    
    <!-- 分页组件 -->
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
        next-text="下一页">
    </el-pagination>
  </div>
</template>
<style scoped>
/* 抑制 ResizeObserver 错误 */
:deep(.el-pagination) {
  margin-top: 10px;
}
</style>