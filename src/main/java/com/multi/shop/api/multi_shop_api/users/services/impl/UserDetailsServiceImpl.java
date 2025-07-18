package com.multi.shop.api.multi_shop_api.users.services.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.multi.shop.api.multi_shop_api.users.entities.User;
import com.multi.shop.api.multi_shop_api.auth.dtos.CustomUserDetails;
import com.multi.shop.api.multi_shop_api.auth.dtos.UserInfo;
import com.multi.shop.api.multi_shop_api.users.repositories.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository repository;

    public UserDetailsServiceImpl(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        UserInfo userInfo = getUserInfo(identifier);

        return new CustomUserDetails(
            identifier, 
            userInfo.user().getPassword(),
            userInfo.user().isEnabled(),
            getAuthorities(userInfo.user())
        );
    }
    
    public UserInfo getUserInfo(String identifier) {
        Optional<User> optionalUser;
        
        if (isNumeric(identifier)) {
            optionalUser = repository.findByPhoneNumber(Long.parseLong(identifier));
        } else {
            optionalUser = repository.findByEmail(identifier);
        }
        
        if (optionalUser.isEmpty())
            throw new UsernameNotFoundException(" ");
        
        return new UserInfo(identifier, optionalUser.get());
    }

    public List<GrantedAuthority> getAuthorities(User user) {
        return user.getRoles().stream()
            .map(role -> new SimpleGrantedAuthority(role.getRole()))
            .collect(Collectors.toList());
    }

    public boolean isNumeric(String str) {
        return str != null && str.matches("\\d+");
    }
}
