package com.app.assetmonitoringsystem.service;

import com.app.assetmonitoringsystem.dto.MaintenanceLogDTO;
import com.app.assetmonitoringsystem.entity.Asset;
import com.app.assetmonitoringsystem.entity.MaintenanceLog;
import com.app.assetmonitoringsystem.exception.ResourceNotFoundException;
import com.app.assetmonitoringsystem.repository.AssetRepository;
import com.app.assetmonitoringsystem.repository.MaintenanceLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;

@Service
public class MaintenanceService {
    private static final Logger logger = LoggerFactory.getLogger(MaintenanceService.class);
    private final MaintenanceLogRepository maintenanceLogRepository;
    private final AssetRepository assetRepository;

    public MaintenanceService(MaintenanceLogRepository maintenanceLogRepository, AssetRepository assetRepository) {
        this.maintenanceLogRepository = maintenanceLogRepository;
        this.assetRepository = assetRepository;
    }

    @Transactional
    public MaintenanceLogDTO createMaintenanceLog(MaintenanceLogDTO dto) {
        Asset asset = assetRepository.findById(dto.getAssetId())
                .orElseThrow(() -> new ResourceNotFoundException("Asset", "id", dto.getAssetId()));
        MaintenanceLog log = new MaintenanceLog();
        log.setAsset(asset);
        log.setScheduledDate(dto.getScheduledDate());
        log.setCompletedDate(dto.getCompletedDate());
        log.setRemarks(dto.getRemarks());
        MaintenanceLog saved = maintenanceLogRepository.save(log);
        logger.info("Maintenance scheduled for Asset #{} on {}", asset.getId(), dto.getScheduledDate());
        return toDTO(saved);
    }

    @Transactional(readOnly = true)
    public Page<MaintenanceLogDTO> getMaintenanceByAsset(Long assetId, Pageable pageable) {
        if (!assetRepository.existsById(assetId)) {
            throw new ResourceNotFoundException("Asset", "id", assetId);
        }
        return maintenanceLogRepository.findByAssetIdOrderByScheduledDateDesc(assetId, pageable).map(this::toDTO);
    }

    @Transactional
    public MaintenanceLogDTO completeMaintenance(Long maintenanceId, String remarks) {
        MaintenanceLog log = maintenanceLogRepository.findById(maintenanceId)
                .orElseThrow(() -> new ResourceNotFoundException("MaintenanceLog", "id", maintenanceId));
        log.setCompletedDate(LocalDate.now());
        if (remarks != null && !remarks.isEmpty()) { log.setRemarks(remarks); }
        maintenanceLogRepository.save(log);
        logger.info("Maintenance #{} completed for Asset #{}", maintenanceId, log.getAsset().getId());
        return toDTO(log);
    }

    private MaintenanceLogDTO toDTO(MaintenanceLog log) {
        MaintenanceLogDTO dto = new MaintenanceLogDTO();
        dto.setId(log.getId());
        dto.setAssetId(log.getAsset().getId());
        dto.setAssetName(log.getAsset().getName());
        dto.setScheduledDate(log.getScheduledDate());
        dto.setCompletedDate(log.getCompletedDate());
        dto.setRemarks(log.getRemarks());
        dto.setCreatedAt(log.getCreatedAt() != null ? log.getCreatedAt().toString() : null);
        return dto;
    }
}
