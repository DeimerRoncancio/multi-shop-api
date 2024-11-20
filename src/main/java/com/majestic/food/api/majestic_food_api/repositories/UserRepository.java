package com.majestic.food.api.majestic_food_api.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.majestic.food.api.majestic_food_api.entities.User;

public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByEmail(String email);

    Optional<User> findByPhoneNumber(Long number);
}
