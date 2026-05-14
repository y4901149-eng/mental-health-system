// 认证工具函数
// 作用：管理 JWT Token 的存储、读取、删除

const TOKEN_KEY = 'mental_health_token'
const USER_KEY = 'mental_health_user'

/** 保存 token 到 localStorage */
export function setToken(token) {
  localStorage.setItem(TOKEN_KEY, token)
}

/** 从 localStorage 获取 token */
export function getToken() {
  return localStorage.getItem(TOKEN_KEY)
}

/** 删除 token（退出登录时调用） */
export function removeToken() {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(USER_KEY)
}

/** 保存用户信息 */
export function setUserInfo(userInfo) {
  localStorage.setItem(USER_KEY, JSON.stringify(userInfo))
}

/** 获取用户信息 */
export function getUserInfo() {
  const userStr = localStorage.getItem(USER_KEY)
  return userStr ? JSON.parse(userStr) : null
}

/** 检查是否已登录 */
export function isLoggedIn() {
  return !!getToken()
}
