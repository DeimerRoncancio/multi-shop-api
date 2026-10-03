package com.multi.shop.identity.users.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.multi.shop.identity.users.entities.Role;

public interface RoleRepository extends JpaRepository<Role, String> {
    Optional<Role> findByRole(String role);
}
