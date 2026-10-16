package com.multi.shop.transactions.controllers;

import com.multi.shop.transactions.dtos.GuestAvatarDTO;
import com.multi.shop.transactions.services.GuestAvatarService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/app/guests")
public class GuestsController {
    private final GuestAvatarService avatarService;

    public GuestsController(GuestAvatarService avatarService) {
        this.avatarService = avatarService;
    }

    @GetMapping("/avatar")
    public GuestAvatarDTO getAvatar(@RequestParam String email) {
        return avatarService.getAvatar(email);
    }

    @PutMapping("/avatar")
    public GuestAvatarDTO saveAvatar(@Valid @RequestBody GuestAvatarDTO dto) {
        return avatarService.saveAvatar(dto);
    }
}
