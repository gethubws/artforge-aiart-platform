package com.aiart.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tag_combination_stat")
public class TagCombinationStat {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long tagAId;
    private Long tagBId;
    private Long usageCount;
    private LocalDateTime lastUsedAt;
}
