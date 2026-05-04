package com.app.assetmonitoringsystem.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Entity representing a factory asset (machine, equipment).
 */
@Entity
@Table(name = "assets")
public class Asset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String type;

    private String location;

    @Column(nullable = false)
    private Double thresholdTemp;

    @Column(nullable = false)
    private Double thresholdPressure;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to")
    private User assignedTo;

    @OneToMany(mappedBy = "asset", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<SensorData> sensorDataList;

    @OneToMany(mappedBy = "asset", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Alert> alerts;

    @OneToMany(mappedBy = "asset", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MaintenanceLog> maintenanceLogs;

    @OneToMany(mappedBy = "asset", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UptimeLog> uptimeLogs;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Constructors
    public Asset() {}

    public Asset(String name, String type, String location, Double thresholdTemp, Double thresholdPressure) {
        this.name = name;
        this.type = type;
        this.location = location;
        this.thresholdTemp = thresholdTemp;
        this.thresholdPressure = thresholdPressure;
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

    public User getAssignedTo() { return assignedTo; }
    public void setAssignedTo(User assignedTo) { this.assignedTo = assignedTo; }

    public List<SensorData> getSensorDataList() { return sensorDataList; }
    public void setSensorDataList(List<SensorData> sensorDataList) { this.sensorDataList = sensorDataList; }

    public List<Alert> getAlerts() { return alerts; }
    public void setAlerts(List<Alert> alerts) { this.alerts = alerts; }

    public List<MaintenanceLog> getMaintenanceLogs() { return maintenanceLogs; }
    public void setMaintenanceLogs(List<MaintenanceLog> maintenanceLogs) { this.maintenanceLogs = maintenanceLogs; }

    public List<UptimeLog> getUptimeLogs() { return uptimeLogs; }
    public void setUptimeLogs(List<UptimeLog> uptimeLogs) { this.uptimeLogs = uptimeLogs; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
