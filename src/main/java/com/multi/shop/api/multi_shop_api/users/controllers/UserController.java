package com.multi.shop.api.multi_shop_api.users.controllers;

import com.multi.shop.api.multi_shop_api.common.exceptions.NotFoundException;
import com.multi.shop.api.multi_shop_api.images.entities.Image;
import com.multi.shop.api.multi_shop_api.users.dtos.PasswordDTO;
import com.multi.shop.api.multi_shop_api.users.dtos.UserDTO;
import com.multi.shop.api.multi_shop_api.users.dtos.UserResponseDTO;
import com.multi.shop.api.multi_shop_api.users.enums.Field;
import com.multi.shop.api.multi_shop_api.users.mappers.UserMapper;
import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.multi.shop.api.multi_shop_api.users.entities.User;
import com.multi.shop.api.multi_shop_api.users.services.UserService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/app/users")
public class UserController {
    private final UserService service;
    private final UserMapper userMapper;

    public UserController(UserService service, UserMapper userMapper) {
        this.service = service;
        this.userMapper = userMapper;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Page<UserResponseDTO> viewAll(@PageableDefault Pageable pageable) {
        return service.findAll(pageable);
    }

    @GetMapping("/by-role")
    @PreAuthorize("hasRole('ADMIN')")
    public Page<UserResponseDTO> viewByRole(@PageableDefault Pageable pageable, @RequestParam boolean isAdmin) {
        return service.findByRole(pageable, isAdmin);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDTO> view(@PathVariable String id) {
        Optional<UserResponseDTO> opProduct = service.findOne(id);

        return opProduct.map(product -> ResponseEntity.ok().body(product))
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> update(@PathVariable String id, @Valid @RequestBody UserDTO userDTO) {
        Optional<UserDTO> userOptional = service.update(id, userDTO);
        UserDTO user = userOptional.orElseThrow(() -> new NotFoundException("User not found"));

        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @PutMapping("/update/{id}")
    @PreAuthorize("hasRole('ADMIN') or @authz.isSelf(#id, authentication)")
    public ResponseEntity<UserDTO> updateUser(@Valid @RequestBody UserDTO userDTO, @PathVariable String id) {
        UserDTO user = userMapper.userDTOtoOrAdmin(userDTO, false);
        return update(id, user);
    }

    @PutMapping("/update/password/{id}")
    @PreAuthorize("hasRole('ADMIN') or @authz.isSelf(#id, authentication)")
    public ResponseEntity<Void> updatePassword(@Valid @RequestBody PasswordDTO newPassword,
    @PathVariable String id) {
        service.updatePassword(id, newPassword).orElseThrow(() -> new NotFoundException("User nor found"));
        return ResponseEntity.ok().build();
    }

    @PutMapping("/update/profile-image/{id}")
    @PreAuthorize("hasRole('ADMIN') or @authz.isSelf(#id, authentication)")
    public ResponseEntity<Image> updateProfileImage(@PathVariable String id, @RequestPart MultipartFile file) {
        User user = service.updateProfileImage(id, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(user.getImageUser());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @authz.isSelf(#id, authentication)")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        Optional<User> optionalUser = service.delete(id);
        optionalUser.orElseThrow(() -> new NotFoundException("User not found"));

        return ResponseEntity.ok().build();
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Long>> userStats(@RequestParam boolean isAdmin) {
        return ResponseEntity.ok().body(service.userStats(isAdmin));
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserResponseDTO>> userSearch(@PageableDefault Pageable pageable, @RequestParam String identifier,
    @RequestParam boolean isAdmin, @RequestParam(required = false) Boolean isEnabled,
    @RequestParam(required = false, defaultValue = "NAME") Field field) {
        return ResponseEntity.ok().body(service.userSearch(pageable, identifier, isAdmin, isEnabled, field));
    }

    @GetMapping("/latest-users")
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponseDTO> getLatestUsers() {
        return service.latestUsers();
    }
}
