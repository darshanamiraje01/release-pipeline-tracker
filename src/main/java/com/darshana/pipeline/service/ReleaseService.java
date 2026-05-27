package com.darshana.pipeline.service;

import com.darshana.pipeline.dto.ReleaseDTO;
import com.darshana.pipeline.dto.ReleaseSummaryDTO;
import com.darshana.pipeline.entity.*;
import com.darshana.pipeline.exception.InvalidStageTransitionException;
import com.darshana.pipeline.exception.ResourceNotFoundException;
import com.darshana.pipeline.repository.ReleaseRepository;
import com.darshana.pipeline.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Core business logic for Release management.
 *
 * Key interview talking point — Stage Transition Rules:
 *   PLANNING → IN_PROGRESS  (requires at least 1 task)
 *   IN_PROGRESS → TESTING   (requires no BLOCKED tasks)
 *   TESTING → COMPLETED     (requires all tasks to be DONE)
 *   Any stage → CANCELLED   (always allowed)
 */
@Service
@RequiredArgsConstructor
public class ReleaseService {

    private final ReleaseRepository releaseRepository;
    private final TaskRepository taskRepository;

    // ── CREATE ──────────────────────────────────────────────

    @Transactional
    public ReleaseDTO createRelease(ReleaseDTO dto) {
        if (releaseRepository.existsByVersion(dto.getVersion())) {
            throw new InvalidStageTransitionException(
                "Release version " + dto.getVersion() + " already exists.");
        }
        Release release = Release.builder()
                .name(dto.getName())
                .version(dto.getVersion())
                .description(dto.getDescription())
                .stage(ReleaseStage.PLANNING)
                .createdBy(dto.getCreatedBy() != null ? dto.getCreatedBy() : "system")
                .build();
        return toDTO(releaseRepository.save(release));
    }

    // ── READ ─────────────────────────────────────────────────

    public ReleaseDTO getReleaseById(Long id) {
        return toDTO(findOrThrow(id));
    }

    public Page<ReleaseDTO> getAllReleases(Pageable pageable) {
        return releaseRepository.findAll(pageable).map(this::toDTO);
    }

    public Page<ReleaseDTO> getReleasesByStage(ReleaseStage stage, Pageable pageable) {
        return releaseRepository.findByStage(stage, pageable).map(this::toDTO);
    }

    public Page<ReleaseDTO> searchReleases(String keyword, Pageable pageable) {
        return releaseRepository.findByNameContainingIgnoreCase(keyword, pageable).map(this::toDTO);
    }

    // ── UPDATE ────────────────────────────────────────────────

    @Transactional
    public ReleaseDTO updateRelease(Long id, ReleaseDTO dto) {
        Release release = findOrThrow(id);
        release.setName(dto.getName());
        release.setVersion(dto.getVersion());
        release.setDescription(dto.getDescription());
        return toDTO(releaseRepository.save(release));
    }

    // ── STAGE ADVANCEMENT ─────────────────────────────────────

    /**
     * Advance a release to the next stage with business rule validation.
     *
     * Rules:
     * PLANNING → IN_PROGRESS : must have at least 1 task defined
     * IN_PROGRESS → TESTING  : no tasks can be in BLOCKED status
     * TESTING → COMPLETED    : ALL tasks must be in DONE status
     * Any → CANCELLED        : always permitted
     */
    @Transactional
    public ReleaseDTO advanceStage(Long releaseId) {
        Release release = findOrThrow(releaseId);
        ReleaseStage current = release.getStage();
        List<Task> tasks = taskRepository.findByReleaseId(releaseId);

        ReleaseStage next = switch (current) {
            case PLANNING -> {
                if (tasks.isEmpty()) {
                    throw new InvalidStageTransitionException(
                        "Cannot advance from PLANNING: release has no tasks. Add at least one task first.");
                }
                yield ReleaseStage.IN_PROGRESS;
            }
            case IN_PROGRESS -> {
                long blocked = tasks.stream()
                        .filter(t -> t.getStatus() == TaskStatus.BLOCKED).count();
                if (blocked > 0) {
                    throw new InvalidStageTransitionException(
                        "Cannot advance to TESTING: " + blocked + " task(s) are BLOCKED. Resolve them first.");
                }
                yield ReleaseStage.TESTING;
            }
            case TESTING -> {
                long notDone = tasks.stream()
                        .filter(t -> t.getStatus() != TaskStatus.DONE).count();
                if (notDone > 0) {
                    throw new InvalidStageTransitionException(
                        "Cannot mark COMPLETED: " + notDone + " task(s) are not DONE yet.");
                }
                yield ReleaseStage.COMPLETED;
            }
            case COMPLETED -> throw new InvalidStageTransitionException(
                    "Release is already COMPLETED. No further stage advancement possible.");
            case CANCELLED -> throw new InvalidStageTransitionException(
                    "Release is CANCELLED. Cannot advance a cancelled release.");
        };

        release.setStage(next);
        return toDTO(releaseRepository.save(release));
    }

    @Transactional
    public ReleaseDTO cancelRelease(Long releaseId) {
        Release release = findOrThrow(releaseId);
        if (release.getStage() == ReleaseStage.COMPLETED) {
            throw new InvalidStageTransitionException("Cannot cancel a completed release.");
        }
        release.setStage(ReleaseStage.CANCELLED);
        return toDTO(releaseRepository.save(release));
    }

    // ── SUMMARY REPORT ────────────────────────────────────────

    /**
     * Generates a comprehensive summary report for a release.
     * This is the "generate summary" feature — a key business value endpoint.
     */
    public ReleaseSummaryDTO generateSummary(Long releaseId) {
        Release release = findOrThrow(releaseId);
        List<Task> tasks = taskRepository.findByReleaseId(releaseId);

        int total = tasks.size();
        int done = (int) tasks.stream().filter(t -> t.getStatus() == TaskStatus.DONE).count();
        int pending = (int) tasks.stream().filter(t -> t.getStatus() == TaskStatus.PENDING).count();
        int inProgress = (int) tasks.stream().filter(t -> t.getStatus() == TaskStatus.IN_PROGRESS).count();
        int blocked = (int) tasks.stream().filter(t -> t.getStatus() == TaskStatus.BLOCKED).count();

        double completionPct = total == 0 ? 0.0 : Math.round((done * 100.0 / total) * 10.0) / 10.0;

        List<String> blockedTitles = tasks.stream()
                .filter(t -> t.getStatus() == TaskStatus.BLOCKED)
                .map(Task::getTitle)
                .collect(Collectors.toList());

        Map<String, Long> byPriority = tasks.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getPriority().name(),
                        Collectors.counting()
                ));

        // Determine readiness
        boolean ready = false;
        String readinessMsg;
        String nextStage = "N/A";

        switch (release.getStage()) {
            case PLANNING -> {
                ready = !tasks.isEmpty();
                readinessMsg = ready
                        ? "Ready to advance to IN_PROGRESS (" + total + " task(s) defined)"
                        : "Not ready: Add at least one task before advancing";
                nextStage = "IN_PROGRESS";
            }
            case IN_PROGRESS -> {
                ready = blocked == 0;
                readinessMsg = ready
                        ? "Ready to advance to TESTING (no blocked tasks)"
                        : "Not ready: " + blocked + " task(s) are BLOCKED";
                nextStage = "TESTING";
            }
            case TESTING -> {
                ready = (done == total && total > 0);
                readinessMsg = ready
                        ? "All tasks DONE — ready to mark COMPLETED"
                        : "Not ready: " + (total - done) + " task(s) still incomplete";
                nextStage = "COMPLETED";
            }
            case COMPLETED -> readinessMsg = "Release successfully completed.";
            case CANCELLED -> readinessMsg = "Release has been cancelled.";
            default -> readinessMsg = "Unknown state.";
        }

        return ReleaseSummaryDTO.builder()
                .releaseId(release.getId())
                .releaseName(release.getName())
                .version(release.getVersion())
                .currentStage(release.getStage())
                .nextStage(nextStage)
                .totalTasks(total)
                .completedTasks(done)
                .pendingTasks(pending)
                .inProgressTasks(inProgress)
                .blockedTasks(blocked)
                .completionPercentage(completionPct)
                .readyToAdvance(ready)
                .readinessMessage(readinessMsg)
                .tasksByPriority(byPriority)
                .blockedTaskTitles(blockedTitles)
                .generatedAt(LocalDateTime.now())
                .createdBy(release.getCreatedBy())
                .build();
    }

    // ── DELETE ────────────────────────────────────────────────

    @Transactional
    public void deleteRelease(Long id) {
        if (!releaseRepository.existsById(id)) {
            throw new ResourceNotFoundException("Release not found with id: " + id);
        }
        releaseRepository.deleteById(id);
    }

    // ── HELPERS ───────────────────────────────────────────────

    private Release findOrThrow(Long id) {
        return releaseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Release not found with id: " + id));
    }

    private ReleaseDTO toDTO(Release r) {
        List<Task> tasks = r.getTasks();
        int total = tasks.size();
        long done = tasks.stream().filter(t -> t.getStatus() == TaskStatus.DONE).count();
        long pending = tasks.stream().filter(t -> t.getStatus() == TaskStatus.PENDING).count();
        long blocked = tasks.stream().filter(t -> t.getStatus() == TaskStatus.BLOCKED).count();

        return ReleaseDTO.builder()
                .id(r.getId())
                .name(r.getName())
                .version(r.getVersion())
                .description(r.getDescription())
                .stage(r.getStage())
                .createdBy(r.getCreatedBy())
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .totalTasks(total)
                .completedTasks((int) done)
                .pendingTasks((int) pending)
                .blockedTasks((int) blocked)
                .build();
    }
}
