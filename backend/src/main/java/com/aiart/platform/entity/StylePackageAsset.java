package com.aiart.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("style_package_asset")
public class StylePackageAsset {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long stylePackageId;
    private Long contributorId;
    private String logicalKey;
    private Integer revisionNumber;
    private String name;
    private String categoryKey;
    private String assetType;
    private String description;
    private String previewImageUrl;
    private String fileUrl;
    private String thumbnailUrl;
    private String promptText;
    private String negativePromptText;
    private String generationParamsJson;
    private Integer width;
    private Integer height;
    private String fileFormat;
    private String backgroundMode;
    private String licenseScope;
    private String status;
    private Integer sortOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
