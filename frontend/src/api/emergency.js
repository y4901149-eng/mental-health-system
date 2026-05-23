// 紧急联系人 API

import request from '@/utils/request'

/** 获取紧急联系人列表 */
export function getEmergencyList() {
  return request({ url: '/emergency/list', method: 'get' })
}

/** 获取默认紧急联系人 */
export function getDefaultEmergency() {
  return request({ url: '/emergency/default', method: 'get' })
}

/** 新增紧急联系人 */
export function createEmergency(data) {
  return request({ url: '/emergency/create', method: 'post', data })
}

/** 更新紧急联系人 */
export function updateEmergency(data) {
  return request({ url: '/emergency/update', method: 'put', data })
}

/** 删除紧急联系人 */
export function deleteEmergency(id) {
  return request({ url: '/emergency/delete/' + id, method: 'delete' })
}

/** 设置默认紧急联系人 */
export function setDefaultEmergency(id) {
  return request({ url: '/emergency/default/' + id, method: 'post' })
}
