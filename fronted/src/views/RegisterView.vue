<template>
  <div class="register-container">
    <el-card class="register-card">
      <template #header>
        <div class="card-header">
          <span>新用户注册</span>
        </div>
      </template>
      <el-form
        :model="registerForm"
        :rules="rules"
        ref="formRef"
        @submit.prevent="handleRegister"
        label-position="top"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="registerForm.username" placeholder="请输入用户名" size="large" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            type="password"
            v-model="registerForm.password"
            placeholder="请输入密码"
            show-password
            size="large"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="success" native-type="submit" :loading="loading" style="width: 100%;" size="large">
            立即注册
          </el-button>
        </el-form-item>
      </el-form>

      <div class="links">
        <router-link to="/login">已有账户？直接登录</router-link>
      </div>
    </el-card></div>
</template>
<script setup lang="ts">
import { reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import api from '@/services/api';
import { ElMessage, type FormInstance, type FormRules } from 'element-plus';

const router = useRouter();
const loading = ref(false);
const formRef = ref<FormInstance>();
const registerForm = reactive({
  username: '',
  password: '',
});
const rules = reactive<FormRules>({
  username: [{ required: true, message: '用户名不能为空', trigger: 'blur' }, { min: 3, max: 15, message: '长度应在 3 到 15 个字符', trigger: 'blur' }],
  password: [{ required: true, message: '密码不能为空', trigger: 'blur' }, { min: 6, message: '密码长度不能少于 6 位', trigger: 'blur' }],
});

const handleRegister = async () => {
  if (!formRef.value) return;

  loading.value = true;
  try {
    await formRef.value.validate();

    // api.post 返回的是完整的 axios response 对象
    const response = await api.post('/user/register', registerForm);
    const result = response.data;

    if (result.code === 200) {
      ElMessage.success(result.data || '恭喜您，注册成功！即将跳转到登录页面...');
      setTimeout(() => {
        router.push('/login');
      }, 2000);
    } else {
      // 注册失败 (比如用户名已存在)，由组件自己弹出提示
      ElMessage.error(result.message || '注册失败，请稍后重试');
    }
  } catch (error: any) {
    console.error('注册流程失败:', error);
    // 网络错误等已由拦截器处理
  } finally {
    loading.value = false;
  }
};
</script>
<style scoped>
/*//.register-container {
//  display: flex;
//  justify-content: center;
//  align-items: center;
//  height: 100%; !* 注意：这里是 100%，因为它会被放到 full-screen-container 内部 *!
//  background: linear-gradient(to top, #cfd9df 0%, #e2ebf0 100%);
//}*/
/* 这是核心！这个样式将作用于这个页面的根元素 */
.register-container {
  display: flex;
  justify-content: center; /* 水平居中 */
  align-items: center;    /* 垂直居中 */
  width: 100vw;           /* 宽度撑满整个视口 */
  height: 100vh;          /* 高度撑满整个视口 */
  background: linear-gradient(to top, #cfd9df 0%, #e2ebf0 100%);
}
.register-card { width: 400px; padding: 10px; border-radius: 10px; }
.card-header { text-align: center; font-size: 24px; font-weight: bold; color: #303133; }
.links { text-align: center; margin-top: 15px; }
.el-form-item { margin-bottom: 25px; }
</style>
