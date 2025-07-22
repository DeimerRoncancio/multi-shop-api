package com.multi.shop.api.multi_shop_api.users.services.impl;

import com.multi.shop.api.multi_shop_api.auth.dtos.RegisterUserDTO;
import com.multi.shop.api.multi_shop_api.common.exceptions.InvalidPasswordException;
import com.multi.shop.api.multi_shop_api.common.exceptions.NotFoundException;
import com.multi.shop.api.multi_shop_api.common.exceptions.PasswordMatchException;
import com.multi.shop.api.multi_shop_api.images.services.ImageService;
import com.multi.shop.api.multi_shop_api.users.dtos.PasswordDTO;
import com.multi.shop.api.multi_shop_api.users.dtos.UserDTO;
import com.multi.shop.api.multi_shop_api.users.dtos.UserResponseDTO;
import com.multi.shop.api.multi_shop_api.users.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.multi.shop.api.multi_shop_api.images.entities.Image;
import com.multi.shop.api.multi_shop_api.users.entities.Role;
import com.multi.shop.api.multi_shop_api.users.entities.User;
import com.multi.shop.api.multi_shop_api.users.mappers.UserMapper;
import com.multi.shop.api.multi_shop_api.users.repositories.RoleRepository;
import com.multi.shop.api.multi_shop_api.users.repositories.UserRepository;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {
    private final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    private final UserRepository repository;
    private final RoleRepository roleRepository;
    private final ImageService imageService;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository repository, RoleRepository roleRepository, ImageService imageService, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.roleRepository = roleRepository;
        this.imageService = imageService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponseDTO> findAll(Pageable pageable) {
        Page<User> users = repository.findAll(pageable);
        return users.map(UserMapper.MAPPER::userToResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponseDTO> findByRole(Pageable pageable, boolean isAdmin) {
        Page<User> admins = null;

        if (isAdmin) admins = repository.findByAdminTrue(pageable);
        else admins = repository.findByAdminFalse(pageable);

        return admins.map(UserMapper.MAPPER::userToResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserResponseDTO> findOne(String id){
        Optional<User> user = repository.findById(id);
        return user.map(UserMapper.MAPPER::userToResponseDTO);
    }

    @Override
    @Transactional
    public RegisterUserDTO save(RegisterUserDTO userDTO) {
        User user = UserMapper.MAPPER.registerDTOtoUser(userDTO);
        MultipartFile file = userDTO.profileImage();

        List<Role> roles = new ArrayList<>();
        roleRepository.findByRole("ROLE_USER").ifPresent(roles::add);

        if (user.isAdmin())
            roleRepository.findByRole("ROLE_ADMIN").ifPresent(roles::add);

        if (file != null && !file.isEmpty()) {
            Image image = uploadProfileImage(file);
            user.setImageUser(image);
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRoles(roles);
        repository.save(user);
        return UserMapper.MAPPER.userToRegisterDTO(user);
    }

    @Override
    @Transactional
    public Optional<UserDTO> update(String id, UserDTO userDTO) {
        Optional<User> optionalUser = repository.findById(id);

        if (optionalUser.isEmpty()) return Optional.empty();

        User user = optionalUser.get();
        List<Role> roles = user.getRoles();

        if (userDTO.admin() && !user.isAdmin())
            roleRepository.findByRole("ROLE_ADMIN").ifPresent(roles::add);
        if (!userDTO.admin() && user.isAdmin())
            roleRepository.findByRole("ROLE_ADMIN").ifPresent(roles::remove);

        user.setRoles(roles);
        UserMapper.MAPPER.toUpdateUser(userDTO, user);

        repository.save(user);
        return Optional.of(UserMapper.MAPPER.userToUserDTO(user));
    }

    @Override
    @Transactional
    public Optional<User> updatePassword(String id, PasswordDTO passwordInfo) {
        String currentPassword = passwordInfo.currentPassword();
        String newPassword = passwordInfo.newPassword();
        Optional<User> userDb = repository.findById(id);

        if (userDb.isPresent()) {
            if (!passwordEncoder.matches(currentPassword, userDb.get().getPassword()))
                throw new InvalidPasswordException("Invalid password");
            if (currentPassword.equals(newPassword))
                throw new PasswordMatchException("Both passwords match");

            userDb.get().setPassword(passwordEncoder.encode(newPassword));
            repository.save(userDb.get());
        }

        return userDb;
    }

    @Override
    @Transactional
    public User updateProfileImage(String id, MultipartFile file) {
        Optional<User> userOp = repository.findById(id);
        User user = userOp.orElseThrow(() -> new NotFoundException("User not found"));

        if (user.getImageUser() != null)
            deleteProfileImage(user);
        
        Image image = uploadProfileImage(file);        
        user.setImageUser(image);

        return repository.save(user);
    }

    @Override
    @Transactional
    public Optional<User> delete(String id) {
        Optional<User> optionalUser = repository.findById(id);

        optionalUser.ifPresent(user -> {
            if (user.getImageUser() != null)
                deleteProfileImage(user);
            
            repository.delete(user);
        });
        
        return optionalUser;
    }

    @Override
    @Transactional(readOnly = true)
    public int usersSize() {
        return findAll(Pageable.unpaged()).getContent().size();
    }

    public Image uploadProfileImage(MultipartFile file) {
        Image image = null;
        
        if (file != null && !file.isEmpty()) {
            try {
                image = imageService.uploadImage(file);
            } catch (IOException e) {
                logger.error("Exception to try upload image: {}", String.valueOf(e));
            }
        }

        return image;
    }

    public void deleteProfileImage(User user) {
        try {
            imageService.deleteImage(user.getImageUser());
        } catch(IOException e) {
            logger.error("Exception to try delete the image: {}", String.valueOf(e));
        }
    }
}
