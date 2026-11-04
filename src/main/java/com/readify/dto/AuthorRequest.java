package com.readify.dto;

import jakarta.validation.constraints.NotBlank;

public class AuthorRequest {
    @NotBlank
    private String fullName;

    private String biography;

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getBiography() {
        return biography;
    }

    public void setBiography(String biography) {
        this.biography = biography;
    }
}
