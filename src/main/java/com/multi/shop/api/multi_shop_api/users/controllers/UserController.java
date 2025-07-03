package com.multi.shop.api.multi_shop_api.users.controllers;

import com.multi.shop.api.multi_shop_api.users.entities.dtos.UpdatePasswordRequest;
import com.multi.shop.api.multi_shop_api.users.repository.UserRepository;
import jakarta.validation.Valid;

import org.apache.coyote.Response;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.multi.shop.api.multi_shop_api.users.entities.User;
import com.multi.shop.api.multi_shop_api.users.entities.dtos.UserUpdateRequest;
import com.multi.shop.api.multi_shop_api.users.services.UserService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/app/users")
@CrossOrigin(originPatterns = "*")
public class UserController {
    private final UserService service;
    private final UserRepository repository;

    public UserController(UserService service, UserRepository repository) {
        this.service = service;
        this.repository = repository;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<User> viewAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> view(@PathVariable String id) {
        Optional<User> opProduct = service.findOne(id);

        return opProduct.map(product ->
            ResponseEntity.ok().body(product)
        ).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> update(@Valid @RequestBody UserUpdateRequest user, BindingResult result, 
    @PathVariable String id) {
        handleObjectError(user, id, result);

        if (result.hasErrors())
            return validate(result);
        
        Optional<User> userOptional = service.update(id, user);

        if (userOptional.isPresent())
            return ResponseEntity.status(HttpStatus.CREATED).body(user);

        return ResponseEntity.notFound().build();
    }

    @PutMapping("/update/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> updateUser(@Valid @RequestBody UserUpdateRequest user, BindingResult result, 
    @PathVariable String id) {
        user.setAdmin(false);

        return update(user, result, id);
    }

    @PutMapping("/update/password/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> updatePassword(@Valid @RequestBody UpdatePasswordRequest passwordRequest,
    BindingResult result, @PathVariable String id) throws Exception {
        if (result.hasErrors())
            return validate(result);

        Optional<?> newUser = service.updatePassword(id, passwordRequest);

        if (newUser.isPresent()) {
            if (newUser.get().equals("PASSWORD_UNAUTHORIZED")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(newUser.get());
            }

            if (newUser.get().equals("SAME_PASSWORD"))
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(newUser.get());
        }

        return ResponseEntity.ok().build();
    }

    @PutMapping("/update/profile-image/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> updateProfileImage(@PathVariable String id, @RequestPart MultipartFile file) {
        Optional<User> optionalUser = service.findOne(id);

        if (optionalUser.isPresent()) {
            User user = service.updateProfileImage(optionalUser.get(), file);
            return ResponseEntity.status(HttpStatus.CREATED).body(user.getImageUser());
        }

        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<?> delete(@PathVariable String id) {
        Optional<User> optionalUser = service.delete(id);

        if (optionalUser.isPresent())
            return ResponseEntity.ok().build();
        
        return ResponseEntity.notFound().build();
    }

    public ResponseEntity<?> validate(BindingResult result) {
        Map<String, String> errors = new HashMap<> ();

        result.getFieldErrors().forEach(err -> {
            errors.put(err.getField(), "El campo " + err.getField() + " " + err.getDefaultMessage());
        });

        result.getGlobalErrors().forEach(err -> {
           errors.put(err.getObjectName(), "El campo " + err.getObjectName() + " " + err.getDefaultMessage());
        });

        return ResponseEntity.badRequest().body(errors);
    }

    public void handleObjectError(UserUpdateRequest userDto, String id, BindingResult result) {
        Optional<User> user = repository.findById(id);

        user.ifPresent(a -> {
            if (user.get().getPhoneNumber() == null || user.get().getPhoneNumber().equals(userDto.getPhoneNumber()))
                return;

            repository.findByPhoneNumber(userDto.getPhoneNumber()).ifPresent(u -> {
                String defaultMessage = "tiene un valor existente";
                result.addError(new ObjectError("phoneNumber", defaultMessage));
            });
        });

        user.ifPresent(a -> {
            if (user.get().getEmail().equals(userDto.getEmail())) return;

            repository.findByEmail(userDto.getEmail()).ifPresent(u -> {
                String defaultMessage = "tiene un valor existente";
                result.addError(new ObjectError("email", defaultMessage));
            });
        });
    }
}
