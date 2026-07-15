package com.am.storage.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("file_info")
public class FileInfo {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String originalName;

    private String storedName;

    private String url;

    private Long fileSize;

    private String fileType;

    private String directory;

    private Long uploadUserId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime uploadTime;
}
