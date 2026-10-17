package com.multi.shop.transactions.controllers;

import com.multi.shop.transactions.dtos.GuestAvatarDTO;
import com.multi.shop.transactions.dtos.PurchaseDTO;
import com.multi.shop.transactions.services.GuestAvatarService;
import com.multi.shop.transactions.services.PurchaseService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/app/guests")
public class GuestsController {
    private final GuestAvatarService avatarService;
    private final PurchaseService purchaseService;

    public GuestsController(GuestAvatarService avatarService, PurchaseService purchaseService) {
        this.avatarService = avatarService;
        this.purchaseService = purchaseService;
    }

    @GetMapping("/avatar")
    public GuestAvatarDTO getAvatar(@RequestParam String email) {
        return avatarService.getAvatar(email);
    }

    @PutMapping("/avatar")
    public GuestAvatarDTO saveAvatar(@Valid @RequestBody GuestAvatarDTO dto) {
        return avatarService.saveAvatar(dto);
    }

    @GetMapping("/purchases")
    public List<PurchaseDTO> getPurchases(@RequestParam String email) {
        return purchaseService.getGuestPurchases(email);
    }
}
