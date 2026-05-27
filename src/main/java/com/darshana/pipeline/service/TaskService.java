package com.darshana.pipeline.service;

import com.darshana.pipeline.dto.TaskDTO;
import com.darshana.pipeline.entity.Release;
import com.darshana.pipeline.entity.Task;
import com.darshana.pipeline.entity.TaskPriority;
import com.darshana.pipeline.entity.TaskStatus;
import com.darshana.pipeline.exception.ResourceNotFoundException;
import com.darshana.pipeline.repository.ReleaseRepository;
import com.darshana.pipeline.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final ReleaseRepository releaseRepository;

    @Transactional
    public TaskDTO createTask(TaskDTO dto) {
        Release release = releaseRepository.findById(dto.getReleaseId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Release not found with id: " + dto.getReleaseId()));

        Task task = Task.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .assignee(dto.getAssignee())
                .status(dto.getStatus() != null ? dto.getStatus() : TaskStatus.PENDING)
                .priority(dto.getPriority() != null ? dto.getPriority() : TaskPriority.MEDIUM)
                .release(release)
                .build();

        return toDTO(taskRepository.save(task));
    }

    public TaskDTO getTaskById(Long id) {
        return toDTO(findOrThrow(id));
    }

    public List<TaskDTO> getTasksByRelease(Long releaseId) {
        return taskRepository.findByReleaseId(releaseId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<TaskDTO> getTasksByAssignee(String assignee) {
        return taskRepository.findByAssignee(assignee)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional
    public TaskDTO updateTask(Long id, TaskDTO dto) {
        Task task = findOrThrow(id);
        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());
        task.setAssignee(dto.getAssignee());
        if (dto.getStatus() != null) task.setStatus(dto.getStatus());
        if (dto.getPriority() != null) task.setPriority(dto.getPriority());
        return toDTO(taskRepository.save(task));
    }

    @Transactional
    public TaskDTO updateTaskStatus(Long id, TaskStatus newStatus) {
        Task task = findOrThrow(id);
        task.setStatus(newStatus);
        return toDTO(taskRepository.save(task));
    }

    @Transactional
    public void deleteTask(Long id) {
        if (!taskRepository.existsById(id)) {
            throw new ResourceNotFoundException("Task not found with id: " + id);
        }
        taskRepository.deleteById(id);
    }

    private Task findOrThrow(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Task not found with id: " + id));
    }

    private TaskDTO toDTO(Task t) {
        return TaskDTO.builder()
                .id(t.getId())
                .title(t.getTitle())
                .description(t.getDescription())
                .assignee(t.getAssignee())
                .status(t.getStatus())
                .priority(t.getPriority())
                .releaseId(t.getRelease().getId())
                .releaseName(t.getRelease().getName())
                .createdAt(t.getCreatedAt())
                .updatedAt(t.getUpdatedAt())
                .build();
    }
}
