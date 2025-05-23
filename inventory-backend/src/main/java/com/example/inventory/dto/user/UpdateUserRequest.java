package com.example.inventory.dto.user;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserRequest {
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @Size(min = 6, message = "Password must be at least 6 characters long")
    private String password; // Will be hashed in service layer

    private String role;
}