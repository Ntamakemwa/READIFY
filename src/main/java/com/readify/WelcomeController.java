package com.readify;

import com.readify.common.controller.BaseController;
import com.readify.common.config.ApiVersion;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class WelcomeController extends BaseController {

    @GetMapping("/api")
    public ResponseEntity<Map<String, Object>> welcome() {
        return ResponseEntity.ok(Map.of(
                "message", "Welcome to Readify - Secure Library Management System API",
                "version", "1.0.0",
                "apiVersion", ApiVersion.CURRENT_VERSION,
                "status", "running",
                "endpoints", Map.of(
                    "auth", "/api/v1/auth",
                    "books", "/api/v1/books",
                    "authors", "/api/v1/authors",
                    "categories", "/api/v1/categories",
                    "loans", "/api/v1/loans",
                    "version", "/api/v1/version"
                )
        ));
    }
}
