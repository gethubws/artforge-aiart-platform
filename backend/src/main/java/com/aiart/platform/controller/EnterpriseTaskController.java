package com.aiart.platform.controller;

import com.aiart.platform.common.ApiResponse;
import com.aiart.platform.dto.TaskDtos;
import com.aiart.platform.security.CurrentUser;
import com.aiart.platform.service.EnterpriseTaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class EnterpriseTaskController {
    private final EnterpriseTaskService taskService;
    private final CurrentUser currentUser;

    @PostMapping
    public ApiResponse<TaskDtos.TaskCard> create(@Valid @RequestBody TaskDtos.SaveRequest request) {
        return ApiResponse.ok(taskService.create(currentUser.requireUserId(), request));
    }

    @PutMapping("/{taskId}")
    public ApiResponse<TaskDtos.TaskCard> update(@PathVariable Long taskId,
                                                 @Valid @RequestBody TaskDtos.SaveRequest request) {
        return ApiResponse.ok(taskService.update(currentUser.requireUserId(), taskId, request));
    }

    @PostMapping("/{taskId}/publish")
    public ApiResponse<TaskDtos.TaskCard> publish(@PathVariable Long taskId) {
        return ApiResponse.ok(taskService.publish(currentUser.requireUserId(), taskId));
    }

    @PostMapping("/{taskId}/close")
    public ApiResponse<TaskDtos.TaskCard> close(@PathVariable Long taskId) {
        return ApiResponse.ok(taskService.close(currentUser.requireUserId(), taskId));
    }

    @GetMapping("/{taskId}")
    public ApiResponse<TaskDtos.TaskCard> detail(@PathVariable Long taskId) {
        return ApiResponse.ok(taskService.detail(currentUser.userIdOrNull(), taskId));
    }

    @GetMapping("/market")
    public ApiResponse<TaskDtos.TaskPage> market(@RequestParam(defaultValue = "1") int page,
                                                       @RequestParam(defaultValue = "20") int size,
                                                       @RequestParam(required = false) String keyword,
                                                       @RequestParam(required = false) String status,
                                                       @RequestParam(required = false) String tier,
                                                       @RequestParam(required = false) String sort) {
        return ApiResponse.ok(taskService.market(page, size, new TaskDtos.ListQuery(keyword, status, tier, sort)));
    }

    @GetMapping("/my")
    public ApiResponse<TaskDtos.TaskPage> myTasks(@RequestParam(defaultValue = "1") int page,
                                                        @RequestParam(defaultValue = "20") int size,
                                                        @RequestParam(required = false) String keyword,
                                                        @RequestParam(required = false) String status,
                                                        @RequestParam(required = false) String tier,
                                                        @RequestParam(required = false) String sort) {
        return ApiResponse.ok(taskService.myTasks(
                currentUser.requireUserId(),
                page,
                size,
                new TaskDtos.ListQuery(keyword, status, tier, sort)
        ));
    }

    @PostMapping("/{taskId}/submissions")
    public ApiResponse<TaskDtos.SubmissionView> submit(@PathVariable Long taskId,
                                                       @Valid @RequestBody TaskDtos.SubmitRequest request) {
        return ApiResponse.ok(taskService.submit(currentUser.requireUserId(), taskId, request));
    }

    @GetMapping("/{taskId}/submissions")
    public ApiResponse<List<TaskDtos.SubmissionView>> taskSubmissions(@PathVariable Long taskId,
                                                                      @RequestParam(defaultValue = "1") int page,
                                                                      @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(taskService.taskSubmissions(currentUser.requireUserId(), taskId, page, size));
    }

    @GetMapping("/submissions/my")
    public ApiResponse<List<TaskDtos.SubmissionView>> mySubmissions(@RequestParam(defaultValue = "1") int page,
                                                                    @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(taskService.mySubmissions(currentUser.requireUserId(), page, size));
    }

    @PostMapping("/submissions/{submissionId}/review")
    public ApiResponse<TaskDtos.SubmissionView> review(@PathVariable Long submissionId,
                                                       @Valid @RequestBody TaskDtos.ReviewRequest request) {
        return ApiResponse.ok(taskService.review(currentUser.requireUserId(), submissionId, request));
    }
}
