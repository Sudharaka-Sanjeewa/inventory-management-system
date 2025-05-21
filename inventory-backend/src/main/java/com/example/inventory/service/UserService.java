package com.example.inventory.service;

import com.example.inventory.entity.User;
import com.example.inventory.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder; // New import for password hashing
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder; // Inject PasswordEncoder

    @Transactional
    public User registerUser(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("Username '" + user.getUsername() + "' already exists.");
        }
        // Hash the password before saving (CRITICAL SECURITY STEP)
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public Optional<User> loginUser(String username, String rawPassword) {
        Optional<User> foundUserOptional = userRepository.findByUsername(username);
        if (foundUserOptional.isPresent()) {
            User foundUser = foundUserOptional.get();
            if (passwordEncoder.matches(rawPassword, foundUser.getPassword())) {
                // In a real application, you'd return a DTO or token here, not the entity
                // For now, clear password for security before returning
                foundUser.setPassword(null);
                return Optional.of(foundUser);
            }
        }
        return Optional.empty(); // User not found or password mismatch
    }

    public List<User> getAllUsers() {
        List<User> users = userRepository.findAll();
        users.forEach(u -> u.setPassword(null)); // Clear passwords before returning
        return users;
    }

    public Optional<User> getUserById(Long id) {
        Optional<User> user = userRepository.findById(id);
        user.ifPresent(u -> u.setPassword(null)); // Clear password if present
        return user;
    }

    @Transactional
    public User updateUser(Long id, User updatedUser) {
        User userToUpdate = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + id));

        // Update username if provided and different, check for uniqueness
        if (updatedUser.getUsername() != null && !updatedUser.getUsername().equals(userToUpdate.getUsername())) {
            if (userRepository.existsByUsername(updatedUser.getUsername())) {
                throw new IllegalArgumentException("Username '" + updatedUser.getUsername() + "' already exists.");
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
            throw new IllegalArgumentException("User not found with ID: " + id);
        }
        userRepository.deleteById(id);
    }
}