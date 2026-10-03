package com.multi.shop.identity.users.services;

import java.util.List;
import java.util.Optional;

import com.multi.shop.identity.users.entities.Role;

public interface RoleService {
    List<Role> findAll();
    Optional<Role> findOne(String id);
}
