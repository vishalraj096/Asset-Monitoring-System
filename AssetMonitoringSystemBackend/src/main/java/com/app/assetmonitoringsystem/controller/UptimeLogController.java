package com.app.assetmonitoringsystem.controller;

import com.app.assetmonitoringsystem.dto.ApiResponse;
import com.app.assetmonitoringsystem.dto.UptimeLogDTO;
import com.app.assetmonitoringsystem.service.UptimeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/uptime")
public class UptimeLogController {
    private final UptimeService uptimeService;

    public UptimeLogController(UptimeService uptimeService) {
        this.uptimeService = uptimeService;
    }

    @GetMapping("/asset/{id}")
    public ResponseEntity<ApiResponse<Page<UptimeLogDTO>>> getUptimeByAsset(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<UptimeLogDTO> logs = uptimeService.getUptimeByAsset(id, PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.success("Uptime logs retrieved successfully", logs));
    }
}
