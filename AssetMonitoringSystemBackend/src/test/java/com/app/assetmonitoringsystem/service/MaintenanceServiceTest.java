package com.app.assetmonitoringsystem.service;

import com.app.assetmonitoringsystem.dto.MaintenanceLogDTO;
import com.app.assetmonitoringsystem.entity.Asset;
import com.app.assetmonitoringsystem.entity.MaintenanceLog;
import com.app.assetmonitoringsystem.exception.ResourceNotFoundException;
import com.app.assetmonitoringsystem.repository.AssetRepository;
import com.app.assetmonitoringsystem.repository.MaintenanceLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MaintenanceServiceTest {

    @Mock private MaintenanceLogRepository maintenanceLogRepository;
    @Mock private AssetRepository assetRepository;
    @InjectMocks private MaintenanceService maintenanceService;

    private Asset testAsset;

    @BeforeEach
    void setUp() {
        testAsset = new Asset("Motor A", "Motor", "Factory 1", 80.0, 100.0);
        testAsset.setId(1L);
    }

    @Test
    void createMaintenanceLog_Success() {
        MaintenanceLogDTO dto = new MaintenanceLogDTO();
        dto.setAssetId(1L);
        dto.setScheduledDate(LocalDate.now().plusDays(7));
        dto.setRemarks("Routine check");

        MaintenanceLog saved = new MaintenanceLog(testAsset, dto.getScheduledDate(), "Routine check");
        saved.setId(1L);

        when(assetRepository.findById(1L)).thenReturn(Optional.of(testAsset));
        when(maintenanceLogRepository.save(any(MaintenanceLog.class))).thenReturn(saved);

        MaintenanceLogDTO result = maintenanceService.createMaintenanceLog(dto);

        assertNotNull(result);
        assertEquals("Motor A", result.getAssetName());
        verify(maintenanceLogRepository).save(any(MaintenanceLog.class));
    }

    @Test
    void createMaintenanceLog_AssetNotFound_ThrowsException() {
        MaintenanceLogDTO dto = new MaintenanceLogDTO();
        dto.setAssetId(99L);
        dto.setScheduledDate(LocalDate.now());

        when(assetRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> maintenanceService.createMaintenanceLog(dto));
    }

    @Test
    void completeMaintenance_Success() {
        MaintenanceLog log = new MaintenanceLog(testAsset, LocalDate.now(), "Scheduled");
        log.setId(1L);

        when(maintenanceLogRepository.findById(1L)).thenReturn(Optional.of(log));
        when(maintenanceLogRepository.save(any(MaintenanceLog.class))).thenReturn(log);

        MaintenanceLogDTO result = maintenanceService.completeMaintenance(1L, "Completed OK");

        assertNotNull(result);
        verify(maintenanceLogRepository).save(any(MaintenanceLog.class));
    }
}
