package com.multi.shop.api.multi_shop_api.identity.users.services.impl;

import com.multi.shop.api.multi_shop_api.identity.auth.dtos.RegisterUserDTO;
import com.multi.shop.api.multi_shop_api.common.exceptions.InvalidPasswordException;
import com.multi.shop.api.multi_shop_api.common.exceptions.NotFoundException;
import com.multi.shop.api.multi_shop_api.common.exceptions.PasswordMatchException;
import com.multi.shop.api.multi_shop_api.media.api.MediaApi;
import com.multi.shop.api.multi_shop_api.media.api.StoredImage;
import com.multi.shop.api.multi_shop_api.identity.users.dtos.PasswordDTO;
import com.multi.shop.api.multi_shop_api.identity.users.dtos.UserDTO;
import com.multi.shop.api.multi_shop_api.identity.users.dtos.UserResponseDTO;
import com.multi.shop.api.multi_shop_api.identity.users.enums.Field;
import com.multi.shop.api.multi_shop_api.identity.users.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.multi.shop.api.multi_shop_api.identity.users.entities.Role;
import com.multi.shop.api.multi_shop_api.identity.users.entities.User;
import com.multi.shop.api.multi_shop_api.identity.users.mappers.UserMapper;
import com.multi.shop.api.multi_shop_api.identity.users.repositories.RoleRepository;
import com.multi.shop.api.multi_shop_api.identity.users.repositories.UserRepository;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
public class UserServiceImpl implements UserService {
    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository repository;
    private final RoleRepository roleRepository;
    private final MediaApi mediaApi;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public UserServiceImpl(UserRepository repository,
    RoleRepository roleRepository, MediaApi mediaApi, PasswordEncoder passwordEncoder,
    UserMapper userMapper) {
        this.repository = repository;
        this.roleRepository = roleRepository;
        this.mediaApi = mediaApi;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponseDTO> findAll(Pageable pageable) {
        Page<User> users = repository.findAll(pageable);
        return users.map(userMapper::userToResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponseDTO> findByRole(Pageable pageable, boolean isAdmin) {
        Page<User> admins = isAdmin
                ? repository.findByAdminTrue(pageable)
                : repository.findByAdminFalse(pageable);

        return admins.map(userMapper::userToResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserResponseDTO> findOne(String id){
        Optional<User> user = repository.findById(id);
        return user.map(userMapper::userToResponseDTO);
    }

    @Override
    @Transactional
    public RegisterUserDTO save(RegisterUserDTO userDTO) {
        User user = userMapper.registerDTOtoUser(userDTO);

        MultipartFile file = userDTO.profileImage();
        if (file != null && !file.isEmpty()) {
            StoredImage image = uploadProfileImage(file);
            user.setProfileImageId(image == null ? null : image.id());
        }

        List<Role> roles = new ArrayList<>();
        roleRepository.findByRole("ROLE_USER").ifPresent(roles::add);

        if (user.isAdmin())
            roleRepository.findByRole("ROLE_ADMIN").ifPresent(roles::add);

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRoles(roles);
        repository.save(user);
        return userMapper.userToRegisterDTO(user);
    }

    @Override
    @Transactional
    public Optional<UserDTO> update(String id, UserDTO userDTO) {
        return repository.findById(id).map(user -> {
            List<Role> roles = user.getRoles();

            if (userDTO.admin() && !user.isAdmin())
                roleRepository.findByRole("ROLE_ADMIN").ifPresent(roles::add);
            if (!userDTO.admin() && user.isAdmin())
                roleRepository.findByRole("ROLE_ADMIN").ifPresent(roles::remove);

            user.setRoles(roles);
            userMapper.toUpdateUser(userDTO, user);

            repository.save(user);
            return userMapper.userToUserDTO(user);
        });
    }

    @Override
    @Transactional
    public Optional<User> updatePassword(String id, PasswordDTO passwordInfo) {
        return repository.findById(id).map(userDb -> {
            String currentPassword = passwordInfo.currentPassword();
            String newPassword = passwordInfo.newPassword();

            if (!passwordEncoder.matches(currentPassword, userDb.getPassword()))
                throw new InvalidPasswordException("Invalid password");
            if (currentPassword.equals(newPassword))
                throw new PasswordMatchException("Both passwords match");

            userDb.setPassword(passwordEncoder.encode(newPassword));
            repository.save(userDb);

            return userDb;
        });
    }

    @Override
    @Transactional
    public StoredImage updateProfileImage(String id, MultipartFile file) {
        User user = repository.findById(id).orElseThrow(() -> new NotFoundException("User not found"));

        if (user.getProfileImageId() != null) deleteProfileImage(user);

        StoredImage image = uploadProfileImage(file);
        user.setProfileImageId(image == null ? null : image.id());

        repository.save(user);
        return image;
    }

    @Override
    @Transactional
    public Optional<User> delete(String id) {
        return repository.findById(id).map(user -> {
            if (user.getProfileImageId() != null) deleteProfileImage(user);
            repository.delete(user);
            return user;
        });
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> userStats(boolean isAdmin) {
        Map<String, Long> userStats = new HashMap<>();

        userStats.put("totalUsers", isAdmin
                ? repository.countByAdminTrue()
                : repository.countByAdminFalse());
        userStats.put("enabledUsers", isAdmin
                ? repository.countByAdminTrueAndEnabledTrue()
                : repository.countByAdminFalseAndEnabledTrue());
        userStats.put("disabledUsers", isAdmin
                ? repository.countByAdminTrueAndEnabledFalse()
                : repository.countByAdminFalseAndEnabledFalse());

        return userStats;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponseDTO> userSearch(Pageable pageable, String identifier, boolean isAdmin, Boolean isEnabled,Field field) {
        Page<User> users = switch (field) {
            case EMAIL -> isEnabled != null
                ? repository.findByEmailEnabled(identifier, isAdmin, isEnabled, pageable)
                : repository.findByEmail(identifier, isAdmin, pageable);
            case NUMBER -> isEnabled != null
                ? repository.findByPhoneEnabled(identifier, isAdmin, isEnabled, pageable)
                : repository.findByPhone(identifier, isAdmin, pageable);
            default -> isEnabled != null
                ? repository.findByNameEnabled(identifier, isAdmin, isEnabled, pageable)
                : repository.findByName(identifier, isAdmin, pageable);
        };

        return users.map(userMapper::userToResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Long usersSize() {
        return repository.count();
    }

    public StoredImage uploadProfileImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            log.warn("File is null or empty");
            return null;
        }

        try {
            return mediaApi.upload(file);
        } catch (IOException e) {
            log.error("Exception to try upload image: {}", String.valueOf(e));
            return null;
        }
    }

    public void deleteProfileImage(User user) {
        mediaApi.deleteAfterCommit(user.getProfileImageId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDTO> latestUsers() {
        return repository.findTop4ByOrderByCreatedAtDesc().stream()
                .map(userMapper::userToResponseDTO).toList();
    }
}
