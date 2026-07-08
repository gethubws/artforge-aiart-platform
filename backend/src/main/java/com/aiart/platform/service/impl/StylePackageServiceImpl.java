package com.aiart.platform.service.impl;

import com.aiart.platform.dto.StylePackageDtos;
import com.aiart.platform.entity.Artwork;
import com.aiart.platform.entity.StylePackageAccess;
import com.aiart.platform.entity.StylePackage;
import com.aiart.platform.entity.StylePackageReview;
import com.aiart.platform.entity.StylePackageSubmission;
import com.aiart.platform.entity.StylePackageVersion;
import com.aiart.platform.exception.BusinessException;
import com.aiart.platform.exception.ErrorCode;
import com.aiart.platform.mapper.ArtworkMapper;
import com.aiart.platform.mapper.StylePackageAccessMapper;
import com.aiart.platform.mapper.StylePackageMapper;
import com.aiart.platform.mapper.StylePackageReviewMapper;
import com.aiart.platform.mapper.StylePackageSubmissionMapper;
import com.aiart.platform.mapper.StylePackageVersionMapper;
import com.aiart.platform.service.PointService;
import com.aiart.platform.service.StylePackageService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StylePackageServiceImpl implements StylePackageService {
    private final StylePackageMapper stylePackageMapper;
    private final StylePackageAccessMapper stylePackageAccessMapper;
    private final StylePackageSubmissionMapper stylePackageSubmissionMapper;
    private final StylePackageVersionMapper stylePackageVersionMapper;
    private final StylePackageReviewMapper stylePackageReviewMapper;
    private final ArtworkMapper artworkMapper;
    private final PointService pointService;

    @Override
    @Transactional
    public StylePackageDtos.Detail create(Long userId, StylePackageDtos.SaveRequest request) {
        StylePackage stylePackage = new StylePackage();
        stylePackage.setUserId(userId);
        apply(stylePackage, request);
        stylePackage.setStatus("DRAFT");
        stylePackage.setCreatedAt(LocalDateTime.now());
        stylePackage.setUpdatedAt(LocalDateTime.now());
        stylePackageMapper.insert(stylePackage);
        snapshotVersion(stylePackage, "Initial version");
        return detailView(stylePackage, userId);
    }

    @Override
    @Transactional
    public StylePackageDtos.Detail update(Long userId, Long packageId, StylePackageDtos.SaveRequest request) {
        StylePackage stylePackage = requireOwned(userId, packageId);
        if ("ARCHIVED".equals(stylePackage.getStatus())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Archived style package cannot be edited");
        }
        apply(stylePackage, request);
        stylePackage.setUpdatedAt(LocalDateTime.now());
        stylePackageMapper.updateById(stylePackage);
        snapshotVersion(stylePackage, "Package updated");
        return detailView(stylePackage, userId);
    }

    @Override
    @Transactional
    public StylePackageDtos.Detail publish(Long userId, Long packageId) {
        StylePackage stylePackage = requireOwned(userId, packageId);
        stylePackage.setStatus("PUBLISHED");
        stylePackage.setUpdatedAt(LocalDateTime.now());
        stylePackageMapper.updateById(stylePackage);
        return detailView(stylePackage, userId);
    }

    @Override
    @Transactional
    public StylePackageDtos.Detail archive(Long userId, Long packageId) {
        StylePackage stylePackage = requireOwned(userId, packageId);
        stylePackage.setStatus("ARCHIVED");
        stylePackage.setUpdatedAt(LocalDateTime.now());
        stylePackageMapper.updateById(stylePackage);
        return detailView(stylePackage, userId);
    }

    @Override
    @Transactional
    public StylePackageDtos.Detail exchange(Long userId, Long packageId) {
        StylePackage stylePackage = stylePackageMapper.selectById(packageId);
        if (stylePackage == null || !"PUBLISHED".equals(stylePackage.getStatus())) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Style package not found");
        }
        if (stylePackage.getUserId().equals(userId) || hasAccess(userId, packageId)) {
            return detailView(stylePackage, userId);
        }
        BigDecimal price = stylePackage.getPricePoints() == null ? BigDecimal.ZERO : stylePackage.getPricePoints();
        if (price.compareTo(BigDecimal.ZERO) > 0) {
            pointService.spend(userId, price, "STYLE_PACKAGE_EXCHANGE", "STYLE_PACKAGE", packageId);
            pointService.earn(stylePackage.getUserId(), price, "STYLE_PACKAGE_SALE", "STYLE_PACKAGE", packageId);
        }
        StylePackageAccess access = new StylePackageAccess();
        access.setStylePackageId(packageId);
        access.setUserId(userId);
        access.setPaidPoints(price);
        access.setCreatedAt(LocalDateTime.now());
        stylePackageAccessMapper.insert(access);
        return detailView(stylePackage, userId);
    }

    @Override
    public StylePackageDtos.Detail detail(Long viewerId, Long packageId) {
        StylePackage stylePackage = stylePackageMapper.selectById(packageId);
        if (stylePackage == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Style package not found");
        }
        if (!"PUBLISHED".equals(stylePackage.getStatus()) && !stylePackage.getUserId().equals(viewerId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Style package not found");
        }
        return detailView(stylePackage, viewerId);
    }

    @Override
    public List<StylePackageDtos.Card> myPackages(Long userId, int page, int size) {
        Page<StylePackage> result = stylePackageMapper.selectPage(page(page, size), Wrappers.<StylePackage>lambdaQuery()
                .eq(StylePackage::getUserId, userId)
                .orderByDesc(StylePackage::getUpdatedAt));
        return result.getRecords().stream().map(stylePackage -> card(stylePackage, userId)).toList();
    }

    @Override
    public List<StylePackageDtos.Card> marketPackages(Long viewerId, int page, int size) {
        Page<StylePackage> result = stylePackageMapper.selectPage(page(page, size), Wrappers.<StylePackage>lambdaQuery()
                .eq(StylePackage::getStatus, "PUBLISHED")
                .orderByDesc(StylePackage::getUpdatedAt));
        return result.getRecords().stream().map(stylePackage -> card(stylePackage, viewerId)).toList();
    }

    @Override
    public List<StylePackageDtos.VersionView> versions(Long viewerId, Long packageId, int page, int size) {
        StylePackage stylePackage = readablePackage(viewerId, packageId);
        boolean owner = viewerId != null && stylePackage.getUserId().equals(viewerId);
        boolean accessible = owner || isFree(stylePackage) || hasAccess(viewerId, packageId);
        Page<StylePackageVersion> result = stylePackageVersionMapper.selectPage(page(page, size), Wrappers.<StylePackageVersion>lambdaQuery()
                .eq(StylePackageVersion::getStylePackageId, packageId)
                .orderByDesc(StylePackageVersion::getVersionNumber));
        return result.getRecords().stream()
                .map(version -> versionView(version, accessible))
                .toList();
    }

    @Override
    public List<StylePackageDtos.RatingView> reviews(Long viewerId, Long packageId, int page, int size) {
        readablePackage(viewerId, packageId);
        Page<StylePackageReview> result = stylePackageReviewMapper.selectPage(page(page, size), Wrappers.<StylePackageReview>lambdaQuery()
                .eq(StylePackageReview::getStylePackageId, packageId)
                .orderByDesc(StylePackageReview::getUpdatedAt));
        return result.getRecords().stream().map(this::ratingView).toList();
    }

    @Override
    @Transactional
    public StylePackageDtos.RatingView saveReview(Long userId, Long packageId, StylePackageDtos.RatingRequest request) {
        StylePackage stylePackage = stylePackageMapper.selectById(packageId);
        if (stylePackage == null || !"PUBLISHED".equals(stylePackage.getStatus())) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Style package not found");
        }
        if (stylePackage.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Owner cannot review their own style package");
        }
        if (!isFree(stylePackage) && !hasAccess(userId, packageId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "Style package access required");
        }
        StylePackageReview review = stylePackageReviewMapper.selectOne(Wrappers.<StylePackageReview>lambdaQuery()
                .eq(StylePackageReview::getStylePackageId, packageId)
                .eq(StylePackageReview::getReviewerId, userId));
        LocalDateTime now = LocalDateTime.now();
        if (review == null) {
            review = new StylePackageReview();
            review.setStylePackageId(packageId);
            review.setReviewerId(userId);
            review.setCreatedAt(now);
        }
        review.setRating(request.rating());
        review.setComment(trimToNull(request.comment()));
        review.setUpdatedAt(now);
        if (review.getId() == null) {
            stylePackageReviewMapper.insert(review);
        } else {
            stylePackageReviewMapper.updateById(review);
        }
        return ratingView(review);
    }

    @Override
    @Transactional
    public StylePackageDtos.SubmissionView submitArtwork(Long userId, Long packageId, StylePackageDtos.SubmitRequest request) {
        StylePackage stylePackage = stylePackageMapper.selectById(packageId);
        if (stylePackage == null || !"PUBLISHED".equals(stylePackage.getStatus())) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Style package not found");
        }
        if (!stylePackage.getUserId().equals(userId) && !isFree(stylePackage) && !hasAccess(userId, packageId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "Style package access required");
        }
        Artwork artwork = artworkMapper.selectOne(Wrappers.<Artwork>lambdaQuery()
                .eq(Artwork::getId, request.artworkId())
                .eq(Artwork::getUserId, userId)
                .in(Artwork::getStatus, List.of("ACTIVE", "PENDING_AUDIT")));
        if (artwork == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Artwork not found");
        }
        StylePackageSubmission submission = new StylePackageSubmission();
        submission.setStylePackageId(packageId);
        submission.setSubmitterId(userId);
        submission.setArtworkId(artwork.getId());
        submission.setNote(trimToNull(request.note()));
        submission.setStatus("PENDING");
        submission.setCreatedAt(LocalDateTime.now());
        stylePackageSubmissionMapper.insert(submission);
        return submissionView(submission);
    }

    @Override
    public List<StylePackageDtos.SubmissionView> mySubmissions(Long userId, int page, int size) {
        Page<StylePackageSubmission> result = stylePackageSubmissionMapper.selectPage(page(page, size), Wrappers.<StylePackageSubmission>lambdaQuery()
                .eq(StylePackageSubmission::getSubmitterId, userId)
                .orderByDesc(StylePackageSubmission::getCreatedAt));
        return result.getRecords().stream().map(this::submissionView).toList();
    }

    @Override
    public List<StylePackageDtos.SubmissionView> packageSubmissions(Long userId, Long packageId, int page, int size) {
        requireOwned(userId, packageId);
        Page<StylePackageSubmission> result = stylePackageSubmissionMapper.selectPage(page(page, size), Wrappers.<StylePackageSubmission>lambdaQuery()
                .eq(StylePackageSubmission::getStylePackageId, packageId)
                .orderByDesc(StylePackageSubmission::getCreatedAt));
        return result.getRecords().stream().map(this::submissionView).toList();
    }

    @Override
    public List<StylePackageDtos.SubmissionView> packageArtworks(Long viewerId, Long packageId, int page, int size) {
        StylePackage stylePackage = stylePackageMapper.selectById(packageId);
        if (stylePackage == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Style package not found");
        }
        if (!"PUBLISHED".equals(stylePackage.getStatus()) && !stylePackage.getUserId().equals(viewerId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Style package not found");
        }
        Page<StylePackageSubmission> result = stylePackageSubmissionMapper.selectPage(page(page, size), Wrappers.<StylePackageSubmission>lambdaQuery()
                .eq(StylePackageSubmission::getStylePackageId, packageId)
                .eq(StylePackageSubmission::getStatus, "APPROVED")
                .orderByDesc(StylePackageSubmission::getReviewedAt));
        return result.getRecords().stream().map(this::submissionView).toList();
    }

    @Override
    @Transactional
    public StylePackageDtos.SubmissionView reviewSubmission(Long userId, Long submissionId, StylePackageDtos.ReviewRequest request) {
        StylePackageSubmission submission = stylePackageSubmissionMapper.selectById(submissionId);
        if (submission == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Submission not found");
        }
        requireOwned(userId, submission.getStylePackageId());
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
        stylePackageSubmissionMapper.updateById(submission);
        return submissionView(submission);
    }

    private StylePackage requireOwned(Long userId, Long packageId) {
        StylePackage stylePackage = stylePackageMapper.selectOne(Wrappers.<StylePackage>lambdaQuery()
                .eq(StylePackage::getId, packageId)
                .eq(StylePackage::getUserId, userId));
        if (stylePackage == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Style package not found");
        }
        return stylePackage;
    }

    private StylePackage readablePackage(Long viewerId, Long packageId) {
        StylePackage stylePackage = stylePackageMapper.selectById(packageId);
        if (stylePackage == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Style package not found");
        }
        if (!"PUBLISHED".equals(stylePackage.getStatus()) && (viewerId == null || !stylePackage.getUserId().equals(viewerId))) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Style package not found");
        }
        return stylePackage;
    }

    private <T> Page<T> page(int page, int size) {
        return new Page<>(Math.max(1, page), Math.min(Math.max(1, size), 30));
    }

    private void apply(StylePackage stylePackage, StylePackageDtos.SaveRequest request) {
        stylePackage.setName(request.name().trim());
        stylePackage.setDescription(trimToNull(request.description()));
        stylePackage.setCoverImageUrl(trimToNull(request.coverImageUrl()));
        stylePackage.setPromptTemplate(request.promptTemplate().trim());
        stylePackage.setNegativePromptTemplate(trimToNull(request.negativePromptTemplate()));
        stylePackage.setPricePoints(request.pricePoints() == null ? BigDecimal.ZERO : request.pricePoints());
    }

    private StylePackageDtos.Card card(StylePackage stylePackage, Long viewerId) {
        boolean owner = viewerId != null && stylePackage.getUserId().equals(viewerId);
        boolean accessible = owner || isFree(stylePackage) || hasAccess(viewerId, stylePackage.getId());
        return new StylePackageDtos.Card(
                stylePackage.getId(),
                stylePackage.getName(),
                stylePackage.getDescription(),
                stylePackage.getCoverImageUrl(),
                accessible ? stylePackage.getPromptTemplate() : null,
                accessible ? stylePackage.getNegativePromptTemplate() : null,
                stylePackage.getPricePoints(),
                stylePackage.getStatus(),
                stylePackage.getUserId(),
                owner,
                accessible,
                stats(stylePackage.getId()),
                stylePackage.getCreatedAt(),
                stylePackage.getUpdatedAt());
    }

    private StylePackageDtos.Detail detailView(StylePackage stylePackage, Long viewerId) {
        boolean owner = viewerId != null && stylePackage.getUserId().equals(viewerId);
        boolean accessible = owner || isFree(stylePackage) || hasAccess(viewerId, stylePackage.getId());
        return new StylePackageDtos.Detail(
                stylePackage.getId(),
                stylePackage.getName(),
                stylePackage.getDescription(),
                stylePackage.getCoverImageUrl(),
                accessible ? stylePackage.getPromptTemplate() : null,
                accessible ? stylePackage.getNegativePromptTemplate() : null,
                stylePackage.getPricePoints(),
                stylePackage.getStatus(),
                stylePackage.getUserId(),
                owner,
                accessible,
                stats(stylePackage.getId()),
                stylePackage.getCreatedAt(),
                stylePackage.getUpdatedAt());
    }

    private void snapshotVersion(StylePackage stylePackage, String changeNote) {
        Long count = stylePackageVersionMapper.selectCount(Wrappers.<StylePackageVersion>lambdaQuery()
                .eq(StylePackageVersion::getStylePackageId, stylePackage.getId()));
        StylePackageVersion version = new StylePackageVersion();
        version.setStylePackageId(stylePackage.getId());
        version.setUserId(stylePackage.getUserId());
        version.setVersionNumber(count.intValue() + 1);
        version.setName(stylePackage.getName());
        version.setDescription(stylePackage.getDescription());
        version.setCoverImageUrl(stylePackage.getCoverImageUrl());
        version.setPromptTemplate(stylePackage.getPromptTemplate());
        version.setNegativePromptTemplate(stylePackage.getNegativePromptTemplate());
        version.setPricePoints(stylePackage.getPricePoints());
        version.setChangeNote(changeNote);
        version.setCreatedAt(LocalDateTime.now());
        stylePackageVersionMapper.insert(version);
    }

    private StylePackageDtos.Stats stats(Long packageId) {
        long accessCount = stylePackageAccessMapper.selectCount(Wrappers.<StylePackageAccess>lambdaQuery()
                .eq(StylePackageAccess::getStylePackageId, packageId));
        long submissionCount = stylePackageSubmissionMapper.selectCount(Wrappers.<StylePackageSubmission>lambdaQuery()
                .eq(StylePackageSubmission::getStylePackageId, packageId));
        long approvedArtworkCount = stylePackageSubmissionMapper.selectCount(Wrappers.<StylePackageSubmission>lambdaQuery()
                .eq(StylePackageSubmission::getStylePackageId, packageId)
                .eq(StylePackageSubmission::getStatus, "APPROVED"));
        long versionCount = stylePackageVersionMapper.selectCount(Wrappers.<StylePackageVersion>lambdaQuery()
                .eq(StylePackageVersion::getStylePackageId, packageId));
        List<StylePackageReview> reviews = stylePackageReviewMapper.selectList(Wrappers.<StylePackageReview>lambdaQuery()
                .eq(StylePackageReview::getStylePackageId, packageId));
        double averageRating = reviews.isEmpty() ? 0 : reviews.stream()
                .map(StylePackageReview::getRating)
                .filter(rating -> rating != null && rating > 0)
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0);
        double roundedRating = BigDecimal.valueOf(averageRating).setScale(1, RoundingMode.HALF_UP).doubleValue();
        return new StylePackageDtos.Stats(accessCount, submissionCount, approvedArtworkCount, versionCount, reviews.size(), roundedRating);
    }

    private StylePackageDtos.VersionView versionView(StylePackageVersion version, boolean accessible) {
        return new StylePackageDtos.VersionView(
                version.getId(),
                version.getStylePackageId(),
                version.getVersionNumber(),
                version.getName(),
                version.getDescription(),
                version.getCoverImageUrl(),
                accessible ? version.getPromptTemplate() : null,
                accessible ? version.getNegativePromptTemplate() : null,
                version.getPricePoints(),
                version.getChangeNote(),
                version.getCreatedAt());
    }

    private StylePackageDtos.RatingView ratingView(StylePackageReview review) {
        return new StylePackageDtos.RatingView(
                review.getId(),
                review.getStylePackageId(),
                review.getReviewerId(),
                review.getRating(),
                review.getComment(),
                review.getCreatedAt(),
                review.getUpdatedAt());
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private boolean isFree(StylePackage stylePackage) {
        return stylePackage.getPricePoints() == null || stylePackage.getPricePoints().compareTo(BigDecimal.ZERO) <= 0;
    }

    private boolean hasAccess(Long userId, Long packageId) {
        if (userId == null) {
            return false;
        }
        Long count = stylePackageAccessMapper.selectCount(Wrappers.<StylePackageAccess>lambdaQuery()
                .eq(StylePackageAccess::getStylePackageId, packageId)
                .eq(StylePackageAccess::getUserId, userId));
        return count > 0;
    }

    private StylePackageDtos.SubmissionView submissionView(StylePackageSubmission submission) {
        Artwork artwork = artworkMapper.selectById(submission.getArtworkId());
        return new StylePackageDtos.SubmissionView(
                submission.getId(),
                submission.getStylePackageId(),
                submission.getSubmitterId(),
                submission.getArtworkId(),
                artwork == null ? "Artwork" : artwork.getTitle(),
                artwork == null ? null : artwork.getImageUrl(),
                submission.getNote(),
                submission.getStatus(),
                submission.getReviewComment(),
                submission.getCreatedAt(),
                submission.getReviewedAt());
    }
}
