// src/stores/user.ts
import { defineStore } from 'pinia';
import { ref } from 'vue';

export const useUserStore = defineStore('user', () => {
  // 状态
  const token = ref<string | null>(null);
  const userInfo = ref<any>(null);

  // 计算属性
  const isAuthenticated = ref(false);

  // 初始化时从 localStorage 加载 token
  const initToken = () => {
    const savedToken = localStorage.getItem('authToken');
    if (savedToken && savedToken.split('.').length === 3) {
      token.value = savedToken;
      isAuthenticated.value = true;
    }
  };

  // 初始化
  initToken();

  // 操作方法
  const setToken = (newToken: string) => {
    token.value = newToken;
    isAuthenticated.value = true;
    localStorage.setItem('authToken', newToken);
  };

  const clearToken = () => {
    token.value = null;
    isAuthenticated.value = false;
    localStorage.removeItem('authToken');
  };

  const logout = () => {
    clearToken();
    userInfo.value = null;
    // 这里可以添加其他登出清理逻辑
  };

  const setUserInfo = (info: any) => {
    userInfo.value = info;
  };

  return {
    token,
    userInfo,
    isAuthenticated,
    setToken,
    clearToken,
    logout,
    setUserInfo
  };
});
