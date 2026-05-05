package com.app.assetmonitoringsystem.repository;

import com.app.assetmonitoringsystem.entity.Alert;
import com.app.assetmonitoringsystem.entity.AlertStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Alert entity operations.
 */
@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {

    Page<Alert> findAllByOrderByTriggeredAtDesc(Pageable pageable);

    Page<Alert> findByStatusOrderByTriggeredAtDesc(AlertStatus status, Pageable pageable);

    List<Alert> findByAssetIdAndStatus(Long assetId, AlertStatus status);

    Page<Alert> findByAssetIdOrderByTriggeredAtDesc(Long assetId, Pageable pageable);

    long countByStatus(AlertStatus status);

    long countByAssetIdAndStatus(Long assetId, AlertStatus status);
}
