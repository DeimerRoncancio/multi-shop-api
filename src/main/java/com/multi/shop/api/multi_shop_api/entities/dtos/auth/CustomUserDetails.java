package com.multi.shop.api.multi_shop_api.entities.dtos.auth;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.multi.shop.api.multi_shop_api.entities.dtos.LoginUserInfoDTO;

public class CustomUserDetails implements UserDetails {

    private String identity;
    private LoginUserInfoDTO user;
    private String password;
    private boolean enabled;
    private List<GrantedAuthority> authorities;

    public CustomUserDetails(String identity, LoginUserInfoDTO user, String password, boolean enabled, List<GrantedAuthority> authorities) {
        this.identity = identity;
        this.user = user;
        this.password = password;
        this.enabled = enabled;
        this.authorities = authorities;
    }

    @Override
    public String getUsername() {
        return identity;
    }

    public LoginUserInfoDTO getUser() {
        return user;
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
