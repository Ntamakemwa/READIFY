package com.readify.controller;

import com.readify.common.config.ApiVersion;
import com.readify.common.annotation.ApiVersioned;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@ApiVersioned("v1")
public class ApiVersionController extends BaseController {

    @GetMapping("/version")
    public ResponseEntity<Map<String, Object>> getVersion() {
        return ResponseEntity.ok(Map.of(
                "currentVersion", ApiVersion.CURRENT_VERSION,
                "supportedVersions", ApiVersion.SUPPORTED_VERSIONS.split(","),
                "apiStatus", "active",
                "deprecationInfo", Map.of(
                    "v1", Map.of(
                        "status", "active",
                        "deprecated", false,
                        "sunsetDate", "",
                        "migrationGuide", ""
                    ),
                    "v2", Map.of(
                        "status", "planned",
                        "deprecated", false,
                        "sunsetDate", "",
                        "migrationGuide", ""
                    )
                ),
                "changelog", Map.of(
                    "v1.0.0", "Initial API release with basic library management features",
                    "v1.1.0", "Added comprehensive exception handling and API versioning",
                    "v2.0.0", "Planned: Enhanced search capabilities and analytics"
                )
        ));
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "status", "healthy",
                "apiVersion", ApiVersion.CURRENT_VERSION,
                "timestamp", java.time.Instant.now(),
                "services", Map.of(
                    "database", "connected",
                    "authentication", "operational",
                    "authorization", "operational"
                )
        ));
    }
}
