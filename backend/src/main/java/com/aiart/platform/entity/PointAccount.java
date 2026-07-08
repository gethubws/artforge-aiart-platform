package com.aiart.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("point_account")
public class PointAccount {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long userId;
    private BigDecimal balance;
    private BigDecimal frozenBalance;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
