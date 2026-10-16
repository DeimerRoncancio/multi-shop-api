package com.multi.shop.transactions.services.impl;

import com.multi.shop.transactions.dtos.GuestAvatarDTO;
import com.multi.shop.transactions.entities.GuestAvatar;
import com.multi.shop.transactions.repositories.GuestAvatarRepository;
import com.multi.shop.transactions.services.GuestAvatarService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

@Service
public class GuestAvatarServiceImpl implements GuestAvatarService {
    private final GuestAvatarRepository repository;

    public GuestAvatarServiceImpl(GuestAvatarRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public GuestAvatarDTO getAvatar(String email) {
        String key = normalize(email);

        return repository.findById(key)
            .map(saved -> new GuestAvatarDTO(key, saved.getAvatar()))
            .orElse(new GuestAvatarDTO(key, null));
    }

    @Override
    @Transactional
    public GuestAvatarDTO saveAvatar(GuestAvatarDTO dto) {
        String key = normalize(dto.email());
        GuestAvatar avatar = repository.findById(key).orElse(new GuestAvatar(key, dto.avatar()));
        avatar.setAvatar(dto.avatar());

        return new GuestAvatarDTO(key, repository.save(avatar).getAvatar());
    }

    private String normalize(String email) {
        if (email == null || email.isBlank()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El correo es obligatorio");
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
