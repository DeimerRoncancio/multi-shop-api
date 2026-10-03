package com.multi.shop.identity.users.services.impl;

import com.multi.shop.identity.media.MediaApi;
import com.multi.shop.identity.media.StoredImage;
import com.multi.shop.identity.users.mappers.UserMapper;
import com.multi.shop.identity.users.mappers.UserMapperImpl;
import com.multi.shop.identity.users.repositories.RoleRepository;
import com.multi.shop.identity.users.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
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
    private MediaApi mediaApi;
    @Mock
    private PasswordEncoder passwordEncoder;

    @Spy
    private UserMapper userMapper = new UserMapperImpl();

    @InjectMocks
    private UserServiceImpl service;

    @Test
    void doesNotUploadAnythingWithoutAFile() {
        assertThatCode(() -> assertThat(service.uploadProfileImage(null)).isNull())
            .doesNotThrowAnyException();

        verifyNoInteractions(mediaApi);
    }

    @Test
    void doesNotUploadAnEmptyFile() {
        MultipartFile empty = new MockMultipartFile("file", "foto.png", "image/png", new byte[0]);

        assertThat(service.uploadProfileImage(empty)).isNull();

        verifyNoInteractions(mediaApi);
    }

    @Test
    void uploadsAFileThatHasContent() throws IOException {
        MultipartFile file = new MockMultipartFile("file", "foto.png", "image/png", new byte[] {1, 2, 3});
        StoredImage image = new StoredImage("id", "foto.png", "url", "imageId");
        when(mediaApi.upload(file)).thenReturn(image);

        assertThat(service.uploadProfileImage(file)).isSameAs(image);
    }

    @Test
    void returnsNothingWhenTheUploadFails() throws IOException {
        MultipartFile file = new MockMultipartFile("file", "foto.png", "image/png", new byte[] {1, 2, 3});
        when(mediaApi.upload(file)).thenThrow(new IOException("cloudinary caída"));

        assertThat(service.uploadProfileImage(file)).isNull();
        verify(mediaApi, never()).deleteAfterCommit(any());
    }
}
