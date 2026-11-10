package com.am.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * MBTI 测试结果类型
 */
@Data
@TableName("quiz_result_type")
public class QuizResultType {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 结果代号，两字母组合如 NT/NF/ST/SF/SP/SJ/NP/NJ */
    private String code;

    /** 类型命名 */
    private String name;

    /** 类型描述(约100字) */
    private String description;

    /** 排序 */
    private Integer sortOrder;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}
