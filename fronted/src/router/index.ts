// src/router/index.ts
import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user';

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/LoginView.vue'),
      meta: {fullScreen:true}
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('../views/RegisterView.vue'),
      meta: {fullScreen:true}
    },
    {
      path: '/product/:spuId', // 【新增】详情页路由，spuId 是动态参数
      name: 'product-detail',
      component: () => import('../views/ProductDetailView.vue'),
      meta: { requiresAuth: true },
      props: true // 允许将路由参数作为 props 传递给组件
    },
    {
      path: '/',
      name: 'home',
      redirect: '/products/list' // 【改动】默认重定向到商品列表页
    },
    {
      path: '/products/list', // 【新增】商品列表页路由
      name: 'product-list',
      component: () => import('../views/ProductListView.vue'),
      meta: { requiresAuth: true },
      props: true
    },
    {
      path: '/products/create',
      name: 'product-create',
      component: () => import('../views/ProductCreateView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/products/create',
      name: 'product-create',
      component: () => import('../views/ProductCreateView.vue'),
      meta: { requiresAuth: true } // 标记这个路由需要认证
    } ,
    {
      path: '/profile',
      name: 'Profile',
      component: () => import('@/views/Profile.vue'),
      meta: {
        // 如果需要登录才能访问，可以添加路由元信息
        requiresAuth: true
      }
    }
  ]
})

// 全局前置路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore();

  if (to.meta.requiresAuth && !userStore.isAuthenticated) {
    // 如果目标路由需要认证，但用户未登录
    next({ name: 'login' }); // 重定向到登录页
  } else if ((to.name === 'login' || to.name === 'register') && userStore.isAuthenticated) {
    // 如果用户已登录，但想访问登录/注册页
    next({ name: 'home' }); // 重定向到首页
  } else {
    // 其他情况，正常放行
    next();
  }
})

export default router
