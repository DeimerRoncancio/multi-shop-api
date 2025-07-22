package com.multi.shop.api.multi_shop_api.auth.controllers;

import java.util.Optional;

import com.multi.shop.api.multi_shop_api.auth.dtos.RegisterUserDTO;
import com.multi.shop.api.multi_shop_api.auth.mappers.AuthMapper;
import com.multi.shop.api.multi_shop_api.common.exceptions.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.multi.shop.api.multi_shop_api.users.entities.User;
import com.multi.shop.api.multi_shop_api.users.dtos.UserResponseDTO;
import com.multi.shop.api.multi_shop_api.users.repositories.UserRepository;
import com.multi.shop.api.multi_shop_api.users.services.UserService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import jakarta.validation.Valid;

import static com.multi.shop.api.multi_shop_api.security.JwtConfig.*;

@RestController
@RequestMapping("/app/users")
@CrossOrigin(originPatterns = "*")
public class AuthController {
    private final UserService service;
    private final UserRepository repository;

    public AuthController(UserService service, UserRepository repository) {
        this.service = service;
        this.repository = repository;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RegisterUserDTO> create(@Valid @ModelAttribute RegisterUserDTO user) {
        RegisterUserDTO newUser = service.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(newUser);
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterUserDTO> register(@Valid @ModelAttribute RegisterUserDTO user) {
        RegisterUserDTO newUser = AuthMapper.MAPPER.requestDTOtoNotAdmin(user, false);
        return create(newUser);
    }

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<UserResponseDTO> getUser(@RequestHeader("Token") String token) {
        Optional<User> optionalUser;
        String identifier = null;

        try {
            Claims claims = Jwts.parser().verifyWith(SECRET_KEY).build().parseSignedClaims(token).getPayload();
            identifier = claims.getSubject();
        } catch(JwtException e) {
            return ResponseEntity.badRequest().build();
        }

        if (isNumeric(identifier))
            optionalUser = repository.findByPhoneNumber(Long.parseLong(identifier));
        else
            optionalUser = repository.findByEmail(identifier);

        UserResponseDTO user = AuthMapper.MAPPER.userToUserResponse(
            optionalUser.orElseThrow(() -> new NotFoundException("User not found"))
        );

        return ResponseEntity.ok().body(user);
    }

    @GetMapping("/token-validation")
    public ResponseEntity<Void> tokenValidation(@RequestHeader("Token") String token) {
        try {
            Jwts.parser().verifyWith(SECRET_KEY).build().parseSignedClaims(token).getPayload();
        } catch(JwtException e) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok().build();
    }

    public boolean isNumeric(String str) {
        return str != null && str.matches("\\d+");
    }
}
