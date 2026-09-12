package com.multi.shop.api.multi_shop_api.payments.entities;

import com.multi.shop.api.multi_shop_api.users.entities.User;
import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "customers")
public class Customer {
    @Id
    @UuidGenerator
    @JoinColumn(name = "id", updatable = false, nullable = false)
    private String id;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_email", referencedColumnName = "email")
    private User user;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "guest_email", referencedColumnName = "userEmail")
    private Guest guest;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Address> address;

    public Customer() {
        this.address = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Address> getAddress() {
        return address;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Guest getGuest() {
        return guest;
    }

    public void setGuest(Guest guest) {
        this.guest = guest;
    }

    public boolean isGuest() {
        return guest != null;
    }

    public void setAddress(List<Address> address) {
        this.address = address;
    }
}
