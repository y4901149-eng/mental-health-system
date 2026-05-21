// Axios 请求封装
// 作用：统一管理 HTTP 请求，自动携带 token，统一处理错误

import axios from 'axios'
import { Message } from 'element-ui'
import { getToken, removeToken } from './auth'
import router from '../router'

// 创建 axios 实例
const request = axios.create({
  baseURL: '/api',      // 代理前缀，vue.config.js 中已配置转发到后端
  timeout: 15000         // 请求超时时间 15 秒
})

// 请求拦截器：每次请求前自动带上 token
request.interceptors.request.use(
  config => {
    const token = getToken()
    if (token) {
      config.headers['Authorization'] = 'Bearer ' + token
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器：统一处理返回数据和错误
request.interceptors.response.use(
  response => {
    const res = response.data
    // 后端返回 code=200 表示成功
    if (res.code === 200) {
      return res
    }
    // token 过期或未登录
    if (res.code === 401) {
      removeToken()
      router.push('/login')
      Message.error('登录已过期，请重新登录')
      return Promise.reject(new Error(res.message || '未授权'))
    }
    // 其他错误
    Message.error(res.message || '请求失败')
    return Promise.reject(new Error(res.message))
  },
  error => {
    // 请求被取消（弹窗关闭、Tab切换等），静默忽略
    if (axios.isCancel(error)) {
      return Promise.reject({ cancelled: true })
    }
    // 401 → token 过期或未登录，跳转登录页
    if (error.response && error.response.status === 401) {
      removeToken()
      router.push('/login')
      Message.error('登录已过期，请重新登录')
      return Promise.reject(error)
    }
    // 网络错误处理
    if (error.message && error.message.includes('timeout')) {
      Message.error('请求超时，请检查网络')
    } else {
      Message.error('网络错误，请稍后重试')
    }
    return Promise.reject(error)
  }
)

export default request
