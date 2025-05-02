package com.multi.shop.api.multi_shop_api.auth.entities.dtos;

import com.multi.shop.api.multi_shop_api.users.entities.User;

public class UserInfo {

    private String identifier;
    private User user;

    public UserInfo(User user) {
        this.user = user;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
