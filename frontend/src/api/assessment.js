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
