package com.majestic.food.api.majestic_food_api.entities.dtos;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.majestic.food.api.majestic_food_api.entities.Image;
import com.majestic.food.api.majestic_food_api.entities.Role;
import com.majestic.food.api.majestic_food_api.entities.User;
import com.majestic.food.api.majestic_food_api.validation.IfExistsUpdate;
import com.majestic.food.api.majestic_food_api.validation.SizeConstraint;

import jakarta.persistence.CascadeType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class UserUpdateRequest {

    @NotBlank(message = "{NotBlank.validation.text}")
    private String name;
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private Image profileImage;
    private String secondName;
    private String lastnames;

    @IfExistsUpdate(entity = User.class, field = "phoneNumber", message = "{IfExists.user.phone}")
    private Long phoneNumber;
    private String gender;

    @Email
    @IfExistsUpdate(entity = User.class, field = "email")
    @NotBlank(message = "{NotBlank.validation.text}")
    private String email;

    @SizeConstraint(min = 8, max = 255)
    @NotBlank(message = "{NotBlank.validation.text}")
    // @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @ManyToMany
    @JoinTable(
        name = "roles_to_users",
        joinColumns = @JoinColumn(name = "id_user"),
        inverseJoinColumns = @JoinColumn(name = "id_role"),
        uniqueConstraints = @UniqueConstraint(columnNames = {"id_user", "id_role"}))
    @JsonIgnoreProperties("users")
    private List<Role> roles;
    
    private boolean admin;
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Image getProfileImage() {
        return profileImage;
    }
    
    public void setProfileImage(Image profile_image) {
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

    public List<Role> getRoles() {
        return roles;
    }
    
    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }
    
    public boolean isAdmin() {
        return admin;
    }
    
    public void setAdmin(boolean isAdmin) {
        this.admin = isAdmin;
    }
}
