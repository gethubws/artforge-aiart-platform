package com.aiart.platform.controller;

import com.aiart.platform.common.ApiResponse;
import com.aiart.platform.dto.StylePackageDtos;
import com.aiart.platform.security.CurrentUser;
import com.aiart.platform.service.StylePackageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/style-packages")
@RequiredArgsConstructor
public class StylePackageController {
    private final StylePackageService stylePackageService;
    private final CurrentUser currentUser;

    @PostMapping
    public ApiResponse<StylePackageDtos.Detail> create(@Valid @RequestBody StylePackageDtos.SaveRequest request) {
        return ApiResponse.ok(stylePackageService.create(currentUser.requireUserId(), request));
    }

    @PutMapping("/{packageId}")
    public ApiResponse<StylePackageDtos.Detail> update(@PathVariable Long packageId,
                                                       @Valid @RequestBody StylePackageDtos.SaveRequest request) {
        return ApiResponse.ok(stylePackageService.update(currentUser.requireUserId(), packageId, request));
    }

    @PostMapping("/{packageId}/publish")
    public ApiResponse<StylePackageDtos.Detail> publish(@PathVariable Long packageId) {
        return ApiResponse.ok(stylePackageService.publish(currentUser.requireUserId(), packageId));
    }

    @PostMapping("/{packageId}/archive")
    public ApiResponse<StylePackageDtos.Detail> archive(@PathVariable Long packageId) {
        return ApiResponse.ok(stylePackageService.archive(currentUser.requireUserId(), packageId));
    }

    @PostMapping("/{packageId}/exchange")
    public ApiResponse<StylePackageDtos.Detail> exchange(@PathVariable Long packageId) {
        return ApiResponse.ok(stylePackageService.exchange(currentUser.requireUserId(), packageId));
    }

    @PostMapping("/{packageId}/submissions")
    public ApiResponse<StylePackageDtos.SubmissionView> submitArtwork(@PathVariable Long packageId,
                                                                     @Valid @RequestBody StylePackageDtos.SubmitRequest request) {
        return ApiResponse.ok(stylePackageService.submitArtwork(currentUser.requireUserId(), packageId, request));
    }

    @GetMapping("/{packageId}/submissions")
    public ApiResponse<List<StylePackageDtos.SubmissionView>> packageSubmissions(@PathVariable Long packageId,
                                                                                 @RequestParam(defaultValue = "1") int page,
                                                                                 @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(stylePackageService.packageSubmissions(currentUser.requireUserId(), packageId, page, size));
    }

    @GetMapping("/{packageId}/artworks")
    public ApiResponse<List<StylePackageDtos.SubmissionView>> packageArtworks(@PathVariable Long packageId,
                                                                            @RequestParam(defaultValue = "1") int page,
                                                                            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(stylePackageService.packageArtworks(currentUser.userIdOrNull(), packageId, page, size));
    }

    @GetMapping("/{packageId}/versions")
    public ApiResponse<List<StylePackageDtos.VersionView>> versions(@PathVariable Long packageId,
                                                                  @RequestParam(defaultValue = "1") int page,
                                                                  @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(stylePackageService.versions(currentUser.userIdOrNull(), packageId, page, size));
    }

    @GetMapping("/{packageId}/reviews")
    public ApiResponse<List<StylePackageDtos.RatingView>> reviews(@PathVariable Long packageId,
                                                                 @RequestParam(defaultValue = "1") int page,
                                                                 @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(stylePackageService.reviews(currentUser.userIdOrNull(), packageId, page, size));
    }

    @PostMapping("/{packageId}/reviews")
    public ApiResponse<StylePackageDtos.RatingView> saveReview(@PathVariable Long packageId,
                                                              @Valid @RequestBody StylePackageDtos.RatingRequest request) {
        return ApiResponse.ok(stylePackageService.saveReview(currentUser.requireUserId(), packageId, request));
    }

    @GetMapping("/submissions/my")
    public ApiResponse<List<StylePackageDtos.SubmissionView>> mySubmissions(@RequestParam(defaultValue = "1") int page,
                                                                            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(stylePackageService.mySubmissions(currentUser.requireUserId(), page, size));
    }

    @PostMapping("/submissions/{submissionId}/review")
    public ApiResponse<StylePackageDtos.SubmissionView> reviewSubmission(@PathVariable Long submissionId,
                                                                        @Valid @RequestBody StylePackageDtos.ReviewRequest request) {
        return ApiResponse.ok(stylePackageService.reviewSubmission(currentUser.requireUserId(), submissionId, request));
    }

    @GetMapping("/{packageId}")
    public ApiResponse<StylePackageDtos.Detail> detail(@PathVariable Long packageId) {
        return ApiResponse.ok(stylePackageService.detail(currentUser.requireUserId(), packageId));
    }

    @GetMapping("/my")
    public ApiResponse<StylePackageDtos.PackagePage> myPackages(@RequestParam(defaultValue = "1") int page,
                                                               @RequestParam(required = false) String keyword,
                                                               @RequestParam(required = false) Long tagId,
                                                               @RequestParam(required = false) String status,
                                                               @RequestParam(required = false) String sort,
                                                               @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(stylePackageService.myPackages(
                currentUser.requireUserId(),
                new StylePackageDtos.ListQuery(keyword, tagId, status, sort),
                page,
                size));
    }

    @GetMapping("/market")
    public ApiResponse<StylePackageDtos.PackagePage> marketPackages(@RequestParam(defaultValue = "1") int page,
                                                                   @RequestParam(required = false) String keyword,
                                                                   @RequestParam(required = false) Long tagId,
                                                                   @RequestParam(required = false) String status,
                                                                   @RequestParam(required = false) String sort,
                                                                   @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(stylePackageService.marketPackages(
                currentUser.userIdOrNull(),
                new StylePackageDtos.ListQuery(keyword, tagId, status, sort),
                page,
                size));
    }
}
