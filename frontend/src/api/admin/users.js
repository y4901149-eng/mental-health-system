// 管理员用户管理 API

import request from '@/utils/request'

export function getAdminUserList(params) {
  return request({ url: '/admin/users', method: 'get', params })
}

export function createAdminUser(data) {
  return request({ url: '/admin/users', method: 'post', data })
}

export function updateAdminUser(id, data) {
  return request({ url: '/admin/users/' + id, method: 'put', data })
}

export function deleteAdminUser(id) {
  return request({ url: '/admin/users/' + id, method: 'delete' })
}

export function resetPassword(id) {
  return request({ url: '/admin/users/' + id + '/reset-password', method: 'post' })
}

export function toggleUserStatus(id) {
  return request({ url: '/admin/users/' + id + '/toggle-status', method: 'post' })
}
