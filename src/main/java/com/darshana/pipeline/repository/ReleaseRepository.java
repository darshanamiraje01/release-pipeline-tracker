package com.darshana.pipeline.repository;

import com.darshana.pipeline.entity.Release;
import com.darshana.pipeline.entity.ReleaseStage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReleaseRepository extends JpaRepository<Release, Long> {

    // Filter by stage
    Page<Release> findByStage(ReleaseStage stage, Pageable pageable);

    // Filter by creator
    List<Release> findByCreatedBy(String createdBy);

    // Search by name containing keyword
    Page<Release> findByNameContainingIgnoreCase(String keyword, Pageable pageable);

    // Count releases per stage (for dashboard)
    @Query("SELECT r.stage, COUNT(r) FROM Release r GROUP BY r.stage")
    List<Object[]> countByStage();

    // Check for duplicate version
    boolean existsByVersion(String version);
}
