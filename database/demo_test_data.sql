-- =====================================================
-- 课设演示测试数据
-- 新增 5 个普通用户 + 配套 mood_record 数据
-- 密码统一: 123456 (SHA256)
-- =====================================================

-- SHA256('123456') = 8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92

-- 1. 张晓 - 正常用户，有情绪记录和预约
INSERT INTO `user` (id, username, password, nickname, phone, email, role, gender, age, occupation, bio, deleted, status, create_time)
VALUES (35, 'zhangxiao', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', '张晓', '13800001021', 'zhangxiao@test.com', 'user', 2, 21, '英语专业学生', '热爱生活，记录每一天', 0, 1, NOW());

-- 2. 李强 - 高风险用户，有 crisis_alert 记录
INSERT INTO `user` (id, username, password, nickname, phone, email, role, gender, age, occupation, bio, deleted, status, create_time)
VALUES (36, 'liqiang', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', '李强', '13800001022', 'liqiang@test.com', 'user', 1, 23, '计算机专业学生', '', 0, 1, NOW());

-- 3. 王芳 - 正常用户，有预约记录
INSERT INTO `user` (id, username, password, nickname, phone, email, role, gender, age, occupation, bio, deleted, status, create_time)
VALUES (37, 'wangfang', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', '王芳', '13800001023', 'wangfang@test.com', 'user', 2, 20, '心理学专业学生', '想成为一名心理咨询师', 0, 1, NOW());

-- 4. 陈明 - 正常用户，有情绪记录
INSERT INTO `user` (id, username, password, nickname, phone, email, role, gender, age, occupation, bio, deleted, status, create_time)
VALUES (38, 'chenming', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', '陈明', '13800001024', 'chenming@test.com', 'user', 1, 22, '数学专业学生', '喜欢思考', 0, 1, NOW());

-- 5. 胡薇 - 正常用户，情绪记录+预约
INSERT INTO `user` (id, username, password, nickname, phone, email, role, gender, age, occupation, bio, deleted, status, create_time)
VALUES (39, 'huwei', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', '胡薇', '13800001025', 'huwei@test.com', 'user', 2, 21, '艺术设计专业学生', '用画笔表达情绪', 0, 1, NOW());

-- =====================================================
-- mood_record 测试数据（用于 Dashboard 折线图展示）
-- 每个新用户最近 7 天每天 1-2 条记录
-- =====================================================
INSERT INTO mood_record (user_id, mood_score, mood_tag, note, record_date, create_time) VALUES
-- 张晓 (id=35): 情绪平稳向好
(35, 7, 'happy', '今天和朋友出去玩，很开心', '2026-05-24', '2026-05-24 18:30:00'),
(35, 6, 'calm', '普通的一天，状态还行', '2026-05-23', '2026-05-23 20:00:00'),
(35, 5, 'tired', '有点累，早点睡了', '2026-05-22', '2026-05-22 22:00:00'),
(35, 7, 'happy', '收到好消息！', '2026-05-21', '2026-05-21 15:00:00'),
(35, 6, 'calm', '状态平稳', '2026-05-20', '2026-05-20 19:00:00'),
(35, 5, 'tired', '赶作业到很晚', '2026-05-19', '2026-05-19 23:00:00'),
(35, 8, 'happy', '周末愉快', '2026-05-18', '2026-05-18 17:00:00'),
-- 李强 (id=36): 情绪较低，高风险
(36, 4, 'anxious', '最近压力很大，睡不着', '2026-05-24', '2026-05-24 02:00:00'),
(36, 3, 'sad', '感觉很难受', '2026-05-23', '2026-05-23 23:00:00'),
(36, 4, 'anxious', '考试临近，很焦虑', '2026-05-22', '2026-05-22 20:00:00'),
(36, 3, 'sad', '心情很低落', '2026-05-21', '2026-05-21 22:00:00'),
(36, 5, 'tired', '好累', '2026-05-20', '2026-05-20 21:00:00'),
(36, 4, 'anxious', '担心未来', '2026-05-19', '2026-05-19 20:00:00'),
(36, 4, 'sad', '不开心', '2026-05-18', '2026-05-18 19:00:00'),
-- 王芳 (id=37): 情绪良好
(37, 7, 'happy', '今天学习了心理咨询技巧', '2026-05-24', '2026-05-24 16:00:00'),
(37, 8, 'happy', '和朋友聊了很多', '2026-05-23', '2026-05-23 18:00:00'),
(37, 6, 'calm', '平静的一天', '2026-05-22', '2026-05-22 20:00:00'),
(37, 7, 'happy', '被老师表扬了', '2026-05-21', '2026-05-21 14:00:00'),
-- 陈明 (id=38): 情绪稳定
(38, 6, 'calm', '数学题做完了，放松一下', '2026-05-24', '2026-05-24 21:00:00'),
(38, 5, 'tired', '复习了一天', '2026-05-23', '2026-05-23 22:00:00'),
(38, 6, 'calm', '听了音乐，心情不错', '2026-05-22', '2026-05-22 19:00:00'),
(38, 7, 'happy', '搞定了一道难题', '2026-05-21', '2026-05-21 16:00:00'),
-- 胡薇 (id=39): 情绪平稳
(39, 7, 'happy', '画了一幅满意的画', '2026-05-24', '2026-05-24 20:00:00'),
(39, 6, 'calm', '灵感来了', '2026-05-23', '2026-05-23 15:00:00'),
(39, 5, 'tired', '画了一天有点累', '2026-05-22', '2026-05-22 22:00:00'),
(39, 8, 'happy', '作品被老师认可了', '2026-05-21', '2026-05-21 17:00:00');

-- =====================================================
-- crisis_alert 测试数据（李强的高风险预警）
-- =====================================================
INSERT INTO crisis_alert (user_id, alert_level, handle_status, trigger_reason, create_time)
VALUES (36, 4, 'PENDING', '检测到高风险关键词：用户表达了强烈的绝望感和无助感，情绪评分持续低于4分', NOW()),
       (36, 3, 'PENDING', '检测到中度风险关键词：用户多次提到失眠和焦虑情绪', DATE_SUB(NOW(), INTERVAL 1 DAY));

-- =====================================================
-- appointment 测试数据（王芳和胡薇的预约）
-- =====================================================
INSERT INTO appointment (user_id, counselor_name, appointment_date, time_slot, type, remark, status, create_time)
VALUES (37, '李老师', '2026-05-28', '14:00-15:00', 'individual', '想和老师聊聊未来的职业规划', 'confirmed', NOW()),
       (39, '王老师', '2026-05-29', '09:00-10:00', 'individual', '最近有些焦虑，想找人聊聊', 'confirmed', DATE_SUB(NOW(), INTERVAL 1 DAY));
