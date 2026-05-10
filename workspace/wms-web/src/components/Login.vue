<template>
  <div class="login-container">
    <!-- 背景装饰 -->
    <div class="bg-decoration">
      <div class="circle circle-1"></div>
      <div class="circle circle-2"></div>
      <div class="circle circle-3"></div>
    </div>

    <!-- 登录卡片 -->
    <div class="login-card">
      <div class="card-header">
        <h1 class="system-title">超市进销存管理系统</h1>
        <p class="system-subtitle">Supermarket Inventory Management System</p>
      </div>

      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        class="login-form"
        @keyup.enter="handleLogin"
      >
        <el-form-item prop="no">
          <el-input
            v-model="loginForm.no"
            placeholder="请输入账号"
            :prefix-icon="User"
            size="large"
            clearable
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            :prefix-icon="Lock"
            size="large"
            show-password
            clearable
          />
        </el-form-item>

        <el-form-item>
          <div class="remember-me-wrapper">
            <el-checkbox v-model="loginForm.rememberMe" size="large">
              记住密码 (7天免登录)
            </el-checkbox>
          </div>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            @click="handleLogin"
            class="login-btn"
          >
            {{ loading ? '登录中...' : '进入系统' }}
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, getCurrentInstance } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'
import store from '../srtore'

const { proxy } = getCurrentInstance()
const httpUrl = proxy.$httpUrl
const router = useRouter()

// 登录表单数据
const loginForm = ref({
  no: '',
  password: '',
  rememberMe: false  // 是否记住我（7天免登录）
})

// 表单引用
const loginFormRef = ref(null)

// 加载状态
const loading = ref(false)

// 表单验证规则
const loginRules = {
  no: [
    { required: true, message: '请输入账号', trigger: ['blur', 'change'] }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: ['blur', 'change'] }
  ]
}

// 登录处理
const handleLogin = () => {
  if (!loginFormRef.value) return

  loginFormRef.value.validate((valid) => {
    if (valid) {
      loading.value = true

      // 调用JWT登录接口
      axios.post(httpUrl + '/user/jwtLogin', loginForm.value)
        .then(res => {
          loading.value = false

          if (res.data && res.data.code === 200) {
            // 获取返回数据
            const token = res.data.data.token
            const userData = res.data.data.user
            const menuData = res.data.data.menus
            const rememberMe = res.data.data.rememberMe

            // 根据rememberMe决定存储位置
            if (rememberMe) {
              // 记住我：存储到localStorage（持久化，7天免登录）
              localStorage.setItem('token', token)
              localStorage.setItem('user', JSON.stringify(userData))
              localStorage.setItem('menuList', JSON.stringify(menuData))
              localStorage.setItem('rememberMe', 'true')
              
              // 同时也存到sessionStorage（当前会话可用）
              sessionStorage.setItem('token', token)
              sessionStorage.setItem('user', JSON.stringify(userData))
              sessionStorage.setItem('menuList', JSON.stringify(menuData))
            } else {
              // 普通登录：只存储到sessionStorage（关闭浏览器后清除）
              sessionStorage.setItem('token', token)
              sessionStorage.setItem('user', JSON.stringify(userData))
              sessionStorage.setItem('menuList', JSON.stringify(menuData))
              
              // 清除localStorage中的登录信息
              localStorage.removeItem('token')
              localStorage.removeItem('user')
              localStorage.removeItem('menuList')
              localStorage.removeItem('rememberMe')
            }

            // 同步到 Vuex store
            store.commit('setMenu', menuData)

            // 保存用户信息到全局状态
            sessionStorage.setItem('CurUser', JSON.stringify(userData))

            // 显示成功提示
            const expireMsg = rememberMe ? '7天内免登录' : '12小时有效'
            ElMessage.success(`欢迎回来，${userData.name}！Token${expireMsg}`)

            // 跳转到系统首页
            router.push('/Home')
          } else {
            ElMessage.error(res.data?.msg || '登录失败，请重试')
          }
        })
        .catch(error => {
          loading.value = false
          console.error('登录失败:', error)
          ElMessage.error(error.response?.data?.msg || '登录失败，请检查网络连接')
        })
    } else {
      ElMessage.warning('请填写完整的登录信息')
      return false
    }
  })
}
</script>

<style scoped>
/* 全屏渐变背景 */
.login-container {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: hidden;
}

/* 背景装饰圆圈 */
.bg-decoration {
  position: absolute;
  width: 100%;
  height: 100%;
  pointer-events: none;
}

.circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  animation: float 20s infinite ease-in-out;
}

.circle-1 {
  width: 400px;
  height: 400px;
  top: -100px;
  left: -100px;
  animation-delay: 0s;
}

.circle-2 {
  width: 300px;
  height: 300px;
  bottom: -50px;
  right: -50px;
  animation-delay: 5s;
}

.circle-3 {
  width: 200px;
  height: 200px;
  top: 50%;
  left: 60%;
  animation-delay: 10s;
}

@keyframes float {
  0%, 100% {
    transform: translate(0, 0) scale(1);
  }
  25% {
    transform: translate(30px, -30px) scale(1.1);
  }
  50% {
    transform: translate(-20px, 20px) scale(0.9);
  }
  75% {
    transform: translate(20px, 10px) scale(1.05);
  }
}

/* 登录卡片 - 毛玻璃效果 */
.login-card {
  position: relative;
  z-index: 10;
  width: 420px;
  padding: 50px 40px;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: 20px;
  border: 1px solid rgba(255, 255, 255, 0.3);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  animation: cardAppear 0.6s ease-out;
}

@keyframes cardAppear {
  from {
    opacity: 0;
    transform: translateY(-30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 卡片头部 */
.card-header {
  text-align: center;
  margin-bottom: 40px;
}

.system-title {
  font-size: 28px;
  font-weight: 600;
  color: #ffffff;
  margin: 0 0 8px 0;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  letter-spacing: 2px;
}

.system-subtitle {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.85);
  margin: 0;
  font-weight: 300;
  letter-spacing: 1px;
}

/* 表单样式 */
.login-form {
  margin-top: 20px;
}

.login-form :deep(.el-form-item) {
  margin-bottom: 24px;
}

/* 记住我复选框样式 */
.remember-me-wrapper {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  padding: 8px 0;
}

.remember-me-wrapper :deep(.el-checkbox__label) {
  color: rgba(255, 255, 255, 0.9);
  font-size: 14px;
  font-weight: 400;
  user-select: none;
}

.remember-me-wrapper :deep(.el-checkbox__inner) {
  background-color: rgba(255, 255, 255, 0.2);
  border-color: rgba(255, 255, 255, 0.5);
}

.remember-me-wrapper :deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
  background-color: #667eea;
  border-color: #667eea;
}

.remember-me-wrapper :deep(.el-checkbox__input.is-checked + .el-checkbox__label) {
  color: #ffffff;
}

.login-form :deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.9);
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  padding: 8px 12px;
  transition: all 0.3s ease;
}

.login-form :deep(.el-input__wrapper:hover) {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.login-form :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.3);
  border-color: #667eea;
}

.login-form :deep(.el-input__inner) {
  font-size: 15px;
  color: #333;
}

.login-form :deep(.el-input__prefix) {
  color: #667eea;
  font-size: 18px;
}

/* 登录按钮 */
.login-btn {
  width: 100%;
  margin-top: 10px;
  height: 48px;
  font-size: 16px;
  font-weight: 500;
  letter-spacing: 2px;
  border-radius: 10px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.4);
  transition: all 0.3s ease;
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.5);
}

.login-btn:active {
  transform: translateY(0);
}

.login-btn :deep(.el-button__text) {
  color: #ffffff;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .login-card {
    width: 90%;
    padding: 40px 30px;
  }

  .system-title {
    font-size: 24px;
  }

  .system-subtitle {
    font-size: 12px;
  }

  .circle-1 {
    width: 200px;
    height: 200px;
  }

  .circle-2 {
    width: 150px;
    height: 150px;
  }

  .circle-3 {
    width: 100px;
    height: 100px;
  }
}
</style>
