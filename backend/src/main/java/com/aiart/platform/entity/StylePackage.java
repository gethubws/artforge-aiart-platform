package com.aiart.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("style_package")
public class StylePackage {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long userId;
    private String name;
    private String description;
    private String coverImageUrl;
    private String promptTemplate;
    private String negativePromptTemplate;
    private BigDecimal pricePoints;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
