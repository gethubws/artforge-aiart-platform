package com.aiart.platform.service.impl;

import com.aiart.platform.dto.StylePackageDtos;
import com.aiart.platform.entity.Artwork;
import com.aiart.platform.entity.StylePackage;
import com.aiart.platform.entity.StylePackageAccess;
import com.aiart.platform.entity.StylePackageAsset;
import com.aiart.platform.entity.StylePackageCollaborator;
import com.aiart.platform.entity.StylePackageReview;
import com.aiart.platform.entity.StylePackageSubmission;
import com.aiart.platform.entity.StylePackageTag;
import com.aiart.platform.entity.StylePackageVersion;
import com.aiart.platform.entity.StylePackageVersionAsset;
import com.aiart.platform.entity.Tag;
import com.aiart.platform.entity.User;
import com.aiart.platform.exception.BusinessException;
import com.aiart.platform.exception.ErrorCode;
import com.aiart.platform.mapper.ArtworkMapper;
import com.aiart.platform.mapper.StylePackageAccessMapper;
import com.aiart.platform.mapper.StylePackageAssetMapper;
import com.aiart.platform.mapper.StylePackageCollaboratorMapper;
import com.aiart.platform.mapper.StylePackageMapper;
import com.aiart.platform.mapper.StylePackageReviewMapper;
import com.aiart.platform.mapper.StylePackageSubmissionMapper;
import com.aiart.platform.mapper.StylePackageTagMapper;
import com.aiart.platform.mapper.StylePackageVersionMapper;
import com.aiart.platform.mapper.StylePackageVersionAssetMapper;
import com.aiart.platform.mapper.TagMapper;
import com.aiart.platform.mapper.UserMapper;
import com.aiart.platform.service.PointService;
import com.aiart.platform.service.ResourceAssetStorageService;
import com.aiart.platform.service.StylePackageService;
import com.aiart.platform.service.UserEngagementService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class StylePackageServiceImpl implements StylePackageService {
    private static final List<String> REVIEW_STATUSES = List.of("APPROVED", "REJECTED");

    private final StylePackageMapper stylePackageMapper;
    private final StylePackageAccessMapper stylePackageAccessMapper;
    private final StylePackageAssetMapper stylePackageAssetMapper;
    private final StylePackageSubmissionMapper stylePackageSubmissionMapper;
    private final StylePackageVersionMapper stylePackageVersionMapper;
    private final StylePackageVersionAssetMapper stylePackageVersionAssetMapper;
    private final StylePackageReviewMapper stylePackageReviewMapper;
    private final StylePackageTagMapper stylePackageTagMapper;
    private final StylePackageCollaboratorMapper stylePackageCollaboratorMapper;
    private final ArtworkMapper artworkMapper;
    private final TagMapper tagMapper;
    private final UserMapper userMapper;
    private final PointService pointService;
    private final UserEngagementService userEngagementService;
    private final ResourceAssetStorageService resourceAssetStorageService;

    @Override
    @Transactional
    public StylePackageDtos.Detail create(Long userId, StylePackageDtos.SaveRequest request) {
        StylePackage stylePackage = new StylePackage();
        stylePackage.setUserId(userId);
        stylePackage.setStatus("DRAFT");
        stylePackage.setCreatedAt(LocalDateTime.now());
        stylePackage.setUpdatedAt(LocalDateTime.now());
        apply(stylePackage, request, userId);
        stylePackageMapper.insert(stylePackage);
        syncTags(stylePackage.getId(), request.tagIds(), request.tagNames());
        Set<Long> newCollaboratorIds = syncCollaborators(stylePackage.getId(), userId, request.collaborators());
        refreshArtworkCount(stylePackage);
        stylePackageMapper.updateById(stylePackage);
        snapshotVersion(stylePackage, "Initial version");
        notifyStyleWatchers(stylePackage, userId, newCollaboratorIds, "STYLE_COLLABORATOR_ADDED", "你被加入风格包协作",
                "你已加入风格包《" + stylePackage.getName() + "》的协作名单。");
        return detailView(stylePackage, userId);
    }

    @Override
    @Transactional
    public StylePackageDtos.Detail update(Long userId, Long packageId, StylePackageDtos.SaveRequest request) {
        StylePackage stylePackage = requireOwned(userId, packageId);
        if ("ARCHIVED".equals(stylePackage.getStatus())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Archived style package cannot be edited");
        }
        apply(stylePackage, request, userId);
        stylePackage.setUpdatedAt(LocalDateTime.now());
        stylePackageMapper.updateById(stylePackage);
        syncTags(stylePackage.getId(), request.tagIds(), request.tagNames());
        Set<Long> newCollaboratorIds = syncCollaborators(stylePackage.getId(), userId, request.collaborators());
        refreshArtworkCount(stylePackage);
        stylePackageMapper.updateById(stylePackage);
        snapshotVersion(stylePackage, "Package updated");
        notifyStyleWatchers(stylePackage, userId, newCollaboratorIds, "STYLE_COLLABORATOR_ADDED", "你被加入风格包协作",
                "你已加入风格包《" + stylePackage.getName() + "》的协作名单。");
        notifyStyleWatchers(stylePackage, userId, Set.of(), "STYLE_UPDATED", "风格包内容已更新",
                "你关注的风格包《" + stylePackage.getName() + "》有新的内容更新。");
        return detailView(stylePackage, userId);
    }

    @Override
    @Transactional
    public StylePackageDtos.Detail publish(Long userId, Long packageId) {
        StylePackage stylePackage = requireOwned(userId, packageId);
        stylePackage.setStatus("PUBLISHED");
        stylePackage.setUpdatedAt(LocalDateTime.now());
        refreshArtworkCount(stylePackage);
        stylePackageMapper.updateById(stylePackage);
        notifyStyleWatchers(stylePackage, userId, Set.of(), "STYLE_PUBLISHED", "风格包已发布",
                "风格包《" + stylePackage.getName() + "》已公开发布。");
        return detailView(stylePackage, userId);
    }

    @Override
    @Transactional
    public StylePackageDtos.Detail archive(Long userId, Long packageId) {
        StylePackage stylePackage = requireOwned(userId, packageId);
        stylePackage.setStatus("ARCHIVED");
        stylePackage.setUpdatedAt(LocalDateTime.now());
        stylePackageMapper.updateById(stylePackage);
        notifyStyleWatchers(stylePackage, userId, Set.of(), "STYLE_ARCHIVED", "风格包已归档",
                "你关注的风格包《" + stylePackage.getName() + "》已归档。");
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
        if (!"PUBLISHED".equals(stylePackage.getStatus()) && !Objects.equals(stylePackage.getUserId(), viewerId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Style package not found");
        }
        refreshArtworkCount(stylePackage);
        return detailView(stylePackage, viewerId);
    }

    @Override
    public StylePackageDtos.PackagePage myPackages(Long userId, StylePackageDtos.ListQuery query, int page, int size) {
        List<StylePackage> ownedPackages = stylePackageMapper.selectList(Wrappers.<StylePackage>lambdaQuery()
                .eq(StylePackage::getUserId, userId)
                .orderByDesc(StylePackage::getUpdatedAt));
        List<Long> collaborativePackageIds = stylePackageCollaboratorMapper.selectList(
                        Wrappers.<StylePackageCollaborator>lambdaQuery()
                                .eq(StylePackageCollaborator::getUserId, userId))
                .stream()
                .map(StylePackageCollaborator::getStylePackageId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, StylePackage> packageMap = new LinkedHashMap<>();
        ownedPackages.forEach(item -> packageMap.put(item.getId(), item));
        if (!collaborativePackageIds.isEmpty()) {
            stylePackageMapper.selectBatchIds(collaborativePackageIds)
                    .forEach(item -> packageMap.putIfAbsent(item.getId(), item));
        }
        List<StylePackage> packages = packageMap.values().stream()
                .sorted(Comparator.comparing(StylePackage::getUpdatedAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
        return paginateCards(packages.stream()
                .peek(this::refreshArtworkCount)
                .map(stylePackage -> card(stylePackage, userId))
                .toList(), query, page, size);
    }

    @Override
    public StylePackageDtos.PackagePage marketPackages(Long viewerId, StylePackageDtos.ListQuery query, int page, int size) {
        List<StylePackage> packages = stylePackageMapper.selectList(Wrappers.<StylePackage>lambdaQuery()
                .eq(StylePackage::getStatus, "PUBLISHED")
                .orderByDesc(StylePackage::getUpdatedAt));
        return paginateCards(packages.stream()
                .peek(this::refreshArtworkCount)
                .map(stylePackage -> card(stylePackage, viewerId))
                .toList(), query, page, size);
    }

    @Override
    public List<StylePackageDtos.VersionView> versions(Long viewerId, Long packageId, int page, int size) {
        StylePackage stylePackage = readablePackage(viewerId, packageId);
        boolean accessible = canEditPackage(viewerId, packageId) || isFree(stylePackage) || hasAccess(viewerId, packageId);
        Page<StylePackageVersion> result = stylePackageVersionMapper.selectPage(page(page, size), Wrappers.<StylePackageVersion>lambdaQuery()
                .eq(StylePackageVersion::getStylePackageId, packageId)
                .orderByDesc(StylePackageVersion::getVersionNumber));
        return result.getRecords().stream().map(version -> versionView(version, accessible)).toList();
    }

    @Override
    public List<StylePackageDtos.AssetView> assets(Long viewerId, Long packageId, String categoryKey) {
        StylePackage stylePackage = readablePackage(viewerId, packageId);
        boolean downloadable = canEditPackage(viewerId, packageId) || isFree(stylePackage) || hasAccess(viewerId, packageId);
        var query = Wrappers.<StylePackageAsset>lambdaQuery()
                .eq(StylePackageAsset::getStylePackageId, packageId)
                .eq(StylePackageAsset::getStatus, "ACTIVE")
                .orderByAsc(StylePackageAsset::getSortOrder)
                .orderByAsc(StylePackageAsset::getCategoryKey)
                .orderByAsc(StylePackageAsset::getName);
        if (StringUtils.hasText(categoryKey)) {
            query.eq(StylePackageAsset::getCategoryKey, categoryKey.trim());
        }
        return stylePackageAssetMapper.selectList(query).stream()
                .map(asset -> assetView(asset, downloadable))
                .toList();
    }

    @Override
    @Transactional
    public StylePackageDtos.AssetView createAsset(Long userId, Long packageId, StylePackageDtos.AssetSaveRequest request) {
        StylePackage stylePackage = requireEditable(userId, packageId);
        StylePackageAsset asset = newAsset(userId, packageId, request);
        stylePackageAssetMapper.insert(asset);
        touchResources(stylePackage, "Resource added: " + asset.getName());
        notifyStyleWatchers(stylePackage, userId, Set.of(), "STYLE_RESOURCE_ADDED", "风格资源包新增资源",
                "风格资源包《" + stylePackage.getName() + "》新增了资源《" + asset.getName() + "》。");
        return assetView(asset, true);
    }

    @Override
    @Transactional
    public List<StylePackageDtos.AssetView> createAssets(Long userId, Long packageId,
                                                         StylePackageDtos.AssetBatchRequest request) {
        StylePackage stylePackage = requireEditable(userId, packageId);
        List<StylePackageAsset> created = new ArrayList<>();
        Set<String> logicalKeys = new LinkedHashSet<>();
        for (StylePackageDtos.AssetSaveRequest item : request.assets()) {
            String logicalKey = StringUtils.hasText(item.logicalKey()) ? item.logicalKey().trim() : null;
            if (logicalKey != null && !logicalKeys.add(logicalKey)) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "Batch contains duplicate resource logical keys");
            }
            ensureLogicalKeyAvailable(packageId, logicalKey);
            StylePackageAsset asset = newAsset(userId, packageId, item);
            stylePackageAssetMapper.insert(asset);
            created.add(asset);
        }
        touchResources(stylePackage, "Batch resources added: " + created.size());
        notifyStyleWatchers(stylePackage, userId, Set.of(), "STYLE_RESOURCE_BATCH_ADDED", "风格资源包已导入资源",
                "风格资源包《" + stylePackage.getName() + "》批量导入了 " + created.size() + " 项资源。");
        return created.stream().map(asset -> assetView(asset, true)).toList();
    }

    @Override
    @Transactional
    public StylePackageDtos.AssetView updateAsset(Long userId, Long packageId, Long assetId,
                                                   StylePackageDtos.AssetSaveRequest request) {
        StylePackage stylePackage = requireEditable(userId, packageId);
        StylePackageAsset previous = requireActiveAsset(packageId, assetId);
        previous.setStatus("SUPERSEDED");
        previous.setUpdatedAt(LocalDateTime.now());
        stylePackageAssetMapper.updateById(previous);

        StylePackageAsset revision = new StylePackageAsset();
        revision.setStylePackageId(packageId);
        revision.setContributorId(userId);
        revision.setLogicalKey(previous.getLogicalKey());
        revision.setRevisionNumber(previous.getRevisionNumber() + 1);
        applyAsset(revision, request);
        revision.setStatus("ACTIVE");
        revision.setCreatedAt(LocalDateTime.now());
        revision.setUpdatedAt(LocalDateTime.now());
        stylePackageAssetMapper.insert(revision);
        touchResources(stylePackage, "Resource updated: " + revision.getName());
        notifyStyleWatchers(stylePackage, userId, Set.of(), "STYLE_RESOURCE_UPDATED", "风格资源包资源已更新",
                "风格资源包《" + stylePackage.getName() + "》更新了资源《" + revision.getName() + "》。");
        return assetView(revision, true);
    }

    @Override
    @Transactional
    public StylePackageDtos.AssetView archiveAsset(Long userId, Long packageId, Long assetId) {
        StylePackage stylePackage = requireEditable(userId, packageId);
        StylePackageAsset asset = requireActiveAsset(packageId, assetId);
        asset.setStatus("ARCHIVED");
        asset.setUpdatedAt(LocalDateTime.now());
        stylePackageAssetMapper.updateById(asset);
        touchResources(stylePackage, "Resource archived: " + asset.getName());
        notifyStyleWatchers(stylePackage, userId, Set.of(), "STYLE_RESOURCE_ARCHIVED", "风格资源包资源已归档",
                "风格资源包《" + stylePackage.getName() + "》归档了资源《" + asset.getName() + "》。");
        return assetView(asset, true);
    }

    @Override
    public StylePackageDtos.AssetUploadView uploadAssetFile(Long userId, Long packageId, MultipartFile file) {
        requireEditable(userId, packageId);
        ResourceAssetStorageService.UploadedAsset uploaded = resourceAssetStorageService.save(file);
        return new StylePackageDtos.AssetUploadView(
                uploaded.reference(),
                uploaded.originalFilename(),
                uploaded.contentType(),
                uploaded.size());
    }

    @Override
    public StylePackageDtos.AssetDownload assetDownload(Long userId, Long packageId, Long assetId) {
        StylePackage stylePackage = readablePackage(userId, packageId);
        boolean downloadable = canEditPackage(userId, packageId) || isFree(stylePackage) || hasAccess(userId, packageId);
        if (!downloadable) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "Purchase this resource package before downloading files");
        }
        StylePackageAsset asset = requireActiveAsset(packageId, assetId);
        if (!StringUtils.hasText(asset.getFileUrl())) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "This resource does not have a downloadable file");
        }
        String format = StringUtils.hasText(asset.getFileFormat()) ? asset.getFileFormat().toLowerCase() : "bin";
        return new StylePackageDtos.AssetDownload(
                asset.getFileUrl(),
                asset.getName() + "." + format,
                contentTypeFor(format));
    }

    @Override
    public StylePackageDtos.AssetManifest manifest(Long userId, Long packageId) {
        StylePackage stylePackage = readablePackage(userId, packageId);
        boolean accessible = canEditPackage(userId, packageId) || isFree(stylePackage) || hasAccess(userId, packageId);
        if (!accessible) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "Style package access required");
        }
        StylePackageVersion version = stylePackageVersionMapper.selectOne(Wrappers.<StylePackageVersion>lambdaQuery()
                .eq(StylePackageVersion::getStylePackageId, packageId)
                .orderByDesc(StylePackageVersion::getVersionNumber)
                .last("LIMIT 1"));
        List<StylePackageAsset> resources = version == null
                ? activeAssets(packageId)
                : versionAssets(version.getId());
        return new StylePackageDtos.AssetManifest(
                packageId,
                version == null ? null : version.getId(),
                version == null ? null : version.getVersionNumber(),
                stylePackage.getName(),
                stylePackage.getLicenseType(),
                stylePackage.getLicenseSummary(),
                Boolean.TRUE.equals(stylePackage.getCommercialUse()),
                resources.size(),
                (int) resources.stream().map(StylePackageAsset::getCategoryKey).filter(Objects::nonNull).distinct().count(),
                resources.stream().map(asset -> assetView(asset, true)).toList());
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
        notifyStyleWatchers(stylePackage, userId, Set.of(stylePackage.getUserId()), "STYLE_REVIEW_CREATED", "风格包收到新评价",
                "风格包《" + stylePackage.getName() + "》收到了一条新的评分与评价。");
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
        StylePackageSubmission existing = stylePackageSubmissionMapper.selectOne(Wrappers.<StylePackageSubmission>lambdaQuery()
                .eq(StylePackageSubmission::getStylePackageId, packageId)
                .eq(StylePackageSubmission::getArtworkId, artwork.getId())
                .eq(StylePackageSubmission::getSubmitterId, userId)
                .in(StylePackageSubmission::getStatus, List.of("PENDING", "APPROVED")));
        if (existing != null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "This artwork has already been submitted to the style package");
        }
        StylePackageSubmission submission = new StylePackageSubmission();
        submission.setStylePackageId(packageId);
        submission.setSubmitterId(userId);
        submission.setArtworkId(artwork.getId());
        submission.setNote(trimToNull(request.note()));
        submission.setStatus("PENDING");
        submission.setCreatedAt(LocalDateTime.now());
        stylePackageSubmissionMapper.insert(submission);
        notifyStyleWatchers(stylePackage, userId, Set.of(stylePackage.getUserId()), "STYLE_SUBMISSION_CREATED", "风格包收到新投稿",
                "作品《" + artwork.getTitle() + "》已投稿到风格包《" + stylePackage.getName() + "》。");
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
        StylePackage stylePackage = readablePackage(viewerId, packageId);
        Page<StylePackageSubmission> result = stylePackageSubmissionMapper.selectPage(page(page, size), Wrappers.<StylePackageSubmission>lambdaQuery()
                .eq(StylePackageSubmission::getStylePackageId, packageId)
                .eq(StylePackageSubmission::getStatus, "APPROVED")
                .orderByDesc(StylePackageSubmission::getReviewedAt));
        if (viewerId != null && Objects.equals(stylePackage.getUserId(), viewerId)) {
            refreshArtworkCount(stylePackage);
        }
        return result.getRecords().stream().map(this::submissionView).toList();
    }

    @Override
    @Transactional
    public StylePackageDtos.SubmissionView reviewSubmission(Long userId, Long submissionId, StylePackageDtos.ReviewRequest request) {
        StylePackageSubmission submission = stylePackageSubmissionMapper.selectById(submissionId);
        if (submission == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Submission not found");
        }
        StylePackage stylePackage = requireOwned(userId, submission.getStylePackageId());
        if (!"PENDING".equals(submission.getStatus())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Submission has already been reviewed");
        }
        String status = request.status().trim().toUpperCase();
        if (!REVIEW_STATUSES.contains(status)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Review status must be APPROVED or REJECTED");
        }
        submission.setStatus(status);
        submission.setReviewComment(trimToNull(request.comment()));
        submission.setReviewedAt(LocalDateTime.now());
        stylePackageSubmissionMapper.updateById(submission);
        if ("APPROVED".equals(status)) {
            refreshArtworkCount(stylePackage);
            stylePackage.setUpdatedAt(LocalDateTime.now());
            if (stylePackage.getFeaturedArtworkId() == null) {
                stylePackage.setFeaturedArtworkId(submission.getArtworkId());
            }
            stylePackageMapper.updateById(stylePackage);
            snapshotVersion(stylePackage, "Artwork submission approved");
        }
        String reviewLabel = "APPROVED".equals(status) ? "已通过" : "已拒绝";
        notifyStyleWatchers(stylePackage, userId, Set.of(submission.getSubmitterId()), "STYLE_SUBMISSION_REVIEWED", "风格包投稿已审核",
                "你投向风格包《" + stylePackage.getName() + "》的作品已" + reviewLabel + "。");
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

    private StylePackage requireEditable(Long userId, Long packageId) {
        StylePackage stylePackage = stylePackageMapper.selectById(packageId);
        if (stylePackage == null || !canEditPackage(userId, packageId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Style package not found");
        }
        if ("ARCHIVED".equals(stylePackage.getStatus())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Archived style package cannot be edited");
        }
        return stylePackage;
    }

    private StylePackage readablePackage(Long viewerId, Long packageId) {
        StylePackage stylePackage = stylePackageMapper.selectById(packageId);
        if (stylePackage == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Style package not found");
        }
        if (!"PUBLISHED".equals(stylePackage.getStatus()) && !canEditPackage(viewerId, packageId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Style package not found");
        }
        return stylePackage;
    }

    private <T> Page<T> page(int page, int size) {
        return new Page<>(Math.max(1, page), Math.min(Math.max(1, size), 30));
    }

    private void apply(StylePackage stylePackage, StylePackageDtos.SaveRequest request, Long ownerUserId) {
        stylePackage.setName(request.name().trim());
        stylePackage.setDescription(trimToNull(request.description()));
        stylePackage.setCoverImageUrl(trimToNull(request.coverImageUrl()));
        stylePackage.setStyleStatement(trimToNull(request.styleStatement()));
        stylePackage.setPromptGuide(trimToNull(request.promptGuide()));
        stylePackage.setNegativePromptGuide(trimToNull(request.negativePromptGuide()));
        stylePackage.setLicenseType(StringUtils.hasText(request.licenseType()) ? request.licenseType().trim() : "STANDARD");
        stylePackage.setLicenseSummary(trimToNull(request.licenseSummary()));
        stylePackage.setCommercialUse(request.commercialUse() == null || request.commercialUse());
        stylePackage.setFeaturedArtworkId(resolveFeaturedArtworkId(ownerUserId, request.featuredArtworkId()));
        stylePackage.setPricePoints(request.pricePoints() == null ? BigDecimal.ZERO : request.pricePoints());
    }

    private void applyAsset(StylePackageAsset asset, StylePackageDtos.AssetSaveRequest request) {
        String assetType = StringUtils.hasText(request.assetType()) ? request.assetType().trim().toUpperCase() : "IMAGE";
        if (!"IMAGE".equals(assetType)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Only image resources are supported in this version");
        }
        asset.setName(request.name().trim());
        asset.setCategoryKey(request.categoryKey().trim());
        asset.setAssetType(assetType);
        asset.setDescription(trimToNull(request.description()));
        asset.setPreviewImageUrl(request.previewImageUrl().trim());
        asset.setFileUrl(trimToNull(request.fileUrl()));
        asset.setThumbnailUrl(trimToNull(request.thumbnailUrl()));
        asset.setPromptText(trimToNull(request.promptText()));
        asset.setNegativePromptText(trimToNull(request.negativePromptText()));
        asset.setGenerationParamsJson(trimToNull(request.generationParamsJson()));
        asset.setWidth(request.width());
        asset.setHeight(request.height());
        asset.setFileFormat(StringUtils.hasText(request.fileFormat()) ? request.fileFormat().trim().toUpperCase() : "PNG");
        asset.setBackgroundMode(StringUtils.hasText(request.backgroundMode()) ? request.backgroundMode().trim().toUpperCase() : "SOLID");
        asset.setLicenseScope(StringUtils.hasText(request.licenseScope()) ? request.licenseScope().trim().toUpperCase() : "PACKAGE");
        asset.setSortOrder(request.sortOrder() == null ? 0 : request.sortOrder());
    }

    private StylePackageAsset newAsset(Long userId, Long packageId, StylePackageDtos.AssetSaveRequest request) {
        String logicalKey = StringUtils.hasText(request.logicalKey()) ? request.logicalKey().trim() : null;
        ensureLogicalKeyAvailable(packageId, logicalKey);
        StylePackageAsset asset = new StylePackageAsset();
        asset.setStylePackageId(packageId);
        asset.setContributorId(userId);
        asset.setLogicalKey(logicalKey == null ? UUID.randomUUID().toString().replace("-", "") : logicalKey);
        asset.setRevisionNumber(1);
        applyAsset(asset, request);
        asset.setStatus("ACTIVE");
        asset.setCreatedAt(LocalDateTime.now());
        asset.setUpdatedAt(LocalDateTime.now());
        return asset;
    }

    private void ensureLogicalKeyAvailable(Long packageId, String logicalKey) {
        if (!StringUtils.hasText(logicalKey)) {
            return;
        }
        if (stylePackageAssetMapper.selectCount(Wrappers.<StylePackageAsset>lambdaQuery()
                .eq(StylePackageAsset::getStylePackageId, packageId)
                .eq(StylePackageAsset::getLogicalKey, logicalKey)
                .eq(StylePackageAsset::getStatus, "ACTIVE")) > 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST,
                    "Resource logical key already exists; use the revision update endpoint");
        }
    }

    private Long resolveFeaturedArtworkId(Long ownerUserId, Long artworkId) {
        if (artworkId == null) {
            return null;
        }
        Artwork artwork = artworkMapper.selectOne(Wrappers.<Artwork>lambdaQuery()
                .eq(Artwork::getId, artworkId)
                .eq(Artwork::getUserId, ownerUserId));
        if (artwork == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Featured artwork must belong to the style package owner");
        }
        return artworkId;
    }

    private void syncTags(Long packageId, List<Long> tagIds, List<String> tagNames) {
        stylePackageTagMapper.delete(Wrappers.<StylePackageTag>lambdaQuery()
                .eq(StylePackageTag::getStylePackageId, packageId));
        Set<Long> ids = sanitizeIds(tagIds);
        if (ids.isEmpty() && tagNames != null && !tagNames.isEmpty()) {
            List<String> names = tagNames.stream()
                    .filter(StringUtils::hasText)
                    .map(String::trim)
                    .distinct()
                    .toList();
            if (!names.isEmpty()) {
                List<Tag> matchedTags = tagMapper.selectList(Wrappers.<Tag>lambdaQuery()
                        .and(wrapper -> wrapper.in(Tag::getName, names).or().in(Tag::getDisplayNameZh, names)));
                java.util.Map<String, Long> matchedNameMap = new java.util.LinkedHashMap<>();
                for (Tag matchedTag : matchedTags) {
                    if (StringUtils.hasText(matchedTag.getName())) {
                        matchedNameMap.put(matchedTag.getName().trim(), matchedTag.getId());
                    }
                    if (StringUtils.hasText(matchedTag.getDisplayNameZh())) {
                        matchedNameMap.put(matchedTag.getDisplayNameZh().trim(), matchedTag.getId());
                    }
                }
                for (String name : names) {
                    if (!matchedNameMap.containsKey(name)) {
                        throw new BusinessException(ErrorCode.BAD_REQUEST, "Some style tag names do not exist");
                    }
                    ids.add(matchedNameMap.get(name));
                }
            }
        }
        if (ids.isEmpty()) {
            return;
        }
        List<Tag> tags = tagMapper.selectBatchIds(ids);
        if (tags.size() != ids.size()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Some style tags do not exist");
        }
        LocalDateTime now = LocalDateTime.now();
        for (Long tagId : ids) {
            StylePackageTag relation = new StylePackageTag();
            relation.setStylePackageId(packageId);
            relation.setTagId(tagId);
            relation.setCreatedAt(now);
            stylePackageTagMapper.insert(relation);
        }
    }

    private Set<Long> syncCollaborators(Long packageId, Long ownerUserId, List<StylePackageDtos.CollaboratorInput> collaborators) {
        List<StylePackageCollaborator> existingRelations = stylePackageCollaboratorMapper.selectList(
                Wrappers.<StylePackageCollaborator>lambdaQuery()
                        .eq(StylePackageCollaborator::getStylePackageId, packageId));
        Set<Long> existingUserIds = existingRelations.stream()
                .map(StylePackageCollaborator::getUserId)
                .filter(Objects::nonNull)
                .collect(LinkedHashSet::new, Set::add, Set::addAll);
        stylePackageCollaboratorMapper.delete(Wrappers.<StylePackageCollaborator>lambdaQuery()
                .eq(StylePackageCollaborator::getStylePackageId, packageId));
        List<StylePackageDtos.CollaboratorInput> sanitized = collaborators == null ? List.of() : collaborators.stream()
                .filter(Objects::nonNull)
                .filter(item -> item.userId() != null)
                .filter(item -> !Objects.equals(item.userId(), ownerUserId))
                .toList();
        if (sanitized.isEmpty()) {
            return Set.of();
        }
        Set<Long> userIds = new LinkedHashSet<>();
        for (StylePackageDtos.CollaboratorInput item : sanitized) {
            userIds.add(item.userId());
        }
        List<User> users = userMapper.selectBatchIds(userIds);
        if (users.size() != userIds.size()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Some collaborators do not exist");
        }
        LocalDateTime now = LocalDateTime.now();
        for (StylePackageDtos.CollaboratorInput item : sanitized) {
            StylePackageCollaborator collaborator = new StylePackageCollaborator();
            collaborator.setStylePackageId(packageId);
            collaborator.setUserId(item.userId());
            collaborator.setRole(StringUtils.hasText(item.role()) ? item.role().trim() : "CONTRIBUTOR");
            collaborator.setCreatedAt(now);
            stylePackageCollaboratorMapper.insert(collaborator);
        }
        Set<Long> newUserIds = new LinkedHashSet<>(userIds);
        newUserIds.removeAll(existingUserIds);
        return newUserIds;
    }

    private void notifyStyleWatchers(StylePackage stylePackage, Long actorUserId, Set<Long> extraUserIds, String type,
                                     String title, String content) {
        Set<Long> userIds = new LinkedHashSet<>();
        if (extraUserIds != null) {
            userIds.addAll(extraUserIds);
        }
        userIds.addAll(collaboratorUserIds(stylePackage.getId()));
        userIds.addAll(userEngagementService.subscriptionUserIds("STYLE_PACKAGE", stylePackage.getId()));
        userEngagementService.notifyUsers(userIds, actorUserId, type, title, content, "STYLE_PACKAGE", stylePackage.getId());
    }

    private Set<Long> collaboratorUserIds(Long packageId) {
        return stylePackageCollaboratorMapper.selectList(Wrappers.<StylePackageCollaborator>lambdaQuery()
                        .eq(StylePackageCollaborator::getStylePackageId, packageId))
                .stream()
                .map(StylePackageCollaborator::getUserId)
                .filter(Objects::nonNull)
                .collect(LinkedHashSet::new, Set::add, Set::addAll);
    }

    private Set<Long> sanitizeIds(List<Long> rawIds) {
        Set<Long> result = new LinkedHashSet<>();
        if (rawIds == null) {
            return result;
        }
        for (Long value : rawIds) {
            if (value != null) {
                result.add(value);
            }
        }
        return result;
    }

    private StylePackageDtos.PackagePage paginateCards(List<StylePackageDtos.Card> cards, StylePackageDtos.ListQuery query, int page, int size) {
        List<StylePackageDtos.Card> filtered = filterCards(cards, query);
        sortCards(filtered, query == null ? null : query.sort());
        int safePage = Math.max(1, page);
        int safeSize = Math.min(Math.max(1, size), 60);
        int fromIndex = Math.min((safePage - 1) * safeSize, filtered.size());
        int toIndex = Math.min(fromIndex + safeSize, filtered.size());
        return new StylePackageDtos.PackagePage(
                new ArrayList<>(filtered.subList(fromIndex, toIndex)),
                safePage,
                safeSize,
                filtered.size(),
                toIndex < filtered.size());
    }

    private List<StylePackageDtos.Card> filterCards(List<StylePackageDtos.Card> cards, StylePackageDtos.ListQuery query) {
        if (query == null) {
            return new ArrayList<>(cards);
        }
        String keyword = lowerTrim(query.keyword());
        String status = upperTrim(query.status());
        Long tagId = query.tagId();
        return cards.stream()
                .filter(card -> !StringUtils.hasText(status) || status.equalsIgnoreCase(card.status()))
                .filter(card -> tagId == null || card.tags().stream().anyMatch(tag -> Objects.equals(tag.id(), tagId)))
                .filter(card -> {
                    if (!StringUtils.hasText(keyword)) {
                        return true;
                    }
                    String tagText = card.tags().stream()
                            .map(tag -> String.join(" ", safe(tag.displayNameZh()), safe(tag.name())))
                            .reduce("", (left, right) -> left + " " + right);
                    String assetText = card.assetPreviews().stream()
                            .map(asset -> String.join(" ", safe(asset.name()), safe(asset.categoryKey())))
                            .reduce("", (left, right) -> left + " " + right);
                    String text = String.join(" ",
                            safe(card.name()),
                            safe(card.description()),
                            safe(card.styleStatement()),
                            tagText,
                            assetText).toLowerCase();
                    return text.contains(keyword);
                })
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
    }

    private void sortCards(List<StylePackageDtos.Card> cards, String sort) {
        String sortKey = lowerTrim(sort);
        Comparator<StylePackageDtos.Card> comparator;
        if ("resources".equals(sortKey)) {
            comparator = Comparator.comparingLong((StylePackageDtos.Card item) -> item.stats().resourceCount()).reversed();
        } else if ("artworks".equals(sortKey)) {
            comparator = Comparator.comparingLong((StylePackageDtos.Card item) -> item.stats().approvedArtworkCount()).reversed();
        } else if ("rating".equals(sortKey)) {
            comparator = Comparator.comparingDouble((StylePackageDtos.Card item) -> item.stats().averageRating()).reversed();
        } else if ("collaborators".equals(sortKey)) {
            comparator = Comparator.comparingLong((StylePackageDtos.Card item) -> item.stats().collaboratorCount()).reversed();
        } else if ("price".equals(sortKey)) {
            comparator = Comparator.comparing((StylePackageDtos.Card item) -> item.pricePoints() == null ? BigDecimal.ZERO : item.pricePoints()).reversed();
        } else {
            comparator = Comparator.comparing(StylePackageDtos.Card::updatedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed();
        }
        cards.sort(comparator.thenComparing(StylePackageDtos.Card::updatedAt, Comparator.nullsLast(Comparator.reverseOrder())));
    }

    private String lowerTrim(String value) {
        return StringUtils.hasText(value) ? value.trim().toLowerCase() : "";
    }

    private String upperTrim(String value) {
        return StringUtils.hasText(value) ? value.trim().toUpperCase() : "";
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private StylePackageDtos.Card card(StylePackage stylePackage, Long viewerId) {
        boolean owner = viewerId != null && stylePackage.getUserId().equals(viewerId);
        boolean editable = canEditPackage(viewerId, stylePackage.getId());
        boolean accessible = editable || isFree(stylePackage) || hasAccess(viewerId, stylePackage.getId());
        return new StylePackageDtos.Card(
                stylePackage.getId(),
                stylePackage.getName(),
                stylePackage.getDescription(),
                stylePackage.getCoverImageUrl(),
                stylePackage.getStyleStatement(),
                accessible ? stylePackage.getPromptGuide() : null,
                accessible ? stylePackage.getNegativePromptGuide() : null,
                stylePackage.getLicenseType(),
                stylePackage.getLicenseSummary(),
                Boolean.TRUE.equals(stylePackage.getCommercialUse()),
                stylePackage.getFeaturedArtworkId(),
                stylePackage.getPricePoints(),
                stylePackage.getStatus(),
                stylePackage.getUserId(),
                owner,
                editable,
                accessible,
                stats(stylePackage.getId()),
                tags(stylePackage.getId()),
                assetPreviews(stylePackage.getId(), 5),
                approvedArtworkSummaries(stylePackage.getId(), 4),
                collaborators(stylePackage.getId()),
                stylePackage.getCreatedAt(),
                stylePackage.getUpdatedAt());
    }

    private StylePackageDtos.Detail detailView(StylePackage stylePackage, Long viewerId) {
        boolean owner = viewerId != null && stylePackage.getUserId().equals(viewerId);
        boolean editable = canEditPackage(viewerId, stylePackage.getId());
        boolean accessible = editable || isFree(stylePackage) || hasAccess(viewerId, stylePackage.getId());
        return new StylePackageDtos.Detail(
                stylePackage.getId(),
                stylePackage.getName(),
                stylePackage.getDescription(),
                stylePackage.getCoverImageUrl(),
                stylePackage.getStyleStatement(),
                accessible ? stylePackage.getPromptGuide() : null,
                accessible ? stylePackage.getNegativePromptGuide() : null,
                stylePackage.getLicenseType(),
                stylePackage.getLicenseSummary(),
                Boolean.TRUE.equals(stylePackage.getCommercialUse()),
                stylePackage.getFeaturedArtworkId(),
                stylePackage.getPricePoints(),
                stylePackage.getStatus(),
                stylePackage.getUserId(),
                owner,
                editable,
                accessible,
                stats(stylePackage.getId()),
                tags(stylePackage.getId()),
                assetPreviews(stylePackage.getId(), 12),
                approvedArtworkSummaries(stylePackage.getId(), 12),
                collaborators(stylePackage.getId()),
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
        version.setStyleStatement(stylePackage.getStyleStatement());
        version.setPromptGuide(stylePackage.getPromptGuide());
        version.setNegativePromptGuide(stylePackage.getNegativePromptGuide());
        version.setLicenseType(stylePackage.getLicenseType());
        version.setLicenseSummary(stylePackage.getLicenseSummary());
        version.setCommercialUse(stylePackage.getCommercialUse());
        version.setFeaturedArtworkId(stylePackage.getFeaturedArtworkId());
        version.setArtworkCount(stylePackage.getArtworkCount() == null ? 0 : stylePackage.getArtworkCount());
        version.setResourceCount(stylePackage.getResourceCount() == null ? 0 : stylePackage.getResourceCount());
        version.setCategoryCount(stylePackage.getCategoryCount() == null ? 0 : stylePackage.getCategoryCount());
        version.setPricePoints(stylePackage.getPricePoints());
        version.setChangeNote(changeNote);
        version.setCreatedAt(LocalDateTime.now());
        stylePackageVersionMapper.insert(version);
        for (StylePackageAsset asset : activeAssets(stylePackage.getId())) {
            StylePackageVersionAsset relation = new StylePackageVersionAsset();
            relation.setStylePackageVersionId(version.getId());
            relation.setStylePackageAssetId(asset.getId());
            relation.setCreatedAt(LocalDateTime.now());
            stylePackageVersionAssetMapper.insert(relation);
        }
    }

    private void refreshArtworkCount(StylePackage stylePackage) {
        long approvedArtworkCount = stylePackageSubmissionMapper.selectCount(Wrappers.<StylePackageSubmission>lambdaQuery()
                .eq(StylePackageSubmission::getStylePackageId, stylePackage.getId())
                .eq(StylePackageSubmission::getStatus, "APPROVED"));
        int newCount = Math.toIntExact(approvedArtworkCount);
        if (!Objects.equals(stylePackage.getArtworkCount(), newCount)) {
            stylePackage.setArtworkCount(newCount);
        }
    }

    private void refreshResourceCounts(StylePackage stylePackage) {
        List<StylePackageAsset> assets = activeAssets(stylePackage.getId());
        stylePackage.setResourceCount(assets.size());
        stylePackage.setCategoryCount((int) assets.stream()
                .map(StylePackageAsset::getCategoryKey)
                .filter(StringUtils::hasText)
                .distinct()
                .count());
    }

    private void touchResources(StylePackage stylePackage, String changeNote) {
        refreshResourceCounts(stylePackage);
        stylePackage.setUpdatedAt(LocalDateTime.now());
        stylePackageMapper.updateById(stylePackage);
        snapshotVersion(stylePackage, changeNote);
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
        long collaboratorCount = stylePackageCollaboratorMapper.selectCount(Wrappers.<StylePackageCollaborator>lambdaQuery()
                .eq(StylePackageCollaborator::getStylePackageId, packageId));
        List<StylePackageAsset> resources = activeAssets(packageId);
        long categoryCount = resources.stream()
                .map(StylePackageAsset::getCategoryKey)
                .filter(StringUtils::hasText)
                .distinct()
                .count();
        List<StylePackageReview> reviews = stylePackageReviewMapper.selectList(Wrappers.<StylePackageReview>lambdaQuery()
                .eq(StylePackageReview::getStylePackageId, packageId));
        double averageRating = reviews.isEmpty() ? 0 : reviews.stream()
                .map(StylePackageReview::getRating)
                .filter(rating -> rating != null && rating > 0)
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0);
        double roundedRating = BigDecimal.valueOf(averageRating).setScale(1, RoundingMode.HALF_UP).doubleValue();
        return new StylePackageDtos.Stats(accessCount, submissionCount, approvedArtworkCount, versionCount,
                reviews.size(), collaboratorCount, resources.size(), categoryCount, roundedRating);
    }

    private List<StylePackageDtos.TagSummary> tags(Long packageId) {
        List<StylePackageTag> relations = stylePackageTagMapper.selectList(Wrappers.<StylePackageTag>lambdaQuery()
                .eq(StylePackageTag::getStylePackageId, packageId)
                .orderByAsc(StylePackageTag::getCreatedAt));
        if (relations.isEmpty()) {
            return List.of();
        }
        List<Long> ids = relations.stream().map(StylePackageTag::getTagId).toList();
        List<Tag> tags = tagMapper.selectBatchIds(ids);
        Map<Long, Tag> tagMap = new LinkedHashMap<>();
        for (Tag tag : tags) {
            tagMap.put(tag.getId(), tag);
        }
        List<StylePackageDtos.TagSummary> summaries = new ArrayList<>();
        for (Long id : ids) {
            Tag tag = tagMap.get(id);
            if (tag != null) {
                summaries.add(new StylePackageDtos.TagSummary(tag.getId(), tag.getName(), tag.getDisplayNameZh(), tag.getPreviewImageUrl()));
            }
        }
        return summaries;
    }

    private List<StylePackageDtos.ArtworkSummary> approvedArtworkSummaries(Long packageId, int limit) {
        List<StylePackageSubmission> submissions = stylePackageSubmissionMapper.selectList(Wrappers.<StylePackageSubmission>lambdaQuery()
                .eq(StylePackageSubmission::getStylePackageId, packageId)
                .eq(StylePackageSubmission::getStatus, "APPROVED")
                .orderByDesc(StylePackageSubmission::getReviewedAt)
                .last("LIMIT " + Math.max(1, limit)));
        if (submissions.isEmpty()) {
            return List.of();
        }
        List<StylePackageDtos.ArtworkSummary> summaries = new ArrayList<>();
        for (StylePackageSubmission submission : submissions) {
            Artwork artwork = artworkMapper.selectById(submission.getArtworkId());
            if (artwork != null) {
                summaries.add(new StylePackageDtos.ArtworkSummary(
                        artwork.getId(),
                        artwork.getTitle(),
                        artwork.getImageUrl(),
                        artwork.getVisibility(),
                        artwork.getStatus()));
            }
        }
        return summaries;
    }

    private List<StylePackageDtos.AssetPreview> assetPreviews(Long packageId, int limit) {
        return activeAssets(packageId).stream()
                .limit(Math.max(1, limit))
                .map(asset -> new StylePackageDtos.AssetPreview(
                        asset.getId(),
                        asset.getLogicalKey(),
                        asset.getName(),
                        asset.getCategoryKey(),
                        asset.getPreviewImageUrl(),
                        asset.getThumbnailUrl(),
                        asset.getRevisionNumber()))
                .toList();
    }

    private List<StylePackageAsset> activeAssets(Long packageId) {
        return stylePackageAssetMapper.selectList(Wrappers.<StylePackageAsset>lambdaQuery()
                .eq(StylePackageAsset::getStylePackageId, packageId)
                .eq(StylePackageAsset::getStatus, "ACTIVE")
                .orderByAsc(StylePackageAsset::getSortOrder)
                .orderByAsc(StylePackageAsset::getCategoryKey)
                .orderByAsc(StylePackageAsset::getName));
    }

    private List<StylePackageAsset> versionAssets(Long versionId) {
        List<StylePackageVersionAsset> relations = stylePackageVersionAssetMapper.selectList(
                Wrappers.<StylePackageVersionAsset>lambdaQuery()
                        .eq(StylePackageVersionAsset::getStylePackageVersionId, versionId)
                        .orderByAsc(StylePackageVersionAsset::getCreatedAt));
        if (relations.isEmpty()) {
            return List.of();
        }
        List<Long> ids = relations.stream().map(StylePackageVersionAsset::getStylePackageAssetId).toList();
        Map<Long, StylePackageAsset> resourceMap = new LinkedHashMap<>();
        for (StylePackageAsset asset : stylePackageAssetMapper.selectBatchIds(ids)) {
            resourceMap.put(asset.getId(), asset);
        }
        return ids.stream().map(resourceMap::get).filter(Objects::nonNull).toList();
    }

    private StylePackageAsset requireActiveAsset(Long packageId, Long assetId) {
        StylePackageAsset asset = stylePackageAssetMapper.selectOne(Wrappers.<StylePackageAsset>lambdaQuery()
                .eq(StylePackageAsset::getId, assetId)
                .eq(StylePackageAsset::getStylePackageId, packageId)
                .eq(StylePackageAsset::getStatus, "ACTIVE"));
        if (asset == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Style package resource not found");
        }
        return asset;
    }

    private StylePackageDtos.AssetView assetView(StylePackageAsset asset, boolean downloadable) {
        return new StylePackageDtos.AssetView(
                asset.getId(),
                asset.getStylePackageId(),
                asset.getContributorId(),
                asset.getLogicalKey(),
                asset.getRevisionNumber(),
                asset.getName(),
                asset.getCategoryKey(),
                asset.getAssetType(),
                asset.getDescription(),
                asset.getPreviewImageUrl(),
                downloadable ? asset.getFileUrl() : null,
                asset.getThumbnailUrl(),
                downloadable ? asset.getPromptText() : null,
                downloadable ? asset.getNegativePromptText() : null,
                downloadable ? asset.getGenerationParamsJson() : null,
                asset.getWidth(),
                asset.getHeight(),
                asset.getFileFormat(),
                asset.getBackgroundMode(),
                asset.getLicenseScope(),
                asset.getStatus(),
                asset.getSortOrder(),
                downloadable,
                asset.getCreatedAt(),
                asset.getUpdatedAt());
    }

    private List<StylePackageDtos.CollaboratorSummary> collaborators(Long packageId) {
        List<StylePackageCollaborator> relations = stylePackageCollaboratorMapper.selectList(Wrappers.<StylePackageCollaborator>lambdaQuery()
                .eq(StylePackageCollaborator::getStylePackageId, packageId)
                .orderByAsc(StylePackageCollaborator::getCreatedAt));
        if (relations.isEmpty()) {
            return List.of();
        }
        List<Long> userIds = relations.stream().map(StylePackageCollaborator::getUserId).toList();
        List<User> users = userMapper.selectBatchIds(userIds);
        Map<Long, User> userMap = new LinkedHashMap<>();
        for (User user : users) {
            userMap.put(user.getId(), user);
        }
        List<StylePackageDtos.CollaboratorSummary> summaries = new ArrayList<>();
        for (StylePackageCollaborator relation : relations) {
            User user = userMap.get(relation.getUserId());
            if (user != null) {
                summaries.add(new StylePackageDtos.CollaboratorSummary(
                        user.getId(),
                        user.getDisplayName(),
                        user.getAvatarUrl(),
                        relation.getRole()));
            }
        }
        return summaries;
    }

    private StylePackageDtos.VersionView versionView(StylePackageVersion version, boolean accessible) {
        return new StylePackageDtos.VersionView(
                version.getId(),
                version.getStylePackageId(),
                version.getVersionNumber(),
                version.getName(),
                version.getDescription(),
                version.getCoverImageUrl(),
                version.getStyleStatement(),
                accessible ? version.getPromptGuide() : null,
                accessible ? version.getNegativePromptGuide() : null,
                version.getLicenseType(),
                version.getLicenseSummary(),
                Boolean.TRUE.equals(version.getCommercialUse()),
                version.getFeaturedArtworkId(),
                version.getArtworkCount(),
                version.getResourceCount(),
                version.getCategoryCount(),
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

    private String contentTypeFor(String format) {
        return switch (format) {
            case "png" -> "image/png";
            case "jpg", "jpeg" -> "image/jpeg";
            case "webp" -> "image/webp";
            case "gif" -> "image/gif";
            case "zip" -> "application/zip";
            case "json" -> "application/json";
            case "txt" -> "text/plain";
            default -> "application/octet-stream";
        };
    }

    private boolean isFree(StylePackage stylePackage) {
        return stylePackage.getPricePoints() == null || stylePackage.getPricePoints().compareTo(BigDecimal.ZERO) <= 0;
    }

    private boolean canEditPackage(Long userId, Long packageId) {
        if (userId == null) {
            return false;
        }
        StylePackage stylePackage = stylePackageMapper.selectById(packageId);
        if (stylePackage == null) {
            return false;
        }
        if (Objects.equals(stylePackage.getUserId(), userId)) {
            return true;
        }
        return stylePackageCollaboratorMapper.selectCount(Wrappers.<StylePackageCollaborator>lambdaQuery()
                .eq(StylePackageCollaborator::getStylePackageId, packageId)
                .eq(StylePackageCollaborator::getUserId, userId)) > 0;
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
