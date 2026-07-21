package com.aiart.platform.service.impl;

import com.aiart.platform.dto.StylePackageDtos;
import com.aiart.platform.entity.StylePackage;
import com.aiart.platform.entity.StylePackageAsset;
import com.aiart.platform.exception.BusinessException;
import com.aiart.platform.mapper.ArtworkMapper;
import com.aiart.platform.mapper.StylePackageAccessMapper;
import com.aiart.platform.mapper.StylePackageAssetMapper;
import com.aiart.platform.mapper.StylePackageCollaboratorMapper;
import com.aiart.platform.mapper.StylePackageMapper;
import com.aiart.platform.mapper.StylePackageReviewMapper;
import com.aiart.platform.mapper.StylePackageSubmissionMapper;
import com.aiart.platform.mapper.StylePackageTagMapper;
import com.aiart.platform.mapper.StylePackageVersionAssetMapper;
import com.aiart.platform.mapper.StylePackageVersionMapper;
import com.aiart.platform.mapper.TagMapper;
import com.aiart.platform.mapper.UserMapper;
import com.aiart.platform.service.PointService;
import com.aiart.platform.service.ResourceAssetStorageService;
import com.aiart.platform.service.UserEngagementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StylePackageAssetAccessTest {
    @Mock private StylePackageMapper stylePackageMapper;
    @Mock private StylePackageAccessMapper stylePackageAccessMapper;
    @Mock private StylePackageAssetMapper stylePackageAssetMapper;
    @Mock private StylePackageSubmissionMapper stylePackageSubmissionMapper;
    @Mock private StylePackageVersionMapper stylePackageVersionMapper;
    @Mock private StylePackageVersionAssetMapper stylePackageVersionAssetMapper;
    @Mock private StylePackageReviewMapper stylePackageReviewMapper;
    @Mock private StylePackageTagMapper stylePackageTagMapper;
    @Mock private StylePackageCollaboratorMapper stylePackageCollaboratorMapper;
    @Mock private ArtworkMapper artworkMapper;
    @Mock private TagMapper tagMapper;
    @Mock private UserMapper userMapper;
    @Mock private PointService pointService;
    @Mock private UserEngagementService userEngagementService;
    @Mock private ResourceAssetStorageService resourceAssetStorageService;

    @InjectMocks
    private StylePackageServiceImpl service;

    private StylePackage stylePackage;
    private StylePackageAsset asset;

    @BeforeEach
    void setUp() {
        stylePackage = new StylePackage();
        stylePackage.setId(10L);
        stylePackage.setUserId(1L);
        stylePackage.setName("Unified Forest Kit");
        stylePackage.setStatus("PUBLISHED");
        stylePackage.setPricePoints(BigDecimal.TEN);

        asset = new StylePackageAsset();
        asset.setId(20L);
        asset.setStylePackageId(10L);
        asset.setName("Oak Tree");
        asset.setFileUrl("storage://private/style-assets/oak.zip");
        asset.setFileFormat("zip");
        asset.setStatus("ACTIVE");

        when(stylePackageMapper.selectById(10L)).thenReturn(stylePackage);
    }

    @Test
    void rejectsDownloadForUserWithoutAccess() {
        when(stylePackageCollaboratorMapper.selectCount(any())).thenReturn(0L);
        when(stylePackageAccessMapper.selectCount(any())).thenReturn(0L);

        assertThrows(BusinessException.class, () -> service.assetDownload(2L, 10L, 20L));

        verifyNoInteractions(stylePackageAssetMapper);
    }

    @Test
    void allowsOwnerToDownloadPrivateAsset() {
        when(stylePackageAssetMapper.selectOne(any())).thenReturn(asset);

        StylePackageDtos.AssetDownload download = service.assetDownload(1L, 10L, 20L);

        assertEquals(asset.getFileUrl(), download.fileUrl());
        assertEquals("Oak Tree.zip", download.filename());
        assertEquals("application/zip", download.contentType());
    }

    @Test
    void allowsUserWithPurchasedAccessToDownloadPrivateAsset() {
        when(stylePackageCollaboratorMapper.selectCount(any())).thenReturn(0L);
        when(stylePackageAccessMapper.selectCount(any())).thenReturn(1L);
        when(stylePackageAssetMapper.selectOne(any())).thenReturn(asset);

        StylePackageDtos.AssetDownload download = service.assetDownload(2L, 10L, 20L);

        assertEquals(asset.getFileUrl(), download.fileUrl());
    }
}
