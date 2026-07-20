package com.aiart.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("style_package_collaborator")
public class StylePackageCollaborator {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long stylePackageId;
    private Long userId;
    private String role;
    private LocalDateTime createdAt;
}
