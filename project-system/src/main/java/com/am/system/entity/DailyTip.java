package com.am.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 每日小贴士
 */
@Data
@TableName("daily_tip")
public class DailyTip {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 贴士内容 */
    private String content;

    /** 是否启用 */
    private Boolean isEnabled;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}
