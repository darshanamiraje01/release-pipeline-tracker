package com.darshana.pipeline.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a software release in the pipeline.
 * A Release has many Tasks and moves through lifecycle stages.
 *
 * Example: Release "v2.1.0 - Payment Integration"
 *   → Stage: PLANNING
 *   → Tasks: [Define scope, Implement service, Write tests, Deploy]
 */
@Entity
@Table(name = "releases")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Release {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Release name is required")
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "Version is required")
    @Pattern(regexp = "^v\\d+\\.\\d+\\.\\d+$",
             message = "Version must follow format: v1.0.0")
    @Column(nullable = false)
    private String version;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ReleaseStage stage = ReleaseStage.PLANNING;

    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // One release has many tasks
    @OneToMany(mappedBy = "release", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties("release")
    @Builder.Default
    private List<Task> tasks = new ArrayList<>();
}
