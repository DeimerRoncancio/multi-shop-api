package com.majestic.food.api.majestic_food_api.entities.dtos;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.majestic.food.api.majestic_food_api.entities.Order;
import com.majestic.food.api.majestic_food_api.entities.Role;
import com.majestic.food.api.majestic_food_api.entities.User;
import com.majestic.food.api.majestic_food_api.validation.IfExists;
import com.majestic.food.api.majestic_food_api.validation.SizeConstraint;

import jakarta.persistence.CascadeType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class UserUpdateDTO {

    @NotBlank(message = "{NotBlank.validation.text}")
    private String name;
    private String profileImage;
    private String secondName;
    private String lastnames;

    @IfExists(entity = User.class, field = "phoneNumber", message = "{IfExists.user.phone}")
    private Long phoneNumber;
    private String gender;

    @Email
    @IfExists(entity = User.class, field = "email")
    @NotBlank(message = "{NotBlank.validation.text}")
    private String email;

    @NotBlank(message = "{NotBlank.validation.text}")
    @SizeConstraint(min = 8, max = 255)
    // @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @JsonIgnoreProperties({"user"})
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Order> orders;

    @ManyToMany
    @JoinTable(
        name = "roles_to_users",
        joinColumns = @JoinColumn(name = "id_user"),
        inverseJoinColumns = @JoinColumn(name = "id_role"),
        uniqueConstraints = @UniqueConstraint(columnNames = {"id_user", "id_role"}))
    @JsonIgnoreProperties("users")
    private List<Role> roles;

    @Transient
    private boolean isAdmin;
    
    public UserUpdateDTO() {
        this.roles = new ArrayList<> ();
        this.orders = new ArrayList<> ();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profile_image) {
        this.profileImage = profile_image;
    }

    public String getSecondName() {
        return secondName;
    }

    public void setSecondName(String second_name) {
        this.secondName = second_name;
    }

    public String getLastnames() {
        return lastnames;
    }

    public void setLastnames(String lastnames) {
        this.lastnames = lastnames;
    }

    public Long getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(Long phone_number) {
        this.phoneNumber = phone_number;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    
    public List<Order> getOrders() {
        return orders;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }

    public List<Role> getRoles() {
        return roles;
    }

    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }
}
