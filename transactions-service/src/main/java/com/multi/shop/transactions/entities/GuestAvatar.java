package com.multi.shop.transactions.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "guest_avatars")
public class GuestAvatar {
    @Id
    @Column(name = "email", nullable = false, updatable = false)
    private String email;

    @Column(name = "avatar", nullable = false)
    private int avatar;

    public GuestAvatar() {}

    public GuestAvatar(String email, int avatar) {
        this.email = email;
        this.avatar = avatar;
    }

    public String getEmail() {
        return email;
    }

    public int getAvatar() {
        return avatar;
    }

    public void setAvatar(int avatar) {
        this.avatar = avatar;
    }
}
