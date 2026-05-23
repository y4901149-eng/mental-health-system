// 文章相关 API

import request from '@/utils/request'

/** 分页获取文章列表（用户端） */
export function getArticleList(params) {
  return request({
    url: '/article/list',
    method: 'get',
    params
  })
}

/** 获取文章详情（用户端） */
export function getArticleDetail(id) {
  return request({
    url: '/article/' + id,
    method: 'get'
  })
}

/** 获取推荐文章（用户端） */
export function getRecommendedArticles() {
  return request({
    url: '/article/recommended',
    method: 'get'
  })
}

/** 获取文章列表（管理员） */
export function getAdminArticleList(params) {
  return request({
    url: '/admin/articles',
    method: 'get',
    params
  })
}

/** 新增文章（管理员） */
export function createArticle(data) {
  return request({
    url: '/admin/articles',
    method: 'post',
    data
  })
}

/** 编辑文章（管理员） */
export function updateArticle(id, data) {
  return request({
    url: '/admin/articles/' + id,
    method: 'put',
    data
  })
}

/** 删除文章（管理员） */
export function deleteArticle(id) {
  return request({
    url: '/admin/articles/' + id,
    method: 'delete'
  })
}
