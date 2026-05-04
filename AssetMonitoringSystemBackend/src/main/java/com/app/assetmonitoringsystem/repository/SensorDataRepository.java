package com.app.assetmonitoringsystem.repository;

import com.app.assetmonitoringsystem.entity.SensorData;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for SensorData entity operations.
 */
@Repository
public interface SensorDataRepository extends JpaRepository<SensorData, Long> {

    Page<SensorData> findByAssetIdOrderByTimestampDesc(Long assetId, Pageable pageable);

    List<SensorData> findByAssetIdAndTimestampBetween(Long assetId, LocalDateTime start, LocalDateTime end);

    List<SensorData> findTop10ByAssetIdOrderByTimestampDesc(Long assetId);
}
