package com.app.assetmonitoringsystem.service;

import com.app.assetmonitoringsystem.dto.AssetDTO;
import com.app.assetmonitoringsystem.entity.Asset;
import com.app.assetmonitoringsystem.entity.User;
import com.app.assetmonitoringsystem.entity.Role;
import com.app.assetmonitoringsystem.exception.ResourceNotFoundException;
import com.app.assetmonitoringsystem.repository.AssetRepository;
import com.app.assetmonitoringsystem.repository.UserRepository;
import com.app.assetmonitoringsystem.repository.UptimeLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssetServiceTest {

    @Mock private AssetRepository assetRepository;
    @Mock private UserRepository userRepository;
    @Mock private UptimeLogRepository uptimeLogRepository;

    @InjectMocks private AssetService assetService;

    private Asset testAsset;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("John", "john@example.com", "pass", Role.ROLE_OPERATOR);
        testUser.setId(1L);
        testAsset = new Asset("Motor A", "Motor", "Factory 1", 80.0, 100.0);
        testAsset.setId(1L);
        testAsset.setAssignedTo(testUser);
    }

    @Test
    void createAsset_Success() {
        AssetDTO dto = new AssetDTO(null, "Motor A", "Motor", "Factory 1", 80.0, 100.0, 1L, null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(assetRepository.save(any(Asset.class))).thenReturn(testAsset);
        when(uptimeLogRepository.save(any())).thenReturn(null);

        AssetDTO result = assetService.createAsset(dto);

        assertNotNull(result);
        assertEquals("Motor A", result.getName());
        verify(assetRepository).save(any(Asset.class));
        verify(uptimeLogRepository).save(any());
    }

    @Test
    void getAssetById_Success() {
        when(assetRepository.findById(1L)).thenReturn(Optional.of(testAsset));

        AssetDTO result = assetService.getAssetById(1L);

        assertEquals("Motor A", result.getName());
        assertEquals(80.0, result.getThresholdTemp());
    }

    @Test
    void getAssetById_NotFound_ThrowsException() {
        when(assetRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> assetService.getAssetById(99L));
    }

    @Test
    void getAllAssets_ReturnsPaginatedResults() {
        Page<Asset> page = new PageImpl<>(List.of(testAsset));
        when(assetRepository.findAll(any(PageRequest.class))).thenReturn(page);

        Page<AssetDTO> result = assetService.getAllAssets(PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void deleteAsset_Success() {
        when(assetRepository.findById(1L)).thenReturn(Optional.of(testAsset));
        doNothing().when(assetRepository).delete(testAsset);

        assertDoesNotThrow(() -> assetService.deleteAsset(1L));
        verify(assetRepository).delete(testAsset);
    }

    @Test
    void deleteAsset_NotFound_ThrowsException() {
        when(assetRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> assetService.deleteAsset(99L));
    }
}
