package com.darshana.pipeline;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Release Pipeline Tracker
 *
 * A simplified REST API inspired by AutoRABIT's DevOps release automation platform.
 * Tracks software releases through stages: PLANNING → IN_PROGRESS → TESTING → COMPLETED
 *
 * Key Features:
 *  - Create and manage Releases with versioning
 *  - Assign Tasks to releases with priority and status tracking
 *  - Advance release stages with business rule validation
 *  - Generate release summary reports
 *  - Secured with Spring Security HTTP Basic Auth
 */
@SpringBootApplication
public class ReleasePipelineTrackerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReleasePipelineTrackerApplication.class, args);
    }
}
