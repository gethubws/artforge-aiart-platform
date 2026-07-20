package com.aiart.platform.service.impl;

import com.aiart.platform.dto.ArtworkDtos;
import com.aiart.platform.entity.ContentAudit;
import com.aiart.platform.entity.Artwork;
import com.aiart.platform.entity.ArtworkTag;
import com.aiart.platform.entity.Tag;
import com.aiart.platform.entity.TagCategory;
import com.aiart.platform.exception.BusinessException;
import com.aiart.platform.exception.ErrorCode;
import com.aiart.platform.mapper.ArtworkTagMapper;
import com.aiart.platform.mapper.ContentAuditMapper;
import com.aiart.platform.mapper.ArtworkMapper;
import com.aiart.platform.mapper.TagCategoryMapper;
import com.aiart.platform.mapper.TagMapper;
import com.aiart.platform.service.ArtworkService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ArtworkServiceImpl implements ArtworkService {
    private final ArtworkMapper artworkMapper;
    private final ArtworkTagMapper artworkTagMapper;
    private final TagMapper tagMapper;
    private final TagCategoryMapper tagCategoryMapper;
    private final ContentAuditMapper contentAuditMapper;

    @Override
    public ArtworkDtos.ArtworkPage myArtworks(Long userId, int page, int size, String keyword, List<Long> tagIds, String visibility, String status) {
        List<Long> artworkIds = artworkIdsForTags(tagIds);
        if (hasTagFilter(tagIds) && artworkIds.isEmpty()) {
            return emptyPage(page, size);
        }
        var query = Wrappers.<Artwork>lambdaQuery()
                .eq(Artwork::getUserId, userId);
        applyKeyword(query, keyword);
        if (hasTagFilter(tagIds)) {
            query.in(Artwork::getId, artworkIds);
        }
        if (StringUtils.hasText(visibility)) {
            query.eq(Artwork::getVisibility, visibility.trim().toUpperCase());
        }
        if (StringUtils.hasText(status)) {
            query.eq(Artwork::getStatus, status.trim().toUpperCase());
        }
        Page<Artwork> result = artworkMapper.selectPage(page(page, size), query.orderByDesc(Artwork::getCreatedAt));
        return artworkPage(result);
    }

    @Override
    public ArtworkDtos.ArtworkPage publicArtworks(int page, int size, String keyword, List<Long> tagIds) {
        List<Long> artworkIds = artworkIdsForTags(tagIds);
        if (hasTagFilter(tagIds) && artworkIds.isEmpty()) {
            return emptyPage(page, size);
        }
        var query = Wrappers.<Artwork>lambdaQuery()
                .eq(Artwork::getVisibility, "PUBLIC")
                .eq(Artwork::getStatus, "ACTIVE");
        applyKeyword(query, keyword);
        if (hasTagFilter(tagIds)) {
            query.in(Artwork::getId, artworkIds);
        }
        Page<Artwork> result = artworkMapper.selectPage(page(page, size), query.orderByDesc(Artwork::getCreatedAt));
        return artworkPage(result);
    }

    @Override
    @Transactional
    public ArtworkDtos.ArtworkDetail requestPublish(Long userId, Long artworkId) {
        Artwork artwork = requireOwnedArtwork(userId, artworkId);
        if ("PUBLIC".equals(artwork.getVisibility()) && "ACTIVE".equals(artwork.getStatus())) {
            return detail(userId, artworkId);
        }
        Long pending = contentAuditMapper.selectCount(Wrappers.<ContentAudit>lambdaQuery()
                .eq(ContentAudit::getContentType, "ARTWORK")
                .eq(ContentAudit::getContentId, artworkId)
                .eq(ContentAudit::getStatus, "PENDING"));
        if (pending == 0) {
            ContentAudit audit = new ContentAudit();
            audit.setContentType("ARTWORK");
            audit.setContentId(artworkId);
            audit.setSubmitterId(userId);
            audit.setStatus("PENDING");
            audit.setCreatedAt(LocalDateTime.now());
            contentAuditMapper.insert(audit);
        }
        artwork.setVisibility("PRIVATE");
        artwork.setStatus("PENDING_AUDIT");
        artworkMapper.updateById(artwork);
        return detail(userId, artworkId);
    }

    @Override
    @Transactional
    public ArtworkDtos.ArtworkDetail update(Long userId, Long artworkId, ArtworkDtos.UpdateRequest request) {
        Artwork artwork = requireOwnedArtwork(userId, artworkId);
        if (StringUtils.hasText(request.title())) {
            artwork.setTitle(request.title().trim());
        }
        if (request.promptText() != null) {
            artwork.setPromptText(request.promptText().trim());
        }
        if (request.negativePrompt() != null) {
            artwork.setNegativePrompt(trimToNull(request.negativePrompt()));
        }
        if (StringUtils.hasText(request.visibility())) {
            String visibility = request.visibility().trim().toUpperCase();
            if (!List.of("PRIVATE", "PUBLIC").contains(visibility)) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "Unsupported visibility");
            }
            if ("PUBLIC".equals(visibility) && !"PUBLIC".equals(artwork.getVisibility())) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "Use publish request before making an artwork public");
            }
            artwork.setVisibility(visibility);
            if ("PRIVATE".equals(visibility) && "PENDING_AUDIT".equals(artwork.getStatus())) {
                artwork.setStatus("ACTIVE");
            }
        }
        artworkMapper.updateById(artwork);
        if (request.tagIds() != null) {
            replaceArtworkTags(artwork, validPublicTagIds(request.tagIds()));
        }
        return detail(userId, artworkId);
    }

    @Override
    @Transactional
    public ArtworkDtos.ArtworkDetail archive(Long userId, Long artworkId) {
        Artwork artwork = requireOwnedArtwork(userId, artworkId);
        artwork.setStatus("ARCHIVED");
        artwork.setVisibility("PRIVATE");
        artworkMapper.updateById(artwork);
        return detail(userId, artworkId);
    }

    @Override
    @Transactional
    public List<ArtworkDtos.ArtworkDetail> bulkRequestPublish(Long userId, ArtworkDtos.BulkRequest request) {
        return normalizeArtworkIds(request.artworkIds()).stream()
                .map(artworkId -> requestPublish(userId, artworkId))
                .toList();
    }

    @Override
    @Transactional
    public List<ArtworkDtos.ArtworkDetail> bulkUpdateVisibility(Long userId, ArtworkDtos.BulkVisibilityRequest request) {
        String visibility = request.visibility().trim().toUpperCase();
        if (!List.of("PRIVATE", "PUBLIC").contains(visibility)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Unsupported visibility");
        }
        return normalizeArtworkIds(request.artworkIds()).stream()
                .map(artworkId -> {
                    Artwork artwork = requireOwnedArtwork(userId, artworkId);
                    return update(userId, artworkId, new ArtworkDtos.UpdateRequest(
                            artwork.getTitle(),
                            artwork.getPromptText(),
                            artwork.getNegativePrompt(),
                            visibility,
                            null));
                })
                .toList();
    }

    @Override
    @Transactional
    public List<ArtworkDtos.ArtworkDetail> bulkArchive(Long userId, ArtworkDtos.BulkRequest request) {
        return normalizeArtworkIds(request.artworkIds()).stream()
                .map(artworkId -> archive(userId, artworkId))
                .toList();
    }

    @Override
    public ArtworkDtos.ArtworkDetail detail(Long userId, Long artworkId) {
        Artwork artwork = artworkMapper.selectById(artworkId);
        if (artwork == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Artwork not found");
        }
        boolean publicArtwork = "PUBLIC".equals(artwork.getVisibility()) && "ACTIVE".equals(artwork.getStatus());
        boolean owner = userId != null && userId.equals(artwork.getUserId());
        if (!publicArtwork && !owner) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Artwork not found");
        }
        return new ArtworkDtos.ArtworkDetail(
                artwork.getId(),
                artwork.getTitle(),
                artwork.getImageUrl(),
                artwork.getPromptText(),
                artwork.getNegativePrompt(),
                artwork.getGenerationParamsJson(),
                artwork.getVisibility(),
                artwork.getStatus(),
                artwork.getCreatedAt(),
                tagsByArtwork(List.of(artworkId)).getOrDefault(artworkId, List.of()));
    }

    private ArtworkDtos.ArtworkPage artworkPage(Page<Artwork> result) {
        return new ArtworkDtos.ArtworkPage(
                cards(result.getRecords()),
                result.getCurrent(),
                result.getSize(),
                result.getTotal(),
                result.getCurrent() * result.getSize() < result.getTotal());
    }

    private ArtworkDtos.ArtworkPage emptyPage(int page, int size) {
        Page<Artwork> empty = page(page, size);
        return new ArtworkDtos.ArtworkPage(List.of(), empty.getCurrent(), empty.getSize(), 0, false);
    }

    private Page<Artwork> page(int page, int size) {
        long current = Math.max(1, page);
        long limit = Math.min(Math.max(1, size), 60);
        return new Page<>(current, limit);
    }

    private void applyKeyword(com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Artwork> query, String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return;
        }
        String value = keyword.trim();
        query.and(wrapper -> wrapper
                .like(Artwork::getTitle, value)
                .or()
                .like(Artwork::getPromptText, value));
    }

    private Artwork requireOwnedArtwork(Long userId, Long artworkId) {
        Artwork artwork = artworkMapper.selectOne(Wrappers.<Artwork>lambdaQuery()
                .eq(Artwork::getId, artworkId)
                .eq(Artwork::getUserId, userId));
        if (artwork == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Artwork not found");
        }
        return artwork;
    }

    private List<Long> normalizeArtworkIds(List<Long> artworkIds) {
        if (artworkIds == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Artwork ids are required");
        }
        List<Long> cleanIds = artworkIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (cleanIds.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Artwork ids are required");
        }
        if (cleanIds.size() > 100) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "At most 100 artworks can be processed at once");
        }
        return cleanIds;
    }

    private List<Long> validPublicTagIds(List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return List.of();
        }
        List<Long> requested = tagIds.stream().distinct().toList();
        return tagMapper.selectList(Wrappers.<Tag>lambdaQuery()
                .in(Tag::getId, requested)
                .eq(Tag::getStatus, "ACTIVE")
                .eq(Tag::getVisibility, "PUBLIC"))
                .stream()
                .map(Tag::getId)
                .toList();
    }

    private void replaceArtworkTags(Artwork artwork, List<Long> tagIds) {
        artworkTagMapper.delete(Wrappers.<ArtworkTag>lambdaQuery().eq(ArtworkTag::getArtworkId, artwork.getId()));
        LocalDateTime now = LocalDateTime.now();
        for (Long tagId : tagIds) {
            ArtworkTag link = new ArtworkTag();
            link.setArtworkId(artwork.getId());
            link.setTagId(tagId);
            link.setUserId(artwork.getUserId());
            link.setCreatedAt(now);
            artworkTagMapper.insert(link);
        }
    }

    private List<ArtworkDtos.ArtworkCard> cards(List<Artwork> artworks) {
        Map<Long, List<ArtworkDtos.TagSummary>> tagMap = tagsByArtwork(artworks.stream().map(Artwork::getId).toList());
        return artworks.stream()
                .map(artwork -> card(artwork, tagMap.getOrDefault(artwork.getId(), List.of())))
                .toList();
    }

    private ArtworkDtos.ArtworkCard card(Artwork artwork, List<ArtworkDtos.TagSummary> tags) {
        return new ArtworkDtos.ArtworkCard(
                artwork.getId(),
                artwork.getTitle(),
                artwork.getImageUrl(),
                artwork.getPromptText(),
                artwork.getVisibility(),
                artwork.getStatus(),
                artwork.getCreatedAt(),
                tags);
    }

    private Map<Long, List<ArtworkDtos.TagSummary>> tagsByArtwork(List<Long> artworkIds) {
        if (artworkIds == null || artworkIds.isEmpty()) {
            return Map.of();
        }
        List<ArtworkTag> links = artworkTagMapper.selectList(Wrappers.<ArtworkTag>lambdaQuery()
                .in(ArtworkTag::getArtworkId, artworkIds)
                .orderByAsc(ArtworkTag::getCreatedAt));
        if (links.isEmpty()) {
            return Map.of();
        }
        List<Long> tagIds = links.stream().map(ArtworkTag::getTagId).distinct().toList();
        Map<Long, Tag> tags = tagMapper.selectBatchIds(tagIds).stream()
                .collect(Collectors.toMap(Tag::getId, tag -> tag));
        List<Long> categoryIds = tags.values().stream().map(Tag::getCategoryId).distinct().toList();
        Map<Long, String> categoryNames = categoryIds.isEmpty() ? Map.of() : tagCategoryMapper.selectBatchIds(categoryIds).stream()
                .collect(Collectors.toMap(TagCategory::getId, TagCategory::getName));
        Map<Long, List<ArtworkDtos.TagSummary>> result = new LinkedHashMap<>();
        for (ArtworkTag link : links) {
            Tag tag = tags.get(link.getTagId());
            if (tag == null) {
                continue;
            }
            result.computeIfAbsent(link.getArtworkId(), key -> new ArrayList<>())
                    .add(new ArtworkDtos.TagSummary(tag.getId(), tag.getName(), tag.getDisplayNameZh(), categoryNames.get(tag.getCategoryId())));
        }
        return result;
    }

    private List<Long> artworkIdsForTags(List<Long> tagIds) {
        if (!hasTagFilter(tagIds)) {
            return List.of();
        }
        List<Long> cleanTagIds = tagIds.stream().distinct().toList();
        Map<Long, Long> matchedCounts = artworkTagMapper.selectList(Wrappers.<ArtworkTag>lambdaQuery()
                .in(ArtworkTag::getTagId, cleanTagIds)
                .orderByDesc(ArtworkTag::getCreatedAt))
                .stream()
                .collect(Collectors.groupingBy(ArtworkTag::getArtworkId, LinkedHashMap::new, Collectors.mapping(ArtworkTag::getTagId, Collectors.collectingAndThen(Collectors.toSet(), tags -> (long) tags.size()))));
        return matchedCounts.entrySet().stream()
                .filter(entry -> entry.getValue() == cleanTagIds.size())
                .map(Map.Entry::getKey)
                .toList();
    }

    private boolean hasTagFilter(List<Long> tagIds) {
        return tagIds != null && !tagIds.isEmpty();
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
