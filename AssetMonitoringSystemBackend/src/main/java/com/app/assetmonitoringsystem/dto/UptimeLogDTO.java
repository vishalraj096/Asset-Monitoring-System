package com.app.assetmonitoringsystem.dto;

import com.app.assetmonitoringsystem.entity.UptimeStatus;

/**
 * DTO for uptime log responses.
 */
public class UptimeLogDTO {

    private Long id;
    private Long assetId;
    private String assetName;
    private String startTime;
    private String endTime;
    private UptimeStatus status;
    private Long durationMinutes;

    // Constructors
    public UptimeLogDTO() {}

    public UptimeLogDTO(Long id, Long assetId, String assetName,
                        String startTime, String endTime,
                        UptimeStatus status, Long durationMinutes) {
        this.id = id;
        this.assetId = assetId;
        this.assetName = assetName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.durationMinutes = durationMinutes;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getAssetId() { return assetId; }
    public void setAssetId(Long assetId) { this.assetId = assetId; }

    public String getAssetName() { return assetName; }
    public void setAssetName(String assetName) { this.assetName = assetName; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public UptimeStatus getStatus() { return status; }
    public void setStatus(UptimeStatus status) { this.status = status; }

    public Long getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Long durationMinutes) { this.durationMinutes = durationMinutes; }
}
