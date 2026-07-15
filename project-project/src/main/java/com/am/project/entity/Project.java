package com.am.project.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("project")
public class Project {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String logo;

    private String cover;

    private String summary;

    private String markdownContent;

    private String htmlContent;

    private String screenshots;

    private String tags;

    private String githubUrl;

    private String giteeUrl;

    private String demoUrl;

    private String downloadUrl;

    private Boolean isTop;

    private Boolean isVisible;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime publishTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}
