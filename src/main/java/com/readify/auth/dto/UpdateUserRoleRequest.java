package com.readify.auth.dto;

import com.readify.auth.entity.UserRole;
import jakarta.validation.constraints.NotNull;

public class UpdateUserRoleRequest {

    @NotNull
    private UserRole role;

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }
}
