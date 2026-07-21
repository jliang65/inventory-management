package com.jeff.inventorymanagement.service;

import com.jeff.inventorymanagement.dto.UserResponse;
import com.jeff.inventorymanagement.entity.Role;
import com.jeff.inventorymanagement.entity.User;
import com.jeff.inventorymanagement.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User findById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    @Transactional
    public UserResponse updateRole(Long userId, Role newRole) {
        User user = findById(userId);
        user.setRole(newRole);
        userRepository.save(user);
        return toResponse(user);
    }

    public UserResponse toResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        response.setEnabled(user.getEnabled());
        response.setCreatedAt(user.getCreatedAt());
        return response;
    }
}
