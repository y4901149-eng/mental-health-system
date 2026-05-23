// 心理老师管理 API

import request from '@/utils/request'

/** 获取老师列表 */
export function getCounselorList() {
  return request({ url: '/admin/counselors', method: 'get' })
}

/** 新增老师 */
export function createCounselor(data) {
  return request({ url: '/admin/counselors', method: 'post', data })
}

/** 编辑老师 */
export function updateCounselor(id, data) {
  return request({ url: '/admin/counselors/' + id, method: 'put', data })
}

/** 删除老师 */
export function deleteCounselor(id) {
  return request({ url: '/admin/counselors/' + id, method: 'delete' })
}

/** 启用/停用 */
export function toggleCounselor(id) {
  return request({ url: '/admin/counselors/' + id + '/toggle', method: 'put' })
}

/** 获取老师可预约时间段 */
export function getCounselorSlots(id) {
  return request({ url: '/admin/counselors/' + id + '/slots', method: 'get' })
}

/** 保存老师可预约时间段 */
export function saveCounselorSlots(id, timeSlots) {
  return request({ url: '/admin/counselors/' + id + '/slots', method: 'post', data: timeSlots })
}
