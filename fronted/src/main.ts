// src/main.ts

// 1. 导入所有需要的模块
import './assets/main.css'
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'

import App from './App.vue'
import router from './router'

// 2. 创建唯一的 Pinia 实例
const pinia = createPinia()

// 3. 为这个唯一的 Pinia 实例安装插件
pinia.use(piniaPluginPersistedstate)

// 4. 创建 Vue 应用实例
const app = createApp(App)

// 5. 按正确的顺序为应用实例安装插件
app.use(pinia)      // 首先安装配置好的 Pinia
app.use(router)     // 然后安装路由
app.use(ElementPlus)

// 6. （推荐）等待路由准备就绪后再挂载应用，以避免初始化时序问题
router.isReady().then(() => {
  app.mount('#app')
})

// (可选) 保留这个日志用于调试，确认没有整页刷新
console.log('%c Vue App Initializing... ' + new Date().toLocaleTimeString(), 'background: #222; color: #bada55');
