package com.multi.shop.api.multi_shop_api.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http, CorsConfigurationSource corsConfigurationSource) throws Exception {
        return http.authorizeHttpRequests(authz -> authz
                .requestMatchers("/error").permitAll()
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                .requestMatchers(HttpMethod.GET, "/app/payments/success").permitAll()
                .requestMatchers(HttpMethod.GET, "/app/payments/cancel").permitAll()
                .requestMatchers(HttpMethod.GET, "/app/payments/checkout/{transactionId}").permitAll()
                .requestMatchers(HttpMethod.POST, "/app/payments/webhook").permitAll()
                .requestMatchers(HttpMethod.POST, "/app/payments/create-payment-session/{transactionId}").permitAll()
                .requestMatchers(HttpMethod.POST, "/app/payments/cancel-payment-session/{transactionId}").permitAll()
                .requestMatchers(HttpMethod.POST, "/app/payments/create-transaction").permitAll()
                .requestMatchers(HttpMethod.PUT, "/app/payments/add-user/{transactionId}").permitAll()
                .requestMatchers(HttpMethod.PUT, "/app/payments/update-products/{id}").permitAll()
                .requestMatchers(HttpMethod.DELETE, "/app/payments/{id}").permitAll()
                .anyRequest().authenticated())
            .addFilterBefore(new JwtValidationFilter(), UsernamePasswordAuthenticationFilter.class)
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            .httpBasic(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .sessionManagement(management -> management.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .build();
    }
}
