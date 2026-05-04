package com.app.assetmonitoringsystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * DTO for asset creation and update requests.
 */
public class AssetDTO {

    private Long id;

    @NotBlank(message = "Asset name is required")
    private String name;

    @NotBlank(message = "Asset type is required")
    private String type;

    private String location;

    @NotNull(message = "Temperature threshold is required")
    @Positive(message = "Temperature threshold must be positive")
    private Double thresholdTemp;

    @NotNull(message = "Pressure threshold is required")
    @Positive(message = "Pressure threshold must be positive")
    private Double thresholdPressure;

    private Long assignedToId;

    private String assignedToName;

    // Constructors
    public AssetDTO() {}

    public AssetDTO(Long id, String name, String type, String location,
                    Double thresholdTemp, Double thresholdPressure,
                    Long assignedToId, String assignedToName) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.location = location;
        this.thresholdTemp = thresholdTemp;
        this.thresholdPressure = thresholdPressure;
        this.assignedToId = assignedToId;
        this.assignedToName = assignedToName;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public Double getThresholdTemp() { return thresholdTemp; }
    public void setThresholdTemp(Double thresholdTemp) { this.thresholdTemp = thresholdTemp; }

    public Double getThresholdPressure() { return thresholdPressure; }
    public void setThresholdPressure(Double thresholdPressure) { this.thresholdPressure = thresholdPressure; }

    public Long getAssignedToId() { return assignedToId; }
    public void setAssignedToId(Long assignedToId) { this.assignedToId = assignedToId; }

    public String getAssignedToName() { return assignedToName; }
    public void setAssignedToName(String assignedToName) { this.assignedToName = assignedToName; }
}
