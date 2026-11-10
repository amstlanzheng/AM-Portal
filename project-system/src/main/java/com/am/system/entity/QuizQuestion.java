package com.am.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * MBTI 测试题目
 */
@Data
@TableName("quiz_question")
public class QuizQuestion {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 主维度字母(E/I/N/S/T/F/J/P)，用于后台分组展示 */
    private String dimension;

    /** 题干 */
    private String questionText;

    /** 选项数组 JSON：[{"text":"...","scores":{"N":1,"T":2}}] */
    private String options;

    /** 排序 */
    private Integer sortOrder;

    /** 是否启用 */
    private Boolean isEnabled;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}
