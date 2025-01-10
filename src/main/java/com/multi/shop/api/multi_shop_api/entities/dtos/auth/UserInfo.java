package com.multi.shop.api.multi_shop_api.entities.dtos.auth;

import com.multi.shop.api.multi_shop_api.entities.User;

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
