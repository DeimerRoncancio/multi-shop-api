package com.multi.shop.api.multi_shop_api.auth.controllers;

import java.util.Optional;

import com.multi.shop.api.multi_shop_api.auth.dtos.RegisterRequestDTO;
import com.multi.shop.api.multi_shop_api.auth.mappers.AuthMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.multi.shop.api.multi_shop_api.users.entities.User;
import com.multi.shop.api.multi_shop_api.users.dtos.UserResponseDTO;
import com.multi.shop.api.multi_shop_api.users.repository.UserRepository;
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
    public ResponseEntity<RegisterRequestDTO> create(@Valid @ModelAttribute RegisterRequestDTO user) {
        RegisterRequestDTO newUser = service.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(newUser);
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterRequestDTO> register(@Valid @ModelAttribute RegisterRequestDTO user) {
        RegisterRequestDTO userUpdated = AuthMapper.MAPPER.requestDTOtoNotAdmin(user, false);
        return create(userUpdated);
    }

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> getUser(@RequestHeader("Token") String token) {
        Optional<User> optionalUser;
        String identifier = null;

        try {
            Claims claims = Jwts.parser().verifyWith(SECRET_KEY).build().parseSignedClaims(token).getPayload();
            identifier = claims.getSubject();
        } catch(JwtException e) {
            return ResponseEntity.badRequest().build();
        }

        if (isNumeric(identifier)) {
            optionalUser = repository.findByPhoneNumber(Long.parseLong(identifier));
        } else {
            optionalUser = repository.findByEmail(identifier);
        }

        if (optionalUser.isPresent()) {
            UserResponseDTO user = getUser(optionalUser.get());
            return ResponseEntity.ok().body(user);
        }

        return ResponseEntity.notFound().build();
    }

    @GetMapping("/token-validation")
    public ResponseEntity<?> tokenValidation(@RequestHeader("Token") String token) {
        try {
            Jwts.parser().verifyWith(SECRET_KEY).build().parseSignedClaims(token).getPayload();
        } catch(JwtException e) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok().build();
    }

    public UserResponseDTO getUser(User user) {
        return new UserResponseDTO(
            user.getId(),
            user.getName(),
            user.getImageUser(),
            user.getSecondName(),
            user.getLastnames(),
            user.getPhoneNumber(),
            user.getGender(),
            user.getEmail(),
            user.isAdmin(),
            user.isEnabled()
        );
    }

    public boolean isNumeric(String str) {
        return str != null && str.matches("\\d+");
    }
}
