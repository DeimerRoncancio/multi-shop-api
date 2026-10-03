package com.multi.shop.catalog.services.impl;

import com.multi.shop.catalog.media.MediaApi;
import com.multi.shop.catalog.media.StoredImage;
import com.multi.shop.catalog.dtos.ProductDTO;
import com.multi.shop.catalog.entities.Product;
import com.multi.shop.catalog.mappers.ProductMapper;
import com.multi.shop.catalog.mappers.ProductMapperImpl;
import com.multi.shop.catalog.mappers.VariantMapper;
import com.multi.shop.catalog.mappers.VariantMapperImpl;
import com.multi.shop.catalog.repositories.ProductRepository;
import com.multi.shop.catalog.services.ProductCategoryService;
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
    private MediaApi mediaApi;

    @Spy
    private ProductMapper productMapper = new ProductMapperImpl();
    @Spy
    private VariantMapper variantMapper = new VariantMapperImpl();

    @InjectMocks
    private ProductServiceImpl service;

    @Test
    void ignoresFilesThatAreNullOrEmpty() throws IOException {
        StoredImage uploaded = image("foto.png");
        MultipartFile file = file("foto.png", new byte[] {1});
        when(mediaApi.upload(file)).thenReturn(uploaded);

        List<StoredImage> images = service.uploadImages(Arrays.asList(
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
        when(mediaApi.upload(file)).thenThrow(new IOException("cloudinary caída"));

        assertThatThrownBy(() -> service.uploadImages(List.of(file)))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("could not be uploaded");
    }

    @Test
    void doesNotSaveAProductWhenAnImageFails() throws IOException {
        MultipartFile file = file("foto.png", new byte[] {1});
        when(categoryService.findCategoriesByName(any())).thenReturn(List.of());
        when(mediaApi.upload(file)).thenThrow(new IOException("cloudinary caída"));

        assertThatThrownBy(() -> service.save(productDTO(List.of(file))))
            .isInstanceOf(ResponseStatusException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void addsTheUploadedImagesToTheNewProduct() throws IOException {
        StoredImage uploaded = image("foto.png");
        MultipartFile file = file("foto.png", new byte[] {1});
        when(categoryService.findCategoriesByName(any())).thenReturn(List.of());
        when(mediaApi.upload(file)).thenReturn(uploaded);

        service.save(productDTO(List.of(file)));

        verify(repository).save(org.mockito.ArgumentMatchers.argThat((Product product) ->
            product.getImageIds().equals(List.of(uploaded.id()))
        ));
    }

    @Test
    void keepsTheCurrentImagesWhenTheNewOneFails() throws IOException {
        StoredImage current = image("vieja.png");
        List<StoredImage> currentImages = new ArrayList<>(List.of(current));
        MultipartFile file = file("nueva.png", new byte[] {1});
        when(mediaApi.upload(file)).thenThrow(new IOException("cloudinary caída"));

        assertThatThrownBy(() -> service.updateImages(currentImages, List.of(file), null))
            .isInstanceOf(ResponseStatusException.class);

        assertThat(currentImages).containsExactly(current);
    }

    @Test
    void renombraLasImagenesConElNombreDelProductoYLasNumeraDesdeUno() {
        List<StoredImage> renamed = service.renameImages("Nevecón Moderno", List.of(
            image("IMG_2031.jpg"),
            image("captura.PNG")));

        assertThat(renamed)
            .extracting(StoredImage::name)
            .containsExactly("nevecon-moderno-1.jpg", "nevecon-moderno-2.png");
        verify(mediaApi).rename("id-IMG_2031.jpg", "nevecon-moderno-1.jpg");
        verify(mediaApi).rename("id-captura.PNG", "nevecon-moderno-2.png");
    }

    @Test
    void renumeraSinDejarHuecosCuandoSeBorraUnaImagen() {
        List<StoredImage> renamed = service.renameImages("Arrocera", List.of(
            image("arrocera-3.webp"),
            image("arrocera-1.webp")));

        assertThat(renamed)
            .extracting(StoredImage::name)
            .containsExactly("arrocera-1.webp", "arrocera-2.webp");
    }

    @Test
    void lasImagenesNuevasQuedanDespuesDeLasQueYaEstaban() {
        List<StoredImage> renamed = service.renameImages("Arrocera", List.of(
            image("arrocera-1.webp"),
            image("arrocera-2.webp"),
            image("recien-subida.jpg")));

        assertThat(renamed)
            .extracting(StoredImage::name)
            .containsExactly("arrocera-1.webp", "arrocera-2.webp", "arrocera-3.jpg");
    }

    private static StoredImage image(String name) {
        return new StoredImage("id-" + name, name, "url", "imageId");
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
