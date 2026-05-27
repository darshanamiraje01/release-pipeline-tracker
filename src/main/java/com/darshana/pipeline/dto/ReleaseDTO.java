package com.darshana.pipeline.dto;

import com.darshana.pipeline.entity.ReleaseStage;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO for Release — decouples API layer from JPA entity.
 * Never expose raw JPA entities in REST responses.
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ReleaseDTO {

    private Long id;

    @NotBlank(message = "Release name is required")
    private String name;

    @NotBlank(message = "Version is required")
    @Pattern(regexp = "^v\\d+\\.\\d+\\.\\d+$", message = "Version must follow format: v1.0.0")
    private String version;

    private String description;

    private ReleaseStage stage;

    private String createdBy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // Included in response — convenience fields
    private int totalTasks;
    private int completedTasks;
    private int pendingTasks;
    private int blockedTasks;
}
