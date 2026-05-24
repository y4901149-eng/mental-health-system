-- =============================================
-- 危机预警演示数据
-- 覆盖: 关键词 / AI分析 / 情绪趋势 / 频率升级
-- 风险等级: 高危 / 中危 / 关注
-- 来源: AI对话 / 日记 / 情绪记录
-- =============================================

-- 清理旧数据
SET FOREIGN_KEY_CHECKS = 0;
DELETE FROM crisis_alert WHERE id > 0;
DELETE FROM emergency_contact WHERE id > 0;
ALTER TABLE crisis_alert AUTO_INCREMENT = 1;
ALTER TABLE emergency_contact AUTO_INCREMENT = 1;

-- ===== 紧急联系人 =====
INSERT INTO emergency_contact (id, user_id, name, relation, phone, is_default) VALUES
(1, 2, '张父', '父亲', '13800002001', 1),
(2, 5, 'test朋友', '朋友', '13800005001', 1),
(3, 7, '李母', '母亲', '13900007001', 1),
(4, 14, '王伟', '父亲', '13800138000', 1),
(5, 14, '李芳', '母亲', '13900139000', 0),
(6, 14, '王老师', '辅导员', '13600136000', 0),
(7, 15, '王父', '父亲', '13800015001', 1),
(8, 16, '张母', '母亲', '13800016001', 1),
(9, 17, '刘父', '父亲', '13800017001', 1),
(10, 18, '陈父', '父亲', '13800018001', 1),
(11, 18, '陈母', '母亲', '13800018002', 0);

-- ===== 危机预警 =====
-- 1. 关键词 + 高危 + AI对话
INSERT INTO crisis_alert (user_id, alert_level, handle_status, trigger_reason, create_time) VALUES
(7, 5, 'PENDING',
 'AI对话触发预警 | 关键词:自杀(5级) | AI分析:AI识别为高危(5级) | 情绪分:6.5(0级) | 最终:5级 | 原文:我真的想死，活着太累了',
 '2026-05-24 08:00:00');

-- 2. 关键词 + 中危 + 日记
INSERT INTO crisis_alert (user_id, alert_level, handle_status, trigger_reason, create_time) VALUES
(2, 3, 'PENDING',
 '日记触发预警 | 关键词:崩溃(3级) | AI分析:AI识别为中危(3级) | 情绪分:5.0(0级) | 最终:3级 | 原文:今天真的崩溃了，感觉撑不下去了',
 '2026-05-23 22:30:00');

-- 3. AI分析 + 关注 + AI对话
INSERT INTO crisis_alert (user_id, alert_level, handle_status, trigger_reason, create_time) VALUES
(5, 2, 'PENDING',
 'AI对话触发预警 | 关键词:未命中 | AI分析:AI识别为需关注(2级) | 情绪分:6.0(0级) | 最终:2级 | 原文:最近很焦虑，担心考试考不好',
 '2026-05-24 09:15:00');

-- 4. 情绪趋势 + 高危 + 情绪记录
INSERT INTO crisis_alert (user_id, alert_level, handle_status, trigger_reason, create_time) VALUES
(14, 5, 'PENDING',
 '情绪分触发预警 | 关键词:未命中 | AI分析:-(-) | 情绪分:2.3(4级) | 连续低分:5天 | 最终:5级',
 '2026-05-24 06:00:00');

-- 5. 关键词+AI分析 + 高危 + AI对话
INSERT INTO crisis_alert (user_id, alert_level, handle_status, trigger_reason, create_time) VALUES
(15, 5, 'PENDING',
 'AI对话触发预警 | 关键词:活不下去(5级) | AI分析:AI识别为高危(5级) | 情绪分:4.8(0级) | 最终:5级 | 原文:我真的活不下去了，每天都很痛苦',
 '2026-05-24 07:30:00');

-- 6. 情绪趋势+频率升级 + 中危 + 情绪记录
INSERT INTO crisis_alert (user_id, alert_level, handle_status, trigger_reason, create_time) VALUES
(16, 3, 'PENDING',
 '情绪分触发预警 | 关键词:未命中 | AI分析:-(-) | 情绪分:3.2(3级) | 连续低分:4天 | 最终:3级 | 近24h同类触发2次,已升级',
 '2026-05-24 05:00:00');

-- 7. AI分析 + 中危 + 日记
INSERT INTO crisis_alert (user_id, alert_level, handle_status, trigger_reason, create_time) VALUES
(17, 3, 'PENDING',
 '日记触发预警 | 关键词:未命中 | AI分析:AI识别为中危(3级) | 情绪分:4.5(0级) | 最终:3级 | 原文:最近压力太大了，晚上总是睡不着',
 '2026-05-23 20:00:00');

-- 8. 关键词 + 关注 + 日记
INSERT INTO crisis_alert (user_id, alert_level, handle_status, trigger_reason, create_time) VALUES
(18, 2, 'PENDING',
 '日记触发预警 | 关键词:没有意义(2级) | AI分析:AI识别为需关注(2级) | 情绪分:5.5(0级) | 最终:2级 | 原文:每天都重复一样的事情，感觉没有意义',
 '2026-05-22 18:00:00');

SET FOREIGN_KEY_CHECKS = 1;
