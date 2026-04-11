package com.sahaya.setu.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Disable CSRF so React can send POST requests
                .cors(cors -> cors.configure(http)) // Allow Cross-Origin Requests from React
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll() // Keep everything open for this exact step
                );
        return http.build();
    }
}
