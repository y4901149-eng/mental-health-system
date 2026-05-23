// 情绪记录相关 API

import request from '@/utils/request'

/** 新增一条情绪记录 */
export function recordMood(data) {
  return request({
    url: '/mood/record',
    method: 'post',
    data
  })
}

/** 获取情绪趋势数据 */
export function getMoodTrend(days) {
  return request({
    url: '/mood/trend',
    method: 'get',
    params: { days: days || 30 }
  })
}

/** 获取情绪分布数据 */
export function getMoodDistribution(days) {
  return request({
    url: '/mood/distribution',
    method: 'get',
    params: { days: days || 30 }
  })
}

/** 获取情绪统计摘要 */
export function getMoodSummary(days) {
  return request({
    url: '/mood/summary',
    method: 'get',
    params: { days: days || 30 }
  })
}

/** 获取今日情绪统计和记录列表 */
export function getTodayMood() {
  return request({
    url: '/mood/today',
    method: 'get'
  })
}

/** 获取最近一段时间的情绪记录 */
export function getMoodRecords(days) {
  return request({
    url: '/mood/records',
    method: 'get',
    params: { days: days || 30 }
  })
}
