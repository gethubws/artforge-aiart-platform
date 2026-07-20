package com.aiart.platform.service;

import com.aiart.platform.dto.TagDtos;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface TagService {
    List<TagDtos.TagCategoryNode> tree();

    List<TagDtos.TagCategoryNode> adminTree();

    List<TagDtos.TagCategorySummary> categorySummaries();

    List<TagDtos.TagOption> options();

    TagDtos.TagPage page(Long categoryId, String keyword, int page, int size);

    TagDtos.TagDetail detail(Long tagId);

    TagDtos.TagDetail adminDetail(Long tagId);

    TagDtos.TagDetail addPreview(Long tagId, MultipartFile file, TagDtos.TagPreviewSaveRequest request);

    TagDtos.TagDetail updatePreview(Long tagId, Long previewId, TagDtos.TagPreviewSaveRequest request);

    TagDtos.TagDetail replacePreviewImage(Long tagId, Long previewId, MultipartFile file);

    TagDtos.TagDetail deletePreview(Long tagId, Long previewId);

    TagDtos.TagDetail reorderPreviews(Long tagId, TagDtos.TagPreviewOrderRequest request);

    TagDtos.TagAnalytics analytics();

    TagDtos.PromptBuildResponse buildPrompt(TagDtos.PromptBuildRequest request);

    TagDtos.TagCategoryNode createCategory(TagDtos.CategorySaveRequest request);

    TagDtos.TagNode createTag(TagDtos.TagSaveRequest request);

    TagDtos.TagNode updateTag(Long tagId, TagDtos.TagSaveRequest request);

    void deactivateTag(Long tagId);
}
