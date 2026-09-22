package com.multi.shop.api.multi_shop_api.security;

import com.multi.shop.api.multi_shop_api.users.entities.User;
import com.multi.shop.api.multi_shop_api.users.repositories.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("authz")
public class AuthorizationChecks {
    private final UserRepository userRepository;

    public AuthorizationChecks(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public boolean isSelf(String userId, Authentication authentication) {
        return userRepository.findById(userId)
            .map(user -> isSameUser(user, authentication))
            .orElse(false);
    }

    private boolean isSameUser(User user, Authentication authentication) {
        if (authentication == null) return false;

        String identity = authentication.getName();
        return identity.equals(user.getEmail())
            || (user.getPhoneNumber() != null && identity.equals(user.getPhoneNumber().toString()));
    }
}
