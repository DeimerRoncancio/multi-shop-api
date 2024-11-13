package com.majestic.food.api.majestic_food_api.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SpringSecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http.authorizeHttpRequests(authz -> authz
            .requestMatchers(HttpMethod.GET, "/app/users").permitAll()
            .requestMatchers(HttpMethod.POST, "/app/users").permitAll()
            .requestMatchers(HttpMethod.GET, "/app/users/{id}").permitAll()
            .requestMatchers(HttpMethod.PUT, "/app/users/{id}").permitAll()
            .requestMatchers(HttpMethod.DELETE, "/app/users/{id}").permitAll()
            .requestMatchers(HttpMethod.GET, "/app/categories").permitAll()
            .requestMatchers(HttpMethod.POST, "/app/categories").permitAll()
            .requestMatchers(HttpMethod.PUT, "/app/categories/{id}").permitAll()
            .requestMatchers(HttpMethod.DELETE, "/app/categories/{id}").permitAll()
            .anyRequest().authenticated())
            .csrf(config -> config.disable())
            .sessionManagement(management -> management.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .build();
    }
}
