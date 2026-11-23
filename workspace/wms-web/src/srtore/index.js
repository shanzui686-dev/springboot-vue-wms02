import { createStore } from 'vuex'
import router from "@/router";
import createPersistedState from 'vuex-persistedstate'

function addNewRoutes(menuList) {
  console.log(menuList)
  let routes = router.options.routes
  console.log(routes)
  menuList.forEach(menu => {
    // 跳过一级菜单（menuClick为空），只处理有实际路由的二级菜单
    if (!menu.menuClick || menu.menuClick === '') return
    routes.forEach(routerItem => {
      if (routerItem.path === "/Index") {
        // menuClick 可能已包含前导 /，避免双斜杠
        const routePath = menu.menuClick.startsWith('/') ? menu.menuClick : '/' + menu.menuClick
        let childRoute = {
          path: routePath,
          name: menu.menuName,
          meta: {
            title: menu.menuName
          },
          component: () => import('../components/' + menu.menuComponent)
        }
        routerItem.children.push(childRoute)
      }
    })
  })
  routes.forEach(route => {
    router.addRoute(route)
  })
}

export default createStore({
  state: {
    menu: []
  },
  mutations: {
    setMenu(state, menuList) {
      state.menu = menuList
      addNewRoutes(menuList)
    }
  },

  getters: {
    getMenu(state) {
      return state.menu
    }
  },
  plugins: [
    createPersistedState({})
  ],
})