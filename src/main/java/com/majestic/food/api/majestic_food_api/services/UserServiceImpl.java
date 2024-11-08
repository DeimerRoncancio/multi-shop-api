package com.majestic.food.api.majestic_food_api.services;

import com.majestic.food.api.majestic_food_api.entities.User;
import com.majestic.food.api.majestic_food_api.repositories.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository repository;
    
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
    public User save(User user) {
        return repository.save(user);
    }

    @Override
    @Transactional
    public Optional<User> update(String id, User user) {
        Optional<User> optionalUser = repository.findById(id);
        
        optionalUser.ifPresent(userDb -> {

            userDb.setName(user.getName());
            userDb.setProfileImage(user.getProfileImage());
            userDb.setSecondName(user.getSecondName());
            userDb.setLastnames(user.getLastnames());
            userDb.setPhoneNumber(user.getPhoneNumber());
            userDb.setGender(user.getGender());
            userDb.setEmail(user.getEmail());
            userDb.setPassword(user.getPassword());

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

    @Override
    public boolean existsByEmail(String email) {
        return repository.existsByEmail(email);
    }

    @Override
    public boolean existsByPhoneNumber(Long number) {
        return repository.existsByPhoneNumber(number);
    }
}
