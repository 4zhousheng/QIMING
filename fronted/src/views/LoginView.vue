<!--<template>-->
<!--  &lt;!&ndash; 1. 【布局修正】使用 flex 容器来实现真正的全屏垂直水平居中 &ndash;&gt;-->
<!--  <div class="auth-container">-->
<!--    <el-card class="auth-card">-->
<!--      <template #header>-->
<!--        <div class="card-header">-->
<!--          <span>用户登录</span>-->
<!--        </div>-->
<!--      </template>-->

<!--      &lt;!&ndash; 表单部分使用了 Element Plus 组件，并添加了美化属性 &ndash;&gt;-->
<!--      <el-form :model="loginForm" :rules="rules" ref="formRef" @submit.prevent="handleLogin" label-position="top">-->
<!--        <el-form-item label="用户名" prop="username">-->
<!--          <el-input v-model="loginForm.username" placeholder="请输入用户名" size="large" />-->
<!--        </el-form-item>-->
<!--        <el-form-item label="密码" prop="password">-->
<!--          <el-input type="password" v-model="loginForm.password" placeholder="请输入密码" show-password size="large" />-->
<!--        </el-form-item>-->
<!--        <el-form-item>-->
<!--          <el-button type="primary" native-type="submit" :loading="loading" style="width:100%;" size="large">登录</el-button>-->
<!--        </el-form-item>-->
<!--      </el-form>-->
<!--      <div class="links">-->
<!--        <router-link to="/register">没有账户？去注册</router-link>-->
<!--      </div>-->
<!--    </el-card>-->
<!--  </div>-->
<!--</template>-->

<!--<script setup lang="ts">-->
<!--// -&#45;&#45; Script 部分：完全保留你原来的、正确的逻辑 -&#45;&#45;-->
<!--import { reactive, ref } from 'vue';-->
<!--import { useRouter } from 'vue-router';-->
<!--import { useUserStore } from '@/stores/user';-->
<!--import api from '@/services/api';-->
<!--import { ElMessage, type FormInstance, type FormRules } from 'element-plus';-->
<!--const router = useRouter();-->
<!--const userStore = useUserStore();-->
<!--const loading = ref(false);-->
<!--const formRef = ref<FormInstance>();-->

<!--const loginForm = reactive({-->
<!--  username: '',-->
<!--  password: '',-->
<!--});-->

<!--const rules = reactive<FormRules>({-->
<!--  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],-->
<!--  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],-->
<!--});-->

<!--const handleLogin = async () => {-->
<!--  if (!formRef.value) return;-->

<!--  loading.value = true;-->
<!--  try {-->
<!--    await formRef.value.validate();-->

<!--    // 【核心修正】api.post 现在返回的是完整的 axios response 对象-->
<!--    const response = await api.post('/user/login', loginForm);-->

<!--    const result = response.data; // 我们自己获取 response.data-->

<!--    // 【核心修正】我们自己在这里判断业务 code-->
<!--    if (result.code === 200) {-->
<!--      // 登录成功-->
<!--      const jwtToken = result.data;-->
<!--      userStore.setToken(jwtToken);-->
<!--      router.push('/');-->
<!--    } else {-->
<!--      // 登录失败 (密码错误等)，由我们自己弹出提示-->
<!--      ElMessage.error(result.message || '登录失败，请检查您的凭证');-->
<!--    }-->

<!--  } catch (error: any) {-->
<!--    // 这个 catch 现在主要捕获网络错误或 500 等由拦截器抛出的错误-->
<!--    console.error('登录流程失败:', error);-->
<!--    // 拦截器已经弹出了消息，这里可以不用再弹-->
<!--  } finally {-->
<!--    loading.value = false;-->
<!--  }-->
<!--};-->
<!--</script>-->

<!--<style scoped>-->
<!--/* 2. 【布局修正】添加全屏居中和美化样式 */-->
<!--.auth-container {-->
<!--  display: flex;-->
<!--  justify-content: center; /* 水平居中 */-->
<!--  align-items: center;    /* 垂直居中 */-->
<!--  width: 100vw;           /* 宽度撑满整个视口 */-->
<!--  height: 100vh;          /* 高度撑满整个视口 */-->
<!--  background: linear-gradient(to top, #cfd9df 0%, #e2ebf0 100%);-->
<!--}-->

<!--.auth-card {-->
<!--  width: 400px;-->
<!--  padding: 10px;-->
<!--  border-radius: 10px;-->
<!--}-->

<!--.card-header { text-align: center; font-size: 24px; font-weight: bold; color: #303133; }-->
<!--.links { text-align: center; margin-top: 15px; }-->
<!--.el-form-item { margin-bottom: 25px; }-->
<!--</style>-->
<template>
  <!-- 1. 【布局修正】使用 flex 容器来实现真正的全屏垂直水平居中 -->
  <div class="auth-container">
    <el-card class="auth-card">
      <template #header>
        <div class="card-header">
          <img src="@/assets/logo.svg" alt="Logo" class="logo" />
          <span class="app-name">商品管理系统</span>
        </div>
      </template>

      <!-- 表单部分使用了 Element Plus 组件，并添加了美化属性 -->
      <el-form :model="loginForm" :rules="rules" ref="formRef" @submit.prevent="handleLogin" label-position="top">
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名"
            size="large"
            autocomplete="username"
          >
            <template #prefix>
              <el-icon><User /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
            type="password"
            v-model="loginForm.password"
            placeholder="请输入密码"
            show-password
            size="large"
            autocomplete="current-password"
          >
            <template #prefix>
              <el-icon><Lock /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            native-type="submit"
            :loading="loading"
            style="width:100%;"
            size="large"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>

      <div class="links">
        <router-link to="/register">没有账户？去注册</router-link>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue';
import { useRouter, useRoute } from 'vue-router'; // 导入 useRoute 获取查询参数
import { useUserStore } from '@/stores/user';
import api from '@/services/api';
import { ElMessage, type FormInstance, type FormRules } from 'element-plus';
import { User, Lock } from '@element-plus/icons-vue';

const router = useRouter();
const route = useRoute(); // 获取当前路由信息
const userStore = useUserStore();
const loading = ref(false);
const formRef = ref<FormInstance>();

const loginForm = reactive({
  username: '',
  password: '',
});

const rules = reactive<FormRules>({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在3-20个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在6-20个字符', trigger: 'blur' }
  ],
});

const handleLogin = async () => {
  if (!formRef.value) return;

  loading.value = true;

  try {
    // 验证表单
    const valid = await formRef.value.validate();
    if (!valid) return;

    // 发送登录请求
    const response = await api.post('/user/login', loginForm);
    const result = response.data;

    // 处理登录结果
    if (response.code === 200) {
      // 保存 token
      const jwtToken = result;
      userStore.setToken(jwtToken);

      // 延迟显示成功消息
      ElMessage.success({
        message: '登录成功！',
        duration: 1500,
        onClose: () => {
          // 【关键修改】登录成功后跳转到商品列表页
          redirectAfterLogin();
        }
      });
    } else {
      // 登录失败 (密码错误等)
      ElMessage.error(response.message || '登录失败，请检查您的凭证');
    }

  } catch (error: any) {
    // 处理错误消息
    let errorMsg = '登录失败，请重试';

    if (error.response?.data?.message) {
      errorMsg = error.response.data.message;
    } else if (error.message) {
      errorMsg = error.message;
    }

    // 特殊处理凭证错误
    if (errorMsg.includes('密码错误') || errorMsg.includes('用户不存在')) {
      errorMsg = '用户名或密码错误';
    }

    ElMessage.error(errorMsg);
  } finally {
    loading.value = false;
  }
};

// 【关键修改】登录成功后的重定向逻辑
const redirectAfterLogin = () => {
  // 1. 检查是否有重定向参数
  const redirect = route.query.redirect as string | undefined;

  // 2. 检查用户是否有权限访问目标页面
  if (redirect && isValidRedirect(redirect)) {
    router.push(redirect);
  }
  // 3. 默认重定向到商品列表页
  else {
    router.push({ name: 'product-list' });
  }
};

// 验证重定向路径是否有效（防止开放重定向漏洞）
const isValidRedirect = (path: string): boolean => {
  try {
    const url = new URL(path, window.location.origin);
    // 确保重定向目标在同源内
    return url.origin === window.location.origin;
  } catch {
    return false;
  }
};
</script>

<style scoped>
/* 2. 【布局修正】添加全屏居中和美化样式 */
.auth-container {
  display: flex;
  justify-content: center; /* 水平居中 */
  align-items: center;    /* 垂直居中 */
  width: 100vw;           /* 宽度撑满整个视口 */
  height: 100vh;          /* 高度撑满整个视口 */
  background: linear-gradient(135deg, #6a11cb 0%, #2575fc 100%);
  overflow: hidden;
  position: relative;
}

.auth-container::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(255,255,255,0.1) 0%, rgba(255,255,255,0) 70%);
  z-index: 1;
}

.auth-card {
  width: 400px;
  padding: 25px 30px;
  border-radius: 16px;
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.25);
  position: relative;
  z-index: 2;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border: none;
}

.card-header {
  text-align: center;
  margin-bottom: 10px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.logo {
  width: 80px;
  height: 80px;
  margin-bottom: 15px;
}

.app-name {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  letter-spacing: 1px;
}

.links {
  text-align: center;
  margin-top: 15px;
  font-size: 14px;
}

.links a {
  color: #409eff;
  text-decoration: none;
  transition: color 0.3s;
}

.links a:hover {
  color: #66b1ff;
  text-decoration: underline;
}

.el-form-item {
  margin-bottom: 25px;
}

/* 响应式设计 */
@media (max-width: 480px) {
  .auth-card {
    width: 90vw;
    padding: 20px 15px;
  }

  .app-name {
    font-size: 20px;
  }

  .logo {
    width: 60px;
    height: 60px;
  }
}
</style>
