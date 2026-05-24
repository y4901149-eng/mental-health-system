// 危机预警 API

import request from '@/utils/request'

/** 获取预警列表 */
export function getCrisisList(params) {
  return request({ url: '/crisis/list', method: 'get', params })
}

/** 处理预警（status: RESOLVED 或 PENDING） */
export function handleCrisis(id, remark, status) {
  return request({ url: '/crisis/handle', method: 'post', data: { id, remark, status } })
}

/** 创建预警 */
export function createCrisis(data) {
  return request({ url: '/crisis/create', method: 'post', data })
}

/** 删除预警 */
export function deleteCrisis(id) {
  return request({ url: '/crisis/' + id, method: 'delete' })
}

/** 标记已通知监护人 */
export function notifyGuardian(data) {
  return request({ url: '/crisis/notify', method: 'post', data })
}

/** 获取预警用户的紧急联系人（管理员） */
export function getEmergencyContacts(userId) {
  return request({ url: '/crisis/' + userId + '/emergency-contacts', method: 'get' })
}
