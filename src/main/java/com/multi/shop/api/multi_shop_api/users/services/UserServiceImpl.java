package com.multi.shop.api.multi_shop_api.users.services;

import com.multi.shop.api.multi_shop_api.images.services.ImageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.multi.shop.api.multi_shop_api.auth.entities.RegisterRequest;
import com.multi.shop.api.multi_shop_api.images.entities.Image;
import com.multi.shop.api.multi_shop_api.users.entities.Role;
import com.multi.shop.api.multi_shop_api.users.entities.User;
import com.multi.shop.api.multi_shop_api.users.entities.dtos.UserUpdateRequest;
import com.multi.shop.api.multi_shop_api.users.mappers.UserMapper;
import com.multi.shop.api.multi_shop_api.users.repository.RoleRepository;
import com.multi.shop.api.multi_shop_api.users.repository.UserRepository;

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
    public RegisterRequest save(RegisterRequest userDTO, MultipartFile file) {
        List<Role> roles = new ArrayList<>();

        roleRepository.findByRole("ROLE_USER").ifPresent(roles::add);

        if (userDTO.isAdmin())
            roleRepository.findByRole("ROLE_ADMIN").ifPresent(roles::add);

        if (file != null && !file.isEmpty()) {
            Image image = uploadProfileImage(file);
            userDTO.setImageUser(image);
        }
        
        userDTO.setRoles(roles);
        User user = UserMapper.mapper.userCreateDTOtoUser(userDTO);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        repository.save(user);
        return userDTO;
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

    public User updatePassword(User userDTO, String password) {
        User userDb = new User();

        if (passwordEncoder.matches(userDTO.getPassword(), userDb.getPassword()))
            userDTO.setPassword(userDTO.getPassword());
        else
            userDTO.setPassword(passwordEncoder.encode(userDTO.getPassword()));

        return new User();
    }

    @Override
    @Transactional
    public User updateProfileImage(User user, MultipartFile file) {
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
        List<User> users = findAll();
        return users.size();
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
