package com.app.assetmonitoringsystem.service;

import com.app.assetmonitoringsystem.dto.UptimeLogDTO;
import com.app.assetmonitoringsystem.entity.UptimeLog;
import com.app.assetmonitoringsystem.entity.UptimeStatus;
import com.app.assetmonitoringsystem.exception.ResourceNotFoundException;
import com.app.assetmonitoringsystem.repository.AssetRepository;
import com.app.assetmonitoringsystem.repository.UptimeLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Duration;

@Service
public class UptimeService {
    private static final Logger logger = LoggerFactory.getLogger(UptimeService.class);
    private final UptimeLogRepository uptimeLogRepository;
    private final AssetRepository assetRepository;

    public UptimeService(UptimeLogRepository uptimeLogRepository, AssetRepository assetRepository) {
        this.uptimeLogRepository = uptimeLogRepository;
        this.assetRepository = assetRepository;
    }

    @Transactional(readOnly = true)
    public Page<UptimeLogDTO> getUptimeByAsset(Long assetId, Pageable pageable) {
        if (!assetRepository.existsById(assetId)) {
            throw new ResourceNotFoundException("Asset", "id", assetId);
        }
        return uptimeLogRepository.findByAssetIdOrderByStartTimeDesc(assetId, pageable).map(this::toDTO);
    }

    private UptimeLogDTO toDTO(UptimeLog log) {
        UptimeLogDTO dto = new UptimeLogDTO();
        dto.setId(log.getId());
        dto.setAssetId(log.getAsset().getId());
        dto.setAssetName(log.getAsset().getName());
        dto.setStartTime(log.getStartTime().toString());
        dto.setEndTime(log.getEndTime() != null ? log.getEndTime().toString() : null);
        dto.setStatus(log.getStatus());
        if (log.getEndTime() != null) {
            dto.setDurationMinutes(Duration.between(log.getStartTime(), log.getEndTime()).toMinutes());
        }
        return dto;
    }
}
