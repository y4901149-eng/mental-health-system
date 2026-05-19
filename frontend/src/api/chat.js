// AI 对话 API

import request from '@/utils/request'

/** 获取会话列表 */
export function getSessions() {
  return request({ url: '/chat/sessions', method: 'get' })
}

/** 创建新会话 */
export function createSession(title) {
  return request({ url: '/chat/session/create', method: 'post', data: { title } })
}

/** 获取会话消息 */
export function getSessionMessages(sessionId) {
  return request({ url: '/chat/session/' + sessionId + '/messages', method: 'get' })
}

/** 发送消息 */
export function sendMessage(sessionId, content) {
  return request({ url: '/chat/session/' + sessionId + '/send', method: 'post', data: { content } })
}

/** 删除会话 */
export function deleteSession(sessionId) {
  return request({ url: '/chat/session/' + sessionId, method: 'delete' })
}
