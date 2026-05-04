package com.app.assetmonitoringsystem.controller;

import com.app.assetmonitoringsystem.dto.ApiResponse;
import com.app.assetmonitoringsystem.dto.SensorDataDTO;
import com.app.assetmonitoringsystem.service.SensorDataService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sensors")
public class SensorDataController {
    private final SensorDataService sensorDataService;

    public SensorDataController(SensorDataService sensorDataService) {
        this.sensorDataService = sensorDataService;
    }

    @PostMapping("/send-data")
    public ResponseEntity<ApiResponse<SensorDataDTO>> sendSensorData(@Valid @RequestBody SensorDataDTO dto) {
        SensorDataDTO result = sensorDataService.ingestSensorData(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Sensor data ingested successfully", result));
    }

    @GetMapping("/asset/{id}")
    public ResponseEntity<ApiResponse<Page<SensorDataDTO>>> getSensorDataByAsset(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<SensorDataDTO> data = sensorDataService.getSensorDataByAsset(id, PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.success("Sensor data retrieved successfully", data));
    }
}
