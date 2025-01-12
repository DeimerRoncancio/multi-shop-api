package com.multi.shop.api.multi_shop_api.auth;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.multi.shop.api.multi_shop_api.entities.User;
import com.multi.shop.api.multi_shop_api.entities.dtos.UserInfoRequest;
import com.multi.shop.api.multi_shop_api.repositories.UserRepository;
import com.multi.shop.api.multi_shop_api.services.UserService;

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
    public ResponseEntity<?> create(@Valid @ModelAttribute RegisterRequest user, BindingResult result, 
    @RequestPart MultipartFile profileImage) {
        if (result.hasFieldErrors())
            return validate(result);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(service.save(user, profileImage));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @ModelAttribute RegisterRequest user, BindingResult result, 
    @RequestPart MultipartFile file) {
        user.setAdmin(false);

        return create(user, result, file);
    }

    @GetMapping("/get-user/{token}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> getUser(@PathVariable String token) {
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

        UserInfoRequest user = getUser(optionalUser.get());
        
        return ResponseEntity.ok().body(user);
    }

    @GetMapping("/token-validation/{token}")
    public ResponseEntity<?> tokenValidation(@PathVariable String token) {
        try {
            Jwts.parser().verifyWith(SECRET_KEY).build().parseSignedClaims(token).getPayload();
        } catch(JwtException e) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok().build();
    }

    public ResponseEntity<?> validate(BindingResult result) {
        Map<String, String> errors = new HashMap<> ();

        result.getFieldErrors().forEach(err -> {
            errors.put(err.getField(), "El campo " + err.getField() + " " + err.getDefaultMessage());
        });

        return ResponseEntity.badRequest().body(errors);
    }

    public UserInfoRequest getUser(User user) {
        return new UserInfoRequest(
            user.getName(),
            user.getProfileImage(),
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
