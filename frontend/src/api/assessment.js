// 心理评估相关 API

import request from '@/utils/request'

/** 获取已发布量表列表 */
export function getAssessmentList() {
  return request({
    url: '/assessment/list',
    method: 'get'
  })
}

/** 获取量表详情（含题目） */
export function getAssessmentDetail(id) {
  return request({
    url: '/assessment/' + id,
    method: 'get'
  })
}

/** 提交评估答案 */
export function submitAssessment(data) {
  return request({
    url: '/assessment/submit',
    method: 'post',
    data
  })
}

/** 获取用户的评估历史 */
export function getUserRecords() {
  return request({
    url: '/assessment/records',
    method: 'get'
  })
}

/** 获取单次测评结果详情 */
export function getAssessmentRecord(id) {
  return request({
    url: '/assessment/record/' + id,
    method: 'get'
  })
}

/** 获取测评板块列表（用户端） */
export function getAssessmentCategories() {
  return request({ url: '/assessment/categories', method: 'get' })
}

/** 获取板块列表（管理员） */
export function getCategoryList() {
  return request({ url: '/admin/categories', method: 'get' })
}

/** 新增板块 */
export function createCategory(data) {
  return request({ url: '/admin/categories', method: 'post', data })
}

/** 编辑板块 */
export function updateCategory(id, data) {
  return request({ url: '/admin/categories/' + id, method: 'put', data })
}

/** 删除板块 */
export function deleteCategory(id) {
  return request({ url: '/admin/categories/' + id, method: 'delete' })
}

/** 启用/停用板块 */
export function toggleCategory(id) {
  return request({ url: '/admin/categories/' + id + '/toggle', method: 'put' })
}
