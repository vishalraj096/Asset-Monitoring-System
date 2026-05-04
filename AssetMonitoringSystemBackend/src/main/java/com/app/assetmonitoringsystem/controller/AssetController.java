package com.app.assetmonitoringsystem.controller;

import com.app.assetmonitoringsystem.dto.ApiResponse;
import com.app.assetmonitoringsystem.dto.AssetDTO;
import com.app.assetmonitoringsystem.service.AssetService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/assets")
public class AssetController {
    private final AssetService assetService;

    public AssetController(AssetService assetService) {
        this.assetService = assetService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_MANAGER')")
    public ResponseEntity<ApiResponse<AssetDTO>> createAsset(@Valid @RequestBody AssetDTO dto) {
        AssetDTO created = assetService.createAsset(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Asset created successfully", created));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<AssetDTO>>> getAllAssets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<AssetDTO> assets = assetService.getAllAssets(pageable);
        return ResponseEntity.ok(ApiResponse.success("Assets retrieved successfully", assets));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AssetDTO>> getAssetById(@PathVariable Long id) {
        AssetDTO asset = assetService.getAssetById(id);
        return ResponseEntity.ok(ApiResponse.success("Asset retrieved successfully", asset));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_MANAGER')")
    public ResponseEntity<ApiResponse<AssetDTO>> updateAsset(@PathVariable Long id, @Valid @RequestBody AssetDTO dto) {
        AssetDTO updated = assetService.updateAsset(id, dto);
        return ResponseEntity.ok(ApiResponse.success("Asset updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> deleteAsset(@PathVariable Long id) {
        assetService.deleteAsset(id);
        return ResponseEntity.ok(ApiResponse.success("Asset deleted successfully"));
    }
}
