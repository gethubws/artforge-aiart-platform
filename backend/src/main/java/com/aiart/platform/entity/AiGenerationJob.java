package com.aiart.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ai_generation_job")
public class AiGenerationJob {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long userId;
    private String provider;
    private String promptText;
    private String negativePrompt;
    @TableField("params_json")
    private String paramsJson;
    private String status;
    private String imageUrl;
    private String errorMessage;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
}
