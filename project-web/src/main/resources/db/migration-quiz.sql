-- ============================================
-- MBTI 变体测试：建表 + 初始数据（幂等，可重复执行）
-- ============================================

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

-- 仅当表为空时写入初始题目，避免每次启动重复插入
INSERT INTO `quiz_question` (`dimension`, `question_text`, `options`, `sort_order`, `is_enabled`)
SELECT `t`.`dimension`, `t`.`question_text`, `t`.`options`, `t`.`sort_order`, 1 FROM (
    SELECT 'E' AS `dimension`, '你走进一个派对的那一刻，更接近哪种状态？' AS `question_text`,
        '[{"text":"很快融入人群，跟半屋子的人都能聊上","scores":{"E":2}},{"text":"找个熟悉的角落，更想一对一深聊","scores":{"I":2}},{"text":"先观察一会儿气氛，再决定待在哪","scores":{"E":1,"I":1}}]' AS `options`, 1 AS `sort_order`
    UNION ALL SELECT 'I', '忙碌一周之后，你靠什么回血？',
        '[{"text":"约朋友吃饭聚会，越热闹越放松","scores":{"E":2}},{"text":"一个人待着，看书打游戏或者发呆","scores":{"I":2}}]', 2
    UNION ALL SELECT 'N', '下面哪件事更让你兴奋？',
        '[{"text":"一个可能改变行业玩法的狂想","scores":{"N":2}},{"text":"一个明天就能上线的实用方案","scores":{"S":2}}]', 3
    UNION ALL SELECT 'S', '学习一个新工具时，你更习惯？',
        '[{"text":"先看整体架构和设计理念","scores":{"N":2}},{"text":"直接跟着教程上手，边做边学","scores":{"S":2}}]', 4
    UNION ALL SELECT 'T', '做重要决定时，你更倾向于？',
        '[{"text":"用数据和逻辑权衡利弊","scores":{"T":2}},{"text":"考虑身边人的感受和团队氛围","scores":{"F":2}}]', 5
    UNION ALL SELECT 'F', '朋友来找你倒苦水，你的第一反应是？',
        '[{"text":"帮TA分析问题出在哪，给出建议","scores":{"T":2}},{"text":"先共情安慰，别的不着急","scores":{"F":2}}]', 6
    UNION ALL SELECT 'J', '你的待办清单通常是？',
        '[{"text":"列得清清楚楚，划掉一项爽一项","scores":{"J":2}},{"text":"大概在心里，随机应变才是精髓","scores":{"P":2}}]', 7
    UNION ALL SELECT 'P', '一场说走就走的旅行，你会？',
        '[{"text":"查攻略订酒店，行程排得明明白白","scores":{"J":2}},{"text":"先买票，到了再说","scores":{"P":2}}]', 8
    UNION ALL SELECT 'N', '哪句话更能打动你？',
        '[{"text":"「我们在做一件没人做过的事」","scores":{"N":2,"J":1}},{"text":"「这事现在就能用，稳定可靠」","scores":{"S":2,"T":1}}]', 9
    UNION ALL SELECT 'T', '项目做到一半需求突然变了，你？',
        '[{"text":"快速评估影响，调整计划继续推进","scores":{"T":1,"J":1}},{"text":"嘴上骂骂咧咧，身体却很诚实接着干","scores":{"F":1,"P":1}}]', 10
) AS `t`
WHERE NOT EXISTS (SELECT 1 FROM `quiz_question`);

INSERT INTO `quiz_result_type` (`code`, `name`, `description`, `sort_order`)
SELECT `t`.`code`, `t`.`name`, `t`.`description`, `t`.`sort_order` FROM (
    SELECT 'NT' AS `code`, '策略家' AS `name`, '你擅长抽象思维与全局规划，热衷探索事物背后的本质规律。好奇心和挑战欲是你的引擎，做决定时既看得远又讲逻辑，是天生的方向感持有者。' AS `description`, 1 AS `sort_order`
    UNION ALL SELECT 'NF', '理想家', '你在意人的成长与事情的意义，共情力强。决策时价值观优先，擅长鼓舞他人、在非黑即白之外找到第三种可能。世界因为你多了一点温度。', 2
    UNION ALL SELECT 'ST', '实干家', '你重视事实与经验，讲究用数据和结果说话。执行稳、效率高，最擅长把想法落成看得见摸得着的交付物，是团队里最靠谱的那类人。', 3
    UNION ALL SELECT 'SF', '支持者', '你对他人的需求敏锐，做事踏实又有温度。喜欢具体而及时的帮助，不画大饼只解决问题，是团队默契与信任的黏合剂。', 4
    UNION ALL SELECT 'SP', '行动派', '你对当下环境反应极快，动手往往快过犹豫。热爱自由与即兴，哪里刺激去哪里，未知领域里第一个冲锋的总是你。', 5
    UNION ALL SELECT 'SJ', '守卫者', '规则与可靠是你的底色，答应的事一定办到。擅长沉淀流程、维护秩序，把风险提前摁死在摇篮里，是团队安全感的来源。', 6
    UNION ALL SELECT 'NP', '探索者', '你点子一个接一个，总能在变化里发现新可能。比起收尾你更享受开局，世界那么大，什么都想试试看。', 7
    UNION ALL SELECT 'NJ', '指挥官', '直觉加执行，你一边画蓝图一边推进落地。目标感极强，愿意用当下的确定性换取长期的超额回报，是能带队伍打胜仗的人。', 8
) AS `t`
WHERE NOT EXISTS (SELECT 1 FROM `quiz_result_type`);
