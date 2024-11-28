package com.majestic.food.api.majestic_food_api.services;

import com.majestic.food.api.majestic_food_api.auth.RegisterRequest;
import com.majestic.food.api.majestic_food_api.entities.Image;
import com.majestic.food.api.majestic_food_api.entities.Role;
import com.majestic.food.api.majestic_food_api.entities.User;
import com.majestic.food.api.majestic_food_api.entities.dtos.UserUpdateRequest;
import com.majestic.food.api.majestic_food_api.mappers.UserMapper;
import com.majestic.food.api.majestic_food_api.repositories.RoleRepository;
import com.majestic.food.api.majestic_food_api.repositories.UserRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

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

    private final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    
    @Override
    @Transactional(readOnly = true)
    public List<User> findAll() {
        return repository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findOne(String id){ 
        return repository.findById(id);
    }

    @Override
    @Transactional
    public User save(RegisterRequest userDTO, MultipartFile file) {
        List<Role> roles = new ArrayList<>();

        roleRepository.findByRole("ROLE_USER").ifPresent(roles::add);

        if (userDTO.isAdmin())
            roleRepository.findByRole("ROLE_ADMIN").ifPresent(roles::add);

        Image image = uploadProfileImage(file);
        userDTO.setProfileImage(image);
        
        userDTO.setRoles(roles);
        User user = UserMapper.mapper.userCreateDTOtoUser(userDTO);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        return repository.save(user);
    }

    @Override
    @Transactional
    public Optional<User> update(String id, UserUpdateRequest userDTO) {
        Optional<User> optionalUser = repository.findById(id);
        
        optionalUser.ifPresent(userDb -> {
            List<Role> roles = userDb.getRoles();
            
            if (userDTO.isAdmin() && !userDb.isAdmin())
                roleRepository.findByRole("ROLE_ADMIN").ifPresent(roles::add);
            
            if (!userDTO.isAdmin() && userDb.isAdmin())
                roleRepository.findByRole("ROLE_ADMIN").ifPresent(roles::remove);

            userDTO.setRoles(roles);
            UserMapper.mapper.toUpdateUser(userDTO, userDb);

            repository.save(userDb);
        });

        return optionalUser;
    }

    @Override
    @Transactional
    public User updateProfileImage(User user, MultipartFile file) {
        if (user.getProfileImage() != null)
            deleteProfileImage(user);
        
        Image image = uploadProfileImage(file);        
        user.setProfileImage(image);

        return repository.save(user);
    }

    @Override
    @Transactional
    public Optional<User> delete(String id) {
        Optional<User> optionalUser = repository.findById(id);

        optionalUser.ifPresent(user -> {
            if (user.getProfileImage() != null)
                deleteProfileImage(user);
            
            repository.delete(user);
        });

        return optionalUser;
    }

    public Image uploadProfileImage(MultipartFile file) {
        Image image = null;
        
        if (file != null && !file.isEmpty()) {
            try {
                image = imageService.uploadImage(file);
            } catch (IOException e) {
                logger.error("Exception to try upload image: " + e);
            }
        }

        return image;
    }

    public void deleteProfileImage(User user) {
        try {
            imageService.deleteImage(user.getProfileImage());
        } catch(IOException e) {
            logger.error("Exception to try delete the image: " + e);
        }
    }
}
