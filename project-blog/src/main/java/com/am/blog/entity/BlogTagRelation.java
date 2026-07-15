package com.am.blog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("blog_tag_relation")
public class BlogTagRelation {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long blogId;

    private Long tagId;
}
