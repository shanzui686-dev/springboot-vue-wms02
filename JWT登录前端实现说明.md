# JWT 7天免登录 - 前端实现说明

## 功能概述

已在前端完成JWT 7天免登录功能的实现，主要包括：
- ✅ Login.vue 登录表单增加"记住密码"复选框
- ✅ 调用后端 `/user/jwtLogin` 接口
- ✅ Token 智能存储（localStorage/sessionStorage）
- ✅ Axios 请求拦截器自动携带 Token
- ✅ 401 错误自动处理并跳转登录页

## 已修改/创建的文件

### 1. Login.vue（登录页面）
**文件路径**: `workspace/wms-web/src/components/Login.vue`

**主要修改**:

#### ① 添加"记住我"复选框
```vue
<el-form-item>
  <div class="remember-me-wrapper">
    <el-checkbox v-model="loginForm.rememberMe" size="large">
      记住密码 (7天免登录)
    </el-checkbox>
  </div>
</el-form-item>
```

#### ② 表单数据增加 rememberMe 字段
```javascript
const loginForm = ref({
  no: '',
  password: '',
  rememberMe: false  // 是否记住我（7天免登录）
})
```

#### ③ 登录逻辑对接 JWT 接口
```javascript
// 调用JWT登录接口
axios.post(httpUrl + '/user/jwtLogin', loginForm.value)
  .then(res => {
    if (res.data.code === 200) {
      const token = res.data.data.token
      const userData = res.data.data.user
      const menuData = res.data.data.menus
      const rememberMe = res.data.data.rememberMe
      
      // 根据rememberMe决定存储位置
      if (rememberMe) {
        // 记住我：存储到localStorage（7天免登录）
        localStorage.setItem('token', token)
        localStorage.setItem('user', JSON.stringify(userData))
        localStorage.setItem('menuList', JSON.stringify(menuData))
      } else {
        // 普通登录：只存储到sessionStorage（关闭浏览器后清除）
        sessionStorage.setItem('token', token)
        sessionStorage.setItem('user', JSON.stringify(userData))
        sessionStorage.setItem('menuList', JSON.stringify(menuData))
      }
    }
  })
```

#### ④ 自定义复选框样式
```css
.remember-me-wrapper :deep(.el-checkbox__label) {
  color: rgba(255, 255, 255, 0.9);
  font-size: 14px;
}
```

### 2. main.js（全局配置）
**文件路径**: `workspace/wms-web/src/main.js`

**主要修改**:

#### ① 请求拦截器 - 自动携带 Token
```javascript
axios.interceptors.request.use(
  config => {
    // 优先从localStorage获取Token（记住我）
    let token = localStorage.getItem('token')
    if (!token) {
      token = sessionStorage.getItem('token')
    }
    
    if (token) {
      config.headers['Authorization'] = 'Bearer ' + token
    }
    
    return config
  }
)
```

#### ② 响应拦截器 - 处理 Token 过期
```javascript
axios.interceptors.response.use(
  response => response,
  error => {
    if (error.response && error.response.status === 401) {
      // 清除所有存储的登录信息
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      localStorage.removeItem('menuList')
      sessionStorage.removeItem('token')
      sessionStorage.removeItem('user')
      sessionStorage.removeItem('menuList')
      
      // 跳转登录页
      router.push('/')
    }
    return Promise.reject(error)
  }
)
```

### 3. request.js（请求工具类）- 新建
**文件路径**: `workspace/wms-web/src/utils/request.js`

**功能**:
- 创建统一的 axios 实例
- 封装 Token 自动携带逻辑
- 封装 401 错误处理
- 提供退出登录方法

**使用方法**:
```javascript
import request from '@/utils/request'

// 使用方式1：直接使用
request.get('/user/list')

// 使用方式2：在组件中使用
import { logout } from '@/utils/request'
logout()  // 退出登录
```

## Token 存储策略

### 记住我（7天免登录）
```javascript
// 存储到 localStorage（持久化）
localStorage.setItem('token', token)
localStorage.setItem('user', JSON.stringify(userData))
localStorage.setItem('menuList', JSON.stringify(menuData))
localStorage.setItem('rememberMe', 'true')

// 同时也存到 sessionStorage（当前会话可用）
sessionStorage.setItem('token', token)
sessionStorage.setItem('user', JSON.stringify(userData))
sessionStorage.setItem('menuList', JSON.stringify(menuData))
```

### 普通登录（12小时有效）
```javascript
// 只存储到 sessionStorage（关闭浏览器后清除）
sessionStorage.setItem('token', token)
sessionStorage.setItem('user', JSON.stringify(userData))
sessionStorage.setItem('menuList', JSON.stringify(menuData))

// 清除 localStorage 中的登录信息
localStorage.removeItem('token')
localStorage.removeItem('user')
localStorage.removeItem('menuList')
localStorage.removeItem('rememberMe')
```

## 工作流程

### 1. 登录流程
```
用户输入账号密码
   ↓
勾选"记住密码"？
   ├─ 是 → rememberMe: true
   └─ 否 → rememberMe: false
   ↓
调用 POST /user/jwtLogin
   ↓
后端验证密码，生成 Token
   ├─ rememberMe=true → Token 有效期 7 天
   └─ rememberMe=false → Token 有效期 12 小时
   ↓
前端接收 Token 和用户信息
   ↓
根据 rememberMe 决定存储位置
   ├─ localStorage（7天）
   └─ sessionStorage（12小时）
   ↓
跳转到首页
```

### 2. 请求流程
```
发起 HTTP 请求
   ↓
axios 请求拦截器
   ↓
从 storage 获取 Token
   ├─ 优先 localStorage（记住我）
   └─ 其次 sessionStorage（普通登录）
   ↓
添加 Authorization 请求头
   ↓
发送请求到后端
   ↓
后端验证 Token
   ├─ 有效 → 返回数据
   └─ 无效/过期 → 返回 401
   ↓
前端响应拦截器
   └─ 401 → 清除登录信息 → 跳转登录页
```

## 测试步骤

### 1. 测试普通登录（12小时）
1. 打开浏览器，访问登录页
2. 输入账号密码
3. **不勾选**"记住密码"
4. 点击"进入系统"
5. 打开浏览器开发者工具 → Application → Storage
6. 验证：
   - ✅ `sessionStorage` 中有 `token`、`user`、`menuList`
   - ❌ `localStorage` 中没有 `token`、`user`、`menuList`
7. 关闭浏览器再打开
8. 验证：需要重新登录（Token 已清除）

### 2. 测试记住我登录（7天）
1. 打开浏览器，访问登录页
2. 输入账号密码
3. **勾选**"记住密码 (7天免登录)"
4. 点击"进入系统"
5. 打开浏览器开发者工具 → Application → Storage
6. 验证：
   - ✅ `localStorage` 中有 `token`、`user`、`menuList`、`rememberMe`
   - ✅ `sessionStorage` 中有 `token`、`user`、`menuList`
7. 关闭浏览器再打开
8. 验证：**无需重新登录**（直接从 localStorage 读取 Token）

### 3. 测试 Token 自动携带
1. 登录成功后，打开开发者工具 → Network
2. 访问任意需要登录的页面（如商品管理）
3. 查看请求头：
   ```
   Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
   ```
4. 验证：所有请求都自动携带了 Token

### 4. 测试 Token 过期处理
1. 修改后端 JWT 过期时间为 1 分钟（方便测试）
2. 登录系统
3. 等待 1 分钟后刷新页面
4. 验证：
   - ✅ 弹出提示"登录已过期，请重新登录"
   - ✅ 自动跳转到登录页
   - ✅ localStorage 和 sessionStorage 中的 Token 已清除

### 5. 测试退出登录
1. 在 Header 组件中添加退出按钮
2. 点击退出按钮，调用 `logout()` 方法
3. 验证：
   - ✅ 所有存储的登录信息已清除
   - ✅ 跳转到登录页

## 退出登录实现

在 Header.vue 中添加退出按钮：

```vue
<template>
  <div class="header">
    <!-- 其他内容 -->
    <el-dropdown @command="handleCommand">
      <span class="user-info">
        {{ userName }}
        <el-icon><ArrowDown /></el-icon>
      </span>
      <template #dropdown>
        <el-dropdown-menu>
          <el-dropdown-item command="logout">退出登录</el-dropdown-item>
        </el-dropdown-menu>
      </template>
    </el-dropdown>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowDown } from '@element-plus/icons-vue'
import { logout } from '@/utils/request'

const router = useRouter()
const userName = ref(JSON.parse(sessionStorage.getItem('user') || '{}').name || '用户')

const handleCommand = (command) => {
  if (command === 'logout') {
    logout()  // 调用退出登录方法
  }
}
</script>
```

## 注意事项

### 1. Token 存储安全
- ✅ Token 存储在浏览器本地（localStorage/sessionStorage）
- ⚠️ 生产环境建议使用 HTTPS 协议
- ⚠️ 敏感操作建议二次验证

### 2. 跨域配置
确保后端 CORS 配置允许携带 Authorization 头：
```java
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowedHeaders("*")
                .allowCredentials(true);  // 允许携带凭证
    }
}
```

### 3. Token 刷新机制（可选）
当前实现 Token 过期后直接跳转登录页，如需自动刷新 Token，可添加：
```javascript
// 在响应拦截器中
if (error.response.status === 401) {
  // 尝试刷新 Token
  return refreshToken().then(newToken => {
    // 使用新 Token 重新请求
    config.headers['Authorization'] = 'Bearer ' + newToken
    return service.request(config)
  }).catch(() => {
    // 刷新失败，跳转登录页
    router.push('/')
  })
}
```

## 常见问题

### Q1: 为什么勾选"记住我"后关闭浏览器还要重新登录？
A: 检查是否同时存储到了 localStorage 和 sessionStorage。如果只存到 sessionStorage，关闭浏览器后会清除。

### Q2: Token 过期后没有自动跳转登录页？
A: 检查响应拦截器是否正确处理 401 状态码，以及后端是否返回正确的 HTTP 状态码。

### Q3: 如何查看当前 Token 的有效期？
A: 使用 JWT 解析工具（如 https://jwt.io/）粘贴 Token，查看 `exp` 字段的过期时间。

### Q4: 如何强制用户退出登录？
A: 后端可以将 Token 加入黑名单，前端清除本地存储的 Token 即可。

## 相关文件清单

| 文件 | 路径 | 说明 |
|------|------|------|
| Login.vue | workspace/wms-web/src/components/Login.vue | 登录页面（已修改） |
| main.js | workspace/wms-web/src/main.js | 全局配置（已修改） |
| request.js | workspace/wms-web/src/utils/request.js | 请求工具类（新建） |
| JwtUtils.java | wms/src/main/java/com/wms/common/JwtUtils.java | JWT 工具类（后端） |
| LoginDTO.java | wms/src/main/java/com/wms/entity/LoginDTO.java | 登录 DTO（后端） |
| UserController.java | wms/src/main/java/com/wms/controller/UserController.java | 用户控制器（后端） |
| JwtInterceptor.java | wms/src/main/java/com/wms/common/JwtInterceptor.java | JWT 拦截器（后端） |
