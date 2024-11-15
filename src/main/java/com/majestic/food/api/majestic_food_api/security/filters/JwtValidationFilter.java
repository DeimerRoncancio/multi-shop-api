package com.majestic.food.api.majestic_food_api.security.filters;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.majestic.food.api.majestic_food_api.security.SimpleGrantedAuthorityCreator;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import static com.majestic.food.api.majestic_food_api.security.JwtConfig.*;

public class JwtValidationFilter extends BasicAuthenticationFilter {

    public JwtValidationFilter(AuthenticationManager authenticationManager) {
        super(authenticationManager);
    }

    @Override
    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
    throws IOException, ServletException {
        
        String header = response.getHeader("application/json");

        if (header == null || !header.startsWith("Bearer")) {
            chain.doFilter(request, response);
            return;
        }

        String token = header.replace(PREFIX_TOKEN, "").trim();

        try {
            Claims claims = Jwts.parser().verifyWith(SECRET_KEY).build().parseSignedClaims(token).getPayload();
            String username = claims.getSubject();
            Object claimAuthorities = claims.get("authorities");

            Collection<? extends GrantedAuthority> authorities = Arrays.asList(
                new ObjectMapper()
                    .addMixIn(SimpleGrantedAuthority.class, SimpleGrantedAuthorityCreator.class)
                    .readValue(
                        claimAuthorities.toString(),
                        GrantedAuthority.class  
                    )
            );

            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                username, null, authorities
            );

            SecurityContextHolder.getContext().setAuthentication(authenticationToken);

            chain.doFilter(request, response);
            
        } catch(JwtException e){
            Map<String, Object> body = new HashMap<> ();
            body.put("error", e.getMessage());
            body.put("message", "El token es invalido");

            response.getWriter().write(new ObjectMapper().writeValueAsString(body));
            response.setContentType(CONTENT_TYPE);
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
        }
    }
}
