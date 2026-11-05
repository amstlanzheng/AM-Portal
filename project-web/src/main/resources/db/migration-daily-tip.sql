-- ============================================
-- 每日小贴士：建表 + 初始数据（幂等，可重复执行）
-- ============================================

CREATE TABLE IF NOT EXISTS `daily_tip` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `content` VARCHAR(500) NOT NULL COMMENT '贴士内容',
    `is_enabled` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `deleted` INT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_enabled` (`is_enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='每日小贴士表';

-- 仅当表为空时写入默认贴士，避免每次启动重复插入
INSERT INTO `daily_tip` (`content`, `is_enabled`)
SELECT `t`.`content`, `t`.`is_enabled` FROM (
    SELECT '代码是写给人看的，顺便让机器执行。' AS `content`, 1 AS `is_enabled`
    UNION ALL SELECT '提交之前先看一眼 git diff，能省掉一半返工。', 1
    UNION ALL SELECT '接口先定契约再写实现，联调时间至少减半。', 1
    UNION ALL SELECT '重复三次的代码抽成函数，重复三次的配置抽成变量。', 1
    UNION ALL SELECT '先让它跑起来，再让它跑得对，最后让它跑得快。', 1
) AS `t`
WHERE NOT EXISTS (SELECT 1 FROM `daily_tip`);
