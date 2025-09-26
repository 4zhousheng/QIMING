<template>
  <!--
    【动态布局核心】
    根据当前路由的 meta.fullScreen 标志，
    来决定是应用后台主布局，还是全屏布局。
  -->
  <!-- 1. 后台主布局 (需要登录后才能看到，并且不是全屏页面) -->
  <el-container v-if="!route.meta.fullScreen && userStore.isAuthenticated" class="app-container">
    <el-header class="app-header">
      <div class="logo">
        <img src="@/assets/logo.svg" alt="logo" />
        <span>Venus Commerce MGT</span>
      </div>
      <div class="header-right">
        <!-- 【智能欢迎语】绑定到 welcomeName 计算属性 -->
        <span class="welcome-text">欢迎, {{ welcomeName }}!</span>

        <!-- 用户头像与下拉菜单 -->
        <el-dropdown trigger="click">
          <el-avatar :src="avatarUrl" class="user-avatar" />
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="navigateToProfile">个人中心</el-dropdown-item>
              <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>

    <el-main class="app-main">
      <RouterView />
    </el-main>
  </el-container>
  <!-- 全局支付栏 -->
  <div v-if="!cartStore.isEmpty" class="global-checkout-bar">
    <div class="cart-info">
      <el-badge :value="cartStore.totalQuantity" class="cart-badge" type="danger" :max="99">
        <el-icon @click="isCartDrawerVisible = true" class="cart-icon"><ShoppingCartFull /></el-icon>
      </el-badge>
      <span class="total-price">总计: ¥{{ cartStore.totalPrice }}</span>
    </div>
    <el-button
      type="danger"
      round
      @click="handleCheckout"
      :loading="isCheckingOut"
    >
      去支付
    </el-button>
  </div>


  <!-- 2. 全屏布局 (例如登录/注册页) -->
  <div v-else class="full-screen-container">
    <RouterView />
  </div>
  <!-- 购物车详情抽屉 -->
  <CartDetailDrawer v-model="isCartDrawerVisible" />
</template>

<script setup lang="ts">
import { RouterView, useRoute, useRouter } from 'vue-router';
import { useUserStore } from '@/stores/user';
import { ref, watch, computed } from 'vue';
import { jwtDecode } from 'jwt-decode';
import api from '@/services/api';
import type { ApiResult } from '@/models/api';
import CartDetailDrawer from '@/components/CartDetailDrawer.vue';
import {useCartStore} from "@/stores/ cart.ts";
import {createOrder} from "@/api/order.ts";
import {ElMessageBox} from "element-plus"; // 引入抽屉组件
import {ElMessage} from "element-plus";

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();

// --- 响应式状态 ---
const username = ref('');
const nickname = ref<string | null>(null); // 用于存储昵称
const avatarUrl = ref('https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'); // 默认头像
const cartStore = useCartStore();
const isCartDrawerVisible = ref(false);
const isCheckingOut = ref(false);
// --- 计算属性 ---
const welcomeName = computed(() => {
  // 如果 nickname 存在且不为空字符串，则优先显示 nickname
  if (nickname.value) {
    return nickname.value;
  }
  // 否则，回退到显示 username
  return username.value;
});


// --- 方法 ---

// 从 Token 中解析用户名
const updateUsernameFromToken = () => {
  if (userStore.token) {
    try {
      const decoded: { sub: string } = jwtDecode(userStore.token);
      username.value = decoded.sub;
    } catch (e) {
      console.error('无法解码 JWT，可能是无效的 Token:', e);
      // 如果 token 解码失败，说明 token 已经无效，执行登出
      userStore.logout();
    }
  } else {
    username.value = '';
  }
};

// 从后端获取用户头像
const fetchUserAvatar = async () => {
  if (userStore.isAuthenticated) {
    try {
      const result = await api.get<ApiResult<string | null>>('/user/avatar/get');
      const url = result.data;
      if (url) {
        avatarUrl.value = url;
      }
    } catch (error) {
      console.error('获取用户头像失败:', error);
      // 获取失败时，保持默认头像
    }
  }
};

// 获取用户昵称
const fetchUserNickname = async () => {
  if (userStore.isAuthenticated) {
    try {
      const result = await api.get<ApiResult<string | null>>('/user/nickname/get');
      nickname.value = result.data;
    } catch (error) {
      console.error('获取用户昵称失败:', error);
      nickname.value = null; // 获取失败时，确保 nickname 为 null
    }
  }
};

// 退出登录
const handleLogout = () => {
  userStore.logout();
};

// 跳转到个人中心 (预留)
const navigateToProfile = () => {
  router.push('/profile'); // 假设未来有 /profile 路由
};


// --- 监听器 ---

// 监听 Pinia store 中 token 的变化
// 这个 watch 会在应用初始化、用户登录、用户登出时被触发
watch(
  () => userStore.token,
  (newToken) => {
    updateUsernameFromToken(); // 首先更新用户名
    if (newToken) {
      // 如果有了新 token (登录成功)，并行地获取头像和昵称
      fetchUserAvatar();
      fetchUserNickname();
    } else {
      // 如果 token 变为了 null (登出成功)，重置所有用户信息
      avatarUrl.value = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png';
      nickname.value = null;
    }
  },
  { immediate: true } // immediate: true 保证了组件在初始加载时就会立即执行一次
);

// 去支付
const handleCheckout = async () => {
  if (cartStore.isEmpty) return;

  isCheckingOut.value = true;
  try {
    const res = await createOrder(cartStore.itemList);
    if (res.code === 200) {
      ElMessage.success(res.message || '订单创建成功！');
      // 订单成功后清空购物车
      cartStore.clearCart();
      // 这里可以跳转到订单成功页或订单列表页
      // router.push('/order/success');
    } else {
      // 业务失败的提示，拦截器可能已经处理了 http 错误
      ElMessage.error(res.message || '创建订单失败');
    }
  } catch (error: any) {
    // 拦截器会处理大部分错误提示，这里可以捕获并打印
    console.error("创建订单时发生异常:", error);
    // 如果错误信息是余额不足，可以给个弹窗提示
    if (error?.response?.data?.message?.includes('余额不足')) {
      ElMessageBox.confirm(
        '您的账户余额不足，是否前往充值？',
        '提示',
        {
          confirmButtonText: '去充值',
          cancelButtonText: '取消',
          type: 'warning',
        }
      ).then(() => {
        router.push('/profile'); // 跳转到个人中心充值
      })
    }
  } finally {
    isCheckingOut.value = false;
  }
};
</script>

<style>
/*
  全局样式，影响整个应用。
  注意这里没有 `scoped`。
*/
html, body, #app {
  height: 100%;
  margin: 0;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

/* 后台主布局样式 */
.app-container {
  height: 100%;
  background-color: #f0f2f5;
}
.app-header {
  background-color: #fff;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #e7e7e7;
  padding: 0 20px;
}
.logo {
  display: flex;
  align-items: center;
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}
.logo img {
  height: 32px;
  margin-right: 12px;
}
.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}
.welcome-text {
  color: #606266;
  font-size: 14px;
}
.user-avatar {
  cursor: pointer;
  border: 1px solid #eee;
  transition: transform 0.3s;
}
.user-avatar:hover {
  transform: scale(1.1);
}
.app-main {
  padding: 20px;
  overflow-y: auto;
}

/* 全屏布局容器的样式 */
.full-screen-container {
  height: 100%;
  width: 100%;
}

.global-checkout-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 60px;
  background-color: #303133;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px 0 25px;
  z-index: 100;
  color: #fff;
  box-shadow: 0 -2px 10px rgba(0,0,0,0.1);
}
.cart-info {
  display: flex;
  align-items: center;
  gap: 20px;
}
.cart-badge {
  cursor: pointer;
}
.cart-icon {
  font-size: 28px;
}
.total-price {
  font-size: 18px;
  font-weight: bold;
}
</style>
