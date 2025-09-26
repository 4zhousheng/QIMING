// src/services/api.ts
import axios, { type AxiosError, type AxiosInstance, type InternalAxiosRequestConfig, type AxiosResponse } from 'axios';
import { ElMessage } from 'element-plus';
import router from '@/router';
import { useUserStore } from '@/stores/user';

// 创建 axios 实例
const api: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8081/api',
  timeout: 10000,
  // headers: {
  //   // 'Content-Type': 'application/json',
  // },
});

// 错误代码映射
const ERROR_MESSAGES: Record<number | string, string> = {
  400: '请求参数错误',
  401: '身份验证失败，请重新登录',
  403: '权限不足，无法访问此资源',
  404: '资源不存在',
  408: '请求超时',
  500: '服务器内部错误',
  502: '网关错误',
  503: '服务不可用',
  ECONNABORTED: '请求超时',
  NETWORK_ERROR: '网络连接失败',
  DEFAULT: '请求失败，请稍后再试',
};

// 创建用户存储实例的函数
const getUserStore = () => {
  try {
    return useUserStore();
  } catch (error) {
    console.warn('无法访问用户存储，可能未初始化');
    return {
      token: null as string | null,
      clearToken: () => {},
      logout: () => {},
      isAuthenticated: false,
    };
  }
};

// 处理用户登出
const handleLogout = () => {
  const userStore = getUserStore();

  // 清除认证信息
  localStorage.removeItem('authToken');
  userStore.clearToken?.();
  userStore.logout?.();

  // 避免在登录页面显示错误
  // 使用路由名称而不是路径进行判断
  if (router.currentRoute.value.name !== 'login') {
    // 重定向到登录页面
    router.push({
      name: 'login',
      query: {
        redirect: router.currentRoute.value.fullPath
      }
    });
  }
};

// 请求拦截器 - 添加认证 Token
api.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  if (!(config.data instanceof FormData || config.data instanceof URLSearchParams)) {
    config.headers['Content-Type'] = 'application/json';
  }
  const token = localStorage.getItem('authToken');
  if (token) {
    // 验证 token 格式
    if (token.split('.').length === 3) {
      config.headers = config.headers || {};
      config.headers.Authorization = `Bearer ${token}`;
    } else {
      console.warn('无效的 token 格式，跳过 Authorization 头设置');
      handleLogout(); // 清除无效 token
    }
  }

  return config;
}, (error) => {
  return Promise.reject(error);
});

// 响应拦截器 - 统一错误处理
api.interceptors.response.use(
  (response: AxiosResponse) => {
    // 直接返回响应数据
    return response.data;
  },
  (error: AxiosError) => {
    const status = error.response?.status;
    const routeName = router.currentRoute.value.name;

    // 处理 401 未授权错误
    if (status === 401) {
      ElMessage.warning('会话已过期，请重新登录');
      handleLogout();
      return Promise.reject(new Error('会话过期'));
    }

    // 处理 403 禁止访问错误 - 强制退出登录
    if (status === 403) {
      // 仅在非登录/注册页面显示错误
      if (routeName !== 'login' && routeName !== 'register') {
        ElMessage.error('权限不足，请重新登录');
      }
      handleLogout();
      return Promise.reject(new Error('权限不足'));
    }

    // 处理其他错误
    let errorMessage = ERROR_MESSAGES.DEFAULT;

    if (status && ERROR_MESSAGES[status]) {
      errorMessage = ERROR_MESSAGES[status];
    } else if (error.code && ERROR_MESSAGES[error.code as string]) {
      errorMessage = ERROR_MESSAGES[error.code as string];
    } else if (error.response?.data && typeof error.response.data === 'object' && 'message' in error.response.data) {
      errorMessage = (error.response.data as any).message;
    } else if (error.message) {
      errorMessage = error.message;
    }

    // 仅在非登录/注册页面显示错误
    if (routeName !== 'login' && routeName !== 'register') {
      ElMessage.error(errorMessage);
    }

    return Promise.reject(error);
  }
);
export interface ApiResult<T = any> {
  code: number;
  message: string;
  data: T;
}
export default api;
