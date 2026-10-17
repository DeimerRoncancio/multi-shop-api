package com.multi.shop.transactions.controllers;

import com.multi.shop.transactions.common.ControllerAdvice;
import com.multi.shop.transactions.dtos.GuestAvatarDTO;
import com.multi.shop.transactions.services.GuestAvatarService;
import com.multi.shop.transactions.services.PurchaseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GuestsControllerTest {
    private GuestAvatarService avatarService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        avatarService = mock(GuestAvatarService.class);
        mockMvc = MockMvcBuilders
            .standaloneSetup(new GuestsController(avatarService, mock(PurchaseService.class)))
            .setControllerAdvice(new ControllerAdvice())
            .build();
    }

    @Test
    void returnsTheSavedAvatar() throws Exception {
        when(avatarService.getAvatar("ana@correo.com")).thenReturn(new GuestAvatarDTO("ana@correo.com", 3));

        mockMvc.perform(get("/app/guests/avatar").param("email", "ana@correo.com"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("ana@correo.com"))
            .andExpect(jsonPath("$.avatar").value(3));
    }

    @Test
    void savesTheAvatar() throws Exception {
        when(avatarService.saveAvatar(any())).thenReturn(new GuestAvatarDTO("ana@correo.com", 5));

        mockMvc.perform(put("/app/guests/avatar")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"ana@correo.com\",\"avatar\":5}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.avatar").value(5));
    }

    @Test
    void rejectsAvatarsOutOfRange() throws Exception {
        mockMvc.perform(put("/app/guests/avatar")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"ana@correo.com\",\"avatar\":6}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.avatar").exists());

        verify(avatarService, never()).saveAvatar(any());
    }

    @Test
    void rejectsInvalidEmails() throws Exception {
        mockMvc.perform(put("/app/guests/avatar")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"no-es-correo\",\"avatar\":2}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.email").exists());
    }
}
