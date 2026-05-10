<script setup>
import {onMounted, ref, getCurrentInstance} from 'vue'
import { Search, Download, ShoppingCart } from '@element-plus/icons-vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const { proxy } = getCurrentInstance()
const httpUrl = proxy.$httpUrl
const $message = ElMessage

const user = JSON.parse(sessionStorage.getItem('user') || '{}')
const tableData = ref([])
const cycleDays = ref(7)
const goodstypeData = ref([])

// 加载分类数据
const loadGoodsType = () => {
  axios.get(httpUrl + '/goodstype/list').then(res => {
    const result = res.data
    if (result.code === 200) {
      goodstypeData.value = Array.isArray(result.data) ? result.data : []
    }
  }).catch(error => {
    console.error('请求失败:', error)
  })
}

// 格式化分类名称
const formatGoodsType = (row) => {
  const temp = goodstypeData.value.find(item => item.id === row.categoryId)
  return temp ? temp.name : '-'
}

// 加载补货建议数据
const loadRestockData = () => {
  axios.get(httpUrl + '/stats/suggestRestock', {
    params: {
      cycleDays: cycleDays.value
    }
  }).then(res => {
    const result = res.data
    if (result.code === 200) {
      tableData.value = Array.isArray(result.data) ? result.data : []
      $message.success('数据计算成功')
    } else {
      $message.error('获取数据失败')
    }
  }).catch(error => {
    console.error('请求失败:', error)
    $message.error('请求失败')
  })
}

// 导出报表
const exportReport = () => {
  if (tableData.value.length === 0) {
    $message.warning('暂无数据可导出')
    return
  }
  
  // 调用后端接口导出Excel
  axios.get(httpUrl + '/stats/exportRestockSuggestion', {
    params: {
      cycleDays: cycleDays.value
    },
    responseType: 'blob' // 重要：设置响应类型为 blob
  }).then(res => {
    // 创建下载链接
    const blob = new Blob([res.data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const link = document.createElement('a')
    const url = URL.createObjectURL(blob)
    
    link.setAttribute('href', url)
    link.setAttribute('download', `智能补货建议_${new Date().getTime()}.xlsx`)
    link.style.visibility = 'hidden'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    URL.revokeObjectURL(url)
    
    $message.success('导出成功')
  }).catch(error => {
    console.error('导出失败:', error)
    $message.error('导出失败')
  })
}

// 快捷采购
const quickPurchase = (row) => {
  // 跳转到采购单创建页面，并携带商品信息
  const purchaseData = {
    goodsId: row.goodsId,
    goodsName: row.goodsName,
    suggestQuantity: row.suggestQuantity
  }
  
  // 使用 sessionStorage 传递数据
  sessionStorage.setItem('quickPurchaseData', JSON.stringify(purchaseData))
  
  // 触发路由跳转或打开采购单创建对话框
  // 这里使用自定义事件通知父组件
  window.dispatchEvent(new CustomEvent('quick-purchase', { detail: purchaseData }))
  
  $message.success(`已将【${row.goodsName}】添加到采购单`)
}

// 组件挂载时加载数据
onMounted(() => {
  loadGoodsType()
  loadRestockData()
})
</script>

<template>
  <div>
    <!-- 顶部控制台 -->
    <div style="margin-bottom: 15px; display: flex; align-items: center; gap: 10px;">
      <span style="font-weight: bold;">预计采购天数：</span>
      <el-input-number 
        v-model="cycleDays" 
        :min="1" 
        :max="90" 
        :step="1"
        style="width: 150px;"
      />
      <el-button type="primary" :icon="Search" @click="loadRestockData">重新计算</el-button>
      <el-button type="success" :icon="Download" @click="exportReport">导出报表</el-button>
    </div>

    <!-- 核心数据表格 -->
    <el-table 
      :data="tableData" 
      style="width: 100%;"
      :header-cell-style="{background:'#f2f5fc',color:'#555'}"
      border
    >
      <el-table-column prop="barcode" label="商品条码" width="130">
        <template #default="scope">
          {{ scope.row.barcode || '-' }}
        </template>
      </el-table-column>
      
      <el-table-column prop="goodsName" label="商品名称" min-width="150" show-overflow-tooltip />
      
      <el-table-column prop="categoryId" label="分类" width="100">
        <template #default="scope">
          {{ formatGoodsType(scope.row) }}
        </template>
      </el-table-column>
      
      <el-table-column prop="currentStock" label="当前库存" width="100" align="center">
        <template #default="scope">
          <el-tag v-if="scope.row.currentStock <= scope.row.safetyStock" type="danger" effect="dark">
            {{ scope.row.currentStock }}
          </el-tag>
          <span v-else>{{ scope.row.currentStock }}</span>
        </template>
      </el-table-column>
      
      <el-table-column prop="safetyStock" label="安全库存" width="100" align="center" />
      
      <el-table-column prop="dailyAverageSales" label="近30天日均销量" width="130" align="right">
        <template #default="scope">
          {{ scope.row.dailyAverageSales ? Number(scope.row.dailyAverageSales).toFixed(2) : '0.00' }}
        </template>
      </el-table-column>
      
      <el-table-column prop="suggestQuantity" label="建议采购量" width="120" align="center">
        <template #default="scope">
          <span style="color: #f56c6c; font-weight: bold; font-size: 16px;">
            {{ scope.row.suggestQuantity || 0 }}
          </span>
        </template>
      </el-table-column>
      
      <el-table-column label="操作" width="120" align="center">
        <template #default="scope">
          <el-button 
            size="small" 
            type="primary" 
            :icon="ShoppingCart"
            @click="quickPurchase(scope.row)"
          >
            快捷采购
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<style scoped>
</style>
