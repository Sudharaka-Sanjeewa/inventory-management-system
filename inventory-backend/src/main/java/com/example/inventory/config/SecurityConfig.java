package com.example.inventory.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; // Import for BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder; // Import for PasswordEncoder

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests((authz) -> authz
                        // Permit all requests for now. This should be refined for production.
                        // Example for production:
                        // .requestMatchers("/api/users/register", "/api/users/login").permitAll()
                        // .requestMatchers("/api/**").authenticated()
                        // .anyRequest().denyAll()
                        .anyRequest().permitAll() // Allow all requests without authentication for initial setup
                )
                .httpBasic(withDefaults()) // Basic HTTP authentication
                .csrf((csrf) -> csrf.disable()); // Disable CSRF for simplicity (re-evaluate for production)
        return http.build();
    }

    // Define a PasswordEncoder bean for password hashing
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}