package com.app.assetmonitoringsystem.service;

import com.app.assetmonitoringsystem.dto.SensorDataDTO;
import com.app.assetmonitoringsystem.entity.*;
import com.app.assetmonitoringsystem.exception.ResourceNotFoundException;
import com.app.assetmonitoringsystem.repository.AlertRepository;
import com.app.assetmonitoringsystem.repository.AssetRepository;
import com.app.assetmonitoringsystem.repository.SensorDataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SensorDataServiceTest {

    @Mock private SensorDataRepository sensorDataRepository;
    @Mock private AssetRepository assetRepository;
    @Mock private AlertRepository alertRepository;
    @Mock private RabbitTemplate rabbitTemplate;

    @InjectMocks private SensorDataService sensorDataService;

    private Asset testAsset;

    @BeforeEach
    void setUp() {
        testAsset = new Asset("Motor A", "Motor", "Factory 1", 80.0, 100.0);
        testAsset.setId(1L);
    }

    @Test
    void ingestSensorData_NormalValues_NoAlerts() {
        SensorDataDTO dto = new SensorDataDTO(null, 1L, 70.0, 90.0, null, null);
        SensorData savedData = new SensorData(testAsset, 70.0, 90.0);
        savedData.setId(1L);

        when(assetRepository.findById(1L)).thenReturn(Optional.of(testAsset));
        when(sensorDataRepository.save(any(SensorData.class))).thenReturn(savedData);

        SensorDataDTO result = sensorDataService.ingestSensorData(dto);

        assertNotNull(result);
        assertEquals(70.0, result.getTemperature());
        verify(alertRepository, never()).save(any(Alert.class));
    }

    @Test
    void ingestSensorData_TempExceedsThreshold_CreatesAlert() {
        SensorDataDTO dto = new SensorDataDTO(null, 1L, 85.0, 90.0, null, null);
        SensorData savedData = new SensorData(testAsset, 85.0, 90.0);
        savedData.setId(1L);

        when(assetRepository.findById(1L)).thenReturn(Optional.of(testAsset));
        when(sensorDataRepository.save(any(SensorData.class))).thenReturn(savedData);
        when(alertRepository.save(any(Alert.class))).thenReturn(new Alert());

        SensorDataDTO result = sensorDataService.ingestSensorData(dto);

        assertNotNull(result);
        verify(alertRepository, times(1)).save(any(Alert.class));
    }

    @Test
    void ingestSensorData_BothExceedThreshold_CreatesTwoAlerts() {
        SensorDataDTO dto = new SensorDataDTO(null, 1L, 85.0, 110.0, null, null);
        SensorData savedData = new SensorData(testAsset, 85.0, 110.0);
        savedData.setId(1L);

        when(assetRepository.findById(1L)).thenReturn(Optional.of(testAsset));
        when(sensorDataRepository.save(any(SensorData.class))).thenReturn(savedData);
        when(alertRepository.save(any(Alert.class))).thenReturn(new Alert());

        sensorDataService.ingestSensorData(dto);

        verify(alertRepository, times(2)).save(any(Alert.class));
    }

    @Test
    void ingestSensorData_AssetNotFound_ThrowsException() {
        SensorDataDTO dto = new SensorDataDTO(null, 99L, 70.0, 90.0, null, null);
        when(assetRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> sensorDataService.ingestSensorData(dto));
    }
}
