<!-- eslint-disable vue/multi-word-component-names -->
<script setup>
import { ref, getCurrentInstance } from 'vue'
import { useRouter } from 'vue-router'
import { Fold, Expand, ArrowDown } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'
import store from '../srtore'

const { proxy } = getCurrentInstance()
const httpUrl = proxy.$httpUrl
const router = useRouter()

const props = defineProps({
  isCollapse: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['doCollapse'])

// 当前登录用户信息
const user = JSON.parse(sessionStorage.getItem('user') || '{}')

// 修改密码对话框
const pwdDialogVisible = ref(false)
const pwdFormRef = ref(null)

// 修改密码表单
const pwdForm = ref({
  userId: user.id,
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 修改密码表单验证规则
const pwdRules = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能小于 6 位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== pwdForm.value.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 注销账号
const cancelAccount = () => {
  ElMessageBox.confirm('您确定要注销该账号吗？注销后账号将被停用，无法再次登录。', '提示', {
    confirmButtonText: '确定注销',
    cancelButtonText: '取消',
    type: 'error',
    center: true
  }).then(() => {
    // 调用后端注销接口
    axios.post(httpUrl + '/user/cancel', {
      userId: user.id
    }).then(res => {
      if (res.data.code === 200) {
        ElMessage.success('账号注销成功')
        // 清除所有登录信息
        localStorage.removeItem('token')
        localStorage.removeItem('user')
        localStorage.removeItem('menuList')
        localStorage.removeItem('rememberMe')
        sessionStorage.clear()
        
        // 强制跳转并刷新页面
        window.location.href = '/'
      } else {
        ElMessage.error(res.data.msg || '注销失败')
      }
    }).catch(error => {
      console.error('注销失败:', error)
      ElMessage.error(error.response?.data?.msg || '注销失败，请重试')
    })
  }).catch(() => {
    ElMessage.info('已取消注销')
  })
}

// 折叠侧边栏
const collapse = () => {
  emit('doCollapse')
}

// 退出登录
const logout = () => {
  ElMessageBox.confirm('您确定要退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
    center: true
  }).then(() => {
    // 清除所有登录信息（包括localStorage和sessionStorage）
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    localStorage.removeItem('menuList')
    localStorage.removeItem('rememberMe')
    sessionStorage.clear()
    
    ElMessage.success('退出登录成功')
    
    // 强制跳转并刷新页面
    window.location.href = '/'
  }).catch(() => {
    ElMessage.info('已取消退出登录')
  })
}

// 打开修改密码对话框
const openPwdDialog = () => {
  pwdForm.value = {
    userId: user.id,
    oldPassword: '',
    newPassword: '',
    confirmPassword: ''
  }
  pwdDialogVisible.value = true
}

// 提交修改密码
const submitPwdChange = () => {
  if (!pwdFormRef.value) return

  pwdFormRef.value.validate((valid) => {
    if (!valid) {
      return false
    }

    // 调用后端修改密码接口
    axios.post(httpUrl + '/user/updatePwd', {
      userId: pwdForm.value.userId,
      oldPassword: pwdForm.value.oldPassword,
      newPassword: pwdForm.value.newPassword
    }).then(res => {
      if (res.data.code === 200) {
        ElMessage.success('密码修改成功，请重新登录')
        pwdDialogVisible.value = false
        // 清除所有登录信息
        localStorage.removeItem('token')
        localStorage.removeItem('user')
        localStorage.removeItem('menuList')
        localStorage.removeItem('rememberMe')
        sessionStorage.clear()
        
        // 修改密码成功后直接退出登录，无需二次确认
        window.location.href = '/'
      } else {
        ElMessage.error(res.data.msg || '密码修改失败')
      }
    }).catch(error => {
      console.error('修改密码失败:', error)
      ElMessage.error(error.response?.data?.msg || '密码修改失败，请重试')
    })
  })
}

// 取消修改密码
const cancelPwdChange = () => {
  pwdDialogVisible.value = false
  pwdFormRef.value?.resetFields()
}
</script>

<template>
  <div style="display: flex;line-height: 60px; align-items: center;">
    <!-- 折叠侧边栏按钮 -->
    <div style="margin-top: 8px; cursor: pointer;" @click="collapse">
      <el-icon :size="30">
        <component :is="isCollapse ? Expand : Fold" />
      </el-icon>
    </div>
    
    <!-- 系统标题 -->
    <div style="flex: 1; text-align: center; font-size: 34px;">
      <span>欢迎来到中小型超市进销存管理系统</span>
    </div>
    
    <!-- 用户下拉菜单 -->
    <el-dropdown>
      <div style="display: inline-flex; align-items: center; cursor: pointer;">
        <span>{{ user.name }}</span>
        <el-tag 
          :type="user.roleId === 0 ? 'danger' : (user.roleId === 1 ? 'primary' : 'success')" 
          size="small" 
          style="margin-left: 8px;">
          {{ user.roleId === 0 ? '系统管理员' : (user.roleId === 1 ? '库存管理员' : '收银员') }}
        </el-tag>
        <el-icon :size="20" style="margin-left: 5px;"><ArrowDown /></el-icon>
      </div>

      <template #dropdown>
        <el-dropdown-item @click="openPwdDialog">修改密码</el-dropdown-item>
        <el-dropdown-item @click="cancelAccount" class="danger-item">注销账号</el-dropdown-item>
        <el-dropdown-item @click="logout" class="danger-item">退出登录</el-dropdown-item>
      </template>
    </el-dropdown>
  </div>

  <!-- 修改密码对话框 -->
  <el-dialog
    v-model="pwdDialogVisible"
    title="修改密码"
    width="500px"
    :close-on-click-modal="false"
  >
    <el-form
      ref="pwdFormRef"
      :model="pwdForm"
      :rules="pwdRules"
      label-width="100px"
    >
      <el-form-item label="原密码" prop="oldPassword">
        <el-input
          v-model="pwdForm.oldPassword"
          type="password"
          placeholder="请输入原密码"
          show-password
        />
      </el-form-item>
      
      <el-form-item label="新密码" prop="newPassword">
        <el-input
          v-model="pwdForm.newPassword"
          type="password"
          placeholder="请输入新密码（至少6位）"
          show-password
        />
      </el-form-item>
      
      <el-form-item label="确认密码" prop="confirmPassword">
        <el-input
          v-model="pwdForm.confirmPassword"
          type="password"
          placeholder="请再次输入新密码"
          show-password
        />
      </el-form-item>
    </el-form>
    
    <template #footer>
      <el-button @click="cancelPwdChange">取消</el-button>
      <el-button type="primary" @click="submitPwdChange">确定</el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
.danger-item {
  color: #f56c6c !important;
}

:deep(.danger-item:hover) {
  background-color: #fef0f0 !important;
  color: #f56c6c !important;
}
</style>
