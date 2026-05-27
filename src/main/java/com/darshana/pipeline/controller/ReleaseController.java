package com.darshana.pipeline.controller;

import com.darshana.pipeline.dto.ReleaseDTO;
import com.darshana.pipeline.dto.ReleaseSummaryDTO;
import com.darshana.pipeline.entity.ReleaseStage;
import com.darshana.pipeline.service.ReleaseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Release management.
 *
 * Base URL: /api/releases
 *
 * Endpoints:
 *   POST   /api/releases                          → Create release
 *   GET    /api/releases                          → List all (paginated)
 *   GET    /api/releases/{id}                     → Get by ID
 *   GET    /api/releases?stage=IN_PROGRESS        → Filter by stage
 *   GET    /api/releases/search?keyword=payment   → Search by name
 *   PUT    /api/releases/{id}                     → Update release
 *   PATCH  /api/releases/{id}/advance             → Advance to next stage
 *   PATCH  /api/releases/{id}/cancel              → Cancel release
 *   GET    /api/releases/{id}/summary             → Generate summary report
 *   DELETE /api/releases/{id}                     → Delete release
 */
@RestController
@RequestMapping("/api/releases")
@RequiredArgsConstructor
public class ReleaseController {

    private final ReleaseService releaseService;

    @PostMapping
    public ResponseEntity<ReleaseDTO> createRelease(@Valid @RequestBody ReleaseDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(releaseService.createRelease(dto));
    }

    @GetMapping
    public ResponseEntity<Page<ReleaseDTO>> getAllReleases(
            @RequestParam(required = false) ReleaseStage stage,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {

        if (stage != null) {
            return ResponseEntity.ok(releaseService.getReleasesByStage(stage, pageable));
        }
        return ResponseEntity.ok(releaseService.getAllReleases(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReleaseDTO> getReleaseById(@PathVariable Long id) {
        return ResponseEntity.ok(releaseService.getReleaseById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<ReleaseDTO>> searchReleases(
            @RequestParam String keyword,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(releaseService.searchReleases(keyword, pageable));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReleaseDTO> updateRelease(
            @PathVariable Long id,
            @Valid @RequestBody ReleaseDTO dto) {
        return ResponseEntity.ok(releaseService.updateRelease(id, dto));
    }

    /**
     * Advance release to next stage.
     * Enforces business rules — see ReleaseService.advanceStage() for full logic.
     */
    @PatchMapping("/{id}/advance")
    public ResponseEntity<ReleaseDTO> advanceStage(@PathVariable Long id) {
        return ResponseEntity.ok(releaseService.advanceStage(id));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ReleaseDTO> cancelRelease(@PathVariable Long id) {
        return ResponseEntity.ok(releaseService.cancelRelease(id));
    }

    /**
     * Generate a comprehensive summary report for a release.
     * Returns completion %, task breakdown, blocked tasks, and readiness status.
     */
    @GetMapping("/{id}/summary")
    public ResponseEntity<ReleaseSummaryDTO> generateSummary(@PathVariable Long id) {
        return ResponseEntity.ok(releaseService.generateSummary(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRelease(@PathVariable Long id) {
        releaseService.deleteRelease(id);
        return ResponseEntity.noContent().build();
    }
}
