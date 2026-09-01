package com.multi.shop.api.multi_shop_api.payments.entities;

import com.multi.shop.api.multi_shop_api.users.entities.User;
import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;

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
    private String customerAddress;

    public Customer() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCustomerAddress() {
        return customerAddress;
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

    public void setCustomerAddress(String customerAddress) {
        this.customerAddress = customerAddress;
    }
}
