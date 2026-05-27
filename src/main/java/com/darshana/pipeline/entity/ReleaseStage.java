package com.darshana.pipeline.entity;

/**
 * Represents the lifecycle stages of a software release pipeline.
 * Modeled after real-world DevOps release workflows (inspired by AutoRABIT).
 *
 * Flow: PLANNING → IN_PROGRESS → TESTING → COMPLETED
 *       Any stage → CANCELLED
 */
public enum ReleaseStage {
    PLANNING,       // Release created, tasks being defined
    IN_PROGRESS,    // Active development underway
    TESTING,        // QA and validation phase
    COMPLETED,      // Successfully deployed
    CANCELLED       // Release abandoned
}
