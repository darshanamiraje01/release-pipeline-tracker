package com.darshana.pipeline.dto;

import com.darshana.pipeline.entity.TaskPriority;
import com.darshana.pipeline.entity.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TaskDTO {

    private Long id;

    @NotBlank(message = "Task title is required")
    private String title;

    private String description;

    private String assignee;

    private TaskStatus status;

    private TaskPriority priority;

    @NotNull(message = "Release ID is required")
    private Long releaseId;

    private String releaseName;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
