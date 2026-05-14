// 用户状态管理模块
// 作用：管理登录状态、用户信息，提供登录/退出操作

import { setToken, removeToken, setUserInfo, getUserInfo } from '@/utils/auth'
import { login, getUserInfo as fetchUserInfo } from '@/api/user'

const user = {
  // 状态
  state: {
    token: '',
    userInfo: null
  },

  // 获取状态值（类似计算属性）
  getters: {
    isLoggedIn: state => !!state.token,
    userInfo: state => state.userInfo,
    isAdmin: state => state.userInfo && state.userInfo.role === 'admin'
  },

  // 修改状态（同步操作）
  mutations: {
    SET_TOKEN: (state, token) => {
      state.token = token
    },
    SET_USER_INFO: (state, info) => {
      state.userInfo = info
    },
    RESET: (state) => {
      state.token = ''
      state.userInfo = null
    }
  },

  // 异步操作（登录、退出等）
  actions: {
    // 登录
    login({ commit }, loginData) {
      return new Promise((resolve, reject) => {
        login(loginData).then(res => {
          const data = res.data
          // 保存 token
          commit('SET_TOKEN', data.token)
          setToken(data.token)
          // 保存用户信息
          commit('SET_USER_INFO', data)
          setUserInfo(data)
          resolve(data)
        }).catch(error => {
          reject(error)
        })
      })
    },

    // 退出登录
    logout({ commit }) {
      commit('RESET')
      removeToken()
    },

    // 从 localStorage 恢复登录状态（页面刷新后调用）
    restoreLogin({ commit }) {
      const token = localStorage.getItem('mental_health_token')
      const userInfo = getUserInfo()
      if (token && userInfo) {
        commit('SET_TOKEN', token)
        commit('SET_USER_INFO', userInfo)
      }
    }
  }
}

export default user
