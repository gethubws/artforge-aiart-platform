package com.aiart.platform.service;

import com.aiart.platform.dto.StylePackageDtos;

import java.util.List;

public interface StylePackageService {
    StylePackageDtos.Detail create(Long userId, StylePackageDtos.SaveRequest request);

    StylePackageDtos.Detail update(Long userId, Long packageId, StylePackageDtos.SaveRequest request);

    StylePackageDtos.Detail publish(Long userId, Long packageId);

    StylePackageDtos.Detail archive(Long userId, Long packageId);

    StylePackageDtos.Detail exchange(Long userId, Long packageId);

    StylePackageDtos.Detail detail(Long viewerId, Long packageId);

    List<StylePackageDtos.Card> myPackages(Long userId, StylePackageDtos.ListQuery query, int page, int size);

    List<StylePackageDtos.Card> marketPackages(Long viewerId, StylePackageDtos.ListQuery query, int page, int size);

    List<StylePackageDtos.VersionView> versions(Long viewerId, Long packageId, int page, int size);

    List<StylePackageDtos.RatingView> reviews(Long viewerId, Long packageId, int page, int size);

    StylePackageDtos.RatingView saveReview(Long userId, Long packageId, StylePackageDtos.RatingRequest request);

    StylePackageDtos.SubmissionView submitArtwork(Long userId, Long packageId, StylePackageDtos.SubmitRequest request);

    List<StylePackageDtos.SubmissionView> mySubmissions(Long userId, int page, int size);

    List<StylePackageDtos.SubmissionView> packageSubmissions(Long userId, Long packageId, int page, int size);

    List<StylePackageDtos.SubmissionView> packageArtworks(Long viewerId, Long packageId, int page, int size);

    StylePackageDtos.SubmissionView reviewSubmission(Long userId, Long submissionId, StylePackageDtos.ReviewRequest request);
}
