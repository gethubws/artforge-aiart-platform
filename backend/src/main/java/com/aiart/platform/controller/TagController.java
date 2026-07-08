package com.aiart.platform.controller;

import com.aiart.platform.common.ApiResponse;
import com.aiart.platform.dto.TagDtos;
import com.aiart.platform.security.CurrentUser;
import com.aiart.platform.service.TagService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController {
    private final TagService tagService;
    private final CurrentUser currentUser;

    @GetMapping("/tree")
    public ApiResponse<List<TagDtos.TagCategoryNode>> tree() {
        return ApiResponse.ok(tagService.tree());
    }

    @GetMapping("/admin/tree")
    public ApiResponse<List<TagDtos.TagCategoryNode>> adminTree() {
        currentUser.requireAdmin();
        return ApiResponse.ok(tagService.adminTree());
    }

    @PostMapping("/build-prompt")
    public ApiResponse<TagDtos.PromptBuildResponse> buildPrompt(@RequestBody TagDtos.PromptBuildRequest request) {
        return ApiResponse.ok(tagService.buildPrompt(request));
    }

    @PostMapping("/categories")
    public ApiResponse<TagDtos.TagCategoryNode> createCategory(@Valid @RequestBody TagDtos.CategorySaveRequest request) {
        currentUser.requireAdmin();
        return ApiResponse.ok(tagService.createCategory(request));
    }

    @PostMapping
    public ApiResponse<TagDtos.TagNode> createTag(@Valid @RequestBody TagDtos.TagSaveRequest request) {
        currentUser.requireAdmin();
        return ApiResponse.ok(tagService.createTag(request));
    }

    @PutMapping("/{tagId}")
    public ApiResponse<TagDtos.TagNode> updateTag(@PathVariable Long tagId, @Valid @RequestBody TagDtos.TagSaveRequest request) {
        currentUser.requireAdmin();
        return ApiResponse.ok(tagService.updateTag(tagId, request));
    }

    @DeleteMapping("/{tagId}")
    public ApiResponse<Void> deactivateTag(@PathVariable Long tagId) {
        currentUser.requireAdmin();
        tagService.deactivateTag(tagId);
        return ApiResponse.ok();
    }
}
