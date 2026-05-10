import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'
import store from '../srtore'

// 创建axios实例
const service = axios.create({
  baseURL: 'http://localhost:8090', // API基础地址
  timeout: 15000, // 请求超时时间
  headers: {
    'Content-Type': 'application/json'
  }
})

/**
 * 清除登录信息
 */
function clearLoginInfo() {
  // 清除localStorage（7天免登录数据）
  localStorage.removeItem('token')
  localStorage.removeItem('user')
  localStorage.removeItem('menuList')
  localStorage.removeItem('rememberMe')
  
  // 清除sessionStorage（当前会话数据）
  sessionStorage.removeItem('token')
  sessionStorage.removeItem('user')
  sessionStorage.removeItem('menuList')
  sessionStorage.removeItem('CurUser')
  
  // 清除Vuex中的菜单数据
  store.commit('setMenu', [])
}

/**
 * 获取Token
 * 优先从localStorage获取（记住我），其次从sessionStorage获取（普通登录）
 */
function getToken() {
  let token = localStorage.getItem('token')
  if (!token) {
    token = sessionStorage.getItem('token')
  }
  return token
}

// ==================== 请求拦截器 ====================
service.interceptors.request.use(
  config => {
    // 从本地存储获取Token
    const token = getToken()
    
    // 如果存在Token，添加到请求头
    if (token) {
      config.headers['Authorization'] = 'Bearer ' + token
    }
    
    return config
  },
  error => {
    console.error('请求拦截器错误:', error)
    ElMessage.error('请求配置错误')
    return Promise.reject(error)
  }
)

// ==================== 响应拦截器 ====================
service.interceptors.response.use(
  response => {
    // 直接返回响应数据
    return response
  },
  error => {
    // 处理HTTP错误状态码
    if (error.response) {
      const status = error.response.status
      
      switch (status) {
        case 401:
          // Token过期或无效
          console.warn('Token已过期，清除登录信息并跳转登录页')
          clearLoginInfo()
          ElMessage.warning('登录已过期，请重新登录')
          router.push('/')
          break
          
        case 403:
          // 没有权限
          ElMessage.error('没有权限访问该资源')
          break
          
        case 404:
          // 请求的资源不存在
          ElMessage.error('请求的资源不存在')
          break
          
        case 500:
          // 服务器错误
          ElMessage.error('服务器内部错误')
          break
          
        default:
          // 其他错误
          const message = error.response.data?.msg || error.message || '请求失败'
          ElMessage.error(message)
      }
    } else if (error.request) {
      // 请求已发送但没有收到响应
      console.error('网络错误:', error.request)
      ElMessage.error('网络连接失败，请检查网络')
    } else {
      // 请求配置错误
      console.error('请求错误:', error.message)
      ElMessage.error('请求配置错误')
    }
    
    return Promise.reject(error)
  }
)

/**
 * 退出登录
 */
export function logout() {
  clearLoginInfo()
  ElMessage.success('已安全退出')
  router.push('/')
}

export default service
