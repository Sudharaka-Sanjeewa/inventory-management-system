package com.example.inventory.controller;

import com.example.inventory.dto.user.LoginRequest;
import com.example.inventory.dto.user.RegisterUserRequest;
import com.example.inventory.dto.user.UpdateUserRequest;
import com.example.inventory.dto.user.UserDTO;
import com.example.inventory.entity.User;
import com.example.inventory.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    // Helper method to convert User entity to UserDTO (omits password)
    private UserDTO convertToDto(User user) {
        UserDTO dto = new UserDTO();
        dto.setUserId(user.getUserId());
        dto.setUsername(user.getUsername());
        dto.setRole(user.getRole());
        // Password is intentionally NOT set in the DTO for security
        return dto;
    }

    // Helper method to convert RegisterUserRequest to User entity
    // The password will be hashed in the UserService
    private User convertToEntity(RegisterUserRequest dto) {
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(dto.getPassword()); // Raw password to be hashed by service
        user.setRole(dto.getRole() != null ? dto.getRole() : "ROLE_USER"); // Set default or provided role
        return user;
    }

    // Helper method to update User entity from UpdateUserRequest
    // The password will be hashed in the UserService if provided
    private User convertToEntity(UpdateUserRequest dto, User existingUser) {
        if (dto.getUsername() != null) {
            existingUser.setUsername(dto.getUsername());
        }
        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            existingUser.setPassword(dto.getPassword()); // Raw password to be hashed by service
        }
        if (dto.getRole() != null) {
            existingUser.setRole(dto.getRole());
        }
        return existingUser;
    }

    @PostMapping("/register")
    public ResponseEntity<UserDTO> registerUser(@Valid @RequestBody RegisterUserRequest registerUserRequest) {
        try {
            User userToRegister = convertToEntity(registerUserRequest);
            User savedUser = userService.registerUser(userToRegister);
            return new ResponseEntity<>(convertToDto(savedUser), HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.CONFLICT); // Username already exists
        }
    }

    @PostMapping("/login")
    public ResponseEntity<UserDTO> loginUser(@Valid @RequestBody LoginRequest loginRequest) {
        return userService.loginUser(loginRequest.getUsername(), loginRequest.getPassword())
                .map(this::convertToDto) // Convert entity to DTO if login successful
                .map(userDTO -> new ResponseEntity<>(userDTO, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.UNAUTHORIZED)); // User not found or password mismatch
    }

    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        List<UserDTO> userDTOs = users.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return new ResponseEntity<>(userDTOs, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(this::convertToDto) // Convert entity to DTO if found
                .map(userDTO -> new ResponseEntity<>(userDTO, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest updateUserRequest) {
        try {
            User existingUser = userService.getUserById(id)
                    .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + id));

            User updatedUserEntity = convertToEntity(updateUserRequest, existingUser);
            User savedUser = userService.updateUser(id, updatedUserEntity);

            return new ResponseEntity<>(convertToDto(savedUser), HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // User not found or username conflict
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // User not found
        }
    }
}