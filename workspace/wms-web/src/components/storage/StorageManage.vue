<script setup>
import {onBeforeMount, ref, getCurrentInstance} from 'vue'
import { Search } from '@element-plus/icons-vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const { proxy } = getCurrentInstance()
const httpUrl = proxy.$httpUrl
const $message = ElMessage


const tableData = ref([])
const pageSize = ref(10)
const pageNum = ref(1)
const total = ref(0)
const name = ref('')
const centerDialogVisible = ref(false)
const formRef = ref()
const resetForm = () => {
  formRef.value.resetFields();
}
const form = ref({
  id:'',
  name: '',
  remark: '',

})
const rules = {
  name: [
    { required: true, message: '请输入仓库名', trigger: 'blur' }
  ]
}


const add = () => {
  centerDialogVisible.value = true
  console.log('打开新增对话框')
}
const mod = (row) => {
  form.value.id= row.id
  form.value.name= row.name
  form.value.remark= row.remark
  centerDialogVisible.value = true
}
const del = (id) => {
  axios.get(httpUrl+'/storage/del?id='+id).then(res => {
    console.log('保存结果:', res)
    if(res.data.code == 200) {
      centerDialogVisible.value = false
      // 重置表单
      form.value = {
        id:'',
        name: '',
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
        remark: form.value.remark
      }

      console.log('保存数据:', submitData)

      // 判断是新增还是修改
      if(form.value.id){
        // 修改操作
        submitData.id = form.value.id
        axios.post(httpUrl+'/storage/update', submitData).then(res => {
          console.log('保存结果:', res)
          if(res.data.code == 200) {
            centerDialogVisible.value = false
            // 重置表单
            form.value = {
              id:'',
              name: '',
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
        axios.post(httpUrl+'/storage/save', submitData).then(res => {
          console.log('保存结果:', res)
          if(res.data.code == 200) {
            centerDialogVisible.value = false
            // 重置表单
            form.value = {
              id:'',
              name: '',
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
  loadPost()
}
const loadPost = () => {
  console.log('开始查询，参数:', {
    pagesize:pageSize.value,
    pagenum:pageNum.value,
    param: {
      name:name.value
    }
  })
  axios.post(httpUrl+'/storage/listPage',{
    pagesize:pageSize.value,
    pagenum:pageNum.value,
    param: {
      name:name.value
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
  loadPost()
})
</script>

<template>
  <div>
    <div style="margin-bottom: 10px;">
      <el-input v-model="name" @keyup.enter="loadPost" placeholder="请输入仓库名" :suffix-icon="Search" style="width: 200px;"></el-input>

      <el-button type="primary" style="margin-left: 10px;" @click="loadPost">查询</el-button>
      <el-button type="success" @click="resetParam" >重置</el-button>
      <el-button type="primary" style="margin-left: 10px;" @click="add">新增</el-button>
    </div>
    <el-table :data="tableData" style="width: 100%;"
              :header-cell-style="{background:'#f2f5fc',color:'#555'}"
              border
    >
      <el-table-column prop="id" label="ID" width="140"></el-table-column>
      <el-table-column prop="name" label="仓库名" width="120"></el-table-column>
      <el-table-column prop="remark" label="备注"></el-table-column>
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
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="仓库名" label-width="80px" prop="name">
          <el-input v-model="form.name"></el-input>
        </el-form-item>
        <el-form-item label="备注" label-width="80px" prop="remark">
          <el-input type="textarea" v-model="form.remark"></el-input>
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