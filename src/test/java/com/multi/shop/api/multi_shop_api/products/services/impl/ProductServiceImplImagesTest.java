package com.multi.shop.api.multi_shop_api.products.services.impl;

import com.multi.shop.api.multi_shop_api.images.entities.Image;
import com.multi.shop.api.multi_shop_api.images.services.ImageService;
import com.multi.shop.api.multi_shop_api.images.services.TransactionalImages;
import com.multi.shop.api.multi_shop_api.products.dtos.ProductDTO;
import com.multi.shop.api.multi_shop_api.products.entities.Product;
import com.multi.shop.api.multi_shop_api.products.mappers.ProductMapper;
import com.multi.shop.api.multi_shop_api.products.mappers.ProductMapperImpl;
import com.multi.shop.api.multi_shop_api.products.mappers.VariantMapper;
import com.multi.shop.api.multi_shop_api.products.mappers.VariantMapperImpl;
import com.multi.shop.api.multi_shop_api.products.repositories.ProductRepository;
import com.multi.shop.api.multi_shop_api.products.services.ProductCategoryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplImagesTest {
    @Mock
    private ProductRepository repository;
    @Mock
    private ProductCategoryService categoryService;
    @Mock
    private ImageService imageService;
    @Mock
    private TransactionalImages transactionalImages;

    @Spy
    private ProductMapper productMapper = new ProductMapperImpl();
    @Spy
    private VariantMapper variantMapper = new VariantMapperImpl();

    @InjectMocks
    private ProductServiceImpl service;

    @Test
    void ignoresFilesThatAreNullOrEmpty() throws IOException {
        Image uploaded = new Image();
        MultipartFile file = file("foto.png", new byte[] {1});
        when(imageService.uploadImage(file)).thenReturn(uploaded);

        List<Image> images = service.uploadImages(Arrays.asList(
            null, file("vacia.png", new byte[0]), file
        ));

        assertThat(images).containsExactly(uploaded);
        assertThat(images).doesNotContainNull();
    }

    @Test
    void returnsNoImagesWhenThereAreNoFiles() {
        assertThat(service.uploadImages(null)).isEmpty();
        assertThat(service.uploadImages(List.of())).isEmpty();
    }

    @Test
    void failsInsteadOfSavingANullImage() throws IOException {
        MultipartFile file = file("foto.png", new byte[] {1});
        when(imageService.uploadImage(file)).thenThrow(new IOException("cloudinary caída"));

        assertThatThrownBy(() -> service.uploadImages(List.of(file)))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("could not be uploaded");
    }

    @Test
    void doesNotSaveAProductWhenAnImageFails() throws IOException {
        MultipartFile file = file("foto.png", new byte[] {1});
        when(categoryService.findCategoriesByName(any())).thenReturn(List.of());
        when(imageService.uploadImage(file)).thenThrow(new IOException("cloudinary caída"));

        assertThatThrownBy(() -> service.save(productDTO(List.of(file))))
            .isInstanceOf(ResponseStatusException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void addsTheUploadedImagesToTheNewProduct() throws IOException {
        Image uploaded = new Image();
        MultipartFile file = file("foto.png", new byte[] {1});
        when(categoryService.findCategoriesByName(any())).thenReturn(List.of());
        when(imageService.uploadImage(file)).thenReturn(uploaded);

        service.save(productDTO(List.of(file)));

        verify(repository).save(org.mockito.ArgumentMatchers.argThat((Product product) ->
            product.getProductImages().size() == 1 && product.getProductImages().contains(uploaded)
        ));
    }

    @Test
    void keepsTheCurrentImagesWhenTheNewOneFails() throws IOException {
        Image current = new Image();
        current.setName("vieja.png");
        List<Image> currentImages = new ArrayList<>(List.of(current));
        MultipartFile file = file("nueva.png", new byte[] {1});
        when(imageService.uploadImage(file)).thenThrow(new IOException("cloudinary caída"));

        assertThatThrownBy(() -> service.updateImages(currentImages, List.of(file), null))
            .isInstanceOf(ResponseStatusException.class);

        assertThat(currentImages).containsExactly(current);
    }

    private static MultipartFile file(String name, byte[] content) {
        return new MockMultipartFile("images", name, "image/png", content);
    }

    private static ProductDTO productDTO(List<MultipartFile> images) {
        return new ProductDTO(
            "Cafe", "Un cafe", 25000L,
            List.of(), List.of(), List.of(), List.of(),
            images, List.of(), List.of("Bebidas")
        );
    }
}
