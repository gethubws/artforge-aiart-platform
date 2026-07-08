package com.aiart.platform.service;

import com.aiart.platform.dto.TaskDtos;

import java.util.List;

public interface EnterpriseTaskService {
    TaskDtos.TaskCard create(Long userId, TaskDtos.SaveRequest request);

    TaskDtos.TaskCard update(Long userId, Long taskId, TaskDtos.SaveRequest request);

    TaskDtos.TaskCard publish(Long userId, Long taskId);

    TaskDtos.TaskCard close(Long userId, Long taskId);

    List<TaskDtos.TaskCard> market(int page, int size, String status);

    List<TaskDtos.TaskCard> myTasks(Long userId, int page, int size, String status);

    TaskDtos.SubmissionView submit(Long userId, Long taskId, TaskDtos.SubmitRequest request);

    List<TaskDtos.SubmissionView> taskSubmissions(Long userId, Long taskId, int page, int size);

    List<TaskDtos.SubmissionView> mySubmissions(Long userId, int page, int size);

    TaskDtos.SubmissionView review(Long userId, Long submissionId, TaskDtos.ReviewRequest request);
}
