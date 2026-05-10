<template>
  <div style="padding: 20px;">
    <h1 style="text-align: center; color: #409EFF;">{{'欢迎你！' + user.name }}</h1>
    <el-descriptions :title="'个人中心'" :column="2" size="large" border>
      <el-descriptions-item>
        <template #label>
          <i class="el-icon-user"></i>
          账号
        </template>
        {{ user.no }}
      </el-descriptions-item>
      <el-descriptions-item>
        <template #label>
          <i class="el-icon-mobile-phone"></i>
          电话
        </template>
        {{ user.phone }}
      </el-descriptions-item>
      <el-descriptions-item>
        <template #label>
          <i class="el-icon-location-outline"></i>
          性别
        </template>
        <el-tag v-if="user.sex === 0" type="danger" size="small">男</el-tag>
        <el-tag v-else-if="user.sex === 1" type="success" size="small">女</el-tag>
      </el-descriptions-item>
      <el-descriptions-item>
        <template #label>
          <i class="el-icon-timer"></i>
          年龄
        </template>
        {{ user.age }}
      </el-descriptions-item>
      <el-descriptions-item>
        <template #label>
          <i class="el-icon-setting"></i>
          角色
        </template>
        <el-tag v-if="user.roleId === 0" type="danger" size="small">系统管理员</el-tag>
        <el-tag v-else-if="user.roleId === 1" type="primary" size="small">库存管理员</el-tag>
        <el-tag v-else type="success" size="small">收银员</el-tag>
      </el-descriptions-item>
    </el-descriptions>
    <div style="margin-top: 30px; text-align: center;">
      <DateUtils />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import DateUtils from './DateUtils.vue'

const router = useRouter()
const user = ref({})

onMounted(() => {
  // 优先从localStorage获取用户信息（7天免登录），其次从sessionStorage获取（普通登录）
  let userData = localStorage.getItem('user')
  if (!userData) {
    userData = sessionStorage.getItem('user')
  }
  user.value = JSON.parse(userData || '{}')
  
  // 如果没有用户信息，说明已退出登录，跳转到登录页
  if (!user.value.id) {
    router.replace('/')
  }
})
</script>

<style scoped>
</style>
