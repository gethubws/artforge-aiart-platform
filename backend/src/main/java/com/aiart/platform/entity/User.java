package com.aiart.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("users")
public class User {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String username;
    private String passwordHash;
    private String displayName;
    private String avatarUrl;
    private String role;
    private String status;
    private BigDecimal pointBalance;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
