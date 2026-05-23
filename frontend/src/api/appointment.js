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

/** 获取咨询师列表 */
export function getCounselors() {
  return request({
    url: '/appointment/counselors',
    method: 'get'
  })
}

/** 获取某咨询师某日期已被预约的时间段 */
export function getBookedSlots(counselorName, date) {
  return request({
    url: '/appointment/booked-slots',
    method: 'get',
    params: { counselorName, date }
  })
}

/** 获取咨询师可预约时间段（用户端） */
export function getCounselorTimeSlots(counselorId, date) {
  return request({
    url: '/appointment/counselors/' + counselorId + '/slots',
    method: 'get',
    params: { date }
  })
}
