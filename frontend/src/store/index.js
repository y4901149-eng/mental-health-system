// Vuex 状态管理入口
// 作用：管理全局状态（用户信息等），所有组件都能通过 this.$store 访问

import Vue from 'vue'
import Vuex from 'vuex'
import user from './modules/user'

Vue.use(Vuex)

const store = new Vuex.Store({
  modules: {
    user  // 用户模块
  }
})

export default store
