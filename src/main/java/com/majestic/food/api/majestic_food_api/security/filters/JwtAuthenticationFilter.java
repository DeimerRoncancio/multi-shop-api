package com.majestic.food.api.majestic_food_api.security.filters;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import static com.majestic.food.api.majestic_food_api.security.JwtConfig.*;

public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private AuthenticationManager authenticationManager;
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    public JwtAuthenticationFilter(AuthenticationManager authManager) {
        this.authenticationManager = authManager;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) 
    throws AuthenticationException {

        String emailOrPhone = null;
        String password = null;

        try {
            Map<String, String> credentials = new ObjectMapper().readValue(request.getInputStream(), new TypeReference<Map<String, String>>() {});

            emailOrPhone = credentials.get("emailOrPhone");
            password = credentials.get("password");
        } catch(IOException e) {
            logger.error("Exception by bringing user: " + e);
        }

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
            emailOrPhone,
            password
        );
        
        return authenticationManager.authenticate(authenticationToken);
    }

    @Override
    public void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, 
    FilterChain filter, Authentication authResult) throws IOException {

        Claims rolesClaims = getRoles(authResult);
        User user = (User) authResult.getPrincipal();
        String username = user.getUsername();
        
        String token = getToken(username, rolesClaims);
        
        Map<String, Object> body = new HashMap<> ();
        body.put("username", username);
        body.put("token", token);
        body.put("message", String.format("¡Bienvenido %s! Ha iniciado sesión exitosamente", username));
        
        response.addHeader(HEADER_AUTHORIZATION, PREFIX_TOKEN + token);
        response.getWriter().write(new ObjectMapper().writeValueAsString(body));
        response.setContentType(CONTENT_TYPE);
        response.setStatus(200);
    }

    @Override
    public void unsuccessfulAuthentication(HttpServletRequest reques, HttpServletResponse response, 
    AuthenticationException failed) throws IOException, ServletException {

        Map<String, Object> body = new HashMap<> ();
        body.put("message", "Error en la authenticación. Usuario o contraseña incorrectos.");
        body.put("error", failed.getMessage());

        response.getWriter().write(new ObjectMapper().writeValueAsString(body));
        response.setContentType(CONTENT_TYPE);
        response.setStatus(401);
    }

    public String getToken(String username, Claims rolesClaims) {
        return Jwts.builder()
            .subject(username)
            .claims(rolesClaims)
            .expiration(new Date(System.currentTimeMillis() + 3600000))
            .issuedAt(new Date())
            .signWith(SECRET_KEY)
            .compact();
    }

    public Claims getRoles(Authentication authResult) throws IOException {
        Collection<? extends GrantedAuthority> roles = authResult.getAuthorities();

        return Jwts.claims().add("authorities", new ObjectMapper().writeValueAsString(roles)).build();
    }
}
