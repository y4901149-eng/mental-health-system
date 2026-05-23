-- User profile completion migration.
-- Run this after the original schema when avatar uploads are enabled.

ALTER TABLE `user`
  MODIFY COLUMN `avatar` LONGTEXT COMMENT '头像URL或base64图片数据';
