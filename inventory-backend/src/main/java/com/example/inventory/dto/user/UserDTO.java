package com.example.inventory.dto.user;

import lombok.Data;

@Data
public class UserDTO {
    private Long userId;
    private String username;
    private String role;
    // Password is intentionally omitted for security reasons in the DTO
}