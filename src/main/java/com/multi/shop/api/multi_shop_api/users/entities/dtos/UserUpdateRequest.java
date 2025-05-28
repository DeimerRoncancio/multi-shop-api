package com.multi.shop.api.multi_shop_api.users.entities.dtos;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.multi.shop.api.multi_shop_api.users.entities.Role;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class UserUpdateRequest {
    @NotBlank(message = "{NotBlank.validation.text}")
    private String name;
    private String secondName;
    private String lastnames;
    private Long phoneNumber;
    private String gender;

    @Email
    @NotBlank(message = "{NotBlank.validation.text}")
    private String email;

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
