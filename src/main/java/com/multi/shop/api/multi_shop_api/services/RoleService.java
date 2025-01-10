package com.multi.shop.api.multi_shop_api.services;

import java.util.List;
import java.util.Optional;

import com.multi.shop.api.multi_shop_api.entities.Role;

public interface RoleService {

    List<Role> findAll();

    Optional<Role> findOne(String id);
}
