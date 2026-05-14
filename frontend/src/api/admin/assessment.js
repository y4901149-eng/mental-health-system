// 管理员量表管理 API

import request from '@/utils/request'

export function getAssessmentList(params) {
  return request({ url: '/admin/assessments', method: 'get', params })
}
export function createAssessment(data) {
  return request({ url: '/admin/assessments', method: 'post', data })
}
export function updateAssessment(id, data) {
  return request({ url: '/admin/assessments/' + id, method: 'put', data })
}
export function deleteAssessment(id) {
  return request({ url: '/admin/assessments/' + id, method: 'delete' })
}
export function getAssessmentQuestions(id) {
  return request({ url: '/admin/assessments/' + id + '/questions', method: 'get' })
}
export function createQuestion(data) {
  return request({ url: '/admin/assessments/questions', method: 'post', data })
}
export function updateQuestion(id, data) {
  return request({ url: '/admin/assessments/questions/' + id, method: 'put', data })
}
export function deleteQuestion(id) {
  return request({ url: '/admin/assessments/questions/' + id, method: 'delete' })
}
