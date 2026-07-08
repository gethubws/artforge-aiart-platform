package com.aiart.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tag_category")
public class TagCategory {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String name;
    private String slug;
    private Long parentId;
    private Integer sortOrder;
    private String status;
    private LocalDateTime createdAt;
}
