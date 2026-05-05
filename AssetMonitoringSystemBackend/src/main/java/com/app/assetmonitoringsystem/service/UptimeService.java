package com.app.assetmonitoringsystem.service;

import com.app.assetmonitoringsystem.dto.UptimeLogDTO;
import com.app.assetmonitoringsystem.entity.Asset;
import com.app.assetmonitoringsystem.entity.UptimeLog;
import com.app.assetmonitoringsystem.entity.UptimeStatus;
import com.app.assetmonitoringsystem.exception.ResourceNotFoundException;
import com.app.assetmonitoringsystem.repository.AlertRepository;
import com.app.assetmonitoringsystem.repository.AssetRepository;
import com.app.assetmonitoringsystem.repository.SensorDataRepository;
import com.app.assetmonitoringsystem.repository.UptimeLogRepository;
import com.app.assetmonitoringsystem.entity.AlertStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service handling uptime/downtime tracking for assets.
 * Includes a scheduled job that periodically evaluates asset status
 * based on active alerts and records uptime/downtime transitions.
 */
@Service
public class UptimeService {
    private static final Logger logger = LoggerFactory.getLogger(UptimeService.class);
    private final UptimeLogRepository uptimeLogRepository;
    private final AssetRepository assetRepository;
    private final AlertRepository alertRepository;

    public UptimeService(UptimeLogRepository uptimeLogRepository,
                         AssetRepository assetRepository,
                         AlertRepository alertRepository) {
        this.uptimeLogRepository = uptimeLogRepository;
        this.assetRepository = assetRepository;
        this.alertRepository = alertRepository;
    }

    @Transactional(readOnly = true)
    public Page<UptimeLogDTO> getUptimeByAsset(Long assetId, Pageable pageable) {
        if (!assetRepository.existsById(assetId)) {
            throw new ResourceNotFoundException("Asset", "id", assetId);
        }
        return uptimeLogRepository.findByAssetIdOrderByStartTimeDesc(assetId, pageable).map(this::toDTO);
    }

    /**
     * Scheduled job that runs every 60 seconds to evaluate the status of all assets.
     * 
     * Logic:
     * - If an asset has any ACTIVE alerts → status = DOWN
     * - If an asset has no ACTIVE alerts → status = UP
     * - When status changes, close the current uptime log (set endTime) and create a new one.
     * - This provides automatic uptime/downtime tracking as a bonus feature.
     */
    @Scheduled(fixedRate = 60000) // Every 60 seconds
    @Transactional
    public void evaluateAssetStatuses() {
        List<Asset> assets = assetRepository.findAll();
        LocalDateTime now = LocalDateTime.now();

        for (Asset asset : assets) {
            // Determine current status based on active alerts
            long activeAlertCount = alertRepository.countByAssetIdAndStatus(asset.getId(), AlertStatus.ACTIVE);
            UptimeStatus currentStatus = (activeAlertCount > 0) ? UptimeStatus.DOWN : UptimeStatus.UP;

            // Find the latest open uptime log (no endTime)
            Optional<UptimeLog> latestLogOpt = uptimeLogRepository
                    .findTopByAssetIdAndEndTimeIsNullOrderByStartTimeDesc(asset.getId());

            if (latestLogOpt.isPresent()) {
                UptimeLog latestLog = latestLogOpt.get();

                // If status has changed, close the old log and open a new one
                if (latestLog.getStatus() != currentStatus) {
                    latestLog.setEndTime(now);
                    uptimeLogRepository.save(latestLog);

                    UptimeLog newLog = new UptimeLog(asset, now, currentStatus);
                    uptimeLogRepository.save(newLog);

                    logger.info("Asset #{} '{}' status changed: {} → {}",
                            asset.getId(), asset.getName(), latestLog.getStatus(), currentStatus);
                }
                // If status is the same, do nothing — the log stays open
            } else {
                // No open log exists — create one (handles assets created before scheduling was added)
                UptimeLog newLog = new UptimeLog(asset, now, currentStatus);
                uptimeLogRepository.save(newLog);
                logger.info("Asset #{} '{}' initial uptime log created with status: {}",
                        asset.getId(), asset.getName(), currentStatus);
            }
        }
    }

    /**
     * Calculate uptime summary for an asset — total up/down minutes and uptime percentage.
     * Used by the frontend uptime chart visualization.
     */
    @Transactional(readOnly = true)
    public java.util.Map<String, Object> getUptimeSummary(Long assetId) {
        if (!assetRepository.existsById(assetId)) {
            throw new ResourceNotFoundException("Asset", "id", assetId);
        }

        List<UptimeLog> allLogs = uptimeLogRepository.findByAssetIdAndStatus(assetId, UptimeStatus.UP);
        List<UptimeLog> downLogs = uptimeLogRepository.findByAssetIdAndStatus(assetId, UptimeStatus.DOWN);

        LocalDateTime now = LocalDateTime.now();

        long totalUpMinutes = calculateTotalMinutes(allLogs, now);
        long totalDownMinutes = calculateTotalMinutes(downLogs, now);
        long totalMinutes = totalUpMinutes + totalDownMinutes;

        double uptimePercentage = totalMinutes > 0 ? (double) totalUpMinutes / totalMinutes * 100.0 : 100.0;

        java.util.Map<String, Object> summary = new java.util.LinkedHashMap<>();
        summary.put("totalUpMinutes", totalUpMinutes);
        summary.put("totalDownMinutes", totalDownMinutes);
        summary.put("totalMinutes", totalMinutes);
        summary.put("uptimePercentage", Math.round(uptimePercentage * 100.0) / 100.0);

        // Current status
        Optional<UptimeLog> latestOpen = uptimeLogRepository
                .findTopByAssetIdAndEndTimeIsNullOrderByStartTimeDesc(assetId);
        summary.put("currentStatus", latestOpen.map(log -> log.getStatus().name()).orElse("UNKNOWN"));

        return summary;
    }

    private long calculateTotalMinutes(List<UptimeLog> logs, LocalDateTime now) {
        long total = 0;
        for (UptimeLog log : logs) {
            LocalDateTime end = log.getEndTime() != null ? log.getEndTime() : now;
            total += Duration.between(log.getStartTime(), end).toMinutes();
        }
        return total;
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
