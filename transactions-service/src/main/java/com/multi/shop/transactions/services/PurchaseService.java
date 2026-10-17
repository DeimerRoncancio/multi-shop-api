package com.multi.shop.transactions.services;

import com.multi.shop.transactions.dtos.PurchaseDTO;

import java.util.List;

public interface PurchaseService {
    List<PurchaseDTO> getUserPurchases(String userIdentity);
    List<PurchaseDTO> getGuestPurchases(String email);
}
