import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue')
  },
  {
    path: '/',
    component: () => import('@/layout/index.vue'),
    redirect: '/dashboard/overview',
    children: [
      {
        path: '/dashboard/overview',
        name: 'DashboardOverview',
        component: () => import('@/views/dashboard/Overview.vue')
      },
      {
        path: '/dashboard/orders',
        name: 'OrderAnalytics',
        component: () => import('@/views/dashboard/OrderAnalytics.vue')
      },
      {
        path: '/dashboard/users',
        name: 'UserAnalytics',
        component: () => import('@/views/dashboard/UserAnalytics.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
