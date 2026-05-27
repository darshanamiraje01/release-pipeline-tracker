package com.darshana.pipeline.service;

import com.darshana.pipeline.dto.ReleaseDTO;
import com.darshana.pipeline.dto.ReleaseSummaryDTO;
import com.darshana.pipeline.entity.*;
import com.darshana.pipeline.exception.InvalidStageTransitionException;
import com.darshana.pipeline.exception.ResourceNotFoundException;
import com.darshana.pipeline.repository.ReleaseRepository;
import com.darshana.pipeline.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit Tests for ReleaseService — tests business logic in isolation.
 *
 * Using: JUnit 5 + Mockito
 * Pattern: Arrange → Act → Assert
 *
 * Interview point: "I used @ExtendWith(MockitoExtension.class) instead of
 * @SpringBootTest to keep tests fast — no Spring context is loaded,
 * dependencies are mocked with Mockito."
 */
@ExtendWith(MockitoExtension.class)
class ReleaseServiceTest {

    @Mock
    private ReleaseRepository releaseRepository;

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private ReleaseService releaseService;

    private Release planningRelease;
    private Release inProgressRelease;

    @BeforeEach
    void setUp() {
        planningRelease = Release.builder()
                .id(1L)
                .name("Test Release")
                .version("v1.0.0")
                .stage(ReleaseStage.PLANNING)
                .createdBy("admin")
                .build();

        inProgressRelease = Release.builder()
                .id(2L)
                .name("Active Release")
                .version("v2.0.0")
                .stage(ReleaseStage.IN_PROGRESS)
                .createdBy("admin")
                .build();
    }

    // ── CREATE TESTS ─────────────────────────────────────────

    @Test
    @DisplayName("Should create release successfully when version is unique")
    void createRelease_success() {
        // Arrange
        ReleaseDTO dto = ReleaseDTO.builder()
                .name("New Release")
                .version("v3.0.0")
                .createdBy("admin")
                .build();

        when(releaseRepository.existsByVersion("v3.0.0")).thenReturn(false);
        when(releaseRepository.save(any(Release.class))).thenReturn(planningRelease);

        // Act
        ReleaseDTO result = releaseService.createRelease(dto);

        // Assert
        assertThat(result).isNotNull();
        verify(releaseRepository, times(1)).save(any(Release.class));
    }

    @Test
    @DisplayName("Should throw exception when version already exists")
    void createRelease_duplicateVersion_throwsException() {
        // Arrange
        ReleaseDTO dto = ReleaseDTO.builder()
                .name("Duplicate")
                .version("v1.0.0")
                .build();
        when(releaseRepository.existsByVersion("v1.0.0")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> releaseService.createRelease(dto))
                .isInstanceOf(InvalidStageTransitionException.class)
                .hasMessageContaining("already exists");
    }

    // ── STAGE ADVANCEMENT TESTS ───────────────────────────────

    @Test
    @DisplayName("Should advance from PLANNING to IN_PROGRESS when tasks exist")
    void advanceStage_planningToInProgress_success() {
        // Arrange
        Task task = Task.builder().id(1L).title("Task 1")
                .status(TaskStatus.PENDING).release(planningRelease).build();

        when(releaseRepository.findById(1L)).thenReturn(Optional.of(planningRelease));
        when(taskRepository.findByReleaseId(1L)).thenReturn(List.of(task));
        when(releaseRepository.save(any())).thenReturn(planningRelease);

        // Act
        releaseService.advanceStage(1L);

        // Assert — stage was changed to IN_PROGRESS
        verify(releaseRepository, times(1)).save(argThat(r ->
                r.getStage() == ReleaseStage.IN_PROGRESS));
    }

    @Test
    @DisplayName("Should throw exception when advancing PLANNING with no tasks")
    void advanceStage_planningNoTasks_throwsException() {
        // Arrange
        when(releaseRepository.findById(1L)).thenReturn(Optional.of(planningRelease));
        when(taskRepository.findByReleaseId(1L)).thenReturn(Collections.emptyList());

        // Act & Assert
        assertThatThrownBy(() -> releaseService.advanceStage(1L))
                .isInstanceOf(InvalidStageTransitionException.class)
                .hasMessageContaining("no tasks");
    }

    @Test
    @DisplayName("Should throw exception when advancing IN_PROGRESS with blocked tasks")
    void advanceStage_inProgressBlockedTasks_throwsException() {
        // Arrange
        Task blockedTask = Task.builder().id(1L).title("Blocked Task")
                .status(TaskStatus.BLOCKED).release(inProgressRelease).build();

        when(releaseRepository.findById(2L)).thenReturn(Optional.of(inProgressRelease));
        when(taskRepository.findByReleaseId(2L)).thenReturn(List.of(blockedTask));

        // Act & Assert
        assertThatThrownBy(() -> releaseService.advanceStage(2L))
                .isInstanceOf(InvalidStageTransitionException.class)
                .hasMessageContaining("BLOCKED");
    }

    // ── SUMMARY TESTS ─────────────────────────────────────────

    @Test
    @DisplayName("Should generate correct summary with completion percentage")
    void generateSummary_correctCompletionPercentage() {
        // Arrange
        Task doneTask = Task.builder().status(TaskStatus.DONE)
                .priority(TaskPriority.HIGH).release(planningRelease).title("T1").build();
        Task pendingTask = Task.builder().status(TaskStatus.PENDING)
                .priority(TaskPriority.LOW).release(planningRelease).title("T2").build();

        when(releaseRepository.findById(1L)).thenReturn(Optional.of(planningRelease));
        when(taskRepository.findByReleaseId(1L)).thenReturn(List.of(doneTask, pendingTask));

        // Act
        ReleaseSummaryDTO summary = releaseService.generateSummary(1L);

        // Assert
        assertThat(summary.getTotalTasks()).isEqualTo(2);
        assertThat(summary.getCompletedTasks()).isEqualTo(1);
        assertThat(summary.getCompletionPercentage()).isEqualTo(50.0);
    }

    // ── NOT FOUND TEST ────────────────────────────────────────

    @Test
    @DisplayName("Should throw ResourceNotFoundException when release not found")
    void getReleaseById_notFound_throwsException() {
        // Arrange
        when(releaseRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> releaseService.getReleaseById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }
}
