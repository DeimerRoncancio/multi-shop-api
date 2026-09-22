package com.multi.shop.api.multi_shop_api.users.services.impl;

import com.multi.shop.api.multi_shop_api.images.entities.Image;
import com.multi.shop.api.multi_shop_api.images.services.ImageService;
import com.multi.shop.api.multi_shop_api.users.repositories.RoleRepository;
import com.multi.shop.api.multi_shop_api.users.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplProfileImageTest {
    @Mock
    private UserRepository repository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private ImageService imageService;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl service;

    @Test
    void doesNotUploadAnythingWithoutAFile() {
        assertThatCode(() -> assertThat(service.uploadProfileImage(null)).isNull())
            .doesNotThrowAnyException();

        verifyNoInteractions(imageService);
    }

    @Test
    void doesNotUploadAnEmptyFile() {
        MultipartFile empty = new MockMultipartFile("file", "foto.png", "image/png", new byte[0]);

        assertThat(service.uploadProfileImage(empty)).isNull();

        verifyNoInteractions(imageService);
    }

    @Test
    void uploadsAFileThatHasContent() throws IOException {
        MultipartFile file = new MockMultipartFile("file", "foto.png", "image/png", new byte[] {1, 2, 3});
        Image image = new Image();
        when(imageService.uploadImage(file)).thenReturn(image);

        assertThat(service.uploadProfileImage(file)).isSameAs(image);
    }

    @Test
    void returnsNothingWhenTheUploadFails() throws IOException {
        MultipartFile file = new MockMultipartFile("file", "foto.png", "image/png", new byte[] {1, 2, 3});
        when(imageService.uploadImage(file)).thenThrow(new IOException("cloudinary caída"));

        assertThat(service.uploadProfileImage(file)).isNull();
        verify(imageService, never()).deleteImage(any());
    }
}
