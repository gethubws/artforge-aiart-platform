package com.aiart.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("point_transaction")
public class PointTransaction {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long userId;
    private BigDecimal amount;
    private String direction;
    private String reason;
    private String referenceType;
    private Long referenceId;
    private LocalDateTime createdAt;
}
