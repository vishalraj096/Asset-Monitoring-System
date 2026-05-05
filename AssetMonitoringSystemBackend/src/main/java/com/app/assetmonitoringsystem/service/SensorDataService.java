package com.app.assetmonitoringsystem.service;

import com.app.assetmonitoringsystem.config.RabbitMQConfig;
import com.app.assetmonitoringsystem.dto.SensorDataDTO;
import com.app.assetmonitoringsystem.entity.*;
import com.app.assetmonitoringsystem.exception.ResourceNotFoundException;
import com.app.assetmonitoringsystem.repository.AlertRepository;
import com.app.assetmonitoringsystem.repository.AssetRepository;
import com.app.assetmonitoringsystem.repository.SensorDataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Service handling sensor data ingestion, threshold checking, and alert triggering.
 */
@Service
public class SensorDataService {

    private static final Logger logger = LoggerFactory.getLogger(SensorDataService.class);

    private final SensorDataRepository sensorDataRepository;
    private final AssetRepository assetRepository;
    private final AlertRepository alertRepository;
    private final RabbitTemplate rabbitTemplate;

    public SensorDataService(SensorDataRepository sensorDataRepository,
                             AssetRepository assetRepository,
                             AlertRepository alertRepository,
                             RabbitTemplate rabbitTemplate) {
        this.sensorDataRepository = sensorDataRepository;
        this.assetRepository = assetRepository;
        this.alertRepository = alertRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Ingest sensor data, check thresholds, and trigger alerts if needed.
     * Null-safe: skips threshold check if the asset's threshold value is null.
     */
    @Transactional
    public SensorDataDTO ingestSensorData(SensorDataDTO dto) {
        Asset asset = assetRepository.findById(dto.getAssetId())
                .orElseThrow(() -> new ResourceNotFoundException("Asset", "id", dto.getAssetId()));

        // Save sensor data
        SensorData sensorData = new SensorData();
        sensorData.setAsset(asset);
        sensorData.setTemperature(dto.getTemperature());
        sensorData.setPressure(dto.getPressure());
        sensorData.setTimestamp(LocalDateTime.now());

        SensorData saved = sensorDataRepository.save(sensorData);
        logger.info("Sensor data ingested for Asset #{}: temp={}, pressure={}",
                asset.getId(), dto.getTemperature(), dto.getPressure());

        // Check temperature threshold (null-safe to prevent NPE)
        if (asset.getThresholdTemp() != null && dto.getTemperature() != null
                && dto.getTemperature() > asset.getThresholdTemp()) {
            String message = String.format(
                    "ALERT: Temperature %.1f°C exceeds threshold %.1f°C for asset '%s' (ID: %d)",
                    dto.getTemperature(), asset.getThresholdTemp(), asset.getName(), asset.getId());

            Alert alert = new Alert(asset, AlertType.TEMP_HIGH, message);
            alertRepository.save(alert);
            logger.warn(message);

            // Publish alert to RabbitMQ
            publishToRabbitMQ(message);
        }

        // Check pressure threshold (null-safe to prevent NPE)
        if (asset.getThresholdPressure() != null && dto.getPressure() != null
                && dto.getPressure() > asset.getThresholdPressure()) {
            String message = String.format(
                    "ALERT: Pressure %.1f exceeds threshold %.1f for asset '%s' (ID: %d)",
                    dto.getPressure(), asset.getThresholdPressure(), asset.getName(), asset.getId());

            Alert alert = new Alert(asset, AlertType.PRESSURE_HIGH, message);
            alertRepository.save(alert);
            logger.warn(message);

            // Publish alert to RabbitMQ
            publishToRabbitMQ(message);
        }

        return toDTO(saved);
    }

    /**
     * Get sensor data for an asset with pagination.
     */
    @Transactional(readOnly = true)
    public Page<SensorDataDTO> getSensorDataByAsset(Long assetId, Pageable pageable) {
        if (!assetRepository.existsById(assetId)) {
            throw new ResourceNotFoundException("Asset", "id", assetId);
        }
        return sensorDataRepository.findByAssetIdOrderByTimestampDesc(assetId, pageable)
                .map(this::toDTO);
    }

    private void publishToRabbitMQ(String message) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.ALERT_EXCHANGE,
                    RabbitMQConfig.ALERT_ROUTING_KEY,
                    message);
        } catch (Exception e) {
            logger.warn("RabbitMQ unavailable, alert logged to console: {}", message);
        }
    }

    private SensorDataDTO toDTO(SensorData data) {
        SensorDataDTO dto = new SensorDataDTO();
        dto.setId(data.getId());
        dto.setAssetId(data.getAsset().getId());
        dto.setAssetName(data.getAsset().getName());
        dto.setTemperature(data.getTemperature());
        dto.setPressure(data.getPressure());
        dto.setTimestamp(data.getTimestamp().toString());
        return dto;
    }
}
