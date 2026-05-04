package com.app.assetmonitoringsystem.controller;

import com.app.assetmonitoringsystem.dto.ApiResponse;
import com.app.assetmonitoringsystem.dto.MaintenanceLogDTO;
import com.app.assetmonitoringsystem.service.MaintenanceService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/maintenance")
public class MaintenanceController {
    private final MaintenanceService maintenanceService;

    public MaintenanceController(MaintenanceService maintenanceService) {
        this.maintenanceService = maintenanceService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<MaintenanceLogDTO>> createMaintenance(@Valid @RequestBody MaintenanceLogDTO dto) {
        MaintenanceLogDTO created = maintenanceService.createMaintenanceLog(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Maintenance log created successfully", created));
    }

    @GetMapping("/asset/{id}")
    public ResponseEntity<ApiResponse<Page<MaintenanceLogDTO>>> getMaintenanceByAsset(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<MaintenanceLogDTO> logs = maintenanceService.getMaintenanceByAsset(id, PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.success("Maintenance logs retrieved successfully", logs));
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<MaintenanceLogDTO>> completeMaintenance(
            @PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        String remarks = (body != null) ? body.get("remarks") : null;
        MaintenanceLogDTO completed = maintenanceService.completeMaintenance(id, remarks);
        return ResponseEntity.ok(ApiResponse.success("Maintenance completed successfully", completed));
    }
}
