package com.aiart.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("artwork")
public class Artwork {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long userId;
    private String title;
    private String promptText;
    private String negativePrompt;
    private String imageUrl;
    private String thumbnailUrl;
    @TableField("generation_params_json")
    private String generationParamsJson;
    private Long sourceJobId;
    private String visibility;
    private String status;
    private LocalDateTime createdAt;
}
