package com.readify.auth.service;

import com.readify.auth.entity.User;
import com.readify.auth.entity.UserRole;
import com.readify.auth.repository.UserRepository;
import com.readify.common.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<Map<String, Object>> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> Map.<String, Object>of(
                        "id", user.getId(),
                        "fullName", user.getFullName(),
                        "email", user.getEmail(),
                        "status", user.getStatus().name(),
                        "enabled", user.isEnabled(),
                        "role", user.getRole().name()
                ))
                .toList();
    }

    public Map<String, Object> updateUserRole(Long id, UserRole role) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        user.setRole(role);
        User saved = userRepository.save(user);
        return Map.of(
                "message", "User role updated successfully.",
                "id", saved.getId(),
                "email", saved.getEmail(),
                "role", saved.getRole().name()
        );
    }
}
