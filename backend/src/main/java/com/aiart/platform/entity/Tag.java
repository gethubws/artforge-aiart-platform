package com.aiart.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("tag")
public class Tag {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long categoryId;
    private String name;
    private String promptText;
    private String negativePromptText;
    private String previewImageUrl;
    private BigDecimal weight;
    private Integer usageCount;
    private String visibility;
    private String status;
    private LocalDateTime createdAt;
}
