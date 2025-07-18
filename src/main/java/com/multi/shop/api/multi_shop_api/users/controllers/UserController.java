package com.multi.shop.api.multi_shop_api.users.controllers;

import com.multi.shop.api.multi_shop_api.common.exceptions.NotFoundException;
import com.multi.shop.api.multi_shop_api.images.entities.Image;
import com.multi.shop.api.multi_shop_api.users.dtos.PasswordDTO;
import com.multi.shop.api.multi_shop_api.users.dtos.UserDTO;
import com.multi.shop.api.multi_shop_api.users.mappers.UserMapper;
import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.multi.shop.api.multi_shop_api.users.entities.User;
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
import java.util.Optional;

@RestController
@RequestMapping("/app/users")
@CrossOrigin(originPatterns = "*")
public class UserController {
    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Page<User> viewAll(Pageable pageable) {
        return service.findAll(pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> view(@PathVariable String id) {
        Optional<User> opProduct = service.findOne(id);

        return opProduct.map(product -> ResponseEntity.ok().body(product))
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> update(@Valid @RequestBody UserDTO userDTO, @PathVariable String id) {
        Optional<UserDTO> userOptional = service.update(id, userDTO);
        UserDTO user = userOptional.orElseThrow(() -> new NotFoundException("User not found"));

        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @PutMapping("/update/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<UserDTO> updateUser(@Valid @RequestBody UserDTO userDTO, @PathVariable String id) {
        UserDTO user = UserMapper.MAPPER.userDTOtoNotAdmin(userDTO, false);
        return update(user, id);
    }

    @PutMapping("/update/password/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Void> updatePassword(@Valid @RequestBody PasswordDTO newPassword,
    @PathVariable String id) {
        service.updatePassword(id, newPassword).orElseThrow(() -> new NotFoundException("User nor found"));
        return ResponseEntity.ok().build();
    }

    @PutMapping("/update/profile-image/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Image> updateProfileImage(@PathVariable String id, @RequestPart MultipartFile file) {
        Optional<User> optionalUser = service.findOne(id);
        User user = optionalUser.orElseThrow(() -> new NotFoundException("User not found"));

        return ResponseEntity.status(HttpStatus.CREATED).body(user.getImageUser());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        Optional<User> optionalUser = service.delete(id);
        optionalUser.orElseThrow(() -> new NotFoundException("User not found"));

        return ResponseEntity.ok().build();
    }
}
