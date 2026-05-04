package com.app.assetmonitoringsystem.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity representing an uptime/downtime period for an asset.
 */
@Entity
@Table(name = "uptime_logs")
public class UptimeLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    @Column(nullable = false)
    private LocalDateTime startTime;

    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UptimeStatus status;

    // Constructors
    public UptimeLog() {}

    public UptimeLog(Asset asset, LocalDateTime startTime, UptimeStatus status) {
        this.asset = asset;
        this.startTime = startTime;
        this.status = status;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Asset getAsset() { return asset; }
    public void setAsset(Asset asset) { this.asset = asset; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public UptimeStatus getStatus() { return status; }
    public void setStatus(UptimeStatus status) { this.status = status; }
}
