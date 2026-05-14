// 首页仪表盘 API

import request from '@/utils/request'

/** 获取首页聚合统计数据 */
export function getDashboardSummary() {
  return request({
    url: '/dashboard/summary',
    method: 'get'
  })
}
