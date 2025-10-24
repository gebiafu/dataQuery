import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue')
  },
  {
    path: '/',
    name: 'Layout',
    component: () => import('@/views/Layout.vue'),
    redirect: '/datasource',
    children: [
      {
        path: '/datasource',
        name: 'DataSource',
        component: () => import('@/views/DataSource.vue'),
        meta: { title: '数据源管理' }
      },
      {
        path: '/event',
        name: 'Event',
        component: () => import('@/views/Event.vue'),
        meta: { title: '事件管理' }
      },
      {
        path: '/schedule',
        name: 'Schedule',
        component: () => import('@/views/Schedule.vue'),
        meta: { title: '调度管理' }
      },
      {
        path: '/event-log',
        name: 'EventLog',
        component: () => import('@/views/EventLog.vue'),
        meta: { title: '事件日志' }
      },
      {
        path: '/user-management',
        name: 'UserManagement',
        component: () => import('@/views/UserManagement.vue'),
        meta: { title: '用户管理', requireAdmin: true }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const userRole = localStorage.getItem('userRole')
  
  if (to.path !== '/login' && !token) {
    next('/login')
  } else if (to.path === '/login' && token) {
    next('/')
  } else if (to.meta.requireAdmin && userRole !== 'admin') {
    // 需要管理员权限但不是管理员
    next('/')
  } else {
    next()
  }
})

export default router
