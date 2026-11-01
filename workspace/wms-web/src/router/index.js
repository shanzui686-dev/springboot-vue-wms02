import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'login',
    component: () => import('../components/Login.vue')
  },
  {
    path: '/Index',
    name: 'index',
    component: () => import('../components/Index.vue'),
    redirect: (to) => {
      // 根据用户角色决定默认首页
      const user = JSON.parse(localStorage.getItem('user') || sessionStorage.getItem('user') || '{}')
      return user.roleId === 0 ? '/Dashboard' : '/Home'
    },
    children: [
      {
        path: '/Home',
        name: 'home',
        meta: {
          title: '首页'
           },
        component: () => import('../components/Home.vue')
      },
      {
        path: '/Dashboard',
        name: 'dashboard',
        meta: {
          title: '销售统计'
        },
        component: () => import('../components/dashboard/Dashboard.vue')
      },
      {
        path: '/Admin',
        name: 'admin',
        meta: {
          title: '管理员管理'
        },
        component: () => import('../components/admin/AdminManage.vue')
        },

      {
        path: '/User',
        name: 'user',
        meta: {
          title: '用户管理'

        },

        component: () => import('../components/user/UserManage.vue')
      },
      {
        path: '/Storage',
        name: 'storage',
        meta: {
          title: '仓库管理'
        },
        component: () => import('../components/storage/StorageManage.vue')
      },
      {
        path: '/Goods',
        name: 'goods',
        meta: {
          title: '商品管理'
        },
        component: () => import('../components/goods/GoodsManage.vue')
      },
      {
        path: '/Goodstype',
        name: 'goodstype',
        meta: {
          title: '商品分类管理'
        },
        component: () => import('../components/goodstype/GoodstypeManage.vue')
      },
      {
        path: '/Record',
        name: 'record',
        meta: {
          title: '记录管理'
        },
        component: () => import('../components/record/RecordManage.vue')
      },
      {
        path: '/SalesRecord',
        name: 'salesRecord',
        meta: {
          title: '销售记录查询'
        },
        component: () => import('../components/record/SalesRecord.vue')
      },
      {
        path: '/ReturnManage',
        name: 'returnManage',
        meta: {
          title: '退换货管理'
        },
        component: () => import('../components/record/ReturnManage.vue')
      },
      {
        path: '/CashierDesk',
        name: 'cashierDesk',
        meta: {
          title: '收银台'
        },
        component: () => import('../components/cashinerdesk/CashierDesk.vue')
      },
      {
        path: '/Supplier',
        name: 'supplier',
        meta: {
          title: '供应商管理'
        },
        component: () => import('../components/supplier/SupplierManage.vue')
      },
      {
        path: '/PurchaseManage',
        name: 'purchaseManage',
        meta: {
          title: '采购管理'
        },
        component: () => import('../components/purchase/PurchaseManage.vue')
      },
      {
        path: '/PurchaseReturnReview',
        name: 'purchaseReturnReview',
        meta: {
          title: '采购退回审核'
        },
        component: () => import('../components/purchase/PurchaseReturnReview.vue')
      },
      {
        path: '/RestockSuggest',
        name: 'restockSuggest',
        meta: {
          title: '智能补货建议'
        },
        component: () => import('../components/stats/RestockSuggest.vue')
      },
      {
        path: '/Check',
        name: 'inventoryCheck',
        meta: {
          title: '库存盘点'
        },
        component: () => import('../components/inventory/Check.vue')
      },
      {
        path: '/LossReport',
        name: 'lossReport',
        meta: {
          title: '损耗管理'
        },
        component: () => import('../components/inventory/LossReport.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// ==================== 路由白名单 ====================
const whiteList = ['/', '/login']

// ==================== 角色-路由映射 ====================
// 0=店长 1=库存管理员 2=收银员
const roleRouteMap = {
  // 用户管理 - 店长管理账号，所有角色可访问个人中心(Home)
  '/Admin':          [0],           // 管理员账号管理 - 仅店长
  '/User':           [0],           // 用户账号管理 - 仅店长

  // 商品管理 - 店长+库存管理员（收银员只读）
  '/Goods':          [0, 1, 2],     // 商品信息
  '/Goodstype':      [0, 1, 2],     // 商品分类

  // 采购管理
  '/Supplier':       [0, 1],        // 供应商信息维护 - 店长+库存管理员
  '/PurchaseManage': [0, 1],        // 采购入库 - 店长+库存管理员（店长审核）
  '/PurchaseReturnReview': [0],     // 采购退回审核 - 仅店长
  '/ReturnManage':   [0, 2],        // 退换货 - 店长+收银员（店长审核）

  // 库存管理
  '/Check':          [0, 1],        // 库存盘点 - 店长+库存管理员
  '/RestockSuggest': [0, 1],        // 补货建议报表 - 店长+库存管理员

  // 销售管理
  '/CashierDesk':    [0, 2],        // 收银结算 - 店长+收银员
  '/SalesRecord':    [0, 2],        // 销售记录 - 店长+收银员

  // 统计分析 - 仅店长
  '/Dashboard':      [0],           // 销售统计
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

// ==================== 全局前置守卫 ====================
router.beforeEach((to, from, next) => {
  // 设置页面标题
  if (to.meta.title) {
    document.title = to.meta.title + ' - 超市进销存管理系统'
  } else {
    document.title = '超市进销存管理系统'
  }

  // 获取Token
  const token = getToken()

  // 获取用户信息
  const user = JSON.parse(localStorage.getItem('user') || sessionStorage.getItem('user') || '{}')
  const defaultHome = user.roleId === 0 ? '/Dashboard' : '/Home'

  // 判断是否在白名单中
  if (whiteList.includes(to.path)) {
    // 白名单页面直接放行
    if (token && to.path === '/') {
      // 如果已登录且访问登录页，重定向到默认首页
      next(defaultHome)
    } else {
      next()
    }
  } else {
    // 非白名单页面需要验证登录状态
    if (token) {
      // 已登录，检查角色权限
      const allowedRoles = roleRouteMap[to.path]
      if (allowedRoles) {
        const roleId = user.roleId
        if (roleId !== undefined && !allowedRoles.includes(roleId)) {
          next(defaultHome)
          return
        }
      }
      next()
    } else {
      // 未登录，重定向到登录页
      next('/')
    }
  }
})

export default router
