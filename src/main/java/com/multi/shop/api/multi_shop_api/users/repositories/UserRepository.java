package com.multi.shop.api.multi_shop_api.users.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.multi.shop.api.multi_shop_api.users.entities.User;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmail(String email);
    Optional<User> findByPhoneNumber(Long number);
    Page<User> findByAdminTrue(Pageable pageable);
    Page<User> findByAdminFalse(Pageable pageable);
}
