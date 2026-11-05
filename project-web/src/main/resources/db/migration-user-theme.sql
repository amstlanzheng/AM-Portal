-- ============================================
-- 用户主题偏好：user 表加列 + 站点默认主题配置键（幂等，可重复执行）
-- ============================================

-- user 表加 theme 列（MySQL 无 ADD COLUMN IF NOT EXISTS，用 information_schema 判断）
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'user' AND COLUMN_NAME = 'theme');
SET @ddl = IF(@col_exists = 0,
    'ALTER TABLE `user` ADD COLUMN `theme` VARCHAR(20) NULL COMMENT ''界面主题偏好(NULL=跟随站点默认)'' AFTER `is_admin`',
    'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 站点默认主题配置键（仅当不存在时插入）
INSERT INTO `sys_config` (`config_key`, `config_value`, `description`)
SELECT 'site_theme', 'teal', '站点默认界面主题(teal/blue/violet/rose/amber/slate)'
WHERE NOT EXISTS (SELECT 1 FROM `sys_config` WHERE `config_key` = 'site_theme');
