// 情绪周报 API

import request from '@/utils/request'

export function getLatestReport() {
  return request({ url: '/report/latest', method: 'get' })
}

export function getReportList() {
  return request({ url: '/report/list', method: 'get' })
}

export function generateReport() {
  return request({ url: '/report/generate', method: 'post' })
}
