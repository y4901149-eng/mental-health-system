/**
 * 统一风险等级工具
 * 所有页面从此文件导入，保证显示一致
 * 映射：0→正常, 1-2→关注, 3→中危, 4-5→高危
 */

/** 等级数字转文字 */
export function levelText(lv) {
  if (lv >= 4) return '高危'
  if (lv === 3) return '中危'
  if (lv >= 1) return '关注'
  return '正常'
}

/** 等级对应标签类型 */
export function levelTagType(lv) {
  if (lv >= 4) return 'danger'
  if (lv === 3) return 'warning'
  return 'info'
}

/** 情绪分数转风险等级文本 */
export function moodLevelText(score) {
  if (score == null || score === '') return ''
  const s = parseFloat(score)
  if (s <= 2.5) return '高危'
  if (s <= 3.5) return '中危'
  if (s <= 4.0) return '关注'
  return '正常'
}

/** 格式化 triggerReason 显示，替换所有数字等级为文字 */
export function formatTriggerReason(r) {
  if (!r) return '-'
  let txt = r.replace(/ \| 原文:.*/, '').trim()
  // 替换 (X级) 为 (文字)
  txt = txt.replace(/\((\d+)级\)/g, (m, l) => '(' + levelText(parseInt(l)) + ')')
  // 替换 X级- 为 文字-
  txt = txt.replace(/(\d+)级-/g, (m, l) => levelText(parseInt(l)) + '-')
  // 替换独立 X级
  txt = txt.replace(/(\d+)级/g, (m, l) => levelText(parseInt(l)))
  return txt
}

/** 标记提示的标签类型 */
export const TAG_TYPES = {
  danger: { type: 'danger', effect: 'dark' },
  warning: { type: 'warning', effect: 'dark' },
  info: { type: 'info', effect: 'dark' },
  success: { type: 'success', effect: 'dark' }
}
