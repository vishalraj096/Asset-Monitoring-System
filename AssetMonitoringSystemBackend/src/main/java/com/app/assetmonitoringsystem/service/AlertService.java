package com.app.assetmonitoringsystem.service;

import com.app.assetmonitoringsystem.dto.AlertDTO;
import com.app.assetmonitoringsystem.entity.Alert;
import com.app.assetmonitoringsystem.entity.AlertStatus;
import com.app.assetmonitoringsystem.exception.BadRequestException;
import com.app.assetmonitoringsystem.exception.ResourceNotFoundException;
import com.app.assetmonitoringsystem.repository.AlertRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Service handling alert retrieval and resolution.
 */
@Service
public class AlertService {

    private static final Logger logger = LoggerFactory.getLogger(AlertService.class);

    private final AlertRepository alertRepository;

    public AlertService(AlertRepository alertRepository) {
        this.alertRepository = alertRepository;
    }

    /**
     * Get all alerts with pagination.
     */
    @Transactional(readOnly = true)
    public Page<AlertDTO> getAllAlerts(Pageable pageable) {
        return alertRepository.findAllByOrderByTriggeredAtDesc(pageable)
                .map(this::toDTO);
    }

    /**
     * Get alerts by status with pagination.
     */
    @Transactional(readOnly = true)
    public Page<AlertDTO> getAlertsByStatus(AlertStatus status, Pageable pageable) {
        return alertRepository.findByStatusOrderByTriggeredAtDesc(status, pageable)
                .map(this::toDTO);
    }

    /**
     * Get alerts for a specific asset with pagination.
     */
    @Transactional(readOnly = true)
    public Page<AlertDTO> getAlertsByAsset(Long assetId, Pageable pageable) {
        return alertRepository.findByAssetIdOrderByTriggeredAtDesc(assetId, pageable)
                .map(this::toDTO);
    }

    /**
     * Resolve an active alert.
     */
    @Transactional
    public AlertDTO resolveAlert(Long alertId) {
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("Alert", "id", alertId));

        if (alert.getStatus() == AlertStatus.RESOLVED) {
            throw new BadRequestException("Alert is already resolved");
        }

        alert.setStatus(AlertStatus.RESOLVED);
        alert.setResolvedAt(LocalDateTime.now());
        alertRepository.save(alert);
        logger.info("Alert #{} resolved for Asset #{}", alertId, alert.getAsset().getId());

        return toDTO(alert);
    }

    /**
     * Get count of active alerts.
     */
    @Transactional(readOnly = true)
    public long getActiveAlertCount() {
        return alertRepository.countByStatus(AlertStatus.ACTIVE);
    }

    private AlertDTO toDTO(Alert alert) {
        AlertDTO dto = new AlertDTO();
        dto.setId(alert.getId());
        dto.setAssetId(alert.getAsset().getId());
        dto.setAssetName(alert.getAsset().getName());
        dto.setType(alert.getType());
        dto.setMessage(alert.getMessage());
        dto.setStatus(alert.getStatus());
        dto.setTriggeredAt(alert.getTriggeredAt().toString());
        dto.setResolvedAt(alert.getResolvedAt() != null ? alert.getResolvedAt().toString() : null);
        return dto;
    }
}
