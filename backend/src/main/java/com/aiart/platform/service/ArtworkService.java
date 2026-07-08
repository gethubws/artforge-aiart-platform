package com.aiart.platform.service;

import com.aiart.platform.dto.ArtworkDtos;

import java.util.List;

public interface ArtworkService {
    List<ArtworkDtos.ArtworkCard> myArtworks(Long userId, int page, int size, List<Long> tagIds, String visibility, String status);

    List<ArtworkDtos.ArtworkCard> publicArtworks(int page, int size, List<Long> tagIds);

    ArtworkDtos.ArtworkDetail requestPublish(Long userId, Long artworkId);

    ArtworkDtos.ArtworkDetail update(Long userId, Long artworkId, ArtworkDtos.UpdateRequest request);

    ArtworkDtos.ArtworkDetail archive(Long userId, Long artworkId);

    List<ArtworkDtos.ArtworkDetail> bulkRequestPublish(Long userId, ArtworkDtos.BulkRequest request);

    List<ArtworkDtos.ArtworkDetail> bulkUpdateVisibility(Long userId, ArtworkDtos.BulkVisibilityRequest request);

    List<ArtworkDtos.ArtworkDetail> bulkArchive(Long userId, ArtworkDtos.BulkRequest request);

    ArtworkDtos.ArtworkDetail detail(Long userId, Long artworkId);
}
