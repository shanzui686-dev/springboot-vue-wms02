<!-- eslint-disable vue/multi-word-component-names -->
<script setup>
import {onBeforeMount, ref, getCurrentInstance} from 'vue'
import { Search } from '@element-plus/icons-vue'
import axios from 'axios'

const { proxy } = getCurrentInstance()
const httpUrl = proxy.$httpUrl
const $message = proxy.$message
let checkAge = (rule, value, callback) => {
  if(value && value > 150){
    callback(new Error('年龄输⼊过⼤'));
  }else{
    callback();
  }
};
let checkDuplicate =(rule,value,callback)=>{
  if(!value){
    return callback();
  }
  // 如果是编辑模式且账号未变，则不验证
  if(form.value.id && value === form.value.no){
    return callback();
  }
  axios.get(httpUrl+"/user/findByNo?no="+value).then(res=>{
    if(res.data.code !=200){
      callback()
    }else{
      callback(new Error('账号已经存在'));
    }
  }).catch(error=>{
    callback();
  })
};
const tableData = ref([])
const pageSize = ref(10)
const pageNum = ref(1)
const total = ref(0)
const name = ref('')
const sex = ref('')
const centerDialogVisible = ref(false)
const formRef = ref(null)
const resetForm = () => {
  formRef.value.resetFields();
}
const form = ref({
  id:'',
  no:'',
  name: '',
  password: '',
  age: '',
  phone: '',
  sex:'0',
  roleId: ''
})
const rules = {
  no: [
    { required: true, message: '请输入账号', trigger: 'blur' },
    { min: 3, max: 8, message: '长度在 3 到 8 个字符', trigger: 'blur' },
    {validator:checkDuplicate,trigger: 'blur'}
  ],
  name: [
    { required: true, message: '请输入名字', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 3, max: 8, message: '长度在 3 到 8 个字符', trigger: 'blur' }
  ],
  age: [
    {required: true, message: '请输⼊年龄', trigger: 'blur'},
    {min: 1, max: 3, message: '⻓度在 1 到 3 个位', trigger: 'blur'},
    {pattern: /^([1-9][0-9]*){1,3}$/,message: '年龄必须为正整数字',trigger: "blur"},
    {validator:checkAge,trigger: 'blur'}
  ],
  phone: [
    {required: true,message: "⼿机号不能为空",trigger: "blur"},
    {pattern: /^1[3|4|5|6|7|8|9][0-9]\d{8}$/, message: "请输⼊正确的⼿机号码", trigger:
          "blur"}
  ]
}
const sexs = ref([
    {
      value: '1',
      label: '男'
    },
    {
      value: '0',
      label: '女'
    }
])

const add = () => {
  centerDialogVisible.value = true
  console.log('打开新增对话框')
}
const mod = (row) => {
  form.value.id= row.id
  form.value.no= row.no
  form.value.name= row.name
  form.value.password=' '
  form.value.age= row.age+''
  form.value.sex= row.sex+''
  form.value.phone= row.phone
  form.value.roleId= row.roleId
  centerDialogVisible.value = true
}
const del = (id) => {
  axios.get(httpUrl+'/user/del?id='+id).then(res => {
    console.log('保存结果:', res)
    if(res.data.code == 200) {
      $message.success('操作成功!')
      centerDialogVisible.value = false
      // 重置表单
      form.value = {
        no:'',
        name: '',
        password: '',
        age: '',
        phone: '',
        sex:'0',
        roleId: ''
      }
      loadPost() // 刷新列表
    } else {
      $message.error('操作失败!')
    }
  })
}
const save = () => {
  // 验证必填字段
  formRef.value.validate((valid) => {
    if (valid) {
      // 准备提交的数据，转换性别为 Integer
      const submitData = {
        no: form.value.no,
        name: form.value.name,
        password: form.value.password,
        age: form.value.age ? parseInt(form.value.age) : null,
        sex: form.value.sex !== undefined ? parseInt(form.value.sex) : 1,
        phone: form.value.phone,
        roleId: form.value.roleId ? parseInt(form.value.roleId) : 2, // 默认普通用户
        isvalid: 'Y'
      }
      
      // 如果是编辑，添加 id 字段
      if(form.value.id){
        submitData.id = form.value.id
      }
      
      console.log('保存数据:', submitData)
      
      // 判断是新增还是修改
      if(form.value.id){
        // 修改操作
        axios.post(httpUrl+'/user/update', submitData).then(res => {
          console.log('保存结果:', res)
          if(res.data.code == 200) {
            $message.success('操作成功!')
            centerDialogVisible.value = false
            // 重置表单
            form.value = {
              no:'',
              name: '',
              password: '',
              age: '',
              phone: '',
              sex:'0',
              roleId: ''
            }
            loadPost() // 刷新列表
          } else {
            $message.error('操作失败!')
          }
        }).catch(error => {
          console.error('保存失败:', error)
          $message.error('保存失败：' + error.message)
        })
      }else {
        // 新增操作
        axios.post(httpUrl+'/user/save', submitData).then(res => {
          console.log('保存结果:', res)
          if(res.data.code == 200) {
            $message.success('操作成功!')
            centerDialogVisible.value = false
            // 重置表单
            form.value = {
              no:'',
              name: '',
              password: '',
              age: '',
              phone: '',
              sex:'0',
              roleId: ''
            }
            loadPost() // 刷新列表
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
const resetParam = () => {
  name.value = ''
  sex.value = ''
  loadPost()
}
const loadGet = () => {
  axios.get(httpUrl+'/user/list').then(res=>res.data).then(res=>{
    console.log(res)
  })
}
const loadPost = () => {
  console.log('开始查询，参数:', {
    pagesize:pageSize.value,
    pagenum:pageNum.value,
    param: {
      name:name.value,
      sex:sex.value
    }
  })
  axios.post(httpUrl+'/user/listPageC',{
    pagesize:pageSize.value,
    pagenum:pageNum.value,
    param: {
      name:name.value,
      sex:sex.value
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
onBeforeMount(() => {
  //loadGet();
  loadPost()
})
</script>

<template>
  <div>
    <div style="margin-bottom: 10px;">
      <el-input v-model="name" @keyup.enter="loadPost" placeholder="请输入名字" :suffix-icon="Search" style="width: 200px;"></el-input>
      <el-select v-model="sex" placeholder="请选择性别" style="width: 150px; margin-left: 10px;">
        <el-option
            v-for="item in sexs"
            :key="item.value"
            :label="item.label"
            :value="item.value">
        </el-option>
      </el-select>
      <el-button type="primary" style="margin-left: 10px;" @click="loadPost">查询</el-button>
      <el-button type="success" @click="resetParam" >重置</el-button>
      <el-button type="primary" style="margin-left: 10px;" @click="add">新增</el-button>
    </div>
  <el-table :data="tableData" style="width: 100%;" 
  :header-cell-style="{background:'#f2f5fc',color:'#555'}"
  border
  >
    <el-table-column prop="id" label="ID" width="140"></el-table-column>
    <el-table-column prop="no" label="账号" width="180"></el-table-column>
    <el-table-column prop="name" label="姓名" width="120"></el-table-column>
    <el-table-column prop="age" label="年龄" width="80"></el-table-column>
    <el-table-column prop="sex" label="性别" width="80">
      <template #default="scope">
        <el-tag
            :type="scope.row.sex=== 1 ? 'primary' : 'success'"
            disable-transitions>{{scope.row.sex=== 1 ? '男' : '女'}}</el-tag>
      </template>
    </el-table-column>
    <el-table-column prop="roleId" label="角色" width="120">
      <template #default="scope">
        <el-tag
            :type="scope.row.roleId=== 0 ? 'danger' : (scope.row.roleId=== 1 ? 'primary' : 'success')"
            disable-transitions>{{scope.row.roleId=== 0 ? '系统管理员' : (scope.row.roleId=== 1 ? '库存管理员' : '收银员')}}</el-tag>
      </template>
    </el-table-column>
    <el-table-column prop="phone" label="电话" width="180"></el-table-column>
    <el-table-column prop="operate" label="操作">
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
        title="提示"
        v-model="centerDialogVisible"
        width="30%"
        center>
      <el-form ref="formRef" :model="form" :rules="rules" >
        <el-form-item label="账号" label-width="80px" prop="no">
          <el-input v-model="form.no"></el-input>
        </el-form-item>
        <el-form-item label="名字" label-width="80px" prop="name">
          <el-input v-model="form.name"></el-input>
        </el-form-item>
        <el-form-item label="密码" label-width="80px" prop="password">
          <el-input v-model="form.password" type="password"></el-input>
        </el-form-item>
        <el-form-item label="年龄" label-width="80px" prop="age">
          <el-input v-model="form.age" type="number"></el-input>
        </el-form-item>
        <el-form-item label="性别" label-width="80px" prop="sex">
          <el-radio-group v-model="form.sex">
            <el-radio value="1">男</el-radio>
            <el-radio value="0">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="电话" label-width="80px" prop="phone">
          <el-input v-model="form.phone"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="centerDialogVisible = false">取 消</el-button>
          <el-button type="primary" @click="save">确 定</el-button>
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