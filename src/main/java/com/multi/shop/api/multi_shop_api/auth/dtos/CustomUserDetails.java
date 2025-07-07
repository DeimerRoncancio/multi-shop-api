package com.multi.shop.api.multi_shop_api.auth.dtos;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.multi.shop.api.multi_shop_api.users.entities.User;

public record CustomUserDetails(
    String identity,
    String password,
    boolean enabled,
    List<GrantedAuthority> authorities
) implements UserDetails {
    @Override
    public String getUsername() {
        return identity;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
}
