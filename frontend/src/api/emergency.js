// 紧急联系人 API

import request from '@/utils/request'

export function getEmergencyList() {
  return request({ url: '/emergency/list', method: 'get' })
}

export function getDefaultEmergency() {
  return request({ url: '/emergency/default', method: 'get' })
}

export function createEmergency(data) {
  return request({ url: '/emergency/create', method: 'post', data })
}

export function updateEmergency(data) {
  return request({ url: '/emergency/update', method: 'put', data })
}

export function deleteEmergency(id) {
  return request({ url: '/emergency/delete/' + id, method: 'delete' })
}

export function setDefaultEmergency(id) {
  return request({ url: '/emergency/default/' + id, method: 'post' })
}
