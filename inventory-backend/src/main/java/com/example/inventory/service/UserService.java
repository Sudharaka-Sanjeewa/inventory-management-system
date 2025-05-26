package com.example.inventory.service;

import com.example.inventory.entity.User;
import com.example.inventory.repository.UserRepository;
import com.example.inventory.exception.ResourceNotFoundException;
import com.example.inventory.exception.DuplicateResourceException;
import com.example.inventory.exception.InvalidOperationException; // For login failure or other business rules
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) { // Changed return type to User and removed Optional
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
    }

    @Transactional
    public User registerUser(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new DuplicateResourceException("Username '" + user.getUsername() + "' already exists.");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public User loginUser(String username, String rawPassword) { // Changed return type from Optional<User> to User
        User foundUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid username or password.")); // Or a more generic message for security

        if (!passwordEncoder.matches(rawPassword, foundUser.getPassword())) {
            throw new InvalidOperationException("Invalid username or password."); // More specific for login failure
        }
        // In a real application, you might clear the password or return a DTO here
        foundUser.setPassword(null); // Clear password before returning for security
        return foundUser;
    }

    @Transactional
    public User updateUser(Long id, User updatedUser) {
        User userToUpdate = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));

        // Update username if provided and different, check for uniqueness
        if (updatedUser.getUsername() != null && !updatedUser.getUsername().equals(userToUpdate.getUsername())) {
            if (userRepository.existsByUsername(updatedUser.getUsername())) {
                throw new DuplicateResourceException("Username '" + updatedUser.getUsername() + "' already exists.");
            }
            userToUpdate.setUsername(updatedUser.getUsername());
        }

        // Only update password if provided and not null/empty, hash it
        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
            userToUpdate.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
        }

        if (updatedUser.getRole() != null) {
            userToUpdate.setRole(updatedUser.getRole());
        }

        User savedUser = userRepository.save(userToUpdate);
        savedUser.setPassword(null); // Clear password before returning
        return savedUser;
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with ID: " + id);
        }
        // No associated entities to check for User in this schema, but if there were,
        // you'd add a check here and throw InvalidOperationException
        userRepository.deleteById(id);
    }
}