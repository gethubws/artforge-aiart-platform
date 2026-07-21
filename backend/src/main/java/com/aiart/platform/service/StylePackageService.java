package com.aiart.platform.service;

import com.aiart.platform.dto.StylePackageDtos;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface StylePackageService {
    StylePackageDtos.Detail create(Long userId, StylePackageDtos.SaveRequest request);

    StylePackageDtos.Detail update(Long userId, Long packageId, StylePackageDtos.SaveRequest request);

    StylePackageDtos.Detail publish(Long userId, Long packageId);

    StylePackageDtos.Detail archive(Long userId, Long packageId);

    StylePackageDtos.Detail exchange(Long userId, Long packageId);

    StylePackageDtos.Detail detail(Long viewerId, Long packageId);

    StylePackageDtos.PackagePage myPackages(Long userId, StylePackageDtos.ListQuery query, int page, int size);

    StylePackageDtos.PackagePage marketPackages(Long viewerId, StylePackageDtos.ListQuery query, int page, int size);

    List<StylePackageDtos.VersionView> versions(Long viewerId, Long packageId, int page, int size);

    List<StylePackageDtos.AssetView> assets(Long viewerId, Long packageId, String categoryKey);

    StylePackageDtos.AssetView createAsset(Long userId, Long packageId, StylePackageDtos.AssetSaveRequest request);

    List<StylePackageDtos.AssetView> createAssets(Long userId, Long packageId,
                                                  StylePackageDtos.AssetBatchRequest request);

    StylePackageDtos.AssetView updateAsset(Long userId, Long packageId, Long assetId,
                                            StylePackageDtos.AssetSaveRequest request);

    StylePackageDtos.AssetView archiveAsset(Long userId, Long packageId, Long assetId);

    StylePackageDtos.AssetUploadView uploadAssetFile(Long userId, Long packageId, MultipartFile file);

    StylePackageDtos.AssetDownload assetDownload(Long userId, Long packageId, Long assetId);

    StylePackageDtos.AssetManifest manifest(Long userId, Long packageId);

    List<StylePackageDtos.RatingView> reviews(Long viewerId, Long packageId, int page, int size);

    StylePackageDtos.RatingView saveReview(Long userId, Long packageId, StylePackageDtos.RatingRequest request);

    StylePackageDtos.SubmissionView submitArtwork(Long userId, Long packageId, StylePackageDtos.SubmitRequest request);

    List<StylePackageDtos.SubmissionView> mySubmissions(Long userId, int page, int size);

    List<StylePackageDtos.SubmissionView> packageSubmissions(Long userId, Long packageId, int page, int size);

    List<StylePackageDtos.SubmissionView> packageArtworks(Long viewerId, Long packageId, int page, int size);

    StylePackageDtos.SubmissionView reviewSubmission(Long userId, Long submissionId, StylePackageDtos.ReviewRequest request);
}
