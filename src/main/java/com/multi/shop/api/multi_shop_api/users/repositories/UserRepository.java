package com.multi.shop.api.multi_shop_api.users.repositories;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.multi.shop.api.multi_shop_api.users.entities.User;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmail(String email);
    Optional<User> findByPhoneNumber(Long number);
    Page<User> findByAdminTrue(Pageable pageable);
    Page<User> findByAdminFalse(Pageable pageable);

    @Query("select u from User u where u.name like CONCAT('%', ?1, '%') and u.admin=?2 and u.enabled=?3")
    Page<User> findByNameEnabled(String name, boolean isAdmin, boolean isEnabled, Pageable pageable);
    @Query("select u from User u where u.name like CONCAT('%', ?1, '%') and u.admin=?2")
    Page<User> findByName(String name, boolean isAdmin, Pageable pageable);

    @Query("select u from User u where u.email like CONCAT('%', ?1, '%') and u.admin=?2 and u.enabled=?3")
    Page<User> findByEmailEnabled(String email, boolean isAdmin, boolean isEnabled, Pageable pageable);
    @Query("select u from User u where u.email like CONCAT('%', ?1, '%') and u.admin=?2")
    Page<User> findByEmail(String email, boolean isAdmin, Pageable pageable);

    @Query("select u from User u where str(u.phoneNumber) like CONCAT('%', ?1, '%') and u.admin=?2 and u.enabled=?3")
    Page<User> findByPhoneEnabled(String phoneNumber, boolean isAdmin, boolean isEnabled, Pageable pageable);
    @Query("select u from User u where str(u.phoneNumber) like CONCAT('%', ?1, '%') and u.admin=?2")
    Page<User> findByPhone(String phoneNumber, boolean isAdmin, Pageable pageable);

    Long countByAdminFalse();
    Long countByAdminTrue();
    Long countByAdminFalseAndEnabledTrue();
    Long countByAdminFalseAndEnabledFalse();
    Long countByAdminTrueAndEnabledTrue();
    Long countByAdminTrueAndEnabledFalse();
}
