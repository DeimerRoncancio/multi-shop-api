package com.multi.shop.transactions.repositories;

import com.multi.shop.transactions.entities.GuestAvatar;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GuestAvatarRepository extends JpaRepository<GuestAvatar, String> {}
