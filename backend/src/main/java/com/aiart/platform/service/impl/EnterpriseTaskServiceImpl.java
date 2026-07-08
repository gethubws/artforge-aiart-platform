package com.aiart.platform.service.impl;

import com.aiart.platform.dto.TaskDtos;
import com.aiart.platform.entity.Artwork;
import com.aiart.platform.entity.EnterpriseTask;
import com.aiart.platform.entity.EnterpriseTaskSubmission;
import com.aiart.platform.exception.BusinessException;
import com.aiart.platform.exception.ErrorCode;
import com.aiart.platform.mapper.ArtworkMapper;
import com.aiart.platform.mapper.EnterpriseTaskMapper;
import com.aiart.platform.mapper.EnterpriseTaskSubmissionMapper;
import com.aiart.platform.service.EnterpriseTaskService;
import com.aiart.platform.service.PointService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EnterpriseTaskServiceImpl implements EnterpriseTaskService {
    private final EnterpriseTaskMapper taskMapper;
    private final EnterpriseTaskSubmissionMapper submissionMapper;
    private final ArtworkMapper artworkMapper;
    private final PointService pointService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public TaskDtos.TaskCard create(Long userId, TaskDtos.SaveRequest request) {
        EnterpriseTask task = new EnterpriseTask();
        task.setPublisherId(userId);
        apply(task, request);
        task.setStatus("DRAFT");
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        taskMapper.insert(task);
        return card(task);
    }

    @Override
    @Transactional
    public TaskDtos.TaskCard update(Long userId, Long taskId, TaskDtos.SaveRequest request) {
        EnterpriseTask task = requireOwned(userId, taskId);
        if ("CLOSED".equals(task.getStatus())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Closed task cannot be edited");
        }
        apply(task, request);
        task.setUpdatedAt(LocalDateTime.now());
        taskMapper.updateById(task);
        return card(task);
    }

    @Override
    @Transactional
    public TaskDtos.TaskCard publish(Long userId, Long taskId) {
        EnterpriseTask task = requireOwned(userId, taskId);
        task.setStatus("PUBLISHED");
        task.setUpdatedAt(LocalDateTime.now());
        taskMapper.updateById(task);
        return card(task);
    }

    @Override
    @Transactional
    public TaskDtos.TaskCard close(Long userId, Long taskId) {
        EnterpriseTask task = requireOwned(userId, taskId);
        task.setStatus("CLOSED");
        task.setUpdatedAt(LocalDateTime.now());
        taskMapper.updateById(task);
        return card(task);
    }

    @Override
    public List<TaskDtos.TaskCard> market(int page, int size, String status) {
        String targetStatus = StringUtils.hasText(status) ? status.trim().toUpperCase() : "PUBLISHED";
        if (!List.of("PUBLISHED", "CLOSED").contains(targetStatus)) {
            targetStatus = "PUBLISHED";
        }
        Page<EnterpriseTask> result = taskMapper.selectPage(page(page, size), Wrappers.<EnterpriseTask>lambdaQuery()
                .eq(EnterpriseTask::getStatus, targetStatus)
                .orderByDesc(EnterpriseTask::getUpdatedAt));
        return result.getRecords().stream().map(this::card).toList();
    }

    @Override
    public List<TaskDtos.TaskCard> myTasks(Long userId, int page, int size, String status) {
        var query = Wrappers.<EnterpriseTask>lambdaQuery().eq(EnterpriseTask::getPublisherId, userId);
        if (StringUtils.hasText(status)) {
            String targetStatus = status.trim().toUpperCase();
            if (List.of("DRAFT", "PUBLISHED", "CLOSED").contains(targetStatus)) {
                query.eq(EnterpriseTask::getStatus, targetStatus);
            }
        }
        Page<EnterpriseTask> result = taskMapper.selectPage(page(page, size), query.orderByDesc(EnterpriseTask::getUpdatedAt));
        return result.getRecords().stream().map(this::card).toList();
    }

    @Override
    @Transactional
    public TaskDtos.SubmissionView submit(Long userId, Long taskId, TaskDtos.SubmitRequest request) {
        EnterpriseTask task = taskMapper.selectById(taskId);
        if (task == null || !"PUBLISHED".equals(task.getStatus())) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Task not found");
        }
        if (task.getPublisherId().equals(userId)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "发布者不能提交自己的任务");
        }
        Artwork artwork = artworkMapper.selectOne(Wrappers.<Artwork>lambdaQuery()
                .eq(Artwork::getId, request.artworkId())
                .eq(Artwork::getUserId, userId)
                .eq(Artwork::getStatus, "ACTIVE"));
        if (artwork == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Artwork not found");
        }
        EnterpriseTaskSubmission submission = new EnterpriseTaskSubmission();
        submission.setTaskId(taskId);
        submission.setSubmitterId(userId);
        submission.setArtworkId(artwork.getId());
        submission.setNote(trimToNull(request.note()));
        submission.setStatus("PENDING");
        submission.setRewardPoints(task.getBudgetPoints() == null ? BigDecimal.ZERO : task.getBudgetPoints());
        submission.setCreatedAt(LocalDateTime.now());
        submissionMapper.insert(submission);
        return submissionView(submission);
    }

    @Override
    public List<TaskDtos.SubmissionView> taskSubmissions(Long userId, Long taskId, int page, int size) {
        requireOwned(userId, taskId);
        Page<EnterpriseTaskSubmission> result = submissionMapper.selectPage(page(page, size), Wrappers.<EnterpriseTaskSubmission>lambdaQuery()
                .eq(EnterpriseTaskSubmission::getTaskId, taskId)
                .orderByDesc(EnterpriseTaskSubmission::getCreatedAt));
        return result.getRecords().stream().map(this::submissionView).toList();
    }

    @Override
    public List<TaskDtos.SubmissionView> mySubmissions(Long userId, int page, int size) {
        Page<EnterpriseTaskSubmission> result = submissionMapper.selectPage(page(page, size), Wrappers.<EnterpriseTaskSubmission>lambdaQuery()
                .eq(EnterpriseTaskSubmission::getSubmitterId, userId)
                .orderByDesc(EnterpriseTaskSubmission::getCreatedAt));
        return result.getRecords().stream().map(this::submissionView).toList();
    }

    @Override
    @Transactional
    public TaskDtos.SubmissionView review(Long userId, Long submissionId, TaskDtos.ReviewRequest request) {
        EnterpriseTaskSubmission submission = submissionMapper.selectById(submissionId);
        if (submission == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Submission not found");
        }
        EnterpriseTask task = requireOwned(userId, submission.getTaskId());
        if (!"PENDING".equals(submission.getStatus())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Submission has already been reviewed");
        }
        String status = request.status().trim().toUpperCase();
        if (!List.of("APPROVED", "REJECTED").contains(status)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Review status must be APPROVED or REJECTED");
        }
        submission.setStatus(status);
        submission.setReviewComment(trimToNull(request.comment()));
        submission.setReviewedAt(LocalDateTime.now());
        if ("APPROVED".equals(status)) {
            BigDecimal reward = request.rewardPoints() == null ? submission.getRewardPoints() : request.rewardPoints();
            reward = reward == null ? BigDecimal.ZERO : reward;
            submission.setRewardPoints(reward);
            if (reward.compareTo(BigDecimal.ZERO) > 0) {
                pointService.spend(task.getPublisherId(), reward, "TASK_REWARD", "ENTERPRISE_TASK", task.getId());
                pointService.earn(submission.getSubmitterId(), reward, "TASK_REWARD", "ENTERPRISE_TASK", task.getId());
            }
        }
        submissionMapper.updateById(submission);
        return submissionView(submission);
    }

    private EnterpriseTask requireOwned(Long userId, Long taskId) {
        EnterpriseTask task = taskMapper.selectOne(Wrappers.<EnterpriseTask>lambdaQuery()
                .eq(EnterpriseTask::getId, taskId)
                .eq(EnterpriseTask::getPublisherId, userId));
        if (task == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Task not found");
        }
        return task;
    }

    private <T> Page<T> page(int page, int size) {
        return new Page<>(Math.max(1, page), Math.min(Math.max(1, size), 30));
    }

    private void apply(EnterpriseTask task, TaskDtos.SaveRequest request) {
        task.setTitle(request.title().trim());
        task.setDescription(trimToNull(request.description()));
        task.setRequirementsJson(toRequirementsJson(request.requirementsText()));
        task.setBudgetPoints(request.budgetPoints() == null ? BigDecimal.ZERO : request.budgetPoints());
        task.setDeadline(request.deadline());
    }

    private TaskDtos.TaskCard card(EnterpriseTask task) {
        long submissionCount = submissionMapper.selectCount(Wrappers.<EnterpriseTaskSubmission>lambdaQuery()
                .eq(EnterpriseTaskSubmission::getTaskId, task.getId()));
        return new TaskDtos.TaskCard(
                task.getId(),
                task.getPublisherId(),
                task.getTitle(),
                task.getDescription(),
                requirementsText(task.getRequirementsJson()),
                task.getBudgetPoints(),
                task.getStatus(),
                task.getDeadline(),
                submissionCount,
                task.getCreatedAt(),
                task.getUpdatedAt());
    }

    private TaskDtos.SubmissionView submissionView(EnterpriseTaskSubmission submission) {
        Artwork artwork = artworkMapper.selectById(submission.getArtworkId());
        return new TaskDtos.SubmissionView(
                submission.getId(),
                submission.getTaskId(),
                submission.getSubmitterId(),
                submission.getArtworkId(),
                artwork == null ? "Artwork" : artwork.getTitle(),
                artwork == null ? null : artwork.getImageUrl(),
                submission.getNote(),
                submission.getStatus(),
                submission.getRewardPoints(),
                submission.getReviewComment(),
                submission.getCreatedAt(),
                submission.getReviewedAt());
    }

    private String toRequirementsJson(String text) {
        try {
            return objectMapper.writeValueAsString(Map.of("text", trimToNull(text) == null ? "" : text.trim()));
        } catch (JsonProcessingException ex) {
            return "{\"text\":\"\"}";
        }
    }

    private String requirementsText(String json) {
        if (!StringUtils.hasText(json)) {
            return "";
        }
        try {
            return objectMapper.readTree(json).path("text").asText("");
        } catch (JsonProcessingException ex) {
            return "";
        }
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
