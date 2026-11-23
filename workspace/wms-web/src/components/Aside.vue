<!-- eslint-disable vue/multi-word-component-names -->
<script setup>
import { OfficeBuilding, Menu, Setting, Management, User, Avatar, DataLine, RefreshRight, WarningFilled, Sell, HomeFilled, Box, DocumentChecked, Sort } from '@element-plus/icons-vue'
import {computed} from "vue";
import store from "../srtore";

defineProps({
  isCollapse: {
    type: Boolean,
    default: false
  }
})

const menu = computed(() => {
  const menuData = store.state.menu
  console.log('Aside 获取到的菜单数据:', menuData)
  console.log('菜单数据长度:', menuData ? menuData.length : 0)
  if (!menuData || menuData.length === 0) {
    console.warn('菜单数据为空，请重新登录')
    return []
  }
  return menuData
})

// 动态获取图标组件
const iconMap = {
  'el-icon-office-building': OfficeBuilding,
  'el-icon-menu': Menu,
  'el-icon-s-management': Setting,
  'el-icon-s-order': Management,
  'el-icon-s-custom': Avatar,
  'el-icon-user-solid': User,
  'el-icon-data-line': DataLine,
  'el-icon-refresh-right': RefreshRight,
  'el-icon-warning-filled': WarningFilled,
  'el-icon-sell': Sell,
  'el-icon-home-filled': HomeFilled,
  'Box': Box,
  'DocumentChecked': DocumentChecked,
  'Sort': Sort
}

const getIconComponent = (iconName) => {
  console.log('图标名称:', iconName)
  return iconMap[iconName] || OfficeBuilding
}

// 判断菜单是否有子菜单
const hasChildren = (menuCode) => {
  return menu.value.some(item => item.menuParentCode === menuCode)
}

// 获取菜单的子菜单
const getChildren = (menuCode) => {
  return menu.value.filter(item => item.menuParentCode === menuCode)
}
</script>

<template>
  <el-menu
      background-color="#545c64"
      text-color="#fff"
      active-text-color="#ffd04b"
      style="height: 100%"
      default-active="/Home"
      :collapse="isCollapse"
      :collapse-transition="false"
      unique-opened
       router
  >
    <el-menu-item index="/Home">
      <el-icon><OfficeBuilding /></el-icon>
      <template #title>首页</template>
    </el-menu-item>

    <!-- 渲染一级菜单（menuParentCode 为 NULL 或 '0'） — 含数据大屏等均由后端 menuRight 控制 -->
    <template v-for="item in menu" :key="item.menuCode">
      <!-- 只处理一级菜单 -->
      <template v-if="!item.menuParentCode || item.menuParentCode === '0'">
        <!-- 如果没有子菜单 -->
        <el-menu-item 
          v-if="!hasChildren(item.menuCode)" 
          :index="item.menuClick"
        >
          <el-icon v-if="item.menuIcon"><component :is="getIconComponent(item.menuIcon)" /></el-icon>
          <template #title>{{item.menuName}}</template>
        </el-menu-item>
        
        <!-- 如果有子菜单 -->
        <el-sub-menu 
          v-else
          :index="item.menuCode"
        >
          <template #title>
            <el-icon v-if="item.menuIcon"><component :is="getIconComponent(item.menuIcon)" /></el-icon>
            <span>{{item.menuName}}</span>
          </template>
          <!-- 渲染子菜单 -->
          <el-menu-item 
            v-for="child in getChildren(item.menuCode)" 
            :key="child.menuCode"
            :index="child.menuClick"
          >
            <el-icon v-if="child.menuIcon"><component :is="getIconComponent(child.menuIcon)" /></el-icon>
            <template #title>{{child.menuName}}</template>
          </el-menu-item>
        </el-sub-menu>
      </template>
    </template>

  </el-menu>
</template>

<style scoped>

</style>