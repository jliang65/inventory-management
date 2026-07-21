package com.jeff.inventorymanagement.dto;

public record AuthResponse(
    String accessToken,
    String tokenType,
    long expiresIn
) {}
