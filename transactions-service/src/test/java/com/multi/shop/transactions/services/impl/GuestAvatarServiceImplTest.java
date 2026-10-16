package com.multi.shop.transactions.services.impl;

import com.multi.shop.transactions.dtos.GuestAvatarDTO;
import com.multi.shop.transactions.entities.GuestAvatar;
import com.multi.shop.transactions.repositories.GuestAvatarRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GuestAvatarServiceImplTest {
    @Mock
    private GuestAvatarRepository repository;

    @InjectMocks
    private GuestAvatarServiceImpl service;

    @Test
    void returnsNoAvatarWhenTheGuestHasNone() {
        when(repository.findById("ana@correo.com")).thenReturn(Optional.empty());

        assertThat(service.getAvatar(" Ana@Correo.com ")).isEqualTo(new GuestAvatarDTO("ana@correo.com", null));
    }

    @Test
    void returnsTheSavedAvatar() {
        when(repository.findById("ana@correo.com")).thenReturn(Optional.of(new GuestAvatar("ana@correo.com", 4)));

        assertThat(service.getAvatar("ana@correo.com").avatar()).isEqualTo(4);
    }

    @Test
    void savesWithTheEmailInLowercase() {
        when(repository.findById("ana@correo.com")).thenReturn(Optional.empty());
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        GuestAvatarDTO saved = service.saveAvatar(new GuestAvatarDTO("ANA@correo.com", 2));

        ArgumentCaptor<GuestAvatar> captor = ArgumentCaptor.forClass(GuestAvatar.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getEmail()).isEqualTo("ana@correo.com");
        assertThat(saved).isEqualTo(new GuestAvatarDTO("ana@correo.com", 2));
    }

    @Test
    void updatesAnExistingAvatar() {
        GuestAvatar existing = new GuestAvatar("ana@correo.com", 1);
        when(repository.findById("ana@correo.com")).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);

        assertThat(service.saveAvatar(new GuestAvatarDTO("ana@correo.com", 5)).avatar()).isEqualTo(5);
        assertThat(existing.getAvatar()).isEqualTo(5);
    }

    @Test
    void rejectsABlankEmail() {
        assertThatThrownBy(() -> service.getAvatar(" ")).isInstanceOf(ResponseStatusException.class);
    }
}
