package com.majestic.food.api.majestic_food_api.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.majestic.food.api.majestic_food_api.entities.User;
import com.majestic.food.api.majestic_food_api.repositories.UserRepository;

import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

@RestController
@RequestMapping("/app")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public List<User> ok() {
        return userRepository.findAll();
    }
}
