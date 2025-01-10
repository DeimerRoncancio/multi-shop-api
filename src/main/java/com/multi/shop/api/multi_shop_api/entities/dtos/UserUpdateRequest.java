package com.multi.shop.api.multi_shop_api.entities.dtos;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.multi.shop.api.multi_shop_api.entities.Role;
import com.multi.shop.api.multi_shop_api.entities.User;
import com.multi.shop.api.multi_shop_api.validation.IfExistsUpdate;
import com.multi.shop.api.multi_shop_api.validation.SizeConstraint;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class UserUpdateRequest {

    @NotBlank(message = "{NotBlank.validation.text}")
    private String name;
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
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @JsonIgnoreProperties({"users", "id"})
    private List<Role> roles;
    
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private boolean admin;
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
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
