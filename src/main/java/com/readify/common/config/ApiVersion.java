package com.readify.common.config;

public class ApiVersion {
    public static final String V1 = "v1";
    public static final String V2 = "v2";
    
    public static final String CURRENT_VERSION = V1;
    public static final String SUPPORTED_VERSIONS = V1;
    
    public static final String API_BASE_PATH = "/api";
    public static final String VERSION_HEADER = "X-API-Version";
    public static final String DEPRECATED_HEADER = "X-API-Deprecated";
    public static final String SUNSET_HEADER = "X-API-Sunset";
    
    private ApiVersion() {
        // Utility class
    }
}
