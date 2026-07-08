package com.aiart.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("enterprise_task_submission")
public class EnterpriseTaskSubmission {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long taskId;
    private Long submitterId;
    private Long artworkId;
    private String note;
    private String status;
    private BigDecimal rewardPoints;
    private String reviewComment;
    private LocalDateTime createdAt;
    private LocalDateTime reviewedAt;
}
