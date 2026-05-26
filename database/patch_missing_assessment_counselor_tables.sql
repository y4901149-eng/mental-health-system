-- Patch missing schema used by assessment categories and counselor appointment flow.
-- Run this after importing database/mental_health.sql.

USE mental_health;

CREATE TABLE IF NOT EXISTS appointment (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  counselor_name VARCHAR(50) DEFAULT NULL,
  appointment_date VARCHAR(20) DEFAULT NULL,
  time_slot VARCHAR(20) DEFAULT NULL,
  type VARCHAR(20) DEFAULT 'individual',
  remark VARCHAR(500) DEFAULT NULL,
  status VARCHAR(20) DEFAULT 'pending',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_user_id (user_id),
  KEY idx_appointment_date (appointment_date),
  KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SET @sql := (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE assessment ADD COLUMN category_id INT NULL COMMENT ''关联 assessment_category.id'' AFTER status',
    'SELECT ''assessment.category_id already exists'''
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'assessment'
    AND COLUMN_NAME = 'category_id'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

CREATE TABLE IF NOT EXISTS assessment_category (
  id BIGINT NOT NULL AUTO_INCREMENT,
  name VARCHAR(50) NOT NULL,
  sort_order INT DEFAULT 0,
  enabled TINYINT DEFAULT 1,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS counselor (
  id BIGINT NOT NULL AUTO_INCREMENT,
  name VARCHAR(50) NOT NULL,
  specialty VARCHAR(200) DEFAULT '',
  intro VARCHAR(500) DEFAULT '',
  enabled TINYINT DEFAULT 1,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS counselor_available_slot (
  id BIGINT NOT NULL AUTO_INCREMENT,
  counselor_id BIGINT NOT NULL,
  week_day TINYINT NOT NULL DEFAULT 0,
  time_slot VARCHAR(20) NOT NULL,
  enabled TINYINT DEFAULT 1,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_counselor_day_slot (counselor_id, week_day, time_slot),
  KEY idx_counselor_id (counselor_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO assessment_category (id, name, sort_order, enabled)
VALUES
  (1, '情绪状态', 1, 1),
  (2, '学习压力', 2, 1),
  (3, '睡眠健康', 3, 1),
  (4, '人际关系', 4, 1),
  (5, '自我认知', 5, 1)
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  sort_order = VALUES(sort_order),
  enabled = VALUES(enabled);

UPDATE assessment
SET category_id = CASE
  WHEN type IN ('stress', 'exam_anxiety', 'learning_motivation') THEN 2
  WHEN type IN ('sleep', 'daytime_fatigue') THEN 3
  WHEN type IN ('relationship', 'support', 'social') THEN 4
  WHEN type IN ('self_esteem', 'resilience') THEN 5
  ELSE 1
END
WHERE category_id IS NULL;

INSERT INTO counselor (id, name, specialty, intro, enabled)
VALUES
  (1, '张一凡', '焦虑管理', '擅长处理学业焦虑、考试压力和社交焦虑问题。', 1),
  (2, '徐艳艳', '情绪调节', '擅长情绪管理、抑郁倾向疏导和人际关系咨询。', 1),
  (3, '丁宁', '压力管理', '擅长压力应对、时间管理和生涯规划咨询。', 1),
  (5, '李雪梅', '情感咨询', '关注情感困扰、亲密关系与日常心理支持。', 1)
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  specialty = VALUES(specialty),
  intro = VALUES(intro),
  enabled = VALUES(enabled);

INSERT INTO counselor_available_slot (counselor_id, week_day, time_slot, enabled)
VALUES
  (1, 1, '09:00-10:00', 1), (1, 1, '10:00-11:00', 1),
  (1, 2, '09:00-10:00', 1), (1, 2, '10:00-11:00', 1),
  (1, 3, '09:00-10:00', 1), (1, 3, '10:00-11:00', 1),
  (1, 4, '09:00-10:00', 1), (1, 4, '10:00-11:00', 1),
  (1, 5, '09:00-10:00', 1), (1, 5, '10:00-11:00', 1),
  (1, 6, '14:00-15:00', 1), (1, 7, '14:00-15:00', 1),
  (2, 1, '14:00-15:00', 1), (2, 1, '15:00-16:00', 1),
  (2, 3, '14:00-15:00', 1), (2, 3, '15:00-16:00', 1),
  (2, 5, '14:00-15:00', 1), (2, 5, '15:00-16:00', 1),
  (3, 2, '09:00-10:00', 1), (3, 2, '10:00-11:00', 1),
  (3, 2, '14:00-15:00', 1), (3, 2, '15:00-16:00', 1),
  (3, 4, '09:00-10:00', 1), (3, 4, '10:00-11:00', 1),
  (3, 4, '14:00-15:00', 1), (3, 4, '15:00-16:00', 1),
  (5, 1, '14:00-15:00', 1), (5, 2, '14:00-15:00', 1),
  (5, 3, '14:00-15:00', 1), (5, 4, '14:00-15:00', 1),
  (5, 5, '14:00-15:00', 1), (5, 6, '14:00-15:00', 1),
  (5, 7, '14:00-15:00', 1)
ON DUPLICATE KEY UPDATE
  enabled = VALUES(enabled);
