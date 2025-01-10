package com.multi.shop.api.multi_shop_api.security.filters;

import static com.multi.shop.api.multi_shop_api.security.JwtConfig.*;

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
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.multi.shop.api.multi_shop_api.auth.LoginRequest;
import com.multi.shop.api.multi_shop_api.entities.dtos.auth.CustomUserDetails;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private final AuthenticationManager authManager;

    public JwtAuthenticationFilter(AuthenticationManager authManager) {
        this.authManager = authManager;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) 
    throws AuthenticationException {

        String identifier = null;
        String password = null;

        try {
            LoginRequest credentials = new ObjectMapper().readValue(request.getInputStream(), LoginRequest.class);
            identifier = credentials.getIdentifier();
            password = credentials.getPassword();
        } catch(IOException e) {
            logger.error("Exception by bringing user: " + e);
        }
        
        return authManager.authenticate(new UsernamePasswordAuthenticationToken(
            identifier,
            password
        ));
    }

    @Override
    public void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, 
    FilterChain filter, Authentication authResult) throws IOException {

        CustomUserDetails user = (CustomUserDetails) authResult.getPrincipal();
        
        Claims rolesClaims = getRoles(authResult);
        String identifier = user.getUsername();
        String token = getToken(identifier, rolesClaims);
        
        Map<String, Object> body = new HashMap<> ();
        body.put("username", identifier);
        body.put("token", token);
        
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

    public String getToken(String identifier, Claims rolesClaims) {
        return Jwts.builder()
            .subject(identifier)
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
