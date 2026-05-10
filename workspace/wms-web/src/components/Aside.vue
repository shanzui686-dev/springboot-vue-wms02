<!-- eslint-disable vue/multi-word-component-names -->
<script setup>
import { OfficeBuilding, Menu, Setting, Management, User, Avatar, DataLine } from '@element-plus/icons-vue'
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
  return menuData
})

// 动态获取图标组件
const iconMap = {
  'el-icon-office-building': OfficeBuilding,
  'el-icon-menu': Menu,
  'el-icon-s-management': Setting,
  'el-icon-s-order': Management,
  'el-icon-s-custom': Avatar,
  'el-icon-user-solid': User
}

const getIconComponent = (iconName) => {
  console.log('图标名称:', iconName)
  return iconMap[iconName] || OfficeBuilding
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
    <el-menu-item index="/Dashboard">
      <el-icon><DataLine /></el-icon>
      <template #title>数据大屏</template>
    </el-menu-item>
    <el-menu-item :index="'/'+item.menuClick" v-for="(item,i) in menu" :key="item.menuClick">
      <el-icon v-if="item.menuIcon"><component :is="getIconComponent(item.menuIcon)" /></el-icon>
      <template #title>{{item.menuName}}</template>
    </el-menu-item>

  </el-menu>
</template>

<style scoped>

</style>