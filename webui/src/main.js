import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

import App from './App.vue'
import router from './router'
// import './api/mock' // 如果需要使用模拟数据，可以启用这行

// 导入主样式文件
import './assets/main.css'

const app = createApp(App)

// 注册 Element Plus 图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 使用插件
app.use(createPinia())
app.use(router)
app.use(ElementPlus, {
  locale: zhCn,
  size: 'default', // 设置组件默认尺寸
  zIndex: 3000    // 设置弹框默认层级
})

// 全局错误处理
app.config.errorHandler = (err, vm, info) => {
  console.error('全局错误:', err, info)
  // 可以在这里添加错误监控上报逻辑
}

// 挂载应用
app.mount('#app')

console.log('应用已成功启动')
console.log('环境信息:', import.meta.env)
