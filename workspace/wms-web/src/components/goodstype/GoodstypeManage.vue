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
const formRef = ref(null)
const resetForm = () => {
  formRef.value?.resetFields();
}
const form = ref({
  id:'',
  name: '',
  remark: ''
})
const rules = {
  name: [
    { required: true, message: '请输入分类名称', trigger: 'blur' },
    { min: 1, max: 50, message: '长度在 1 到 50 个字符', trigger: 'blur' }
  ]
}

const add = () => {
  resetForm()
  form.value = {
    id:'',
    name: '',
    remark: ''
  }
  centerDialogVisible.value = true
}
const mod = (row) => {
  form.value.id = row.id
  form.value.name = row.name
  form.value.remark = row.remark || ''
  centerDialogVisible.value = true
}
const del = (id) => {
  axios.get(httpUrl+'/goodstype/del?id='+id).then(res => {
    if(res.data.code == 200) {
      loadPost()
      $message.success('删除成功!')
    } else {
      $message.error('删除失败!')
    }
  })
}
const save = () => {
  formRef.value.validate((valid) => {
    if (valid) {
      const submitData = {
        name: form.value.name,
        remark: form.value.remark
      }

      if(form.value.id){
        submitData.id = form.value.id
        axios.post(httpUrl+'/goodstype/update', submitData).then(res => {
          if(res.data.code == 200) {
            centerDialogVisible.value = false
            resetForm()
            loadPost()
            $message.success('修改成功!')
          } else {
            $message.error('修改失败!')
          }
        }).catch(error => {
          console.error('保存失败:', error)
          $message.error('保存失败：' + error.message)
        })
      }else {
        axios.post(httpUrl+'/goodstype/save', submitData).then(res => {
          if(res.data.code == 200) {
            centerDialogVisible.value = false
            resetForm()
            loadPost()
            $message.success('新增成功!')
          } else {
            $message.error('新增失败!')
          }
        }).catch(error => {
          console.error('保存失败:', error)
          $message.error('保存失败：' + error.message)
        })
      }
    } else {
      return false;
    }
  });
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
  loadPost()
}

const loadPost = () => {
  axios.post(httpUrl+'/goodstype/listPage',{
    pagesize:pageSize.value,
    pagenum:pageNum.value,
    param: {
      name:name.value
    }
  }).then(res => {
    const result = res.data
    if(result.code==200){
      const newData = Array.isArray(result.data) ? result.data : []
      tableData.value = newData
      total.value = result.total || 0
    }else {
      $message.error('获取数据失败')
    }
  }).catch(error => {
    console.error('请求失败:', error)
    $message.error('请求失败')
  })
}

onBeforeMount(() => {
  loadPost()
})
</script>

<template>
  <div>
    <div style="margin-bottom: 10px;">
      <el-input v-model="name" @keyup.enter="loadPost" placeholder="请输入分类名称" :suffix-icon="Search" style="width: 200px;"></el-input>
      <el-button type="primary" style="margin-left: 10px;" @click="loadPost">查询</el-button>
      <el-button type="success" @click="resetParam" >重置</el-button>
      <el-button type="primary" style="margin-left: 10px;" @click="add">新增</el-button>
    </div>
    <el-table :data="tableData" style="width: 100%;"
              :header-cell-style="{background:'#f2f5fc',color:'#555'}"
              border
    >
      <el-table-column prop="id" label="ID" width="140"></el-table-column>
      <el-table-column prop="name" label="分类名称" width="200"></el-table-column>
      <el-table-column prop="remark" label="备注"></el-table-column>
      <el-table-column prop="operate" label="操作" width="200">
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
        title="分类维护"
        v-model="centerDialogVisible"
        width="30%"
        center>
      <el-form ref="formRef" :model="form" :rules="rules" >
        <el-form-item label="分类名称" label-width="80px" prop="name">
          <el-input v-model="form.name" placeholder="请输入分类名称"></el-input>
        </el-form-item>
        <el-form-item label="备注" label-width="80px" prop="remark">
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
  </div>
</template>
<style scoped>
:deep(.el-pagination) {
  margin-top: 10px;
}
</style>
