package com.am.blog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("blog")
public class Blog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String summary;

    private String cover;

    private Long categoryId;

    private String markdownContent;

    private String htmlContent;

    private Integer viewCount;

    private Boolean isPublished;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime publishTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
