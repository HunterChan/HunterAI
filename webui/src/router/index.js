import { createRouter, createWebHashHistory } from 'vue-router'
import { useUserStore } from '../stores/user'

// 路由配置
const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: {
      title: '登录',
      requiresAuth: false
    }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/Register.vue'),
    meta: {
      title: '注册',
      requiresAuth: false
    }
  },
  {
    path: '/',
    component: () => import('../layout/MainLayout.vue'),
    meta: {
      requiresAuth: true
    },
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('../views/Home.vue'),
        meta: {
          title: '首页',
          keepAlive: true
        }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('../views/Profile.vue'),
        meta: {
          title: '个人资料',
          keepAlive: false
        }
      },
      {
        path: 'photomerge',
        name: 'PhotoMerge',
        component: () => import('../views/PhotoMergePage.vue'),
        meta: {
          title: '甜影合拍',
          keepAlive: true
        }
      },
      // 其他路由将根据需要添加
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('../views/NotFound.vue'),
    meta: {
      title: '404 - 页面未找到',
      requiresAuth: false
    }
  }
]

// 创建路由实例
const router = createRouter({
  history: createWebHashHistory(),
  routes,
  scrollBehavior() {
    // 始终滚动到顶部
    return { top: 0 }
  }
})

// 全局前置守卫
router.beforeEach((to, from, next) => {
  // 更新页面标题
  document.title = to.meta.title ? `${to.meta.title} - HunterAI` : 'HunterAI'
  
  const userStore = useUserStore()
  
  // 检查该路由是否需要登录权限
  if (to.matched.some(record => record.meta.requiresAuth)) {
    // 如果需要登录且用户未登录
    if (!userStore.isLoggedIn) {
      next({
        path: '/login',
        query: { redirect: to.fullPath } // 将要访问的路径作为参数
      })
    } else {
      next()
    }
  } else {
    // 如果不需要登录权限
    if (userStore.isLoggedIn && (to.path === '/login' || to.path === '/register')) {
      // 已登录用户不应再访问登录和注册页
      next({ path: '/' })
    } else {
      next()
    }
  }
})

export default router 