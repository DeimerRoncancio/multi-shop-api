package com.multi.shop.api.multi_shop_api.users.entities.dtos;

import jakarta.validation.constraints.NotBlank;

public class UpdatePasswordRequest {
    @NotBlank
    private String currentPassword;
    @NotBlank
    private String newPassword;

    public UpdatePasswordRequest(String currentPassword, String newPassword) {
        this.currentPassword = currentPassword;
        this.newPassword = newPassword;
    }

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
