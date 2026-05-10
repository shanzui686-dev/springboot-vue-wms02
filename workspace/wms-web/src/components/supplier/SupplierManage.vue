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
const contact = ref('') // 联系人搜索
const centerDialogVisible = ref(false)
const formRef = ref(null)
const resetForm = () => {
  formRef.value?.resetFields();
}
const form = ref({
  id:'',
  name: '',
  contact: '',
  phone: '',
  address: '',
  status: 1,
  remark: ''
})
const rules = {
  name: [
    { required: true, message: '请输入供应商名称', trigger: 'blur' },
    { min: 1, max: 100, message: '长度在 1 到 100 个字符', trigger: 'blur' }
  ],
  contact: [
    { required: true, message: '请输入联系人', trigger: 'blur' }
  ],
  phone: [
    {required: true,message: "手机号不能为空",trigger: "blur"},
    {pattern: /^1[3|4|5|6|7|8|9][0-9]\d{8}$/, message: "请输入正确的手机号码", trigger: "blur"}
  ]
}

const add = () => {
  resetForm()
  form.value = {
    id:'',
    name: '',
    contact: '',
    phone: '',
    address: '',
    status: 1,
    remark: ''
  }
  centerDialogVisible.value = true
}
const mod = (row) => {
  form.value.id = row.id
  form.value.name = row.name
  form.value.contact = row.contact || ''
  form.value.phone = row.phone || ''
  form.value.address = row.address || ''
  form.value.status = row.status !== undefined ? row.status : 1
  form.value.remark = row.remark || ''
  centerDialogVisible.value = true
}
const del = (id) => {
  axios.get(httpUrl+'/supplier/del?id='+id).then(res => {
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
        contact: form.value.contact,
        phone: form.value.phone,
        address: form.value.address,
        status: form.value.status,
        remark: form.value.remark
      }

      if(form.value.id){
        submitData.id = form.value.id
        axios.post(httpUrl+'/supplier/update', submitData).then(res => {
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
        axios.post(httpUrl+'/supplier/save', submitData).then(res => {
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
  contact.value = ''
  loadPost()
}

const loadPost = () => {
  axios.post(httpUrl+'/supplier/listPage',{
    pagesize:pageSize.value,
    pagenum:pageNum.value,
    param: {
      name:name.value,
      contact:contact.value
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

/**
 * 切换供应商状态
 * @param row 当前行数据
 */
const handleStatusChange = (row) => {
  const statusText = row.status === 1 ? '启用' : '禁用'
  
  axios.post(httpUrl+'/supplier/updateStatus', null, {
    params: {
      id: row.id,
      status: row.status
    }
  }).then(res => {
    if(res.data.code == 200) {
      $message.success(res.data.msg || `已${statusText}供应商`)
      // 刷新列表以获取最新数据
      loadPost()
    } else {
      $message.error(res.data.msg || '操作失败')
      // 如果失败，恢复原状态
      row.status = row.status === 1 ? 0 : 1
    }
  }).catch(error => {
    console.error('状态更新失败:', error)
    $message.error('操作失败：' + (error.message || '网络错误'))
    // 如果失败，恢复原状态
    row.status = row.status === 1 ? 0 : 1
  })
}

onBeforeMount(() => {
  loadPost()
})
</script>

<template>
  <div>
    <div style="margin-bottom: 10px;">
      <el-input v-model="name" @keyup.enter="loadPost" placeholder="请输入供应商名称" :suffix-icon="Search" style="width: 200px;"></el-input>
      <el-input v-model="contact" @keyup.enter="loadPost" placeholder="请输入联系人" :suffix-icon="Search" style="width: 200px; margin-left: 10px;"></el-input>
      <el-button type="primary" style="margin-left: 10px;" @click="loadPost">查询</el-button>
      <el-button type="success" @click="resetParam" >重置</el-button>
      <el-button type="primary" style="margin-left: 10px;" @click="add">新增</el-button>
    </div>
    <el-table :data="tableData" style="width: 100%;"
              :header-cell-style="{background:'#f2f5fc',color:'#555'}"
              border
    >
      <el-table-column prop="id" label="ID" width="80"></el-table-column>
      <el-table-column prop="name" label="供应商名称" width="200"></el-table-column>
      <el-table-column prop="contact" label="联系人" width="120"></el-table-column>
      <el-table-column prop="phone" label="联系电话" width="150"></el-table-column>
      <el-table-column prop="address" label="地址" min-width="200"></el-table-column>
      <el-table-column prop="status" label="状态" width="120">
        <template #default="scope">
          <el-switch
              v-model="scope.row.status"
              :active-value="1"
              :inactive-value="0"
              inline-prompt
              active-text="已启用"
              inactive-text="已禁用"
              @change="handleStatusChange(scope.row)"
          />
        </template>
      </el-table-column>
      <el-table-column prop="remark" label="备注" min-width="150"></el-table-column>
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
        title="供应商维护"
        v-model="centerDialogVisible"
        width="50%"
        center>
      <el-form ref="formRef" :model="form" :rules="rules" >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="供应商名称" label-width="100px" prop="name">
              <el-input v-model="form.name" placeholder="请输入供应商名称"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系人" label-width="100px" prop="contact">
              <el-input v-model="form.contact" placeholder="请输入联系人"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="联系电话" label-width="100px" prop="phone">
              <el-input v-model="form.phone" placeholder="请输入联系电话"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" label-width="100px" prop="status">
              <el-radio-group v-model="form.status">
                <el-radio :value="1">启用</el-radio>
                <el-radio :value="0">禁用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="地址" label-width="100px" prop="address">
          <el-input v-model="form.address" placeholder="请输入详细地址"></el-input>
        </el-form-item>
        <el-form-item label="备注" label-width="100px" prop="remark">
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
