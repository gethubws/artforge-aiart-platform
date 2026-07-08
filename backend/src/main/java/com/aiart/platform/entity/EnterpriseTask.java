package com.aiart.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("enterprise_task")
public class EnterpriseTask {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long publisherId;
    private String title;
    private String description;
    @TableField("requirements_json")
    private String requirementsJson;
    private BigDecimal budgetPoints;
    private String status;
    private LocalDateTime deadline;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
