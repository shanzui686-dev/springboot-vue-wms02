# JWT 7天免登录功能实现说明

## 功能概述

基于JWT（JSON Web Token）实现"7天免登录"功能，支持两种登录模式：
- **普通登录**：Token有效期12小时，关闭浏览器需重新登录
- **记住我**：Token有效期7天，7天内无需重复登录

## 实现文件清单

### 后端文件（6个）

1. **pom.xml** - 添加JWT依赖
2. **JwtUtils.java** - JWT工具类，生成和解析Token
3. **LoginDTO.java** - 登录请求DTO
4. **UserController.java** - 添加jwtLogin接口
5. **JwtInterceptor.java** - JWT拦截器，验证Token
6. **JwtInterceptorConfig.java** - 拦截器配置类

## 核心代码说明

### 1. JWT依赖（pom.xml）

```xml
<!-- JWT依赖 -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.11.5</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.11.5</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.11.5</version>
    <scope>runtime</scope>
</dependency>
```

### 2. JwtUtils工具类核心方法

```java
// 生成Token（根据rememberMe设置过期时间）
public static String generateToken(Integer userId, String username, Integer roleId, boolean rememberMe)

// 解析Token
public static Claims parseToken(String token)

// 从Token中获取用户信息
public static Integer getUserIdFromToken(String token)
public static String getUsernameFromToken(String token)
public static Integer getRoleIdFromToken(String token)
public static Boolean getRememberMeFromToken(String token)

// 验证Token是否有效
public static boolean isTokenValid(String token)
```

### 3. 登录接口（UserController.java）

**接口路径**：`POST /user/jwtLogin`

**请求参数**：
```json
{
  "no": "admin",
  "password": "123456",
  "rememberMe": true  // true=7天免登录，false=12小时
}
```

**返回数据**：
```json
{
  "code": 200,
  "msg": "成功",
  "total": 0,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "user": {
      "id": 1,
      "no": "admin",
      "name": "管理员",
      "roleId": 0
    },
    "menus": [...],
    "rememberMe": true,
    "expireTime": "7天"
  }
}
```

### 4. JWT拦截器（JwtInterceptor.java）

- **拦截所有请求**（除了白名单）
- **验证Token**：检查Token是否存在、是否有效、是否过期
- **提取用户信息**：将用户信息存入request属性
- **白名单路径**：
  - `/user/login` - 普通登录接口
  - `/user/jwtLogin` - JWT登录接口
  - `/user/findByNo` - 根据编号查询
  - `/error` - 错误页面
  - Swagger相关路径

## 前端调用示例（Vue 3）

### 1. API接口封装

```javascript
// api/user.js
import axios from 'axios'

// JWT登录接口
export const jwtLogin = (data) => {
  return axios.post('http://localhost:8080/user/jwtLogin', data)
}
```

### 2. 登录页面调用

```vue
<!-- Login.vue -->
<template>
  <div>
    <el-input v-model="loginForm.no" placeholder="请输入账号"></el-input>
    <el-input v-model="loginForm.password" type="password" placeholder="请输入密码"></el-input>
    <el-checkbox v-model="loginForm.rememberMe">7天免登录</el-checkbox>
    <el-button @click="handleLogin">登录</el-button>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { jwtLogin } from '@/api/user'
import { useRouter } from 'vue-router'

const router = useRouter()
const loginForm = ref({
  no: '',
  password: '',
  rememberMe: false  // 默认不记住我
})

const handleLogin = async () => {
  try {
    const res = await jwtLogin(loginForm.value)
    if (res.data.code === 200) {
      // 保存Token到localStorage
      localStorage.setItem('token', res.data.data.token)
      localStorage.setItem('user', JSON.stringify(res.data.data.user))
      localStorage.setItem('menus', JSON.stringify(res.data.data.menus))
      localStorage.setItem('rememberMe', res.data.data.rememberMe)
      
      // 跳转到首页
      router.push('/Index')
    } else {
      // 显示错误信息
      alert(res.data.msg)
    }
  } catch (error) {
    console.error('登录失败', error)
    alert('登录失败，请检查网络连接')
  }
}
</script>
```

### 3. Axios请求拦截器

```javascript
// utils/request.js
import axios from 'axios'

const request = axios.create({
  baseURL: 'http://localhost:8080',
  timeout: 10000
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    // 从localStorage获取Token
    const token = localStorage.getItem('token')
    if (token) {
      // 将Token添加到请求头
      config.headers['Authorization'] = 'Bearer ' + token
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    return response
  },
  error => {
    // 401表示Token过期或无效
    if (error.response && error.response.status === 401) {
      // 清除本地存储
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      localStorage.removeItem('menus')
      localStorage.removeItem('rememberMe')
      
      // 跳转到登录页
      window.location.href = '/'
    }
    return Promise.reject(error)
  }
)

export default request
```

### 4. 路由守卫

```javascript
// router/index.js
import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'login',
    component: () => import('@/views/Login.vue')
  },
  {
    path: '/Index',
    name: 'index',
    component: () => import('@/views/Index.vue'),
    meta: { requiresAuth: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  
  if (to.meta.requiresAuth && !token) {
    // 需要登录但未登录，跳转到登录页
    next('/')
  } else {
    next()
  }
})

export default router
```

## 测试步骤

### 1. 启动后端服务

```bash
cd wms
mvn clean install
mvn spring-boot:run
```

### 2. 测试登录接口

**普通登录（12小时）**：
```bash
curl -X POST http://localhost:8080/user/jwtLogin \
  -H "Content-Type: application/json" \
  -d '{
    "no": "admin",
    "password": "123456",
    "rememberMe": false
  }'
```

**记住我登录（7天）**：
```bash
curl -X POST http://localhost:8080/user/jwtLogin \
  -H "Content-Type: application/json" \
  -d '{
    "no": "admin",
    "password": "123456",
    "rememberMe": true
  }'
```

### 3. 测试需要Token的接口

```bash
# 使用返回的Token访问其他接口
curl -X GET http://localhost:8080/user/list \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

## 注意事项

1. **Token存储位置**：
   - 普通登录（12小时）：可以存储在sessionStorage（关闭浏览器清除）
   - 记住我（7天）：存储在localStorage（持久化存储）

2. **安全性**：
   - JWT密钥（SECRET_KEY）建议放在配置文件中，不要硬编码
   - 生产环境建议使用HTTPS协议
   - 敏感操作建议二次验证

3. **Token刷新**：
   - 可以在Token快过期时自动刷新
   - 或者提示用户重新登录

4. **退出登录**：
   - 前端清除localStorage中的Token
   - 后端无需额外处理（JWT是无状态的）

## 扩展建议

1. **Token刷新机制**：在Token过期前自动刷新
2. **Token黑名单**：实现主动退出功能
3. **多设备登录**：限制同一账号的登录设备数量
4. **Token审计**：记录Token的生成和使用日志
