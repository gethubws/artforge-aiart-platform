package com.aiart.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("style_package_submission")
public class StylePackageSubmission {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long stylePackageId;
    private Long submitterId;
    private Long artworkId;
    private String note;
    private String status;
    private String reviewComment;
    private LocalDateTime createdAt;
    private LocalDateTime reviewedAt;
}
