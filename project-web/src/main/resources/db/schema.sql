-- AM Portal 数据库初始化脚本

CREATE DATABASE IF NOT EXISTS `am_portal` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `am_portal`;

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码(BCrypt)',
    `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `avatar` VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
    `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态: PENDING/ENABLED/DISABLED',
    `is_admin` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否管理员',
    `theme` VARCHAR(20) DEFAULT NULL COMMENT '界面主题偏好(NULL=跟随站点默认)',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` INT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 项目表
CREATE TABLE IF NOT EXISTS `project` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(100) NOT NULL COMMENT '项目名称',
    `logo` VARCHAR(500) DEFAULT NULL COMMENT '项目Logo',
    `cover` VARCHAR(500) DEFAULT NULL COMMENT '项目封面',
    `summary` VARCHAR(500) DEFAULT NULL COMMENT '项目简介',
    `markdown_content` LONGTEXT DEFAULT NULL COMMENT 'Markdown介绍',
    `html_content` LONGTEXT DEFAULT NULL COMMENT 'HTML内容',
    `screenshots` TEXT DEFAULT NULL COMMENT '项目截图(JSON数组)',
    `tags` VARCHAR(500) DEFAULT NULL COMMENT '项目标签(逗号分隔)',
    `github_url` VARCHAR(500) DEFAULT NULL COMMENT 'Github链接',
    `gitee_url` VARCHAR(500) DEFAULT NULL COMMENT 'Gitee链接',
    `demo_url` VARCHAR(500) DEFAULT NULL COMMENT '在线体验地址',
    `download_url` VARCHAR(500) DEFAULT NULL COMMENT '下载地址',
    `is_top` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否置顶',
    `is_visible` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否显示',
    `publish_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `deleted` INT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_visible_top` (`is_visible`, `is_top`, `publish_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='项目表';

-- 博客表
CREATE TABLE IF NOT EXISTS `blog` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `title` VARCHAR(200) NOT NULL COMMENT '标题',
    `summary` VARCHAR(500) DEFAULT NULL COMMENT '摘要',
    `cover` VARCHAR(500) DEFAULT NULL COMMENT '封面',
    `category_id` BIGINT DEFAULT NULL COMMENT '分类ID',
    `markdown_content` LONGTEXT DEFAULT NULL COMMENT 'Markdown正文',
    `html_content` LONGTEXT DEFAULT NULL COMMENT 'HTML正文',
    `view_count` INT NOT NULL DEFAULT 0 COMMENT '阅读量',
    `is_published` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否发布',
    `publish_time` DATETIME DEFAULT NULL COMMENT '发布时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` INT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_category` (`category_id`),
    KEY `idx_published` (`is_published`, `publish_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='博客表';

-- 博客分类表
CREATE TABLE IF NOT EXISTS `blog_category` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
    `description` VARCHAR(200) DEFAULT NULL COMMENT '分类描述',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `deleted` INT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='博客分类表';

-- 博客标签表
CREATE TABLE IF NOT EXISTS `blog_tag` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(50) NOT NULL COMMENT '标签名称',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `deleted` INT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='博客标签表';

-- 博客标签关联表
CREATE TABLE IF NOT EXISTS `blog_tag_relation` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `blog_id` BIGINT NOT NULL COMMENT '博客ID',
    `tag_id` BIGINT NOT NULL COMMENT '标签ID',
    PRIMARY KEY (`id`),
    KEY `idx_blog_id` (`blog_id`),
    KEY `idx_tag_id` (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='博客标签关联表';

-- Prompt表
CREATE TABLE IF NOT EXISTS `prompt` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(100) NOT NULL COMMENT '名称',
    `content` TEXT NOT NULL COMMENT '内容',
    `is_default` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否默认',
    `is_enabled` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` INT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Prompt表';

-- 系统配置表
CREATE TABLE IF NOT EXISTS `sys_config` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `config_key` VARCHAR(100) NOT NULL COMMENT '配置键',
    `config_value` TEXT DEFAULT NULL COMMENT '配置值',
    `description` VARCHAR(200) DEFAULT NULL COMMENT '描述',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';

-- 每日小贴士表
CREATE TABLE IF NOT EXISTS `daily_tip` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `content` VARCHAR(500) NOT NULL COMMENT '贴士内容',
    `is_enabled` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `deleted` INT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_enabled` (`is_enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='每日小贴士表';

-- 文件信息表
CREATE TABLE IF NOT EXISTS `file_info` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `original_name` VARCHAR(255) NOT NULL COMMENT '原始文件名',
    `stored_name` VARCHAR(255) NOT NULL COMMENT '存储文件名',
    `url` VARCHAR(500) NOT NULL COMMENT '访问URL',
    `file_size` BIGINT DEFAULT NULL COMMENT '文件大小(字节)',
    `file_type` VARCHAR(50) DEFAULT NULL COMMENT '文件类型',
    `directory` VARCHAR(100) DEFAULT NULL COMMENT '所属目录',
    `upload_user_id` BIGINT DEFAULT NULL COMMENT '上传用户ID',
    `upload_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    PRIMARY KEY (`id`),
    KEY `idx_directory` (`directory`),
    KEY `idx_upload_time` (`upload_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件信息表';

-- AI对话表
CREATE TABLE IF NOT EXISTS `ai_conversation` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `title` VARCHAR(200) DEFAULT NULL COMMENT '对话标题',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` INT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI对话表';

-- AI消息表
CREATE TABLE IF NOT EXISTS `ai_message` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `conversation_id` BIGINT NOT NULL COMMENT '对话ID',
    `role` VARCHAR(20) NOT NULL COMMENT '角色: user/assistant/system',
    `content` LONGTEXT NOT NULL COMMENT '消息内容',
    `tokens` INT DEFAULT NULL COMMENT 'Token数',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_conversation_id` (`conversation_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI消息表';

-- 操作日志表
CREATE TABLE IF NOT EXISTS `operation_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT DEFAULT NULL COMMENT '操作用户ID',
    `username` VARCHAR(50) DEFAULT NULL COMMENT '操作用户名',
    `operation_type` VARCHAR(50) NOT NULL COMMENT '操作类型',
    `operation_content` TEXT DEFAULT NULL COMMENT '操作内容',
    `result` VARCHAR(20) DEFAULT NULL COMMENT '操作结果',
    `ip` VARCHAR(50) DEFAULT NULL COMMENT 'IP地址',
    `duration` BIGINT DEFAULT NULL COMMENT '耗时(ms)',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_type` (`operation_type`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';

-- 初始化管理员账号 (密码: Chaos33550336%0)
INSERT INTO `user` (`username`, `password`, `nickname`, `status`, `is_admin`) VALUES
('admin', '$2b$12$dnyRzKQUm4wKCeDyWQ6/5el9VDArgfVfQdpDqIZoa4J2WyDXN/0w6', '管理员', 'ENABLED', 1);

-- 初始化系统配置
INSERT INTO `sys_config` (`config_key`, `config_value`, `description`) VALUES
('site_title', 'AM Portal', '网站标题'),
('site_subtitle', '个人展示平台', '网站副标题'),
('site_description', '一个集项目展示、个人博客、AI助手于一体的个人网站', '个人简介'),
('site_avatar', '', '头像URL'),
('site_github', '', 'Github地址'),
('site_gitee', '', 'Gitee地址'),
('site_email', '', '联系邮箱'),
('site_qq', '', 'QQ号'),
('site_wechat_qrcode', '', '微信二维码URL'),
('site_tech_stack', 'Java,Spring Boot,Vue3,MySQL,Redis', '技术栈'),
('site_theme', 'teal', '站点默认界面主题(teal/blue/violet/rose/amber/slate)'),
('ai_enabled', 'true', 'AI助手是否开放给游客');

-- 初始化默认Prompt
INSERT INTO `prompt` (`name`, `content`, `is_default`, `is_enabled`) VALUES
('默认助手', '你是一名Java架构师，擅长Spring Boot、微服务、数据库设计等技术领域。请用专业但友好的语气回答问题。', 1, 1);

-- MBTI测试题目表
CREATE TABLE IF NOT EXISTS `quiz_question` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `dimension` VARCHAR(10) DEFAULT NULL COMMENT '主维度字母(E/I/N/S/T/F/J/P)，用于后台分组展示',
    `question_text` VARCHAR(500) NOT NULL COMMENT '题干',
    `options` JSON NOT NULL COMMENT '选项数组 [{"text":"...","scores":{"N":1,"T":2}}]',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序',
    `is_enabled` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `deleted` INT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_enabled_sort` (`is_enabled`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='MBTI测试题目表';

-- MBTI测试结果类型表
CREATE TABLE IF NOT EXISTS `quiz_result_type` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `code` VARCHAR(10) NOT NULL COMMENT '结果代号，两字母组合如 NT/NF/ST/SF/SP/SJ/NP/NJ',
    `name` VARCHAR(50) NOT NULL COMMENT '类型命名',
    `description` VARCHAR(1000) DEFAULT NULL COMMENT '类型描述(约100字)',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `deleted` INT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='MBTI测试结果类型表';

-- 初始化MBTI题目与结果类型
INSERT INTO `quiz_question` (`dimension`, `question_text`, `options`, `sort_order`, `is_enabled`) VALUES
('E', '你走进一个派对的那一刻，更接近哪种状态？', '[{"text":"很快融入人群，跟半屋子的人都能聊上","scores":{"E":2}},{"text":"找个熟悉的角落，更想一对一深聊","scores":{"I":2}},{"text":"先观察一会儿气氛，再决定待在哪","scores":{"E":1,"I":1}}]', 1, 1),
('I', '忙碌一周之后，你靠什么回血？', '[{"text":"约朋友吃饭聚会，越热闹越放松","scores":{"E":2}},{"text":"一个人待着，看书打游戏或者发呆","scores":{"I":2}}]', 2, 1),
('N', '下面哪件事更让你兴奋？', '[{"text":"一个可能改变行业玩法的狂想","scores":{"N":2}},{"text":"一个明天就能上线的实用方案","scores":{"S":2}}]', 3, 1),
('S', '学习一个新工具时，你更习惯？', '[{"text":"先看整体架构和设计理念","scores":{"N":2}},{"text":"直接跟着教程上手，边做边学","scores":{"S":2}}]', 4, 1),
('T', '做重要决定时，你更倾向于？', '[{"text":"用数据和逻辑权衡利弊","scores":{"T":2}},{"text":"考虑身边人的感受和团队氛围","scores":{"F":2}}]', 5, 1),
('F', '朋友来找你倒苦水，你的第一反应是？', '[{"text":"帮TA分析问题出在哪，给出建议","scores":{"T":2}},{"text":"先共情安慰，别的不着急","scores":{"F":2}}]', 6, 1),
('J', '你的待办清单通常是？', '[{"text":"列得清清楚楚，划掉一项爽一项","scores":{"J":2}},{"text":"大概在心里，随机应变才是精髓","scores":{"P":2}}]', 7, 1),
('P', '一场说走就走的旅行，你会？', '[{"text":"查攻略订酒店，行程排得明明白白","scores":{"J":2}},{"text":"先买票，到了再说","scores":{"P":2}}]', 8, 1),
('N', '哪句话更能打动你？', '[{"text":"「我们在做一件没人做过的事」","scores":{"N":2,"J":1}},{"text":"「这事现在就能用，稳定可靠」","scores":{"S":2,"T":1}}]', 9, 1),
('T', '项目做到一半需求突然变了，你？', '[{"text":"快速评估影响，调整计划继续推进","scores":{"T":1,"J":1}},{"text":"嘴上骂骂咧咧，身体却很诚实接着干","scores":{"F":1,"P":1}}]', 10, 1);

INSERT INTO `quiz_result_type` (`code`, `name`, `description`, `sort_order`) VALUES
('NT', '策略家', '你擅长抽象思维与全局规划，热衷探索事物背后的本质规律。好奇心和挑战欲是你的引擎，做决定时既看得远又讲逻辑，是天生的方向感持有者。', 1),
('NF', '理想家', '你在意人的成长与事情的意义，共情力强。决策时价值观优先，擅长鼓舞他人、在非黑即白之外找到第三种可能。世界因为你多了一点温度。', 2),
('ST', '实干家', '你重视事实与经验，讲究用数据和结果说话。执行稳、效率高，最擅长把想法落成看得见摸得着的交付物，是团队里最靠谱的那类人。', 3),
('SF', '支持者', '你对他人的需求敏锐，做事踏实又有温度。喜欢具体而及时的帮助，不画大饼只解决问题，是团队默契与信任的黏合剂。', 4),
('SP', '行动派', '你对当下环境反应极快，动手往往快过犹豫。热爱自由与即兴，哪里刺激去哪里，未知领域里第一个冲锋的总是你。', 5),
('SJ', '守卫者', '规则与可靠是你的底色，答应的事一定办到。擅长沉淀流程、维护秩序，把风险提前摁死在摇篮里，是团队安全感的来源。', 6),
('NP', '探索者', '你点子一个接一个，总能在变化里发现新可能。比起收尾你更享受开局，世界那么大，什么都想试试看。', 7),
('NJ', '指挥官', '直觉加执行，你一边画蓝图一边推进落地。目标感极强，愿意用当下的确定性换取长期的超额回报，是能带队伍打胜仗的人。', 8);
