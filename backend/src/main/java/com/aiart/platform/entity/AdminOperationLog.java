package com.aiart.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("admin_operation_log")
public class AdminOperationLog {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long operatorId;
    private String action;
    private String targetType;
    private Long targetId;
    private String requestMethod;
    private String requestPath;
    private String summary;
    private String ipAddress;
    private LocalDateTime createdAt;
}
