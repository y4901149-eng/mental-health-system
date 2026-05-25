-- Patch missing schema used by assessment categories and counselor appointment flow.
-- Run this after importing database/mental_health.sql.

SET @sql := (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE assessment ADD COLUMN category_id INT NULL COMMENT ''Assessment category ID'' AFTER status',
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
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(50) NOT NULL,
  sort_order INT DEFAULT 0,
  enabled TINYINT DEFAULT 1,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS counselor (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(50) NOT NULL,
  specialty VARCHAR(100) DEFAULT NULL,
  intro VARCHAR(500) DEFAULT NULL,
  enabled TINYINT DEFAULT 1,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS counselor_available_slot (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  counselor_id BIGINT NOT NULL,
  week_day INT NOT NULL,
  time_slot VARCHAR(50) NOT NULL,
  enabled TINYINT DEFAULT 1,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_counselor_day_slot (counselor_id, week_day, time_slot)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO assessment_category (id, name, sort_order, enabled)
VALUES
  (1, '情绪压力', 1, 1),
  (2, '人际关系', 2, 1),
  (3, '学业职业', 3, 1),
  (4, '睡眠健康', 4, 1)
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  sort_order = VALUES(sort_order),
  enabled = VALUES(enabled);

UPDATE assessment
SET category_id = 1
WHERE category_id IS NULL;

INSERT INTO counselor (id, name, specialty, intro, enabled)
VALUES
  (1, '王老师', '情绪压力、焦虑疏导', '擅长压力管理、焦虑情绪调节与日常心理支持。', 1),
  (2, '李老师', '人际关系、成长困扰', '关注学生成长、人际沟通与自我认知提升。', 1),
  (3, '张老师', '学业压力、职业规划', '擅长学业压力干预、目标管理与职业发展咨询。', 1)
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  specialty = VALUES(specialty),
  intro = VALUES(intro),
  enabled = VALUES(enabled);

INSERT INTO counselor_available_slot (counselor_id, week_day, time_slot, enabled)
SELECT c.id, d.week_day, t.time_slot, 1
FROM counselor c
JOIN (
  SELECT 1 week_day UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5
) d
JOIN (
  SELECT '09:00-10:00' time_slot UNION ALL
  SELECT '10:30-11:30' UNION ALL
  SELECT '14:00-15:00' UNION ALL
  SELECT '15:30-16:30'
) t
WHERE c.id IN (1, 2, 3)
ON DUPLICATE KEY UPDATE
  enabled = VALUES(enabled);
