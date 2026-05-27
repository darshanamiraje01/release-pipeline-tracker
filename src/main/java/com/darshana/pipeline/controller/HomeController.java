package com.darshana.pipeline.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Home controller — provides a welcome endpoint and health check.
 * Fixes the 500 error when hitting http://localhost:8080/
 */
@RestController
public class HomeController {

    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> home() {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("app", "Release Pipeline Tracker");
        info.put("version", "1.0.0");
        info.put("description", "A REST API inspired by AutoRABIT's DevOps release automation platform");
        info.put("status", "running");
        info.put("timestamp", LocalDateTime.now().toString());
        info.put("endpoints", Map.of(
            "releases", "/api/releases",
            "tasks", "/api/tasks",
            "h2-console", "/h2-console"
        ));
        info.put("credentials", Map.of(
            "admin", "admin123 (full access)",
            "user", "user123 (read-only)"
        ));
        return ResponseEntity.ok(info);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "timestamp", LocalDateTime.now().toString()
        ));
    }
}
