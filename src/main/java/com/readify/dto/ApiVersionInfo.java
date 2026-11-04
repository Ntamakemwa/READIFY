package com.readify.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.readify.common.config.ApiVersion;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public class ApiVersionInfo {
    
    @JsonProperty("currentVersion")
    private String currentVersion = ApiVersion.CURRENT_VERSION;
    
    @JsonProperty("supportedVersions")
    private List<String> supportedVersions = List.of(ApiVersion.SUPPORTED_VERSIONS.split(","));
    
    @JsonProperty("apiStatus")
    private String apiStatus = "active";
    
    @JsonProperty("deprecationInfo")
    private Map<String, VersionDetails> deprecationInfo;
    
    @JsonProperty("changelog")
    private Map<String, String> changelog;
    
    @JsonProperty("lastUpdated")
    private Instant lastUpdated = Instant.now();

    public static class VersionDetails {
        @JsonProperty("status")
        private String status;
        
        @JsonProperty("deprecated")
        private boolean deprecated;
        
        @JsonProperty("sunsetDate")
        private String sunsetDate;
        
        @JsonProperty("migrationGuide")
        private String migrationGuide;

        // Constructors
        public VersionDetails() {}

        public VersionDetails(String status, boolean deprecated, String sunsetDate, String migrationGuide) {
            this.status = status;
            this.deprecated = deprecated;
            this.sunsetDate = sunsetDate;
            this.migrationGuide = migrationGuide;
        }

        // Getters and setters
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public boolean isDeprecated() { return deprecated; }
        public void setDeprecated(boolean deprecated) { this.deprecated = deprecated; }
        
        public String getSunsetDate() { return sunsetDate; }
        public void setSunsetDate(String sunsetDate) { this.sunsetDate = sunsetDate; }
        
        public String getMigrationGuide() { return migrationGuide; }
        public void setMigrationGuide(String migrationGuide) { this.migrationGuide = migrationGuide; }
    }

    // Getters and setters
    public String getCurrentVersion() { return currentVersion; }
    public void setCurrentVersion(String currentVersion) { this.currentVersion = currentVersion; }
    
    public List<String> getSupportedVersions() { return supportedVersions; }
    public void setSupportedVersions(List<String> supportedVersions) { this.supportedVersions = supportedVersions; }
    
    public String getApiStatus() { return apiStatus; }
    public void setApiStatus(String apiStatus) { this.apiStatus = apiStatus; }
    
    public Map<String, VersionDetails> getDeprecationInfo() { return deprecationInfo; }
    public void setDeprecationInfo(Map<String, VersionDetails> deprecationInfo) { this.deprecationInfo = deprecationInfo; }
    
    public Map<String, String> getChangelog() { return changelog; }
    public void setChangelog(Map<String, String> changelog) { this.changelog = changelog; }
    
    public Instant getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(Instant lastUpdated) { this.lastUpdated = lastUpdated; }
}
