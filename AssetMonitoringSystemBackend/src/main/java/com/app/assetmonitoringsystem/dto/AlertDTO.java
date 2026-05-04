package com.app.assetmonitoringsystem.dto;

import com.app.assetmonitoringsystem.entity.AlertStatus;
import com.app.assetmonitoringsystem.entity.AlertType;

/**
 * DTO for alert responses.
 */
public class AlertDTO {

    private Long id;
    private Long assetId;
    private String assetName;
    private AlertType type;
    private String message;
    private AlertStatus status;
    private String triggeredAt;
    private String resolvedAt;

    // Constructors
    public AlertDTO() {}

    public AlertDTO(Long id, Long assetId, String assetName, AlertType type,
                    String message, AlertStatus status, String triggeredAt, String resolvedAt) {
        this.id = id;
        this.assetId = assetId;
        this.assetName = assetName;
        this.type = type;
        this.message = message;
        this.status = status;
        this.triggeredAt = triggeredAt;
        this.resolvedAt = resolvedAt;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getAssetId() { return assetId; }
    public void setAssetId(Long assetId) { this.assetId = assetId; }

    public String getAssetName() { return assetName; }
    public void setAssetName(String assetName) { this.assetName = assetName; }

    public AlertType getType() { return type; }
    public void setType(AlertType type) { this.type = type; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public AlertStatus getStatus() { return status; }
    public void setStatus(AlertStatus status) { this.status = status; }

    public String getTriggeredAt() { return triggeredAt; }
    public void setTriggeredAt(String triggeredAt) { this.triggeredAt = triggeredAt; }

    public String getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(String resolvedAt) { this.resolvedAt = resolvedAt; }
}
