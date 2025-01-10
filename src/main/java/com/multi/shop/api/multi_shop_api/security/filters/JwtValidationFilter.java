package com.multi.shop.api.multi_shop_api.security.filters;

import static com.multi.shop.api.multi_shop_api.security.JwtConfig.*;

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
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.multi.shop.api.multi_shop_api.security.SimpleGrantedAuthorityCreator;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JwtValidationFilter extends BasicAuthenticationFilter {

    public JwtValidationFilter(AuthenticationManager authenticationManager) {
        super(authenticationManager);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
    throws IOException, ServletException {

        String token = getTokenByRequest(request);

        if (token == null) {
            chain.doFilter(request, response);
            return;
        }

        try {
            Claims claims = Jwts.parser().verifyWith(SECRET_KEY).build().parseSignedClaims(token).getPayload();
            Object claimAuthorities = claims.get("authorities");
            String username = claims.getSubject();

            Collection<? extends GrantedAuthority> authorities = getAuthorities(claimAuthorities);

            SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                username, null, authorities
            ));

            chain.doFilter(request, response);
            
        } catch(JwtException e){
            Map<String, String> body = new HashMap<> ();
            body.put("error", e.getMessage());
            body.put("message", "El token es invalido");

            response.getWriter().write(new ObjectMapper().writeValueAsString(body));
            response.setContentType(CONTENT_TYPE);
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
        }
    }

    public String getTokenByRequest(HttpServletRequest request) {
        String header = request.getHeader(HEADER_AUTHORIZATION);

        if (!StringUtils.hasText(header) || !header.startsWith(PREFIX_TOKEN))
            return null;

        return header.replace(PREFIX_TOKEN, "").trim();
    }

    public Collection<? extends GrantedAuthority> getAuthorities(Object claimAuthorities) 
    throws JsonMappingException, JsonProcessingException {
        return Arrays.asList(new ObjectMapper()
            .addMixIn(SimpleGrantedAuthority.class, SimpleGrantedAuthorityCreator.class)
            .readValue(claimAuthorities.toString(),SimpleGrantedAuthority[].class));
    }
}
