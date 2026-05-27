package com.darshana.pipeline.entity;

public enum TaskStatus {
    PENDING,        // Not yet started
    IN_PROGRESS,    // Actively being worked on
    DONE,           // Completed
    BLOCKED         // Blocked by dependency or issue
}
