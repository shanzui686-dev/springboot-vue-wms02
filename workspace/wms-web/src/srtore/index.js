import { createStore } from 'vuex'
import router from "@/router";
import createPersistedState from 'vuex-persistedstate'

function addNewRoutes(menuList) {
  console.log(menuList)
  let routes = router.options.routes
  console.log(routes)
  menuList.forEach(menu => {
    routes.forEach(routerItem => {
      if (routerItem.path === "/Index") {
        let childRoute = {
          path: '/' + menu.menuClick,
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