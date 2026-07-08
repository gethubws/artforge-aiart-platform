package com.aiart.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("artwork_tag")
public class ArtworkTag {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long artworkId;
    private Long tagId;
    private Long userId;
    private LocalDateTime createdAt;
}
