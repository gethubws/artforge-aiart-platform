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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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

    @GetMapping("/admin/{tagId}")
    public ApiResponse<TagDtos.TagDetail> adminDetail(@PathVariable Long tagId) {
        currentUser.requireAdmin();
        return ApiResponse.ok(tagService.adminDetail(tagId));
    }

    @GetMapping("/admin/analytics")
    public ApiResponse<TagDtos.TagAnalytics> analytics() {
        currentUser.requireAdmin();
        return ApiResponse.ok(tagService.analytics());
    }

    @GetMapping("/categories")
    public ApiResponse<List<TagDtos.TagCategorySummary>> categories() {
        return ApiResponse.ok(tagService.categorySummaries());
    }

    @GetMapping("/options")
    public ApiResponse<List<TagDtos.TagOption>> options() {
        return ApiResponse.ok(tagService.options());
    }

    @GetMapping
    public ApiResponse<TagDtos.TagPage> page(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int size) {
        return ApiResponse.ok(tagService.page(categoryId, keyword, page, size));
    }

    @GetMapping("/{tagId}")
    public ApiResponse<TagDtos.TagDetail> detail(@PathVariable Long tagId) {
        return ApiResponse.ok(tagService.detail(tagId));
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

    @PostMapping("/{tagId}/previews")
    public ApiResponse<TagDtos.TagDetail> addPreview(
            @PathVariable Long tagId,
            @RequestParam MultipartFile file,
            @RequestParam(required = false) String previewType,
            @RequestParam(required = false) String sceneKey,
            @RequestParam(required = false) String titleZh,
            @RequestParam(required = false) String promptSnapshot,
            @RequestParam(required = false) Integer sortOrder,
            @RequestParam(required = false) Boolean cover) {
        currentUser.requireAdmin();
        return ApiResponse.ok(tagService.addPreview(tagId, file,
                new TagDtos.TagPreviewSaveRequest(previewType, sceneKey, titleZh, promptSnapshot, sortOrder, cover)));
    }

    @PutMapping("/{tagId}/previews/{previewId}")
    public ApiResponse<TagDtos.TagDetail> updatePreview(
            @PathVariable Long tagId,
            @PathVariable Long previewId,
            @RequestBody TagDtos.TagPreviewSaveRequest request) {
        currentUser.requireAdmin();
        return ApiResponse.ok(tagService.updatePreview(tagId, previewId, request));
    }

    @PostMapping("/{tagId}/previews/{previewId}/image")
    public ApiResponse<TagDtos.TagDetail> replacePreviewImage(
            @PathVariable Long tagId,
            @PathVariable Long previewId,
            @RequestParam MultipartFile file) {
        currentUser.requireAdmin();
        return ApiResponse.ok(tagService.replacePreviewImage(tagId, previewId, file));
    }

    @DeleteMapping("/{tagId}/previews/{previewId}")
    public ApiResponse<TagDtos.TagDetail> deletePreview(
            @PathVariable Long tagId,
            @PathVariable Long previewId) {
        currentUser.requireAdmin();
        return ApiResponse.ok(tagService.deletePreview(tagId, previewId));
    }

    @PutMapping("/{tagId}/previews/order")
    public ApiResponse<TagDtos.TagDetail> reorderPreviews(
            @PathVariable Long tagId,
            @RequestBody TagDtos.TagPreviewOrderRequest request) {
        currentUser.requireAdmin();
        return ApiResponse.ok(tagService.reorderPreviews(tagId, request));
    }
}
