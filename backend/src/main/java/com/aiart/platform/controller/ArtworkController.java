package com.aiart.platform.controller;

import com.aiart.platform.common.ApiResponse;
import com.aiart.platform.dto.ArtworkDtos;
import com.aiart.platform.security.CurrentUser;
import com.aiart.platform.service.ArtworkService;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/artworks")
@RequiredArgsConstructor
public class ArtworkController {
    private final ArtworkService artworkService;
    private final CurrentUser currentUser;

    @GetMapping("/my")
    public ApiResponse<List<ArtworkDtos.ArtworkCard>> my(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long tagId,
            @RequestParam(required = false) String tagIds,
            @RequestParam(required = false) String visibility,
            @RequestParam(required = false) String status) {
        return ApiResponse.ok(artworkService.myArtworks(
                currentUser.requireUserId(),
                page,
                size,
                keyword,
                parseTagIds(tagId, tagIds),
                visibility,
                status
        ));
    }

    @GetMapping("/public")
    public ApiResponse<List<ArtworkDtos.ArtworkCard>> publicArtworks(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long tagId,
            @RequestParam(required = false) String tagIds) {
        return ApiResponse.ok(artworkService.publicArtworks(page, size, keyword, parseTagIds(tagId, tagIds)));
    }

    @PostMapping("/{artworkId}/request-publish")
    public ApiResponse<ArtworkDtos.ArtworkDetail> requestPublish(@PathVariable Long artworkId) {
        return ApiResponse.ok(artworkService.requestPublish(currentUser.requireUserId(), artworkId));
    }

    @PutMapping("/{artworkId}")
    public ApiResponse<ArtworkDtos.ArtworkDetail> update(
            @PathVariable Long artworkId,
            @Valid @RequestBody ArtworkDtos.UpdateRequest request) {
        return ApiResponse.ok(artworkService.update(currentUser.requireUserId(), artworkId, request));
    }

    @PostMapping("/{artworkId}/archive")
    public ApiResponse<ArtworkDtos.ArtworkDetail> archive(@PathVariable Long artworkId) {
        return ApiResponse.ok(artworkService.archive(currentUser.requireUserId(), artworkId));
    }

    @PostMapping("/bulk/request-publish")
    public ApiResponse<List<ArtworkDtos.ArtworkDetail>> bulkRequestPublish(@Valid @RequestBody ArtworkDtos.BulkRequest request) {
        return ApiResponse.ok(artworkService.bulkRequestPublish(currentUser.requireUserId(), request));
    }

    @PostMapping("/bulk/visibility")
    public ApiResponse<List<ArtworkDtos.ArtworkDetail>> bulkVisibility(@Valid @RequestBody ArtworkDtos.BulkVisibilityRequest request) {
        return ApiResponse.ok(artworkService.bulkUpdateVisibility(currentUser.requireUserId(), request));
    }

    @PostMapping("/bulk/archive")
    public ApiResponse<List<ArtworkDtos.ArtworkDetail>> bulkArchive(@Valid @RequestBody ArtworkDtos.BulkRequest request) {
        return ApiResponse.ok(artworkService.bulkArchive(currentUser.requireUserId(), request));
    }

    @GetMapping("/{artworkId}")
    public ApiResponse<ArtworkDtos.ArtworkDetail> detail(@PathVariable Long artworkId) {
        return ApiResponse.ok(artworkService.detail(currentUser.userIdOrNull(), artworkId));
    }

    private List<Long> parseTagIds(Long tagId, String tagIds) {
        List<Long> values = new ArrayList<>();
        if (tagId != null) {
            values.add(tagId);
        }
        if (tagIds != null && !tagIds.isBlank()) {
            for (String part : tagIds.split(",")) {
                if (!part.isBlank()) {
                    values.add(Long.parseLong(part.trim()));
                }
            }
        }
        return values.stream().distinct().toList();
    }
}
