// 项目入口文件
// 作用：初始化 Vue 实例，加载插件（ElementUI、Router、Vuex 等）

import Vue from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
import ElementUI from 'element-ui'
import 'element-ui/lib/theme-chalk/index.css'
import './assets/css/global.css'
import { initKeywords } from './utils/crisisKeywords'

// 启动时加载危机关键词（从数据库，失败用 fallback）
initKeywords()

// 1. 安装 ElementUI 组件库
Vue.use(ElementUI)

// 2. 生产模式提示关闭
Vue.config.productionTip = false

// 3. 创建 Vue 实例并挂载
new Vue({
  router,      // 路由
  store,       // 状态管理
  // 在 created 时恢复登录状态（页面刷新后自动恢复）
  created() {
    this.$store.dispatch('restoreLogin')
  },
  render: h => h(App)
}).$mount('#app')
