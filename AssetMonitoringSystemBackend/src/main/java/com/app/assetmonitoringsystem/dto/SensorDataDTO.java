package com.app.assetmonitoringsystem.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * DTO for sending simulated sensor data.
 */
public class SensorDataDTO {

    private Long id;

    @NotNull(message = "Asset ID is required")
    private Long assetId;

    @NotNull(message = "Temperature is required")
    private Double temperature;

    @NotNull(message = "Pressure is required")
    private Double pressure;

    private String timestamp;

    private String assetName;

    // Constructors
    public SensorDataDTO() {}

    public SensorDataDTO(Long id, Long assetId, Double temperature, Double pressure, String timestamp, String assetName) {
        this.id = id;
        this.assetId = assetId;
        this.temperature = temperature;
        this.pressure = pressure;
        this.timestamp = timestamp;
        this.assetName = assetName;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getAssetId() { return assetId; }
    public void setAssetId(Long assetId) { this.assetId = assetId; }

    public Double getTemperature() { return temperature; }
    public void setTemperature(Double temperature) { this.temperature = temperature; }

    public Double getPressure() { return pressure; }
    public void setPressure(Double pressure) { this.pressure = pressure; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public String getAssetName() { return assetName; }
    public void setAssetName(String assetName) { this.assetName = assetName; }
}
