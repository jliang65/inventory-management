package com.jeff.inventorymanagement.controller;

import com.jeff.inventorymanagement.dto.UpdateRoleRequest;
import com.jeff.inventorymanagement.dto.UserResponse;
import com.jeff.inventorymanagement.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    @PutMapping("/{userId}/role")
    public UserResponse updateUserRole(
        @PathVariable Long userId,
        @Valid @RequestBody UpdateRoleRequest request
    ) {
        return userService.updateRole(userId, request.getRole());
    }
}
