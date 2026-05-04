package com.app.assetmonitoringsystem.repository;

import com.app.assetmonitoringsystem.entity.MaintenanceLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository for MaintenanceLog entity operations.
 */
@Repository
public interface MaintenanceLogRepository extends JpaRepository<MaintenanceLog, Long> {

    Page<MaintenanceLog> findByAssetIdOrderByScheduledDateDesc(Long assetId, Pageable pageable);

    List<MaintenanceLog> findByScheduledDateBetween(LocalDate start, LocalDate end);

    List<MaintenanceLog> findByCompletedDateIsNull();
}
