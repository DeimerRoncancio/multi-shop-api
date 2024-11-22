package com.majestic.food.api.majestic_food_api.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.majestic.food.api.majestic_food_api.entities.User;
import com.majestic.food.api.majestic_food_api.repositories.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository repository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {

        Optional<User> optionalUser = repository.findByEmail(identifier);
        String auxIdentifier = null;

        if (optionalUser.isEmpty()) {
            optionalUser = repository.findByPhoneNumber(Long.parseLong(identifier));
            
            if (optionalUser.isEmpty())
                throw new UsernameNotFoundException(String.format("El usuario %s no existe", auxIdentifier));
            
            auxIdentifier = optionalUser.get().getPhoneNumber().toString();
        } else {
            auxIdentifier = optionalUser.get().getEmail();
        }

        if (optionalUser.isEmpty())
            throw new UsernameNotFoundException(String.format("El usuario %s no existe", auxIdentifier));
        
        User user = optionalUser.orElseThrow();

        return new org.springframework.security.core.userdetails.User(
            auxIdentifier, user.getPassword(),
            user.isEnabled(),
            true,
            true,
            true,
            getAuthorities(user)
        );
    }

    public List<GrantedAuthority> getAuthorities(User user) {
        return user.getRoles().stream()
        .map(role -> new SimpleGrantedAuthority(role.getRole()))
        .collect(Collectors.toList());
    }
}
