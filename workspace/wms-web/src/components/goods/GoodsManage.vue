<script setup>
import {onMounted, onActivated, ref, reactive, getCurrentInstance} from 'vue'
import { Search } from '@element-plus/icons-vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'
const { proxy } = getCurrentInstance()
const httpUrl = proxy.$httpUrl
const $message = ElMessage

const user = JSON.parse(sessionStorage.getItem('user') || '{}')
const goodstypeData = ref([])
const supplierData = ref([])
const tableData = ref([])
const pageSize = ref(10)
const pageNum = ref(1)
const total = ref(0)
const name = ref('')
const categoryId = ref('')
const supplierId = ref('')
const isWarning = ref(false)
const centerDialogVisible = ref(false)
const batchDialogVisible = ref(false)
const batchFormMode = ref('add')     // 'add' | 'edit'

// 批次数据
const allBatches = ref([])           // 所有批次（含 goodsId + 仓库名/供应商名）

const formRef = ref()
const resetForm = () => {
  formRef.value?.resetFields();
}
const form = ref({
  id:'',
  name: '',
  goodsType:'',
  count:'',
  minCount:5,
  barcode: '',
  specs: '',
  unit: '',
  retailPrice: 0,
  purchasePrice: 0,
  supplierId: null,
  batchNo: '',
  remark: '',
})
const rules = {
  name: [
    { required: true, message: '请输入商品名', trigger: 'blur' }
  ],
  barcode: [
    { required: true, message: '请输入商品条码', trigger: 'blur' }
  ],
  goodsType: [
    { required: true, message: '请选择分类', trigger: 'change' }
  ],
  count: [
    { required: true, message: '请输入数量', trigger: 'blur' },
    { pattern: /^([1-9][0-9]*){1,4}$/, message: '数量必须为正整数', trigger: 'blur' }
  ],
  retailPrice: [
    { required: true, message: '请输入零售价', trigger: 'blur' }
  ]
}

// ===== 批次表单 =====
const batchFormRef = ref()
const batchForm = ref({
  id: null,
  goodsId: null,
  batchNo: '',
  storageId: null,
  supplierId: null,
  purchasePrice: 0,
  initialCount: 0,
  currentCount: 0,
})
const batchRules = {
  currentCount: [{ required: true, message: '请输入当前库存', trigger: 'blur' }],
}

// 获取当前商品的批次列表
const getBatchesForGoods = (goodsId) => {
  return allBatches.value.filter(b => b.goodsId == goodsId)
}

// 批次子表分页（按 goodsId 独立维护）
const batchPage = reactive({})  // { [goodsId]: { pageNum:1, pageSize:5 } }
const getBatchPage = (goodsId) => {
  if (!batchPage[goodsId]) {
    batchPage[goodsId] = { pageNum: 1, pageSize: 5 }
  }
  return batchPage[goodsId]
}
const getPagedBatches = (goodsId) => {
  const all = getBatchesForGoods(goodsId)
  const bp = getBatchPage(goodsId)
  const start = (bp.pageNum - 1) * bp.pageSize
  return all.slice(start, start + bp.pageSize)
}

// 可用库存 = 有批次时取各批次 currentCount 之和，否则 goods.count - reservedCount
const getAvailableCount = (row) => {
  const batches = getBatchesForGoods(row.id)
  if (batches.length > 0) {
    return batches.reduce((s, b) => s + (b.currentCount || 0), 0)
  }
  return (row.count ?? 0) - (row.reservedCount ?? 0)
}

const mod = (row) => {
  form.value.id = row.id
  form.value.name = row.name
  form.value.goodsType = row.goodsType || row.goodstype
  form.value.count = row.count
  form.value.reservedCount = row.reservedCount ?? 0
  form.value.minCount = row.minCount || 5
  form.value.barcode = row.barcode || ''
  form.value.specs = row.specs || ''
  form.value.unit = row.unit || ''
  form.value.retailPrice = row.retailPrice || 0
  form.value.purchasePrice = row.purchasePrice || 0
  form.value.supplierId = row.supplierId || null
  form.value.batchNo = ''
  form.value.remark = row.remark
  centerDialogVisible.value = true
}
const add = () => {
  resetForm()
  const now = new Date()
  const y = now.getFullYear()
  const M = String(now.getMonth() + 1).padStart(2, '0')
  const d = String(now.getDate()).padStart(2, '0')
  const h = String(now.getHours()).padStart(2, '0')
  const m = String(now.getMinutes()).padStart(2, '0')
  const autoBatchNo = `${y}${M}${d}${h}${m}`
  form.value = {
    id:'',
    name: '',
    goodsType:'',
    count:'',
    minCount: 5,
    barcode: '',
    specs: '',
    unit: '',
    retailPrice: 0,
    purchasePrice: 0,
    supplierId: null,
    batchNo: autoBatchNo,
    remark: '',
  }
  centerDialogVisible.value = true
}
const del = (id) => {
  axios.get(httpUrl+'/goods/del?id='+id).then(res => {
    if(res.data.code == 200) {
      centerDialogVisible.value = false
      form.value = {
        id:'', name: '', goodsType:'',
        count:'', minCount: '', remark: '',
      }
      initPage()
      $message.success('操作成功!')
    } else {
      $message.error('操作失败!')
    }
  })
}
const save = () => {
  if (!formRef.value) return
  formRef.value.validate((valid) => {
    if (valid) {
      const submitData = {
        name: form.value.name,
        goodsType: Number(form.value.goodsType),
        count: Number(form.value.count),
        minCount: form.value.minCount !== '' ? Number(form.value.minCount) : 5,
        barcode: form.value.barcode,
        specs: form.value.specs,
        unit: form.value.unit,
        retailPrice: form.value.retailPrice ? Number(form.value.retailPrice) : 0,
        purchasePrice: form.value.purchasePrice ? Number(form.value.purchasePrice) : 0,
        supplierId: form.value.supplierId ? Number(form.value.supplierId) : null,
        batchNo: form.value.batchNo || '',
        remark: form.value.remark
      }
      if(form.value.id){
        submitData.id = form.value.id
        axios.post(httpUrl+'/goods/update', submitData).then(res => {
          if(res.data.code == 200) {
            centerDialogVisible.value = false
            form.value = {
              id:'', name: '', goodsType:'', count:'', minCount: 5,
              barcode: '', specs: '', unit: '', retailPrice: 0,
              purchasePrice: 0, supplierId: null, remark: '',
            }
            initPage()
            $message.success('操作成功!')
          } else { $message.error('操作失败!') }
        })
      }else {
        axios.post(httpUrl+'/goods/save', submitData).then(res => {
          if(res.data.code == 200) {
            centerDialogVisible.value = false
            form.value = {
              id:'', name: '', goodsType:'', count:'', minCount: 5,
              barcode: '', specs: '', unit: '', retailPrice: 0,
              purchasePrice: 0, supplierId: null, remark: '',
            }
            initPage()
            $message.success('操作成功!')
          } else { $message.error('操作失败!') }
        })
      }
    }
  });
}

// ===== 批次 CRUD =====
const openBatchAdd = (goodsRow) => {
  batchFormMode.value = 'add'
  batchForm.value = {
    id: null,
    goodsId: goodsRow.id,
    batchNo: '',
    storageId: goodsRow.storage || null,
    supplierId: goodsRow.supplierId || null,
    purchasePrice: goodsRow.purchasePrice || 0,
    initialCount: 0,
    currentCount: 0,
  }
  batchDialogVisible.value = true
}

const openBatchEdit = (batch) => {
  batchFormMode.value = 'edit'
  batchForm.value = {
    id: batch.batchId || batch.id,
    goodsId: batch.goodsId,
    batchNo: batch.batchNum || '',
    storageId: batch.storageId || null,
    supplierId: batch.supplierId || null,
    purchasePrice: batch.purchasePrice || 0,
    initialCount: batch.initialCount || 0,
    currentCount: batch.currentCount || 0,
  }
  batchDialogVisible.value = true
}

const saveBatch = () => {
  // 手动校验（避免 el-form ref 在 dialog 中尚未挂载导致静默返回）
  if (batchForm.value.currentCount == null || batchForm.value.currentCount < 0) {
    $message.warning('当前库存不能为空')
    return
  }
  const url = batchFormMode.value === 'add'
    ? '/goods/batch/save'
    : '/goods/batch/update'
  axios.post(httpUrl + url, {
    id: batchForm.value.id,
    goodsId: batchForm.value.goodsId,
    batchNo: batchForm.value.batchNo,
    storageId: Number(batchForm.value.storageId),
    supplierId: batchForm.value.supplierId ? Number(batchForm.value.supplierId) : null,
    purchasePrice: batchForm.value.purchasePrice ? Number(batchForm.value.purchasePrice) : 0,
    initialCount: Number(batchForm.value.initialCount),
    currentCount: Number(batchForm.value.currentCount),
  }).then(res => {
    if (res.data.code === 200) {
      batchDialogVisible.value = false
      loadAllBatches().then(() => loadPost())
      $message.success('操作成功!')
    } else {
      $message.error('操作失败!')
    }
  }).catch(() => {
    $message.error('请求失败，请稍后重试')
  })
}

const delBatch = (batchId) => {
  axios.get(httpUrl + '/goods/batch/del?id=' + batchId).then(res => {
    if (res.data.code === 200) {
      loadAllBatches().then(() => loadPost())
      $message.success('删除成功!')
    } else {
      $message.error('删除失败!')
    }
  })
}

// 预警判断：可用库存 <= 安全库存时告急
const isAlert = (row) => {
  const minCount = row.minCount
  if (minCount != null && minCount >= 0) {
    return getAvailableCount(row) <= minCount
  }
  return false
}

const handleSizeChange = (val) => {
  pageNum.value = 1
  pageSize.value = val
  loadPost()
}

const handleCurrentChange = (val) => {
  pageNum.value = val
  loadPost()
}

const resetParam = () => {
  name.value = ''
  categoryId.value = ''
  supplierId.value = ''
  isWarning.value = false
  loadPost()
}

// 加载批次数据（按供应商筛选）
const loadAllBatches = () => {
  const param = {}
  if (supplierId.value !== '' && supplierId.value != null) {
    param.supplierId = supplierId.value
  }
  return axios.post(httpUrl + '/goods/batchList', {
    pagenum: 1,
    pagesize: 10000,
    param
  }).then(res => {
    if (res.data.code === 200) {
      const data = res.data.data?.records || res.data.data || []
      allBatches.value = Array.isArray(data) ? data : []
      if (allBatches.value.length > 0) {
        console.log('批次数据字段:', Object.keys(allBatches.value[0]))
        console.log('第一条批次:', allBatches.value[0])
      } else {
        console.log('批次数据为空')
      }
    }
  }).catch(() => {})
}

const loadPost = () => {
  axios.post(httpUrl+'/goods/listPage',{
    pagesize:pageSize.value,
    pagenum:pageNum.value,
    param: {
      name:name.value,
      categoryId:categoryId.value,
      supplierId:supplierId.value,
      isWarning:isWarning.value
    }
  }).then(res => {
    const result = res.data
    if(result.code==200){
      const newData = Array.isArray(result.data) ? result.data : []
      tableData.value = newData
      total.value = result.total || 0
      loadAllBatches()
    }else {
      alert('获取数据失败')
    }
  }).catch(error => {
    console.error('请求失败:', error)
  })
}

const handleWarningChange = () => {
  loadPost()
}

const initPage = () => {
  // 下拉数据与列表加载互不阻塞（收银员无供应商权限时列表也要正常显示）
  Promise.allSettled([
    loadGoodsType(),
    loadSupplier()
  ])
  loadPost()
}

onMounted(() => { initPage() })
onActivated(() => { initPage() })

const loadGoodsType = () => {
  return axios.get(httpUrl+'/goodstype/list').then(res => {
    if(res.data.code==200){
      goodstypeData.value = Array.isArray(res.data.data) ? res.data.data : []
    }
  })
}
const loadSupplier = () => {
  return axios.get(httpUrl+'/supplier/list').then(res => {
    if(res.data.code==200){
      supplierData.value = Array.isArray(res.data.data) ? res.data.data : []
    }
  })
}
</script>

<template>
  <div>
    <div style="margin-bottom: 10px;">
      <el-input v-model="name" @keyup.enter="loadPost" placeholder="请输入商品名" :suffix-icon="Search" style="width: 200px;"></el-input>
      <el-select v-model="categoryId" placeholder="请选择分类" style="width: 200px;margin-left: 5px">
        <el-option v-for="item in goodstypeData" :key="item.id" :label="item.name" :value="item.id" />
      </el-select>
      <el-select v-model="supplierId" placeholder="请选择供应商" style="width: 180px; margin-left: 5px" clearable>
        <el-option v-for="item in supplierData" :key="item.id" :label="item.name" :value="item.id" />
      </el-select>
      <el-checkbox v-model="isWarning" @change="handleWarningChange" style="margin-left: 10px;">只看告急商品</el-checkbox>
      <el-button type="primary" style="margin-left: 10px;" @click="loadPost">查询</el-button>
      <el-button type="success" @click="resetParam">重置</el-button>
      <el-button type="primary" style="margin-left: 10px;" @click="add" v-if="user.roleId===0 || user.roleId===1">新增</el-button>
    </div>
    <el-table :data="tableData" style="width: 100%;"
              :header-cell-style="{background:'#f2f5fc',color:'#555'}" border>
      <el-table-column type="expand">
        <template #default="{ row }">
          <div style="padding: 10px 30px;">
            <div style="display: flex; align-items: center; margin-bottom: 8px;">
              <span style="font-weight: bold; color: #303133;">
                批次库存明细（共 {{ getBatchesForGoods(row.id).length }} 个批次）
              </span>
              <el-button v-if="user.roleId===0 || user.roleId===1" type="primary" size="small" style="margin-left: 12px;" @click="openBatchAdd(row)">
                新增批次
              </el-button>
            </div>
            <el-table
              v-if="getBatchesForGoods(row.id).length > 0"
              :data="getPagedBatches(row.id)"
              border
              size="small"
              style="width: 100%;"
            >
              <el-table-column label="批次号" width="160">
                <template #default="{ row: b }">{{ b.batchNum || '-' }}</template>
              </el-table-column>
              <el-table-column prop="supplierName" label="供应商" min-width="140" />
              <el-table-column label="进价" width="100" align="right">
                <template #default="{ row: b }">¥{{ Number(b.purchasePrice || 0).toFixed(2) }}</template>
              </el-table-column>
              <el-table-column label="售价" width="100" align="right">
                <template #default>¥{{ Number(row.retailPrice || 0).toFixed(2) }}</template>
              </el-table-column>
              <el-table-column prop="currentCount" label="当前库存" width="90" align="center">
                <template #default="{ row: b }">
                  <el-tag :type="b.currentCount > 0 ? 'success' : 'danger'" size="small">{{ b.currentCount }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="initialCount" label="原始入库" width="90" align="center" />
              <el-table-column v-if="user.roleId===0 || user.roleId===1" label="操作" width="140" align="center">
                <template #default="{ row: b }">
                  <el-button size="small" type="success" @click="openBatchEdit(b)">编辑</el-button>
                  <el-popconfirm title="确定删除该批次吗？" @confirm="delBatch(b.batchId || b.id)">
                    <template #reference>
                      <el-button size="small" type="danger">删除</el-button>
                    </template>
                  </el-popconfirm>
                </template>
              </el-table-column>
            </el-table>
            <el-pagination
              v-if="getBatchesForGoods(row.id).length > 5"
              small
              layout="prev, pager, next"
              :total="getBatchesForGoods(row.id).length"
              :page-size="getBatchPage(row.id).pageSize"
              :current-page="getBatchPage(row.id).pageNum"
              @current-change="(val) => getBatchPage(row.id).pageNum = val"
              style="margin-top: 6px; justify-content: flex-end; display: flex;"
            />
            <el-empty v-if="getBatchesForGoods(row.id).length === 0" description="暂无批次数据" :image-size="50" />
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="id" label="ID" width="60" align="center" />
      <el-table-column prop="barcode" label="商品条码" width="130">
        <template #default="{ row }">{{ row.barcode || '-' }}</template>
      </el-table-column>
      <el-table-column prop="name" label="商品名" min-width="120" />
      <el-table-column prop="specs" label="规格" width="80">
        <template #default="{ row }">{{ row.specs || '-' }}</template>
      </el-table-column>
      <el-table-column prop="unit" label="单位" width="60" align="center">
        <template #default="{ row }">{{ row.unit || '-' }}</template>
      </el-table-column>

      <el-table-column label="分类" width="80">
        <template #default="{ row }">{{ row.categoryName || '-' }}</template>
      </el-table-column>

      <el-table-column prop="reservedCount" label="预占数量" width="90" align="center">
        <template #default="{ row }">{{ row.reservedCount ?? 0 }}</template>
      </el-table-column>
      <el-table-column label="可用库存" width="90" align="center">
        <template #default="{ row }">
          <el-tag v-if="isAlert(row)" type="danger" effect="dark" style="font-weight: bold;">
            {{ getAvailableCount(row) }}
          </el-tag>
          <el-tag v-else type="success" effect="plain">
            {{ getAvailableCount(row) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="minCount" label="安全库存" width="80" align="center">
        <template #default="{ row }">
          <span :style="{ color: isAlert(row) ? 'red' : '' }">{{ row.minCount || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="remark" label="备注" min-width="100" show-overflow-tooltip />
      <el-table-column label="操作" width="130" align="center" v-if="user.roleId===0 || user.roleId===1">
        <template #default="scope">
          <el-button size="small" type="success" @click="mod(scope.row)">编辑</el-button>
          <el-popconfirm title="确定删除吗？" @confirm="del(scope.row.id)">
            <template #reference>
              <el-button size="small" type="danger">删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="pageNum"
        :page-sizes="[2, 5, 10, 20]"
        :page-size="pageSize"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        :pager-count="5"
        prev-text="上一页"
        next-text="下一页" />
    <el-dialog title="商品维护" v-model="centerDialogVisible" width="50%" center>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="商品名" prop="name">
              <el-input v-model="form.name" placeholder="请输入商品名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="商品条码" prop="barcode">
              <el-input v-model="form.barcode" placeholder="请输入商品条码" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="规格" prop="specs">
              <el-input v-model="form.specs" placeholder="例如：500ml" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="单位" prop="unit">
              <el-input v-model="form.unit" placeholder="例如：瓶、个" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="分类" prop="goodsType">
              <el-select v-model="form.goodsType" placeholder="请选择分类" style="width: 100%">
                <el-option v-for="item in goodstypeData" :key="item.id" :label="item.name" :value="item.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="零售价" prop="retailPrice">
              <el-input-number v-model="form.retailPrice" :precision="2" :min="0" :step="0.01" style="width: 100%" placeholder="请输入零售价" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="进价" prop="purchasePrice">
              <el-input-number v-model="form.purchasePrice" :precision="2" :min="0" :step="0.01" style="width: 100%" placeholder="请输入进价" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="供应商" prop="supplierId">
              <el-select v-model="form.supplierId" placeholder="请选择供应商" style="width: 100%" clearable>
                <el-option v-for="item in supplierData" :key="item.id" :label="item.name" :value="item.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="安全库存下限" prop="minCount">
              <el-input-number v-model="form.minCount" :min="0" :step="1" style="width: 100%" placeholder="默认值5" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="数量" prop="count">
              <el-input v-model="form.count" placeholder="请输入初始数量" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="批次号" prop="batchNo">
              <el-input v-model="form.batchNo" placeholder="留空或输入前缀自动生成（如 20260530143001）" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注" prop="remark">
          <el-input type="textarea" v-model="form.remark" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="centerDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="save">确 定</el-button>
      </template>
    </el-dialog>

    <!-- 批次维护对话框 -->
    <el-dialog :title="batchFormMode==='add' ? '新增批次' : '编辑批次'" v-model="batchDialogVisible" width="40%" center>
      <el-form ref="batchFormRef" :model="batchForm" :rules="batchRules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="批次号" prop="batchNo">
              <el-input v-model="batchForm.batchNo" placeholder="留空自动生成（yyyyMMddHHmmXX）" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="供应商">
              <el-select v-model="batchForm.supplierId" placeholder="请选择供应商" style="width: 100%" clearable>
                <el-option v-for="item in supplierData" :key="item.id" :label="item.name" :value="item.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="进价">
              <el-input-number v-model="batchForm.purchasePrice" :precision="2" :min="0" :step="0.01" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="当前库存" prop="currentCount">
              <el-input-number v-model="batchForm.currentCount" :min="0" :step="1" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="原始入库">
              <el-input-number v-model="batchForm.initialCount" :min="0" :step="1" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="batchDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="saveBatch">确 定</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<style scoped>
:deep(.el-pagination) { margin-top: 10px; }
</style>