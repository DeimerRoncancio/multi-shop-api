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
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private UserRepository repository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ImageService imageService;

    @Autowired
    private PasswordEncoder passwordEncoder;

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

        Image image = upload(file);
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
    public Optional<User> delete(String id) {
        Optional<User> optionalUser = repository.findById(id);
        
        optionalUser.ifPresent(user -> {
            repository.delete(user);
        });

        return optionalUser;
    }

    public Image upload(MultipartFile file) {
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
}
