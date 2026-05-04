package com.app.assetmonitoringsystem.service;

import com.app.assetmonitoringsystem.dto.AlertDTO;
import com.app.assetmonitoringsystem.entity.*;
import com.app.assetmonitoringsystem.exception.BadRequestException;
import com.app.assetmonitoringsystem.exception.ResourceNotFoundException;
import com.app.assetmonitoringsystem.repository.AlertRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlertServiceTest {

    @Mock private AlertRepository alertRepository;
    @InjectMocks private AlertService alertService;

    private Alert testAlert;
    private Asset testAsset;

    @BeforeEach
    void setUp() {
        testAsset = new Asset("Motor A", "Motor", "Factory 1", 80.0, 100.0);
        testAsset.setId(1L);
        testAlert = new Alert(testAsset, AlertType.TEMP_HIGH, "Temperature exceeded");
        testAlert.setId(1L);
        testAlert.setTriggeredAt(LocalDateTime.now());
    }

    @Test
    void getAllAlerts_ReturnsPaginatedResults() {
        Page<Alert> page = new PageImpl<>(List.of(testAlert));
        when(alertRepository.findAllByOrderByTriggeredAtDesc(any(PageRequest.class))).thenReturn(page);

        Page<AlertDTO> result = alertService.getAllAlerts(PageRequest.of(0, 10));
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void resolveAlert_Success() {
        when(alertRepository.findById(1L)).thenReturn(Optional.of(testAlert));
        when(alertRepository.save(any(Alert.class))).thenReturn(testAlert);

        AlertDTO result = alertService.resolveAlert(1L);

        assertNotNull(result);
        verify(alertRepository).save(any(Alert.class));
    }

    @Test
    void resolveAlert_AlreadyResolved_ThrowsException() {
        testAlert.setStatus(AlertStatus.RESOLVED);
        when(alertRepository.findById(1L)).thenReturn(Optional.of(testAlert));

        assertThrows(BadRequestException.class, () -> alertService.resolveAlert(1L));
    }

    @Test
    void resolveAlert_NotFound_ThrowsException() {
        when(alertRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> alertService.resolveAlert(99L));
    }

    @Test
    void getActiveAlertCount_ReturnsCount() {
        when(alertRepository.countByStatus(AlertStatus.ACTIVE)).thenReturn(5L);
        assertEquals(5L, alertService.getActiveAlertCount());
    }
}
