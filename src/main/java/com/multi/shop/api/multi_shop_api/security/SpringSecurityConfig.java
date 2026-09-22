package com.multi.shop.api.multi_shop_api.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;

import com.multi.shop.api.multi_shop_api.security.filters.JwtAuthenticationFilter;
import com.multi.shop.api.multi_shop_api.security.filters.JwtValidationFilter;

@Configuration
@EnableMethodSecurity(prePostEnabled = true)
public class SpringSecurityConfig {
    private final AuthenticationConfiguration authConfig;

    public SpringSecurityConfig(AuthenticationConfiguration authConfig){ 
        this.authConfig = authConfig;
    }

    @Bean
    AuthenticationManager authenticationManager() throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http, CorsConfigurationSource corsConfigurationSource) throws Exception {
        return http.authorizeHttpRequests(authz -> authz
                        .requestMatchers("/error").permitAll()
                        .requestMatchers(HttpMethod.GET, "/app/categories", "/app/categories/{id}").permitAll()
                        .requestMatchers(HttpMethod.GET, "/app/products", "/app/products/{id}").permitAll()
                        .requestMatchers(HttpMethod.GET, "/app/users/token-validation").permitAll()
                        .requestMatchers(HttpMethod.POST, "/app/users/register").permitAll()
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
                .addFilter(new JwtAuthenticationFilter(authenticationManager()))
                .addFilter(new JwtValidationFilter(authenticationManager()))
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .sessionManagement(management -> management.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .build();
    }
}
