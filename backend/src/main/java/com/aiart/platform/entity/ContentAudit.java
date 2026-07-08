package com.aiart.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("content_audit")
public class ContentAudit {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String contentType;
    private Long contentId;
    private Long submitterId;
    private Long auditorId;
    private String status;
    private String commentText;
    private LocalDateTime createdAt;
    private LocalDateTime auditedAt;
}
