// 文章相关 API

import request from '@/utils/request'

/** 分页获取文章列表 */
export function getArticleList(params) {
  return request({
    url: '/article/list',
    method: 'get',
    params
  })
}

/** 获取文章详情 */
export function getArticleDetail(id) {
  return request({
    url: '/article/' + id,
    method: 'get'
  })
}

/** 获取推荐文章 */
export function getRecommendedArticles() {
  return request({
    url: '/article/recommended',
    method: 'get'
  })
}
