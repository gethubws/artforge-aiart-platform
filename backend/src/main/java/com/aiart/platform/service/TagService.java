package com.aiart.platform.service;

import com.aiart.platform.dto.TagDtos;

import java.util.List;

public interface TagService {
    List<TagDtos.TagCategoryNode> tree();

    List<TagDtos.TagCategoryNode> adminTree();

    List<TagDtos.TagCategorySummary> categorySummaries();

    List<TagDtos.TagOption> options();

    TagDtos.TagPage page(Long categoryId, String keyword, int page, int size);

    TagDtos.TagDetail detail(Long tagId);

    TagDtos.PromptBuildResponse buildPrompt(TagDtos.PromptBuildRequest request);

    TagDtos.TagCategoryNode createCategory(TagDtos.CategorySaveRequest request);

    TagDtos.TagNode createTag(TagDtos.TagSaveRequest request);

    TagDtos.TagNode updateTag(Long tagId, TagDtos.TagSaveRequest request);

    void deactivateTag(Long tagId);
}
