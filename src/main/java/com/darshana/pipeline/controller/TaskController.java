package com.darshana.pipeline.controller;

import com.darshana.pipeline.dto.TaskDTO;
import com.darshana.pipeline.entity.TaskStatus;
import com.darshana.pipeline.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Task management.
 *
 * Base URL: /api/tasks
 *
 * Endpoints:
 *   POST   /api/tasks                            → Create task
 *   GET    /api/tasks/{id}                       → Get task by ID
 *   GET    /api/tasks/release/{releaseId}        → All tasks for a release
 *   GET    /api/tasks/assignee/{name}            → Tasks by assignee
 *   PUT    /api/tasks/{id}                       → Update task
 *   PATCH  /api/tasks/{id}/status?status=DONE   → Quick status update
 *   DELETE /api/tasks/{id}                       → Delete task
 */
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    public ResponseEntity<TaskDTO> createTask(@Valid @RequestBody TaskDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(taskService.createTask(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskDTO> getTaskById(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.getTaskById(id));
    }

    @GetMapping("/release/{releaseId}")
    public ResponseEntity<List<TaskDTO>> getTasksByRelease(@PathVariable Long releaseId) {
        return ResponseEntity.ok(taskService.getTasksByRelease(releaseId));
    }

    @GetMapping("/assignee/{assignee}")
    public ResponseEntity<List<TaskDTO>> getTasksByAssignee(@PathVariable String assignee) {
        return ResponseEntity.ok(taskService.getTasksByAssignee(assignee));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskDTO> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody TaskDTO dto) {
        return ResponseEntity.ok(taskService.updateTask(id, dto));
    }

    /**
     * Quick status update — PATCH instead of full PUT.
     * E.g.: PATCH /api/tasks/5/status?status=DONE
     *
     * Interview point: "I used PATCH here because we're only updating
     * one field — status — not the entire resource. That's the semantic
     * difference between PUT and PATCH in REST."
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<TaskDTO> updateTaskStatus(
            @PathVariable Long id,
            @RequestParam TaskStatus status) {
        return ResponseEntity.ok(taskService.updateTaskStatus(id, status));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }
}
