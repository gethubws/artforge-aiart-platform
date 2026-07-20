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
import com.aiart.platform.service.UserEngagementService;
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
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class EnterpriseTaskServiceImpl implements EnterpriseTaskService {
    private final EnterpriseTaskMapper taskMapper;
    private final EnterpriseTaskSubmissionMapper submissionMapper;
    private final ArtworkMapper artworkMapper;
    private final PointService pointService;
    private final UserEngagementService userEngagementService;
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
        notifyTaskWatchers(task, userId, Set.of(), "TASK_UPDATED", "任务内容已更新",
                "你关注的任务《" + task.getTitle() + "》有新的内容调整。");
        return card(task);
    }

    @Override
    @Transactional
    public TaskDtos.TaskCard publish(Long userId, Long taskId) {
        EnterpriseTask task = requireOwned(userId, taskId);
        task.setStatus("PUBLISHED");
        task.setUpdatedAt(LocalDateTime.now());
        taskMapper.updateById(task);
        notifyTaskWatchers(task, userId, Set.of(), "TASK_PUBLISHED", "任务已发布",
                "任务《" + task.getTitle() + "》已经进入公开征集状态。");
        return card(task);
    }

    @Override
    @Transactional
    public TaskDtos.TaskCard close(Long userId, Long taskId) {
        EnterpriseTask task = requireOwned(userId, taskId);
        task.setStatus("CLOSED");
        task.setUpdatedAt(LocalDateTime.now());
        taskMapper.updateById(task);
        notifyTaskWatchers(task, userId, Set.of(), "TASK_CLOSED", "任务已关闭",
                "你关注的任务《" + task.getTitle() + "》已关闭。");
        return card(task);
    }

    @Override
    public TaskDtos.TaskCard detail(Long viewerId, Long taskId) {
        EnterpriseTask task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Task not found");
        }
        if (viewerId == null) {
            if (!"PUBLISHED".equals(task.getStatus()) && !"CLOSED".equals(task.getStatus())) {
                throw new BusinessException(ErrorCode.NOT_FOUND, "Task not found");
            }
        } else if (!viewerId.equals(task.getPublisherId())
                && !"PUBLISHED".equals(task.getStatus())
                && !"CLOSED".equals(task.getStatus())) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Task not found");
        }
        return card(task);
    }

    @Override
    public List<TaskDtos.TaskCard> market(int page, int size, TaskDtos.ListQuery query) {
        String targetStatus = normalizeStatus(query, true);
        var wrapper = Wrappers.<EnterpriseTask>lambdaQuery();
        if (StringUtils.hasText(targetStatus)) {
            wrapper.eq(EnterpriseTask::getStatus, targetStatus);
        } else {
            wrapper.in(EnterpriseTask::getStatus, List.of("PUBLISHED", "CLOSED"));
        }
        return slice(filterAndSort(taskMapper.selectList(wrapper), query), page, size);
    }

    @Override
    public List<TaskDtos.TaskCard> myTasks(Long userId, int page, int size, TaskDtos.ListQuery query) {
        String targetStatus = normalizeStatus(query, false);
        var wrapper = Wrappers.<EnterpriseTask>lambdaQuery()
                .eq(EnterpriseTask::getPublisherId, userId);
        if (StringUtils.hasText(targetStatus)) {
            wrapper.eq(EnterpriseTask::getStatus, targetStatus);
        }
        return slice(filterAndSort(taskMapper.selectList(wrapper), query), page, size);
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
        notifyTaskWatchers(task, userId, Set.of(task.getPublisherId()), "TASK_SUBMISSION_CREATED", "任务收到新投稿",
                "作品《" + artwork.getTitle() + "》已投稿到任务《" + task.getTitle() + "》。");
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
        String reviewLabel = "APPROVED".equals(status) ? "已通过" : "已拒绝";
        notifyTaskWatchers(task, userId, Set.of(submission.getSubmitterId()), "TASK_SUBMISSION_REVIEWED", "任务投稿已审核",
                "你投向《" + task.getTitle() + "》的作品已" + reviewLabel + "。");
        return submissionView(submission);
    }

    private void notifyTaskWatchers(EnterpriseTask task, Long actorUserId, Set<Long> extraUserIds, String type,
                                    String title, String content) {
        Set<Long> userIds = new LinkedHashSet<>();
        if (extraUserIds != null) {
            userIds.addAll(extraUserIds);
        }
        userIds.addAll(userEngagementService.subscriptionUserIds("TASK", task.getId()));
        userEngagementService.notifyUsers(userIds, actorUserId, type, title, content, "TASK", task.getId());
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

    private List<TaskDtos.TaskCard> filterAndSort(List<EnterpriseTask> tasks, TaskDtos.ListQuery query) {
        String keyword = normalizeKeyword(query);
        String tier = normalizeTier(query);
        Comparator<TaskDtos.TaskCard> comparator = taskComparator(query);

        return tasks.stream()
                .map(this::card)
                .filter(card -> matchesKeyword(card, keyword))
                .filter(card -> matchesTier(card, tier))
                .sorted(comparator)
                .toList();
    }

    private List<TaskDtos.TaskCard> slice(List<TaskDtos.TaskCard> cards, int page, int size) {
        int normalizedPage = Math.max(1, page);
        int normalizedSize = Math.min(Math.max(1, size), 30);
        int fromIndex = (normalizedPage - 1) * normalizedSize;
        if (fromIndex >= cards.size()) {
            return List.of();
        }
        int toIndex = Math.min(cards.size(), fromIndex + normalizedSize);
        return cards.subList(fromIndex, toIndex);
    }

    private boolean matchesKeyword(TaskDtos.TaskCard card, String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return true;
        }
        String haystack = String.join(" ",
                card.title() == null ? "" : card.title(),
                card.description() == null ? "" : card.description(),
                card.requirementsText() == null ? "" : card.requirementsText()
        ).toLowerCase();
        return haystack.contains(keyword);
    }

    private boolean matchesTier(TaskDtos.TaskCard card, String tier) {
        if (!StringUtils.hasText(tier)) {
            return true;
        }
        BigDecimal budget = card.budgetPoints() == null ? BigDecimal.ZERO : card.budgetPoints();
        return switch (tier) {
            case "premium" -> budget.compareTo(BigDecimal.valueOf(200)) >= 0;
            case "featured" -> budget.compareTo(BigDecimal.valueOf(80)) >= 0
                    && budget.compareTo(BigDecimal.valueOf(200)) < 0;
            case "standard" -> budget.compareTo(BigDecimal.valueOf(80)) < 0;
            default -> true;
        };
    }

    private Comparator<TaskDtos.TaskCard> taskComparator(TaskDtos.ListQuery query) {
        String sort = normalizeSort(query);
        return switch (sort) {
            case "budget" -> Comparator
                    .comparing(
                            (TaskDtos.TaskCard card) -> card.budgetPoints() == null ? BigDecimal.ZERO : card.budgetPoints(),
                            BigDecimal::compareTo
                    )
                    .reversed()
                    .thenComparing(
                            card -> card.updatedAt() == null ? LocalDateTime.MIN : card.updatedAt(),
                            Comparator.reverseOrder()
                    );
            case "submissions" -> Comparator.comparingLong(TaskDtos.TaskCard::submissionCount)
                    .reversed()
                    .thenComparing(
                            card -> card.updatedAt() == null ? LocalDateTime.MIN : card.updatedAt(),
                            Comparator.reverseOrder()
                    );
            case "deadline" -> Comparator
                    .comparing((TaskDtos.TaskCard card) -> card.deadline() == null ? LocalDateTime.MAX : card.deadline())
                    .thenComparing(
                            card -> card.updatedAt() == null ? LocalDateTime.MIN : card.updatedAt(),
                            Comparator.reverseOrder()
                    );
            default -> Comparator.comparing(
                    (TaskDtos.TaskCard card) -> card.updatedAt() == null ? LocalDateTime.MIN : card.updatedAt(),
                    Comparator.reverseOrder()
            );
        };
    }

    private String normalizeKeyword(TaskDtos.ListQuery query) {
        if (query == null || !StringUtils.hasText(query.keyword())) {
            return "";
        }
        return query.keyword().trim().toLowerCase();
    }

    private String normalizeStatus(TaskDtos.ListQuery query, boolean market) {
        if (query == null || !StringUtils.hasText(query.status())) {
            return "";
        }
        String value = query.status().trim().toUpperCase();
        List<String> allowed = market ? List.of("PUBLISHED", "CLOSED") : List.of("DRAFT", "PUBLISHED", "CLOSED");
        return allowed.contains(value) ? value : "";
    }

    private String normalizeTier(TaskDtos.ListQuery query) {
        if (query == null || !StringUtils.hasText(query.tier())) {
            return "";
        }
        String value = query.tier().trim().toLowerCase();
        return List.of("standard", "featured", "premium").contains(value) ? value : "";
    }

    private String normalizeSort(TaskDtos.ListQuery query) {
        if (query == null || !StringUtils.hasText(query.sort())) {
            return "latest";
        }
        String value = query.sort().trim().toLowerCase();
        return List.of("latest", "budget", "submissions", "deadline").contains(value) ? value : "latest";
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
