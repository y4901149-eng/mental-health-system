// 管理员管理 API（日记/情绪/周报/对话）

import request from '@/utils/request'

export function getAdminDiaryList(params) {
  return request({ url: '/admin/diary', method: 'get', params })
}
export function deleteAdminDiary(id) {
  return request({ url: '/admin/diary/' + id, method: 'delete' })
}

export function getAdminMoodList(params) {
  return request({ url: '/admin/mood', method: 'get', params })
}
export function deleteAdminMood(id) {
  return request({ url: '/admin/mood/' + id, method: 'delete' })
}

export function getAdminReportList(params) {
  return request({ url: '/admin/report', method: 'get', params })
}
export function deleteAdminReport(id) {
  return request({ url: '/admin/report/' + id, method: 'delete' })
}

export function getAdminChatList(params) {
  return request({ url: '/admin/chat', method: 'get', params })
}
export function deleteAdminChat(id) {
  return request({ url: '/admin/chat/' + id, method: 'delete' })
}
export function getAdminChatMessages(id) {
  return request({ url: '/admin/chat/' + id + '/messages', method: 'get' })
}
