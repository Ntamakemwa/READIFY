package com.readify.common.controller;

import com.readify.common.config.ApiVersion;
import com.readify.common.dto.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;

public abstract class BaseController {

    @GetMapping("/version")
    public ResponseEntity<Map<String, Object>> getVersion() {
        return ResponseEntity.ok(Map.of(
                "apiVersion", ApiVersion.CURRENT_VERSION,
                "supportedVersions", ApiVersion.SUPPORTED_VERSIONS.split(","),
                "basePath", ApiVersion.API_BASE_PATH,
                "status", "active"
        ));
    }

    protected <T> ResponseEntity<T> withVersionHeaders(T body) {
        return ResponseEntity.ok()
                .header(ApiVersion.VERSION_HEADER, ApiVersion.CURRENT_VERSION)
                .body(body);
    }
}
