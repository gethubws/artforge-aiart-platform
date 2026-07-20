package com.aiart.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tag_search_log")
public class TagSearchLog {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long categoryId;
    private String keyword;
    private Long resultCount;
    private LocalDateTime createdAt;
}
