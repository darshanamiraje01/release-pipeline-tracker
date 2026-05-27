package com.darshana.pipeline.dto;

import com.darshana.pipeline.entity.ReleaseStage;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Summary report for a release — the "generate summary" feature.
 * This is what makes the project stand out: a meaningful business output.
 *
 * In interviews: "The summary endpoint aggregates task completion stats,
 * identifies blocked tasks, calculates overall progress percentage,
 * and tells you whether the release is ready to advance to the next stage."
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ReleaseSummaryDTO {

    private Long releaseId;
    private String releaseName;
    private String version;
    private ReleaseStage currentStage;
    private String nextStage;

    // Progress metrics
    private int totalTasks;
    private int completedTasks;
    private int pendingTasks;
    private int inProgressTasks;
    private int blockedTasks;
    private double completionPercentage;

    // Readiness check
    private boolean readyToAdvance;
    private String readinessMessage;

    // Breakdown by priority
    private Map<String, Long> tasksByPriority;

    // Blocked tasks need attention
    private List<String> blockedTaskTitles;

    private LocalDateTime generatedAt;
    private String createdBy;
}
