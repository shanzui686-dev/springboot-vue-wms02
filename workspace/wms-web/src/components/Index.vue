<!-- eslint-disable vue/multi-word-component-names -->
<script setup>
import { ref, onMounted, getCurrentInstance } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import Aside from "@/components/Aside.vue";
import Header from "@/components/Header.vue";
import Main from "@/components/Main.vue";
import store from '../srtore'

const { proxy } = getCurrentInstance()
const httpUrl = proxy.$httpUrl

const isCollapse = ref(false)
const aside_width = ref('200px')
const icon = ref('el-icon-s-fold')

const doCollapse = () => {
  isCollapse.value = !isCollapse.value
  aside_width.value = isCollapse.value ? '64px' : '200px'
  icon.value = isCollapse.value ? 'el-icon-s-unfold' : 'el-icon-s-fold'
}

// 页面加载时获取菜单数据
onMounted(async () => {
  const user = JSON.parse(sessionStorage.getItem('user') || '{}')
  if (user.no && user.password) {
    try {
      const res = await axios.post(httpUrl + '/user/login', {
        no: user.no,
        password: user.password
      })
      if (res.data && res.data.code === 200) {
        store.commit('setMenu', res.data.data.menus)
      }
    } catch (error) {
      console.error('获取菜单失败:', error)
    }
  }
})
</script>

<template>
  <el-container style="height: 100vh; border: 1px solid #eee; margin: 0;">
    <el-aside :width="aside_width" style="background-color: rgb(238, 241, 246); margin-left: -1px; transition: width 0.3s;">
      <Aside :isCollapse="isCollapse"></Aside>
    </el-aside>

    <el-container>
      <el-header style="text-align: right; font-size: 12px; line-height: 60px;color: #333; padding: 0 20px;border-bottom: rgba(169,169,169,0.3) 1px solid;">
          <Header @doCollapse="doCollapse" :isCollapse="isCollapse"></Header>
      </el-header>

      <el-main style="padding: 5px; height: calc(100vh - 60px); overflow: auto;">
        <router-view v-slot="{ Component }">
          <keep-alive :include="['SalesRecord']">
            <component :is="Component" />
          </keep-alive>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
/* 全局样式重置 */
:deep(html), :deep(body) {
  margin: 0;
  padding: 0;
  height: 100%;
}

/* 修复菜单标题对齐问题 */
.el-sub-menu__title {
  display: flex;
  align-items: center;
}

.el-menu-item-group__title {
  padding-left: 20px !important;
}
</style>