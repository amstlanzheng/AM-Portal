-- 敏感词系统：建表 + 博客审核字段

CREATE TABLE IF NOT EXISTS `sensitive_word` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `word` VARCHAR(100) NOT NULL COMMENT '敏感词',
    `category` VARCHAR(50) DEFAULT NULL COMMENT '分类',
    `is_enabled` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` INT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_word` (`word`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='敏感词表';

-- 博客表增加审核字段（如果不存在）
SET @dbname = DATABASE();
SET @tablename = 'blog';
SET @columnname1 = 'review_status';
SET @columnname2 = 'review_remark';
SET @preparedStatement1 = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = @columnname1) > 0,
  'SELECT 1',
  'ALTER TABLE blog ADD COLUMN review_status VARCHAR(20) NOT NULL DEFAULT ''PENDING'' COMMENT ''审核状态: PENDING/APPROVED/REJECTED'' AFTER is_published'
));
PREPARE alterIfNotExists1 FROM @preparedStatement1;
EXECUTE alterIfNotExists1;
DEALLOCATE PREPARE alterIfNotExists1;

SET @preparedStatement2 = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = @columnname2) > 0,
  'SELECT 1',
  'ALTER TABLE blog ADD COLUMN review_remark VARCHAR(500) DEFAULT NULL COMMENT ''审核备注'' AFTER review_status'
));
PREPARE alterIfNotExists2 FROM @preparedStatement2;
EXECUTE alterIfNotExists2;
DEALLOCATE PREPARE alterIfNotExists2;

-- 初始化默认敏感词
INSERT IGNORE INTO `sensitive_word` (`word`, `category`) VALUES
('赌博', '违法'), ('色情', '违法'), ('暴力', '违法'), ('毒品', '违法'),
('诈骗', '违法'), ('传销', '违法'), ('枪支', '违法'), ('代开发票', '违法');
