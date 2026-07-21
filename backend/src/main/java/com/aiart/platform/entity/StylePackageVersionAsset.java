package com.aiart.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("style_package_version_asset")
public class StylePackageVersionAsset {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long stylePackageVersionId;
    private Long stylePackageAssetId;
    private LocalDateTime createdAt;
}
