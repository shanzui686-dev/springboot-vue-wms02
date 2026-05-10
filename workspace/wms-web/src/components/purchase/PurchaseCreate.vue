<script setup>
import { ref, getCurrentInstance, computed } from 'vue'
import { Search } from '@element-plus/icons-vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const { proxy } = getCurrentInstance()
const httpUrl = proxy.$httpUrl
const $message = ElMessage

// 定义事件
const emit = defineEmits(['success', 'close'])

// 对话框控制
const dialogVisible = ref(false)

// 表单数据
const formRef = ref(null)
const form = ref({
  purchaseNo: '',
  supplierId: null,
  purchaseDate: new Date().toISOString().slice(0, 19).replace('T', ' '),
  totalAmount: 0,
  remark: '',
  details: []
})

// 供应商列表
const supplierData = ref([])

// 商品选择相关
const goodsSearchKeyword = ref('')
const goodsList = ref([])
const showGoodsSelect = ref(false)

// 表单验证规则
const rules = {
  supplierId: [
    { required: true, message: '请选择供应商', trigger: 'change' }
  ]
}

/**
 * 打开新建采购单对话框
 */
const open = () => {
  resetForm()
  generatePurchaseNo()
  loadSupplier()
  dialogVisible.value = true
}

/**
 * 关闭对话框
 */
const close = () => {
  dialogVisible.value = false
  emit('close')
}

/**
 * 重置表单
 */
const resetForm = () => {
  formRef.value?.resetFields()
  form.value = {
    purchaseNo: '',
    supplierId: null,
    purchaseDate: new Date().toISOString().slice(0, 19).replace('T', ' '),
    totalAmount: 0,
    remark: '',
    details: []
  }
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
  form.value.purchaseNo = `PO${year}${month}${day}${random}`
}

/**
 * 加载供应商列表
 */
const loadSupplier = () => {
  axios.get(httpUrl + '/supplier/list').then(res => {
    const result = res.data
    if (result.code === 200) {
      supplierData.value = Array.isArray(result.data) ? result.data : []
    }
  }).catch(error => {
    console.error('加载供应商失败:', error)
  })
}

/**
 * 搜索商品（根据供应商筛选）
 */
const searchGoods = () => {
  if (form.value.supplierId === null || form.value.supplierId === undefined) {
    $message.warning('请先选择供应商')
    return
  }

  // 准备请求参数
  const param = {}
  if (goodsSearchKeyword.value && goodsSearchKeyword.value.trim() !== '') {
    param.name = goodsSearchKeyword.value
    param.barcode = goodsSearchKeyword.value
  }

  // 使用 POST 请求标准的分页接口，确保能获取到数据
  axios.post(httpUrl + '/goods/listPage', {
    pagenum: 1,
    pagesize: 1000, // 获取足够多的商品用于选择
    param: param
  }).then(res => {
    console.log('商品列表接口返回:', res.data)
    if (res.data.code === 200) {
      let goods = res.data.data
      // 兼容后端返回结构：可能是数组，也可能是分页对象 {records: [], total: 0}
      if (goods && goods.records) {
        goods = goods.records
      } else if (!Array.isArray(goods)) {
        goods = []
      }
      
      console.log('过滤前商品数量:', goods.length)
      // 过滤出当前供应商的商品
      const filteredGoods = goods.filter(item => item.supplierId == form.value.supplierId)
      console.log('过滤后商品数量:', filteredGoods.length, '当前供应商ID:', form.value.supplierId)
      
      goodsList.value = filteredGoods
      
      if (filteredGoods.length === 0 && goods.length > 0) {
        $message.warning('该供应商暂无商品，请先在商品管理中为商品设置供应商')
      }
    }
  }).catch(error => {
    console.error('搜索商品失败:', error)
    $message.error('搜索商品失败')
  })
}

/**
 * 打开商品选择对话框时自动加载商品
 */
const openGoodsSelect = () => {
  goodsSearchKeyword.value = ''
  if (form.value.supplierId !== null && form.value.supplierId !== undefined) {
    searchGoods()
  }
}

/**
 * 添加商品到采购清单
 */
const addGoodsToCart = (goods) => {
  // 检查是否已存在
  const existIndex = form.value.details.findIndex(item => item.goodsId === goods.id)
  
  if (existIndex >= 0) {
    // 已存在，数量+1
    form.value.details[existIndex].count += 1
    calculateSubtotal(form.value.details[existIndex])
    $message.success(`已增加【${goods.name}】的数量`)
  } else {
    // 不存在，添加新行
    form.value.details.push({
      goodsId: goods.id,
      goodsName: goods.name,
      barcode: goods.barcode,
      specs: goods.specs,
      unit: goods.unit,
      currentStock: goods.count || 0,
      count: 1,
      price: goods.purchasePrice || 0,
      subtotal: 0,
      remark: ''
    })
    
    // 计算小计
    const newItem = form.value.details[form.value.details.length - 1]
    calculateSubtotal(newItem)
    
    $message.success(`已添加【${goods.name}】`)
  }
  
  showGoodsSelect.value = false
}

/**
 * 从采购清单删除商品
 */
const removeGoods = (index) => {
  form.value.details.splice(index, 1)
  calculateTotal()
}

/**
 * 计算小计
 */
const calculateSubtotal = (row) => {
  row.subtotal = (row.count || 0) * (row.price || 0)
  calculateTotal()
}

/**
 * 计算总金额
 */
const calculateTotal = () => {
  const total = form.value.details.reduce((sum, item) => {
    return sum + (item.subtotal || 0)
  }, 0)
  form.value.totalAmount = total
}

/**
 * 格式化金额
 */
const formatAmount = (amount) => {
  if (!amount && amount !== 0) return '-'
  return '¥' + Number(amount).toFixed(2)
}

/**
 * 提交订单
 */
const submitOrder = () => {
  formRef.value.validate((valid) => {
    if (valid) {
      // 验证明细
      if (!form.value.details || form.value.details.length === 0) {
        $message.warning('请至少添加一条采购明细')
        return
      }

      // 获取当前登录用户ID作为采购员
      let currentUserId = 1
      try {
        const userStr = localStorage.getItem('user')
        if (userStr) {
          const userInfo = JSON.parse(userStr)
          if (userInfo && userInfo.id) currentUserId = userInfo.id
        }
      } catch (e) {
        console.warn('获取当前用户ID失败，使用默认值', e)
      }

      // 准备提交数据
      const submitData = {
        purchaseNo: form.value.purchaseNo,
        supplierId: form.value.supplierId,
        userId: currentUserId, // 动态使用当前登录用户ID
        purchaseDate: form.value.purchaseDate,
        totalAmount: form.value.totalAmount,
        status: 0, // 默认待入库
        remark: form.value.remark,
        details: form.value.details.map(item => ({
          goodsId: item.goodsId,
          count: item.count,
          price: parseFloat(item.price) || 0,
          subtotal: parseFloat(item.subtotal) || 0,
          remark: item.remark
        }))
      }

      console.log('提交的数据:', submitData)

      axios.post(httpUrl + '/purchase/create', submitData).then(res => {
        if (res.data.code === 200) {
          $message.success('采购单创建成功!')
          dialogVisible.value = false
          emit('success')
          resetForm()
        } else {
          $message.error(res.data.msg || '创建失败!')
        }
      }).catch(error => {
        console.error('创建失败详情:', error)
        const errorMsg = error.response?.data?.msg || error.response?.data?.message || error.message
        $message.error('创建失败：' + errorMsg)
      })
    }
  })
}

/**
 * 暴露方法给父组件
 */
defineExpose({
  open,
  close
})
</script>

<template>
  <el-dialog
    title="新建采购单"
    v-model="dialogVisible"
    width="85%"
    :close-on-click-modal="false"
    center
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
      <!-- 头部信息 -->
      <el-card shadow="never" style="margin-bottom: 20px;">
        <template #header>
          <div style="font-weight: bold;">基本信息</div>
        </template>
        
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="采购单号">
              <el-input v-model="form.purchaseNo"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="供应商" prop="supplierId">
              <el-select 
                v-model="form.supplierId" 
                placeholder="请选择供应商" 
                style="width: 100%"
                @change="searchGoods"
              >
                <el-option
                  v-for="item in supplierData"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id"
                >
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="采购日期">
              <el-date-picker
                v-model="form.purchaseDate"
                type="datetime"
                placeholder="选择日期时间"
                style="width: 100%"
                value-format="YYYY-MM-DD HH:mm:ss"
              >
              </el-date-picker>
            </el-form-item>
          </el-col>
        </el-row>
      </el-card>

      <!-- 选择商品区 -->
      <el-card shadow="never" style="margin-bottom: 20px;">
        <template #header>
          <div style="display: flex; justify-content: space-between; align-items: center;">
            <span style="font-weight: bold;">选择商品</span>
            <el-button type="primary" size="small" @click="showGoodsSelect = true" :disabled="!form.supplierId">
              <el-icon style="margin-right: 5px;"><Search /></el-icon>
              选择商品
            </el-button>
          </div>
        </template>
        
        <div v-if="!form.supplierId" style="text-align: center; padding: 20px; color: #909399;">
          请先选择供应商，然后才能添加商品
        </div>
        <div v-else-if="form.details.length === 0" style="text-align: center; padding: 20px; color: #909399;">
          暂无商品，请点击右上角"选择商品"按钮添加
        </div>
      </el-card>

      <!-- 采购清单表格 -->
      <el-card shadow="never" v-if="form.details.length > 0">
        <template #header>
          <div style="font-weight: bold;">采购清单</div>
        </template>
        
        <el-table :data="form.details" border style="width: 100%">
          <el-table-column label="商品名称" width="180">
            <template #default="scope">
              {{ scope.row.goodsName }}
            </template>
          </el-table-column>
          <el-table-column label="条码" width="140">
            <template #default="scope">
              {{ scope.row.barcode || '-' }}
            </template>
          </el-table-column>
          <el-table-column label="规格" width="100">
            <template #default="scope">
              {{ scope.row.specs || '-' }}
            </template>
          </el-table-column>
          <el-table-column label="单位" width="80">
            <template #default="scope">
              {{ scope.row.unit || '-' }}
            </template>
          </el-table-column>
          <el-table-column label="当前库存" width="100">
            <template #default="scope">
              <span :style="{ color: scope.row.currentStock < 10 ? 'red' : '' }">
                {{ scope.row.currentStock }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="采购数量" width="140">
            <template #default="scope">
              <el-input-number 
                v-model="scope.row.count" 
                :min="1" 
                :precision="0"
                @change="calculateSubtotal(scope.row)"
                style="width: 100%"
              ></el-input-number>
            </template>
          </el-table-column>
          <el-table-column label="采购进价" width="140">
            <template #default="scope">
              <el-input-number 
                v-model="scope.row.price" 
                :min="0" 
                :precision="2"
                @change="calculateSubtotal(scope.row)"
                style="width: 100%"
              ></el-input-number>
            </template>
          </el-table-column>
          <el-table-column label="小计" width="120">
            <template #default="scope">
              <span style="color: #f56c6c; font-weight: bold;">
                {{ formatAmount(scope.row.subtotal) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="备注" min-width="150">
            <template #default="scope">
              <el-input 
                v-model="scope.row.remark" 
                placeholder="备注"
                size="small"
              ></el-input>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="80" fixed="right">
            <template #default="scope">
              <el-button 
                size="small" 
                type="danger" 
                @click="removeGoods(scope.$index)"
              >
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <!-- 底部结算区 -->
      <el-card shadow="never" style="margin-top: 20px; background-color: #f5f7fa;">
        <el-row :gutter="20" align="middle">
          <el-col :span="16">
            <el-form-item label="备注" prop="remark" label-width="60px">
              <el-input 
                v-model="form.remark" 
                placeholder="请输入备注信息"
                :rows="2"
                type="textarea"
              ></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <div style="text-align: right;">
              <div style="font-size: 14px; margin-bottom: 10px;">
                商品总数：<span style="color: #409eff; font-weight: bold;">{{ form.details.length }}</span> 种
              </div>
              <div style="font-size: 18px; margin-bottom: 15px;">
                采购总额：<span style="color: #f56c6c; font-size: 24px; font-weight: bold;">{{ formatAmount(form.totalAmount) }}</span>
              </div>
              <el-button 
                type="primary" 
                size="large" 
                @click="submitOrder"
                :loading="false"
                style="width: 150px;"
              >
                提交订单
              </el-button>
            </div>
          </el-col>
        </el-row>
      </el-card>
    </el-form>

    <!-- 商品选择对话框 -->
    <el-dialog
      title="选择商品"
      v-model="showGoodsSelect"
      width="70%"
      append-to-body
      center
      @open="openGoodsSelect"
    >
      <div style="margin-bottom: 15px;">
        <el-input
          v-model="goodsSearchKeyword"
          placeholder="输入条码或商品名搜索"
          :suffix-icon="Search"
          @keyup.enter="searchGoods"
          clearable
        >
          <template #append>
            <el-button @click="searchGoods">搜索</el-button>
          </template>
        </el-input>
      </div>
      
      <el-table 
        :data="goodsList" 
        border 
        style="width: 100%"
        max-height="400"
        highlight-current-row
      >
        <el-table-column prop="barcode" label="条码" width="140"></el-table-column>
        <el-table-column prop="name" label="商品名称" width="180"></el-table-column>
        <el-table-column prop="specs" label="规格" width="100"></el-table-column>
        <el-table-column prop="unit" label="单位" width="80"></el-table-column>
        <el-table-column prop="count" label="当前库存" width="100">
          <template #default="scope">
            <span :style="{ color: scope.row.count < 10 ? 'red' : '' }">
              {{ scope.row.count }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="purchasePrice" label="参考进价" width="100">
          <template #default="scope">
            {{ formatAmount(scope.row.purchasePrice) }}
          </template>
        </el-table-column>
        <el-table-column prop="retailPrice" label="零售价" width="100">
          <template #default="scope">
            {{ formatAmount(scope.row.retailPrice) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="scope">
            <el-button 
              size="small" 
              type="primary" 
              @click="addGoodsToCart(scope.row)"
            >
              添加
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div v-if="goodsList.length === 0" style="text-align: center; padding: 30px; color: #909399;">
        暂无数据，请输入搜索条件后点击搜索
      </div>
    </el-dialog>
  </el-dialog>
</template>

<style scoped>
:deep(.el-card__header) {
  padding: 12px 20px;
  background-color: #fafafa;
}

:deep(.el-card__body) {
  padding: 20px;
}
</style>
