import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import App from './App.vue'
import './assets/global.css'
import axios from "axios"
import router from './router'
import store from './srtore'

const app = createApp(App)

// 配置全局 API 地址
app.config.globalProperties.$httpUrl = 'http://localhost:8090'

// 配置全局 axios 和 message
app.config.globalProperties.$axios = axios

// 配置axios请求拦截器 - 自动携带Token
axios.interceptors.request.use(
  config => {
    // 优先从localStorage获取Token（记住我），没有则从sessionStorage获取
    let token = localStorage.getItem('token')
    if (!token) {
      token = sessionStorage.getItem('token')
    }
    
    // 如果存在Token，添加到请求头
    if (token) {
      config.headers['Authorization'] = 'Bearer ' + token
    }
    
    return config
  },
  error => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 配置axios响应拦截器 - 处理Token过期
axios.interceptors.response.use(
  response => {
    return response
  },
  error => {
    // 处理401未授权错误（Token过期或无效）
    if (error.response && error.response.status === 401) {
      // 清除所有存储的登录信息
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      localStorage.removeItem('menuList')
      localStorage.removeItem('rememberMe')
      sessionStorage.removeItem('token')
      sessionStorage.removeItem('user')
      sessionStorage.removeItem('menuList')
      sessionStorage.removeItem('CurUser')
      
      // 清除Vuex中的菜单数据
      store.commit('setMenu', [])
      
      // 提示用户重新登录
      if (window.ElementPlus) {
        window.ElementPlus.ElMessage.warning('登录已过期，请重新登录')
      }
      
      // 跳转到登录页
      router.push('/')
    }
    
    return Promise.reject(error)
  }
)

// 配置 Element Plus
app.use(ElementPlus, {
  locale: zhCn,
  size: 'small'
})

// 注册所有 Element Plus 图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 使用路由
app.use(router)
app.use(store)

// 全局消息提示组件
app.config.globalProperties.$message = ElementPlus.ElMessage

app.mount('#app')