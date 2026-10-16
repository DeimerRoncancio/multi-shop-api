package com.multi.shop.transactions.services;

import com.multi.shop.transactions.dtos.GuestAvatarDTO;

public interface GuestAvatarService {
    GuestAvatarDTO getAvatar(String email);
    GuestAvatarDTO saveAvatar(GuestAvatarDTO dto);
}
