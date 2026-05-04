package com.app.assetmonitoringsystem.repository;

import com.app.assetmonitoringsystem.entity.Asset;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Asset entity operations.
 */
@Repository
public interface AssetRepository extends JpaRepository<Asset, Long> {

    List<Asset> findByAssignedToId(Long userId);

    Page<Asset> findByAssignedToId(Long userId, Pageable pageable);

    Page<Asset> findByType(String type, Pageable pageable);

    Page<Asset> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
