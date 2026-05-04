package com.app.assetmonitoringsystem.controller;

import com.app.assetmonitoringsystem.dto.AlertDTO;
import com.app.assetmonitoringsystem.dto.ApiResponse;
import com.app.assetmonitoringsystem.entity.AlertStatus;
import com.app.assetmonitoringsystem.service.AlertService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {
    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<AlertDTO>>> getAllAlerts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status) {
        Page<AlertDTO> alerts;
        if (status != null) {
            alerts = alertService.getAlertsByStatus(AlertStatus.valueOf(status.toUpperCase()), PageRequest.of(page, size));
        } else {
            alerts = alertService.getAllAlerts(PageRequest.of(page, size));
        }
        return ResponseEntity.ok(ApiResponse.success("Alerts retrieved successfully", alerts));
    }

    @PutMapping("/{id}/resolve")
    public ResponseEntity<ApiResponse<AlertDTO>> resolveAlert(@PathVariable Long id) {
        AlertDTO resolved = alertService.resolveAlert(id);
        return ResponseEntity.ok(ApiResponse.success("Alert resolved successfully", resolved));
    }

    @GetMapping("/count/active")
    public ResponseEntity<ApiResponse<Long>> getActiveAlertCount() {
        long count = alertService.getActiveAlertCount();
        return ResponseEntity.ok(ApiResponse.success("Active alert count", count));
    }
}
