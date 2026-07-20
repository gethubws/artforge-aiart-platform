package com.aiart.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tag_preview")
public class TagPreview {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long tagId;
    private String imageUrl;
    private String previewType;
    private String sceneKey;
    private String titleZh;
    private String promptSnapshot;
    private Integer sortOrder;
    private Boolean isCover;
    private LocalDateTime createdAt;
}
