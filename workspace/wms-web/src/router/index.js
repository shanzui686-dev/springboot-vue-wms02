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
    redirect: '/Home',
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
          title: '数据大屏'
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
        path: '/OperationLog',
        name: 'operationLog',
        meta: {
          title: '操作日志'
        },
        component: () => import('../components/system/OperationLog.vue')
      },
      {
        path: '/RestockSuggest',
        name: 'restockSuggest',
        meta: {
          title: '智能补货建议'
        },
        component: () => import('../components/stats/RestockSuggest.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// ==================== 路由白名单 ====================
// 不需要登录即可访问的页面
const whiteList = ['/', '/login']

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
  
  // 判断是否在白名单中
  if (whiteList.includes(to.path)) {
    // 白名单页面直接放行
    if (token && to.path === '/') {
      // 如果已登录且访问登录页，重定向到首页
      next('/Home')
    } else {
      next()
    }
  } else {
    // 非白名单页面需要验证登录状态
    if (token) {
      // 已登录，正常访问
      next()
    } else {
      // 未登录，重定向到登录页
      next('/')
    }
  }
})

export default router
