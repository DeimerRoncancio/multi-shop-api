package com.majestic.food.api.majestic_food_api.entities.dtos;

import com.majestic.food.api.majestic_food_api.entities.User;
import com.majestic.food.api.majestic_food_api.validation.IfExistsUpdate;
import com.majestic.food.api.majestic_food_api.validation.SizeConstraint;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class UserUpdateDTO {

    @NotBlank(message = "{NotBlank.validation.text}")
    private String name;
    private String profileImage;
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
}
