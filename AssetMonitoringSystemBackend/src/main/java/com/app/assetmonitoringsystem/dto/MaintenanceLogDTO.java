package com.app.assetmonitoringsystem.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.FutureOrPresent;
import java.time.LocalDate;

/**
 * DTO for maintenance log creation/update requests.
 */
public class MaintenanceLogDTO {

    private Long id;

    @NotNull(message = "Asset ID is required")
    private Long assetId;

    private String assetName;

    @NotNull(message = "Scheduled date is required")
    private LocalDate scheduledDate;

    private LocalDate completedDate;

    private String remarks;

    private String createdAt;

    // Constructors
    public MaintenanceLogDTO() {}

    public MaintenanceLogDTO(Long id, Long assetId, String assetName,
                             LocalDate scheduledDate, LocalDate completedDate,
                             String remarks, String createdAt) {
        this.id = id;
        this.assetId = assetId;
        this.assetName = assetName;
        this.scheduledDate = scheduledDate;
        this.completedDate = completedDate;
        this.remarks = remarks;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getAssetId() { return assetId; }
    public void setAssetId(Long assetId) { this.assetId = assetId; }

    public String getAssetName() { return assetName; }
    public void setAssetName(String assetName) { this.assetName = assetName; }

    public LocalDate getScheduledDate() { return scheduledDate; }
    public void setScheduledDate(LocalDate scheduledDate) { this.scheduledDate = scheduledDate; }

    public LocalDate getCompletedDate() { return completedDate; }
    public void setCompletedDate(LocalDate completedDate) { this.completedDate = completedDate; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
