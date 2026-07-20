package com.aiart.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("style_package_version")
public class StylePackageVersion {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long stylePackageId;
    private Long userId;
    private Integer versionNumber;
    private String name;
    private String description;
    private String coverImageUrl;
    private String styleStatement;
    private String promptGuide;
    private String negativePromptGuide;
    private Long featuredArtworkId;
    private Integer artworkCount;
    private BigDecimal pricePoints;
    private String changeNote;
    private LocalDateTime createdAt;
}
