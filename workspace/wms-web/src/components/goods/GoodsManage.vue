<script setup>
import {onMounted, ref, getCurrentInstance} from 'vue'
import { Search } from '@element-plus/icons-vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import SelectUser from "@/components/user/SelectUser.vue";

const { proxy } = getCurrentInstance()
const httpUrl = proxy.$httpUrl
const $message = ElMessage

const user = JSON.parse(sessionStorage.getItem('user') || '{}')
const goodstypeData = ref([])
const storageData = ref([])
const supplierData = ref([])
const tableData = ref([])
const pageSize = ref(10)
const pageNum = ref(1)
const total = ref(0)
const name = ref('')
const warehouseId = ref('')
const categoryId = ref('')
const isWarning = ref(false) // 只看告急商品
const centerDialogVisible = ref(false)
const inDialogVisible = ref(false)
const outDialogVisible = ref(false)
const innerVisible = ref(false)
const formRef = ref()
const resetForm = () => {
  formRef.value?.resetFields();
}
const resetInForm = () => {
  formRef1.value?.resetFields();
}
const resetOutForm = () => {
  formRef2.value?.resetFields();
}
let checkCount = (rule, value, callback) => {
  if(value>9999){
    callback(new Error('数量输⼊过'));
  }else{
    callback();
  }
};
const form = ref({
  id:'',
  name: '',
  storage:'',
  goodsType:'',
  count:'',
  minCount:5,
  barcode: '',
  specs: '',
  unit: '',
  retailPrice: 0,
  purchasePrice: 0,
  supplierId: null,
  remark: '',
})
const rules = {
  name: [
    { required: true, message: '请输入商品名', trigger: 'blur' }
  ],
  barcode: [
    { required: true, message: '请输入商品条码', trigger: 'blur' }
  ],
  storage: [
    { required: true, message: '请选择仓库', trigger: 'change' }
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
const form1 = ref({
  goods: '',
  goodsname:'',
  count:'',
  username:'',
  userId:'',
  adminId:'',
  remark: '',
})
const form2 = ref({
  goods: '',
  goodsname:'',
  count:'',
  username:'',
  userId:'',
  adminId:'',
  remark: '',
})
const currentRow = ref({})
const tempUser = ref({})
const tempOutUser = ref({})
const formRef1 = ref()
const formRef2 = ref()
const rules1 = {
  count: [
    {required: true, message: '请输入数量', trigger: 'blur'},
    {pattern: /^([1-9][0-9]*){1,4}$/,message: '数量必须为正整数字',trigger: "blur"},
    {validator:checkCount,trigger: 'blur'}
  ],
  userId: [
    {
      validator: (rule, value, callback) => {
        if (!value || value === 0 || value === '0' || value === '') {
          callback(new Error('请选择申请人'));
        } else {
          callback();
        }
      },
      trigger: 'change'
    }
  ]
}
const rules2 = {
  count: [
    {required: true, message: '请输入数量', trigger: 'blur'},
    {pattern: /^([1-9][0-9]*){1,4}$/,message: '数量必须为正整数字',trigger: "blur"},
    {validator:checkCount,trigger: 'blur'}
  ],
  userId: [
    {
      validator: (rule, value, callback) => {
        if (!value || value === 0 || value === '0' || value === '') {
          callback(new Error('请选择申请人'));
        } else {
          callback();
        }
      },
      trigger: 'change'
    }
  ]
}
const formatStorage = (row) =>{
  let temp=storageData.value.find(item => {
     return item.id === row.storage
  })
  return temp ? temp.name : row.storage
}
const selectCurrentChange = (val) =>{
  currentRow.value = val;
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

const mod = (row) => {
  form.value.id = row.id
  form.value.name = row.name
  form.value.storage = row.storage
  form.value.goodsType = row.goodsType || row.goodstype
  form.value.count = row.count
  form.value.minCount = row.minCount || 5
  form.value.barcode = row.barcode || ''
  form.value.specs = row.specs || ''
  form.value.unit = row.unit || ''
  form.value.retailPrice = row.retailPrice || 0
  form.value.purchasePrice = row.purchasePrice || 0
  form.value.supplierId = row.supplierId || null
  form.value.remark = row.remark
  centerDialogVisible.value = true
}
const add = () => {
  resetForm()
  form.value = {
    id:'',
    name: '',
    storage:'',
    goodsType:'',
    count:'',
    minCount: 5,
    barcode: '',
    specs: '',
    unit: '',
    retailPrice: 0,
    purchasePrice: 0,
    supplierId: null,
    remark: '',
  }
  centerDialogVisible.value = true
}
const del = (id) => {
  axios.get(httpUrl+'/goods/del?id='+id).then(res => {
    console.log('保存结果:', res)
    if(res.data.code == 200) {
      centerDialogVisible.value = false

      // 重置表单
      form.value = {
        id:'',
        name: '',
        storage:'',
        goodstype:'',
        count:'',
        minCount: '',
        remark: '',

      }
      loadPost() // 刷新列表
      $message.success('操作成功!')
    } else {
      $message.error('操作失败!')
    }
  })
}
const save = () => {
  console.log('save 被调用')
  // 验证必填字段
  if (!formRef.value) {
    console.error('formRef 为空')
    return
  }
  formRef.value.validate((valid) => {
    console.log('表单验证结果:', valid)
    if (valid) {
      // 准备提交的数据
      const submitData = {
        name: form.value.name,
        storage: Number(form.value.storage),
        goodsType: Number(form.value.goodsType),
        count: Number(form.value.count),
        minCount: form.value.minCount !== '' ? Number(form.value.minCount) : 5,
        barcode: form.value.barcode,
        specs: form.value.specs,
        unit: form.value.unit,
        retailPrice: form.value.retailPrice ? Number(form.value.retailPrice) : 0,
        purchasePrice: form.value.purchasePrice ? Number(form.value.purchasePrice) : 0,
        supplierId: form.value.supplierId ? Number(form.value.supplierId) : null,
        remark: form.value.remark
      }

      console.log('保存数据:', submitData)

      // 判断是新增还是修改
      if(form.value.id){
        // 修改操作
        submitData.id = form.value.id
        axios.post(httpUrl+'/goods/update', submitData).then(res => {
          console.log('保存结果:', res)
          if(res.data.code == 200) {
            centerDialogVisible.value = false
            // 重置表单
            form.value = {
              id:'',
              name: '',
              storage:'',
              goodstype:'',
              count:'',
              minCount: 5,
              barcode: '',
              specs: '',
              unit: '',
              retailPrice: 0,
              purchasePrice: 0,
              supplierId: null,
              remark: '',
            }
            loadPost() // 刷新列表
            $message.success('操作成功!')
          } else {
            $message.error('操作失败!')
          }
        }).catch(error => {
          console.error('保存失败:', error)
          $message.error('保存失败：' + error.message)
        })
      }else {
        // 新增操作
        axios.post(httpUrl+'/goods/save', submitData).then(res => {
          console.log('保存结果:', res)
          if(res.data.code == 200) {
            centerDialogVisible.value = false
            // 重置表单
            form.value = {
              id:'',
              name: '',
              storage:'',
              goodstype:'',
              count:'',
              minCount: 5,
              barcode: '',
              specs: '',
              unit: '',
              retailPrice: 0,
              purchasePrice: 0,
              supplierId: null,
              remark: '',
            }
            loadPost() // 刷新列表
            $message.success('操作成功!')
          } else {
            $message.error('操作失败!')
          }
        }).catch(error => {
          console.error('保存失败:', error)
          $message.error('保存失败：' + error.message)
        })
      }
    } else {
      console.log('error submit!!');
      return false;
    }
  });
}

// 判断是否需要预警
const isAlert = (row) => {
  if (row.minCount && row.count !== undefined) {
    return row.count <= row.minCount
  }
  return false
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

const inGoods = () => {
  if (!currentRow.value || !currentRow.value.id) {
    $message.warning('请先选择要入库的商品')
    return
  }
  form1.value.goods = currentRow.value.id
  form1.value.goodsname = currentRow.value.name
  form1.value.count = ''
  form1.value.userId = ''
  form1.value.username = ''
  form1.value.remark = ''
  inDialogVisible.value = true
}

const outGoods = () => {
  if (!currentRow.value || !currentRow.value.id) {
    $message.warning('请先选择要出库的商品')
    return
  }
  form2.value.goods = currentRow.value.id
  form2.value.goodsname = currentRow.value.name
  form2.value.count = ''
  form2.value.userId = ''
  form2.value.username = ''
  form2.value.remark = ''
  outDialogVisible.value = true
}

const openUserSelect = () => {
  innerVisible.value = true
}

const openOutUserSelect = () => {
  innerVisible.value = true
}

const doSelectUser = (user) => {
  tempUser.value = user
  form1.value.userId = user.id
  form1.value.username = user.name
}

const doSelectOutUser = (user) => {
  tempOutUser.value = user
  form2.value.userId = user.id
  form2.value.username = user.name
}

const confirmUser = () => {
  if (!tempUser.value || !tempUser.value.id) {
    $message.warning('请选择申请人')
    return
  }
  innerVisible.value = false
}

const confirmOutUser = () => {
  if (!tempOutUser.value || !tempOutUser.value.id) {
    $message.warning('请选择申请人')
    return
  }
  innerVisible.value = false
}

const saveIn = () => {
  console.log('========== 开始入库保存 ==========')
  console.log('当前form1完整数据:', JSON.stringify(form1.value))
  console.log('form1.userId:', form1.value.userId, '类型:', typeof form1.value.userId)
  console.log('form1.username:', form1.value.username)
  console.log('user.id:', user.id)
  
  if (!formRef1.value) {
    console.error('formRef1 为空')
    return
  }
  
  // 在验证前检查申请人
  const currentUserId = form1.value.userId
  console.log('检查userId值:', currentUserId, '是否为空:', !currentUserId, '是否为0:', currentUserId === 0, '是否为1:', currentUserId === 1)
  
  if (!currentUserId || currentUserId === 0 || currentUserId === '0' || currentUserId === '') {
    $message.warning('请先选择申请人')
    console.error('userId无效，无法提交')
    return
  }
  
  const userIdValue = Number(currentUserId)
  console.log('准备提交的userId值:', userIdValue, '类型:', typeof userIdValue)
  
  if (isNaN(userIdValue) || userIdValue <= 0) {
    $message.error('申请人ID无效，请重新选择')
    console.error('userId不是有效数字:', userIdValue)
    return
  }
  
  formRef1.value.validate((valid) => {
    if (valid) {
      const submitData = {
        goods: Number(form1.value.goods),
        count: Number(form1.value.count),
        userId: userIdValue,
        adminId: user.id,
        remark: form1.value.remark
      }
      console.log('========== 入库提交数据 ==========')
      console.log('提交数据:', JSON.stringify(submitData))
      console.log('申请人ID:', submitData.userId, '类型:', typeof submitData.userId)
      console.log('操作人ID:', submitData.adminId, '类型:', typeof submitData.adminId)
      
      axios.post(httpUrl+'/record/save', submitData).then(res => {
        console.log('入库保存结果:', res.data)
        if(res.data.code == 200) {
          inDialogVisible.value = false
          form1.value = {
            goods: '',
            goodsname:'',
            count:'',
            userId:'',
            admin_id:'',
            remark: '',
          }
          loadPost()
          $message.success('入库成功!')
        } else {
          $message.error('操作失败!')
        }
      }).catch(error => {
        console.error('保存失败:', error)
        $message.error('保存失败：' + error.message)
      })
    } else {
      console.log('error submit!!');
      return false;
    }
  });
}

const saveOut = () => {
  console.log('========== 开始出库保存 ==========')
  console.log('当前form2完整数据:', JSON.stringify(form2.value))
  console.log('form2.userId:', form2.value.userId, '类型:', typeof form2.value.userId)
  console.log('form2.username:', form2.value.username)
  console.log('user.id:', user.id)
  
  if (!formRef2.value) {
    console.error('formRef2 为空')
    return
  }
  
  // 在验证前检查申请人
  const currentUserId = form2.value.userId
  console.log('检查userId值:', currentUserId, '是否为空:', !currentUserId, '是否为0:', currentUserId === 0, '是否为1:', currentUserId === 1)
  
  if (!currentUserId || currentUserId === 0 || currentUserId === '0' || currentUserId === '') {
    $message.warning('请先选择申请人')
    console.error('userId无效，无法提交')
    return
  }
  
  const userIdValue = Number(currentUserId)
  console.log('准备提交的userId值:', userIdValue, '类型:', typeof userIdValue)
  
  if (isNaN(userIdValue) || userIdValue <= 0) {
    $message.error('申请人ID无效，请重新选择')
    console.error('userId不是有效数字:', userIdValue)
    return
  }
  
  formRef2.value.validate((valid) => {
    if (valid) {
      const submitData = {
        goods: Number(form2.value.goods),
        count: Number(form2.value.count),
        userId: userIdValue,
        adminId: user.id,
        remark: form2.value.remark
      }
      console.log('========== 出库提交数据 ==========')
      console.log('提交数据:', JSON.stringify(submitData))
      console.log('申请人ID:', submitData.userId, '类型:', typeof submitData.userId)
      console.log('操作人ID:', submitData.adminId, '类型:', typeof submitData.adminId)
      
      axios.post(httpUrl+'/record/out', submitData).then(res => {
        console.log('出库保存结果:', res.data)
        if(res.data.code == 200) {
          outDialogVisible.value = false
          form2.value = {
            goods: '',
            goodsname:'',
            count:'',
            userId:'',
            admin_id:'',
            remark: '',
          }
          loadPost()
          $message.success('出库成功!')
        } else {
          $message.error(res.data.msg || '操作失败!')
        }
      }).catch(error => {
        console.error('保存失败:', error)
        $message.error('保存失败：' + error.message)
      })
    } else {
      console.log('error submit!!');
      return false;
    }
  });
}

const resetParam = () => {
  name.value = ''
  warehouseId.value = ''
  categoryId.value = ''
  isWarning.value = false
  loadPost()
}
const loadPost = () => {
  console.log('开始查询，参数:', {
    pagesize:pageSize.value,
    pagenum:pageNum.value,
    param: {
      name:name.value,
      warehouseId:warehouseId.value,
      categoryId:categoryId.value,
      isWarning:isWarning.value
    }
  })
  axios.post(httpUrl+'/goods/listPage',{
    pagesize:pageSize.value,
    pagenum:pageNum.value,
    param: {
      name:name.value,
      warehouseId:warehouseId.value,
      categoryId:categoryId.value,
      isWarning:isWarning.value
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
// 监听告急商品筛选变化，自动触发查询
const handleWarningChange = () => {
  loadPost()
}

// 组件挂载时并发加载所有下拉数据
onMounted(() => {
  Promise.all([
    loadGoodsType(),
    loadStorage(),
    loadSupplier()
  ]).then(() => {
    // 所有数据加载完成后，再加载表格数据
    loadPost()
  }).catch(error => {
    console.error('数据加载失败:', error)
    $message.error('数据加载失败')
  })
})

const loadStorage = () => {
  return axios.get(httpUrl+'/storage/list').then(res => {
    const result = res.data
    if(result.code==200){
      const newData = Array.isArray(result.data) ? result.data : []
      storageData.value = newData
    }
  }).catch(error => {
    console.error('请求失败:', error)
  })
}
const loadGoodsType = () => {
  return axios.get(httpUrl+'/goodstype/list').then(res => {
    const result = res.data
    if(result.code==200){
      const newData = Array.isArray(result.data) ? result.data : []
      goodstypeData.value = newData
    }
  }).catch(error => {
    console.error('请求失败:', error)
  })
}
const loadSupplier = () => {
  return axios.get(httpUrl+'/supplier/list').then(res => {
    const result = res.data
    if(result.code==200){
      const newData = Array.isArray(result.data) ? result.data : []
      supplierData.value = newData
    }
  }).catch(error => {
    console.error('请求失败:', error)
  })
}
</script>

<template>
  <div>
    <div style="margin-bottom: 10px;">
      <el-input v-model="name" @keyup.enter="loadPost" placeholder="请输入商品名" :suffix-icon="Search" style="width: 200px;"></el-input>
      <el-select v-model="warehouseId" placeholder="请选择仓库" style="width: 200px; margin-left: 5px">
        <el-option
            v-for="item in storageData"
            :key="item.id"
            :label="item.name"
            :value="item.id">
        </el-option>
      </el-select>
      <el-select v-model="categoryId" placeholder="请选择分类" style="width: 200px;margin-left: 5px">
        <el-option
            v-for="item in goodstypeData"
            :key="item.id"
            :label="item.name"
            :value="item.id">
        </el-option>
      </el-select>
      <el-checkbox v-model="isWarning" @change="handleWarningChange" style="margin-left: 10px;">只看告急商品</el-checkbox>
      <el-button type="primary" style="margin-left: 10px;" @click="loadPost">查询</el-button>
      <el-button type="success" @click="resetParam" >重置</el-button>
      <el-button type="primary" style="margin-left: 10px;" @click="add" v-if="user.roleId!=2">新增</el-button>
      <el-button type="success" style="margin-left: 10px;" @click="inGoods">入库</el-button>
      <el-button type="warning" style="margin-left: 10px;" @click="outGoods">出库</el-button>
    </div>
    <el-table :data="tableData" style="width: 100%;"
              :header-cell-style="{background:'#f2f5fc',color:'#555'}"
              border
              highlight-current-row
              @current-change="selectCurrentChange"
    >
      <el-table-column prop="id" label="ID" width="60" align="center"></el-table-column>
      <el-table-column prop="barcode" label="商品条码" width="130">
        <template #default="scope">
          {{ scope.row.barcode || '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="name" label="商品名" min-width="120"></el-table-column>
      <el-table-column prop="specs" label="规格" width="80">
        <template #default="scope">
          {{ scope.row.specs || '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="unit" label="单位" width="60" align="center">
        <template #default="scope">
          {{ scope.row.unit || '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="purchasePrice" label="进价" width="80" align="right">
        <template #default="scope">
          {{ scope.row.purchasePrice ? '¥' + Number(scope.row.purchasePrice).toFixed(2) : '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="retailPrice" label="零售价" width="80" align="right">
        <template #default="scope">
          {{ scope.row.retailPrice ? '¥' + Number(scope.row.retailPrice).toFixed(2) : '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="warehouseName" label="仓库" width="80">
        <template #default="scope">
          {{ scope.row.warehouseName || '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="categoryName" label="分类" width="80">
        <template #default="scope">
          {{ scope.row.categoryName || '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="count" label="库存数量" width="90" align="center">
        <template #default="scope">
          <el-tag v-if="isAlert(scope.row)" type="danger" effect="dark" style="font-weight: bold;">
            {{ scope.row.count }}
          </el-tag>
          <span v-else>{{ scope.row.count }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="minCount" label="安全库存" width="80" align="center">
        <template #default="scope">
          <span :style="{ color: isAlert(scope.row) ? 'red' : '' }">
            {{ scope.row.minCount || '-' }}
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="supplierName" label="供应商" min-width="120">
        <template #default="scope">
          {{ scope.row.supplierName || '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="remark" label="备注" min-width="100" show-overflow-tooltip></el-table-column>
      <el-table-column prop="operate" label="操作" width="130" align="center" v-if="user.roleId!=2">
        <template #default="scope">
          <el-button size="small" type="success" @click="mod(scope.row)">编辑</el-button>
          <el-popconfirm
              title="确定删除吗？"
              @confirm="del(scope.row.id)"
          >
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
        next-text="下一页"
        :page-texts="['共', '条', '前往', '页', '']">
    </el-pagination>
    <el-dialog
        title="商品维护"
        v-model="centerDialogVisible"
        width="50%"
        center>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="商品名" prop="name">
              <el-input v-model="form.name" placeholder="请输入商品名"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="商品条码" prop="barcode">
              <el-input v-model="form.barcode" placeholder="请输入商品条码"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="规格" prop="specs">
              <el-input v-model="form.specs" placeholder="例如：500ml"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="单位" prop="unit">
              <el-input v-model="form.unit" placeholder="例如：瓶、个"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="仓库" prop="storage">
              <el-select v-model="form.storage" placeholder="请选择仓库" style="width: 100%">
                <el-option
                    v-for="item in storageData"
                    :key="item.id"
                    :label="item.name"
                    :value="item.id">
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="分类" prop="goodsType">
              <el-select v-model="form.goodsType" placeholder="请选择分类" style="width: 100%">
                <el-option
                    v-for="item in goodstypeData"
                    :key="item.id"
                    :label="item.name"
                    :value="item.id">
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="零售价" prop="retailPrice">
              <el-input-number v-model="form.retailPrice" :precision="2" :min="0" :step="0.01" style="width: 100%" placeholder="请输入零售价"></el-input-number>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="进价" prop="purchasePrice">
              <el-input-number v-model="form.purchasePrice" :precision="2" :min="0" :step="0.01" style="width: 100%" placeholder="请输入进价"></el-input-number>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="供应商" prop="supplierId">
              <el-select v-model="form.supplierId" placeholder="请选择供应商" style="width: 100%" clearable>
                <el-option
                    v-for="item in supplierData"
                    :key="item.id"
                    :label="item.name"
                    :value="item.id">
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="安全库存下限" prop="minCount">
              <el-input-number v-model="form.minCount" :min="0" :step="1" style="width: 100%" placeholder="默认值5"></el-input-number>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="数量" prop="count">
              <el-input v-model="form.count" placeholder="请输入初始数量"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注" prop="remark">
          <el-input type="textarea" v-model="form.remark" :rows="3" placeholder="请输入备注"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="centerDialogVisible = false">取 消</el-button>
          <el-button type="primary" @click="save">确 定</el-button>
        </span>
      </template>
    </el-dialog>
    <el-dialog
        title="出入库"
        v-model="inDialogVisible"
        width="30%"
        center>
      <el-dialog
          width="70%"
          title="用户选择"
          v-model="innerVisible"
          append-to-body>
        <SelectUser @select="doSelectUser"></SelectUser>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="innerVisible = false">取 消</el-button>
            <el-button type="primary" @click="confirmUser">确 定</el-button>
          </span>
        </template>
      </el-dialog>
      <el-form ref="formRef1" :model="form1" :rules="rules1" label-width="80px">
        <el-form-item label="商品名" label-width="80px">
          <el-input v-model="form1.goodsname" readonly></el-input>
        </el-form-item>
        <el-form-item label="申请人" label-width="80px">
          <el-input v-model="form1.username" readonly @click="openUserSelect"></el-input>
        </el-form-item>
        <el-form-item label="数量" label-width="80px" prop="count">
          <el-input v-model="form1.count"></el-input>
        </el-form-item>
        <el-form-item label="备注" label-width="80px" prop="remark">
          <el-input type="textarea" v-model="form1.remark"></el-input>
        </el-form-item>

      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="inDialogVisible = false">取 消</el-button>
          <el-button type="primary" @click="saveIn">确 定</el-button>
        </span>
      </template>
    </el-dialog>
    <el-dialog
        title="出库"
        v-model="outDialogVisible"
        width="30%"
        center>
      <el-dialog
          width="70%"
          title="用户选择"
          v-model="innerVisible"
          append-to-body>
        <SelectUser @select="doSelectOutUser"></SelectUser>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="innerVisible = false">取 消</el-button>
            <el-button type="primary" @click="confirmOutUser">确 定</el-button>
          </span>
        </template>
      </el-dialog>
      <el-form ref="formRef2" :model="form2" :rules="rules2" label-width="80px">
        <el-form-item label="商品名" label-width="80px">
          <el-input v-model="form2.goodsname" readonly></el-input>
        </el-form-item>
        <el-form-item label="申请人" label-width="80px">
          <el-input v-model="form2.username" readonly @click="openOutUserSelect"></el-input>
        </el-form-item>
        <el-form-item label="数量" label-width="80px" prop="count">
          <el-input v-model="form2.count"></el-input>
        </el-form-item>
        <el-form-item label="备注" label-width="80px" prop="remark">
          <el-input type="textarea" v-model="form2.remark"></el-input>
        </el-form-item>

      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="outDialogVisible = false">取 消</el-button>
          <el-button type="primary" @click="saveOut">确 定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>
<style scoped>
/* 抑制 ResizeObserver 错误 */
:deep(.el-pagination) {
  margin-top: 10px;
}
</style>