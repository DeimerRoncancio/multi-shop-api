package com.multi.shop.api.multi_shop_api.entities.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.multi.shop.api.multi_shop_api.entities.Image;

public class UserInfoRequest {

    private String name;
    @JsonIgnoreProperties("id")
    private Image profileImage;
    private String secondName;
    private String lastnames;
    private Long phoneNumber;
    private String gender;
    private String email;
    private boolean admin;
    private boolean enabled;

    public UserInfoRequest(String name, Image profileImage, String secondName, String lastnames,
            Long phoneNumber, String gender, String email, boolean admin, boolean enabled) {
        this.name = name;
        this.profileImage = profileImage;
        this.secondName = secondName;
        this.lastnames = lastnames;
        this.phoneNumber = phoneNumber;
        this.gender = gender;
        this.email = email;
        this.admin = admin;
        this.enabled = enabled;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Image getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(Image profileImage) {
        this.profileImage = profileImage;
    }

    public String getSecondName() {
        return secondName;
    }

    public void setSecondName(String secondName) {
        this.secondName = secondName;
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

    public void setPhoneNumber(Long phoneNumber) {
        this.phoneNumber = phoneNumber;
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

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
