// 危机预警 API

import request from '@/utils/request'

/** 获取预警列表 */
export function getCrisisList(params) {
  return request({ url: '/crisis/list', method: 'get', params })
}

/** 处理预警 */
export function handleCrisis(id, remark) {
  return request({ url: '/crisis/handle', method: 'post', data: { id, remark } })
}

/** 创建预警 */
export function createCrisis(data) {
  return request({ url: '/crisis/create', method: 'post', data })
}

/** 删除预警 */
export function deleteCrisis(id) {
  return request({ url: '/crisis/' + id, method: 'delete' })
}
