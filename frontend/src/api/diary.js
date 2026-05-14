// 日记相关 API

import request from '@/utils/request'

/** 分页获取日记列表 */
export function getDiaryList(params) {
  return request({
    url: '/diary/list',
    method: 'get',
    params
  })
}

/** 获取单篇日记 */
export function getDiaryDetail(id) {
  return request({
    url: '/diary/' + id,
    method: 'get'
  })
}

/** 新增日记 */
export function createDiary(data) {
  return request({
    url: '/diary/create',
    method: 'post',
    data
  })
}

/** 更新日记 */
export function updateDiary(data) {
  return request({
    url: '/diary/update',
    method: 'put',
    data
  })
}

/** 删除日记 */
export function deleteDiary(id) {
  return request({
    url: '/diary/' + id,
    method: 'delete'
  })
}

/** 上传图片 */
export function uploadImage(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/upload/image',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
