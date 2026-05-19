/**
 * 危机关键词统一管理
 * 启动时从后端 API 加载关键词，失败时用硬编码 fallback
 * 所有前端页面从此文件导入
 */

import request from '@/utils/request'

// ===== 硬编码 fallback =====
const FALLBACK_KWS = [
  '自杀', '想死', '不想活', '活不下去', '结束生命',
  '伤害自己', '自残', '轻生',
  '崩溃', '绝望',
  '没有意义', '活着的意义', '好累', '撑不下去'
]

const FALLBACK_HIGH = ['自杀', '想死', '不想活', '活不下去', '结束生命', '伤害自己', '自残', '轻生']
const FALLBACK_MID  = ['崩溃', '绝望', '撑不下去']
const FALLBACK_LOW  = ['没有意义', '活着的意义', '好累']

// ===== 运行时数据（启动时从 API 加载） =====
let _keywords = []        // 完整关键词列表
let _high = []            // 高危
let _mid = []             // 中危
let _low = []             // 关注
let _loaded = false       // 是否已从 API 加载

// ===== 公开的数据（供组件使用） =====
export { _keywords as RISK_KWS }

/** 从后端加载关键词，失败时用 fallback */
export async function initKeywords() {
  if (_loaded) return  // 防止重复加载
  try {
    const res = await request.get('/crisis/keywords')
    const data = res.data || {}

    _high = (data.high || []).map(item => item.keyword)
    _mid  = (data.mid  || []).map(item => item.keyword)
    _low  = (data.low  || []).map(item => item.keyword)
    _keywords = [..._high, ..._mid, ..._low]

    if (_keywords.length > 0) {
      _loaded = true
      console.log('✅ 已从数据库加载 ' + _keywords.length + ' 个危机关键词')
      return
    }
  } catch (e) {
    console.warn('⚠️ 加载关键词失败，使用本地 fallback')
  }

  // fallback
  _high = [...FALLBACK_HIGH]
  _mid  = [...FALLBACK_MID]
  _low  = [...FALLBACK_LOW]
  _keywords = [...FALLBACK_KWS]
  _loaded = true
}

/** 从文本中提取第一个匹配的风险关键词 */
export function extractKeyword(text) {
  if (!text) return ''
  for (const kw of _keywords) {
    if (text.includes(kw)) return kw
  }
  return ''
}

/** 根据关键词判断风险等级文本 */
export function riskLevelFromKeyword(text) {
  if (!text) return '关注'
  for (const kw of _high) { if (text.includes(kw)) return '高危' }
  for (const kw of _mid)  { if (text.includes(kw)) return '中危' }
  for (const kw of _low)  { if (text.includes(kw)) return '关注' }
  return '关注'
}

/** 根据关键词判断风险等级 Tag 类型 */
export function riskTypeFromKeyword(text) {
  const lv = riskLevelFromKeyword(text)
  if (lv === '高危') return 'danger'
  if (lv === '中危') return 'warning'
  return 'warning'
}

/** 根据分数判断风险等级（用于日记/情绪页） */
export function riskLevelFromScore(score) {
  if (score == null) return '未知'
  if (score >= 60) return '正常'
  if (score >= 40) return '关注'
  if (score >= 20) return '中危'
  return '高危'
}

export function riskTypeFromScore(score) {
  if (score == null) return 'info'
  if (score >= 60) return 'success'
  if (score >= 40) return 'warning'
  if (score >= 20) return 'warning'
  return 'danger'
}

/** 根据 alertLevel 判断预警风险等级 */
export function alertLevelText(level) {
  if (level >= 4) return '高危'
  if (level >= 3) return '中危'
  return '关注'
}

export function alertLevelType(level) {
  if (level >= 4) return 'danger'
  if (level >= 3) return 'warning'
  return 'info'
}
