package com.app.assetmonitoringsystem.service;

import com.app.assetmonitoringsystem.dto.AssetDTO;
import com.app.assetmonitoringsystem.entity.Asset;
import com.app.assetmonitoringsystem.entity.User;
import com.app.assetmonitoringsystem.entity.UptimeLog;
import com.app.assetmonitoringsystem.entity.UptimeStatus;
import com.app.assetmonitoringsystem.exception.ResourceNotFoundException;
import com.app.assetmonitoringsystem.repository.AssetRepository;
import com.app.assetmonitoringsystem.repository.UserRepository;
import com.app.assetmonitoringsystem.repository.UptimeLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Service handling asset CRUD operations and assignment.
 */
@Service
public class AssetService {

    private static final Logger logger = LoggerFactory.getLogger(AssetService.class);

    private final AssetRepository assetRepository;
    private final UserRepository userRepository;
    private final UptimeLogRepository uptimeLogRepository;

    public AssetService(AssetRepository assetRepository,
                        UserRepository userRepository,
                        UptimeLogRepository uptimeLogRepository) {
        this.assetRepository = assetRepository;
        this.userRepository = userRepository;
        this.uptimeLogRepository = uptimeLogRepository;
    }

    /**
     * Create a new asset and start an uptime log.
     */
    @Transactional
    public AssetDTO createAsset(AssetDTO dto) {
        Asset asset = new Asset();
        asset.setName(dto.getName());
        asset.setType(dto.getType());
        asset.setLocation(dto.getLocation());
        asset.setThresholdTemp(dto.getThresholdTemp());
        asset.setThresholdPressure(dto.getThresholdPressure());

        if (dto.getAssignedToId() != null) {
            User user = userRepository.findById(dto.getAssignedToId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", dto.getAssignedToId()));
            asset.setAssignedTo(user);
        }

        Asset saved = assetRepository.save(asset);
        logger.info("Asset created: {} (ID: {})", saved.getName(), saved.getId());

        // Start uptime tracking
        UptimeLog uptimeLog = new UptimeLog(saved, LocalDateTime.now(), UptimeStatus.UP);
        uptimeLogRepository.save(uptimeLog);

        return toDTO(saved);
    }

    /**
     * Get all assets with pagination.
     */
    @Transactional(readOnly = true)
    public Page<AssetDTO> getAllAssets(Pageable pageable) {
        return assetRepository.findAll(pageable).map(this::toDTO);
    }

    /**
     * Get assets assigned to a specific user with pagination.
     */
    @Transactional(readOnly = true)
    public Page<AssetDTO> getAssetsByUser(Long userId, Pageable pageable) {
        return assetRepository.findByAssignedToId(userId, pageable).map(this::toDTO);
    }

    /**
     * Get a single asset by ID.
     */
    @Transactional(readOnly = true)
    public AssetDTO getAssetById(Long id) {
        Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asset", "id", id));
        return toDTO(asset);
    }

    /**
     * Update an existing asset.
     */
    @Transactional
    public AssetDTO updateAsset(Long id, AssetDTO dto) {
        Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asset", "id", id));

        asset.setName(dto.getName());
        asset.setType(dto.getType());
        asset.setLocation(dto.getLocation());
        asset.setThresholdTemp(dto.getThresholdTemp());
        asset.setThresholdPressure(dto.getThresholdPressure());

        if (dto.getAssignedToId() != null) {
            User user = userRepository.findById(dto.getAssignedToId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", dto.getAssignedToId()));
            asset.setAssignedTo(user);
        }

        Asset updated = assetRepository.save(asset);
        logger.info("Asset updated: {} (ID: {})", updated.getName(), updated.getId());
        return toDTO(updated);
    }

    /**
     * Delete an asset by ID.
     */
    @Transactional
    public void deleteAsset(Long id) {
        Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asset", "id", id));
        assetRepository.delete(asset);
        logger.info("Asset deleted: {} (ID: {})", asset.getName(), id);
    }

    private AssetDTO toDTO(Asset asset) {
        AssetDTO dto = new AssetDTO();
        dto.setId(asset.getId());
        dto.setName(asset.getName());
        dto.setType(asset.getType());
        dto.setLocation(asset.getLocation());
        dto.setThresholdTemp(asset.getThresholdTemp());
        dto.setThresholdPressure(asset.getThresholdPressure());
        if (asset.getAssignedTo() != null) {
            dto.setAssignedToId(asset.getAssignedTo().getId());
            dto.setAssignedToName(asset.getAssignedTo().getName());
        }
        return dto;
    }
}
