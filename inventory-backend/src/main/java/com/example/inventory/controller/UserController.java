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
        return dto;
    }

    // Helper method to convert RegisterUserRequest to User entity
    private User convertToEntity(RegisterUserRequest dto) {
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(dto.getPassword());
        user.setRole(dto.getRole() != null ? dto.getRole() : "ROLE_USER");
        return user;
    }

    // Helper method to update User entity from UpdateUserRequest
    private User convertToEntity(UpdateUserRequest dto, User existingUser) {
        if (dto.getUsername() != null) {
            existingUser.setUsername(dto.getUsername());
        }
        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            existingUser.setPassword(dto.getPassword());
        }
        if (dto.getRole() != null) {
            existingUser.setRole(dto.getRole());
        }
        return existingUser;
    }

    @PostMapping("/register")
    public ResponseEntity<UserDTO> registerUser(@Valid @RequestBody RegisterUserRequest registerUserRequest) {
        // Service will throw DuplicateResourceException if username exists
        User userToRegister = convertToEntity(registerUserRequest);
        User savedUser = userService.registerUser(userToRegister);
        return new ResponseEntity<>(convertToDto(savedUser), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<UserDTO> loginUser(@Valid @RequestBody LoginRequest loginRequest) {
        // Service will throw ResourceNotFoundException or InvalidOperationException
        User loggedInUser = userService.loginUser(loginRequest.getUsername(), loginRequest.getPassword());
        return new ResponseEntity<>(convertToDto(loggedInUser), HttpStatus.OK);
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
        // Service will throw ResourceNotFoundException if not found
        User user = userService.getUserById(id);
        return new ResponseEntity<>(convertToDto(user), HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest updateUserRequest) {
        // Service will throw ResourceNotFoundException or DuplicateResourceException
        User existingUser = new User(); // Dummy entity
        User updatedUserEntity = convertToEntity(updateUserRequest, existingUser);
        User savedUser = userService.updateUser(id, updatedUserEntity);
        return new ResponseEntity<>(convertToDto(savedUser), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        // Service will throw ResourceNotFoundException
        userService.deleteUser(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}