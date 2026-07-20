package com.aiart.platform.service.impl;

import com.aiart.platform.dto.TagDtos;
import com.aiart.platform.entity.Tag;
import com.aiart.platform.entity.TagCategory;
import com.aiart.platform.entity.TagPreview;
import com.aiart.platform.exception.BusinessException;
import com.aiart.platform.exception.ErrorCode;
import com.aiart.platform.mapper.TagCategoryMapper;
import com.aiart.platform.mapper.TagMapper;
import com.aiart.platform.mapper.TagPreviewMapper;
import com.aiart.platform.service.TagService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {
    private final TagCategoryMapper tagCategoryMapper;
    private final TagMapper tagMapper;
    private final TagPreviewMapper tagPreviewMapper;

    @Override
    public List<TagDtos.TagCategoryNode> tree() {
        return tree(false);
    }

    @Override
    public List<TagDtos.TagCategoryNode> adminTree() {
        return tree(true);
    }

    @Override
    public List<TagDtos.TagCategorySummary> categorySummaries() {
        List<TagCategory> categories = activeCategories();
        return categories.stream().map(category -> {
            var baseQuery = Wrappers.<Tag>lambdaQuery()
                    .eq(Tag::getCategoryId, category.getId())
                    .eq(Tag::getStatus, "ACTIVE")
                    .eq(Tag::getVisibility, "PUBLIC");
            long count = tagMapper.selectCount(baseQuery);
            Page<Tag> popularPage = tagMapper.selectPage(new Page<>(1, 2, false),
                    Wrappers.<Tag>lambdaQuery()
                            .eq(Tag::getCategoryId, category.getId())
                            .eq(Tag::getStatus, "ACTIVE")
                            .eq(Tag::getVisibility, "PUBLIC")
                            .orderByDesc(Tag::getUsageCount)
                            .orderByAsc(Tag::getCreatedAt));
            return new TagDtos.TagCategorySummary(
                    category.getId(),
                    category.getName(),
                    category.getSlug(),
                    count,
                    popularPage.getRecords().stream().map(this::toNode).toList());
        }).toList();
    }

    @Override
    public List<TagDtos.TagOption> options() {
        List<TagCategory> categories = activeCategories();
        Map<Long, TagCategory> categoryMap = categories.stream()
                .collect(Collectors.toMap(TagCategory::getId, category -> category));
        return tagMapper.selectList(Wrappers.<Tag>lambdaQuery()
                        .eq(Tag::getStatus, "ACTIVE")
                        .eq(Tag::getVisibility, "PUBLIC")
                        .orderByDesc(Tag::getUsageCount)
                        .orderByAsc(Tag::getCreatedAt)).stream()
                .filter(tag -> categoryMap.containsKey(tag.getCategoryId()))
                .map(tag -> {
                    TagCategory category = categoryMap.get(tag.getCategoryId());
                    return new TagDtos.TagOption(tag.getId(), category.getId(), category.getName(), category.getSlug(),
                            tag.getName(), tag.getDisplayNameZh());
                })
                .toList();
    }

    @Override
    public TagDtos.TagPage page(Long categoryId, String keyword, int page, int size) {
        long current = Math.max(1, page);
        long limit = Math.min(Math.max(1, size), 48);
        var query = Wrappers.<Tag>lambdaQuery()
                .eq(Tag::getStatus, "ACTIVE")
                .eq(Tag::getVisibility, "PUBLIC");
        if (categoryId != null) {
            query.eq(Tag::getCategoryId, categoryId);
        }
        if (StringUtils.hasText(keyword)) {
            String value = keyword.trim();
            query.and(wrapper -> wrapper
                    .like(Tag::getName, value)
                    .or()
                    .like(Tag::getDisplayNameZh, value)
                    .or()
                    .like(Tag::getDescriptionZh, value)
                    .or()
                    .like(Tag::getPromptText, value));
        }
        Page<Tag> result = tagMapper.selectPage(new Page<>(current, limit),
                query.orderByDesc(Tag::getUsageCount).orderByAsc(Tag::getCreatedAt));
        return new TagDtos.TagPage(
                result.getRecords().stream().map(this::toNode).toList(),
                result.getCurrent(),
                result.getSize(),
                result.getTotal(),
                result.getCurrent() < result.getPages());
    }

    @Override
    public TagDtos.TagDetail detail(Long tagId) {
        Tag tag = tagMapper.selectOne(Wrappers.<Tag>lambdaQuery()
                .eq(Tag::getId, tagId)
                .eq(Tag::getStatus, "ACTIVE")
                .eq(Tag::getVisibility, "PUBLIC"));
        if (tag == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Tag not found");
        }
        TagCategory category = tagCategoryMapper.selectById(tag.getCategoryId());
        List<TagDtos.TagPreviewNode> previews = tagPreviewMapper.selectList(
                        Wrappers.<TagPreview>lambdaQuery()
                                .eq(TagPreview::getTagId, tagId)
                                .orderByDesc(TagPreview::getIsCover)
                                .orderByAsc(TagPreview::getSortOrder)
                                .orderByAsc(TagPreview::getCreatedAt)).stream()
                .map(this::toPreviewNode)
                .toList();
        if (previews.isEmpty() && StringUtils.hasText(tag.getPreviewImageUrl())) {
            previews = List.of(new TagDtos.TagPreviewNode(
                    null, tag.getPreviewImageUrl(), "COVER", "GENERAL", "封面预览", tag.getPromptText(), 0, true));
        }
        return new TagDtos.TagDetail(
                toNode(tag),
                category == null ? null : category.getId(),
                category == null ? null : category.getName(),
                category == null ? null : category.getSlug(),
                previews);
    }

    private List<TagDtos.TagCategoryNode> tree(boolean includePrivate) {
        List<TagCategory> categories = activeCategories();
        var query = Wrappers.<Tag>lambdaQuery()
                .eq(Tag::getStatus, "ACTIVE");
        if (!includePrivate) {
            query.eq(Tag::getVisibility, "PUBLIC");
        }
        List<Tag> tags = tagMapper.selectList(query.orderByDesc(Tag::getUsageCount));
        Map<Long, List<TagDtos.TagNode>> tagMap = tags.stream()
                .collect(Collectors.groupingBy(Tag::getCategoryId, LinkedHashMap::new, Collectors.mapping(this::toNode, Collectors.toList())));
        return categories.stream()
                .sorted(Comparator.comparing(TagCategory::getSortOrder))
                .map(category -> new TagDtos.TagCategoryNode(
                        category.getId(),
                        category.getName(),
                        category.getSlug(),
                        tagMap.getOrDefault(category.getId(), List.of())))
                .toList();
    }

    @Override
    public TagDtos.PromptBuildResponse buildPrompt(TagDtos.PromptBuildRequest request) {
        List<Long> tagIds = request.tagIds() == null ? List.of() : request.tagIds();
        List<String> promptParts = new ArrayList<>();
        List<String> negativeParts = new ArrayList<>();

        if (StringUtils.hasText(request.freeText())) {
            promptParts.add(request.freeText().trim());
        }
        if (!tagIds.isEmpty()) {
            List<Tag> tags = tagMapper.selectList(Wrappers.<Tag>lambdaQuery()
                    .in(Tag::getId, tagIds)
                    .eq(Tag::getStatus, "ACTIVE")
                    .eq(Tag::getVisibility, "PUBLIC"));
            tags.stream()
                    .sorted(Comparator.comparing(Tag::getWeight).reversed())
                    .forEach(tag -> {
                        promptParts.add(weighted(tag.getPromptText(), tag.getWeight() == null ? null : tag.getWeight().doubleValue()));
                        if (StringUtils.hasText(tag.getNegativePromptText())) {
                            negativeParts.add(tag.getNegativePromptText().trim());
                        }
                    });
        }
        if (StringUtils.hasText(request.negativeText())) {
            negativeParts.add(request.negativeText().trim());
        }

        return new TagDtos.PromptBuildResponse(
                String.join(", ", promptParts),
                String.join(", ", negativeParts),
                tagIds);
    }

    @Override
    @Transactional
    public TagDtos.TagCategoryNode createCategory(TagDtos.CategorySaveRequest request) {
        Long existing = tagCategoryMapper.selectCount(Wrappers.<TagCategory>lambdaQuery().eq(TagCategory::getSlug, request.slug().trim()));
        if (existing > 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Category slug already exists");
        }
        TagCategory category = new TagCategory();
        category.setName(request.name().trim());
        category.setSlug(request.slug().trim());
        category.setParentId(0L);
        category.setSortOrder(request.sortOrder() == null ? 100 : request.sortOrder());
        category.setStatus("ACTIVE");
        category.setCreatedAt(LocalDateTime.now());
        tagCategoryMapper.insert(category);
        return new TagDtos.TagCategoryNode(category.getId(), category.getName(), category.getSlug(), List.of());
    }

    @Override
    @Transactional
    public TagDtos.TagNode createTag(TagDtos.TagSaveRequest request) {
        TagCategory category = tagCategoryMapper.selectById(request.categoryId());
        if (category == null || !"ACTIVE".equals(category.getStatus())) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Tag category not found");
        }
        Tag tag = new Tag();
        apply(tag, request);
        tag.setUsageCount(0);
        tag.setStatus("ACTIVE");
        tag.setCreatedAt(LocalDateTime.now());
        tagMapper.insert(tag);
        return toNode(tag);
    }

    @Override
    @Transactional
    public TagDtos.TagNode updateTag(Long tagId, TagDtos.TagSaveRequest request) {
        Tag tag = tagMapper.selectById(tagId);
        if (tag == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Tag not found");
        }
        apply(tag, request);
        tagMapper.updateById(tag);
        return toNode(tag);
    }

    @Override
    @Transactional
    public void deactivateTag(Long tagId) {
        Tag tag = tagMapper.selectById(tagId);
        if (tag == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Tag not found");
        }
        tag.setStatus("INACTIVE");
        tagMapper.updateById(tag);
    }

    private TagDtos.TagNode toNode(Tag tag) {
        return new TagDtos.TagNode(
                tag.getId(),
                tag.getName(),
                tag.getDisplayNameZh(),
                tag.getDescriptionZh(),
                tag.getPromptText(),
                tag.getNegativePromptText(),
                tag.getPreviewImageUrl(),
                tag.getWeight(),
                tag.getVisibility());
    }

    private List<TagCategory> activeCategories() {
        return tagCategoryMapper.selectList(
                Wrappers.<TagCategory>lambdaQuery()
                        .eq(TagCategory::getStatus, "ACTIVE")
                        .orderByAsc(TagCategory::getSortOrder));
    }

    private TagDtos.TagPreviewNode toPreviewNode(TagPreview preview) {
        return new TagDtos.TagPreviewNode(
                preview.getId(),
                preview.getImageUrl(),
                preview.getPreviewType(),
                preview.getSceneKey(),
                preview.getTitleZh(),
                preview.getPromptSnapshot(),
                preview.getSortOrder(),
                Boolean.TRUE.equals(preview.getIsCover()));
    }

    private void apply(Tag tag, TagDtos.TagSaveRequest request) {
        tag.setCategoryId(request.categoryId());
        tag.setName(request.name().trim());
        tag.setDisplayNameZh(trimToNull(request.displayNameZh()));
        tag.setDescriptionZh(trimToNull(request.descriptionZh()));
        tag.setPromptText(request.promptText().trim());
        tag.setNegativePromptText(trimToNull(request.negativePromptText()));
        tag.setPreviewImageUrl(trimToNull(request.previewImageUrl()));
        tag.setWeight(request.weight() == null ? BigDecimal.ONE : request.weight());
        tag.setVisibility(StringUtils.hasText(request.visibility()) ? request.visibility().trim() : "PUBLIC");
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private String weighted(String text, Double weight) {
        if (!StringUtils.hasText(text) || weight == null || Math.abs(weight - 1.0) < 0.01) {
            return text;
        }
        return "(" + text.trim() + ":" + String.format(java.util.Locale.US, "%.2f", weight) + ")";
    }
}
