// 心理咨询预约 API

import request from '@/utils/request'

/** 创建预约 */
export function createAppointment(data) {
  return request({
    url: '/appointment/create',
    method: 'post',
    data
  })
}

/** 获取我的预约列表 */
export function getMyAppointments() {
  return request({
    url: '/appointment/my',
    method: 'get'
  })
}

/** 取消预约 */
export function cancelAppointment(id) {
  return request({
    url: '/appointment/cancel/' + id,
    method: 'put'
  })
}
