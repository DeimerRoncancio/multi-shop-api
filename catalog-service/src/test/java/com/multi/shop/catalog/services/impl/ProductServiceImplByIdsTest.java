package com.multi.shop.catalog.services.impl;

import com.multi.shop.catalog.entities.Product;
import com.multi.shop.catalog.mappers.ProductMapper;
import com.multi.shop.catalog.mappers.VariantMapper;
import com.multi.shop.catalog.media.MediaApi;
import com.multi.shop.catalog.repositories.ProductCategoryRepository;
import com.multi.shop.catalog.repositories.ProductRepository;
import com.multi.shop.catalog.services.ProductCategoryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplByIdsTest {
    @Mock
    private ProductRepository repository;
    @Mock
    private ProductCategoryRepository categoryRepository;
    @Mock
    private ProductCategoryService categoryService;
    @Mock
    private MediaApi mediaApi;
    @Mock
    private ProductMapper productMapper;
    @Mock
    private VariantMapper variantMapper;

    @InjectMocks
    private ProductServiceImpl service;

    @Test
    void returnsTheProductsThatExistAskingOnceWithoutRepeatedIds() {
        when(repository.findAllById(List.of("a", "b", "borrado"))).thenReturn(List.of(product("a"), product("b")));

        assertThat(service.findByIds(Arrays.asList("a", "b", "a", null, "borrado"))).hasSize(2);
        verify(mediaApi).findAll(anyList());
    }

    @Test
    void returnsNothingWithoutIds() {
        assertThat(service.findByIds(null)).isEmpty();
        assertThat(service.findByIds(List.of())).isEmpty();
        verify(repository, never()).findAllById(anyList());
    }

    @Test
    void asksForAtMostOneHundredIds() {
        List<String> ids = new ArrayList<>(IntStream.range(0, 150).mapToObj(String::valueOf).toList());
        when(repository.findAllById(ids.subList(0, 100))).thenReturn(List.of());

        assertThat(service.findByIds(ids)).isEmpty();
    }

    private Product product(String id) {
        Product product = new Product();
        product.setId(id);
        product.setImageIds(List.of());
        return product;
    }
}
