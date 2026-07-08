package com.aiart.platform.controller;

import com.aiart.platform.common.ApiResponse;
import com.aiart.platform.dto.AuditDtos;
import com.aiart.platform.security.CurrentUser;
import com.aiart.platform.service.ContentAuditService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/audits")
@RequiredArgsConstructor
public class ContentAuditController {
    private final ContentAuditService contentAuditService;
    private final CurrentUser currentUser;

    @GetMapping("/pending")
    public ApiResponse<List<AuditDtos.AuditView>> pending(@RequestParam(defaultValue = "1") int page,
                                                          @RequestParam(defaultValue = "20") int size) {
        currentUser.requireAdmin();
        return ApiResponse.ok(contentAuditService.pending(page, size));
    }

    @GetMapping("/my")
    public ApiResponse<List<AuditDtos.AuditView>> my(@RequestParam(defaultValue = "1") int page,
                                                     @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(contentAuditService.myAudits(currentUser.requireUserId(), page, size));
    }

    @PostMapping("/{auditId}/review")
    public ApiResponse<AuditDtos.AuditView> review(@PathVariable Long auditId,
                                                   @Valid @RequestBody AuditDtos.ReviewRequest request) {
        currentUser.requireAdmin();
        return ApiResponse.ok(contentAuditService.review(currentUser.requireUserId(), auditId, request));
    }
}
