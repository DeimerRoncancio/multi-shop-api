package com.multi.shop.api.multi_shop_api.payments.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "guests")
public class Guest {
    @Id
    @UuidGenerator
    @JoinColumn(name = "id", updatable = false, nullable = false)
    private String id;
    private String userNames;
    @JoinColumn(name = "userEmail")
    private String userEmail;
    private String userPhone;

    public Guest() {}

    public Guest(String id, String userNames, String userEmail, String userPhone) {
        this.id = id;
        this.userNames = userNames;
        this.userEmail = userEmail;
        this.userPhone = userPhone;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserNames() {
        return userNames;
    }

    public void setUserNames(String userNames) {
        this.userNames = userNames;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }
}
