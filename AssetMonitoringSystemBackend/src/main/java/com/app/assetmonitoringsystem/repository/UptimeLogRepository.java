package com.app.assetmonitoringsystem.repository;

import com.app.assetmonitoringsystem.entity.UptimeLog;
import com.app.assetmonitoringsystem.entity.UptimeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for UptimeLog entity operations.
 */
@Repository
public interface UptimeLogRepository extends JpaRepository<UptimeLog, Long> {

    Page<UptimeLog> findByAssetIdOrderByStartTimeDesc(Long assetId, Pageable pageable);

    Optional<UptimeLog> findTopByAssetIdAndEndTimeIsNullOrderByStartTimeDesc(Long assetId);

    List<UptimeLog> findByAssetIdAndStartTimeBetween(Long assetId, LocalDateTime start, LocalDateTime end);

    List<UptimeLog> findByAssetIdAndStatus(Long assetId, UptimeStatus status);
}
