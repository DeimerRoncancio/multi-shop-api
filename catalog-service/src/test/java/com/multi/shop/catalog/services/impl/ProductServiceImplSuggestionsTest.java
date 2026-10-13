package com.multi.shop.catalog.services.impl;

import com.multi.shop.catalog.dtos.ProductSuggestionsDTO;
import com.multi.shop.catalog.entities.Product;
import com.multi.shop.catalog.mappers.ProductMapper;
import com.multi.shop.catalog.mappers.VariantMapper;
import com.multi.shop.catalog.media.MediaApi;
import com.multi.shop.catalog.repositories.ProductRepository;
import com.multi.shop.catalog.services.ProductCategoryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplSuggestionsTest {
    private static final Sort SORT = Sort.by("productName");

    @Mock
    private ProductRepository repository;
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
    void returnsOnlyTheCategoryProductsWhenThereAreEnough() {
        when(repository.findByCategoriesExcluding(List.of("Gamer"), List.of("actual"), PageRequest.of(0, 3, SORT)))
            .thenReturn(List.of(product("a"), product("b")));

        ProductSuggestionsDTO suggestions = service.suggestions(List.of("Gamer"), List.of("actual"), 3, 2, SORT);

        assertThat(suggestions.filled()).isFalse();
        assertThat(suggestions.products()).hasSize(2);
        verify(repository, never()).findExcluding(anyList(), any());
    }

    @Test
    void fillsWithOtherProductsWithoutRepeatingAny() {
        when(repository.findByCategoriesExcluding(List.of("Gamer"), List.of("actual"), PageRequest.of(0, 3, SORT)))
            .thenReturn(List.of(product("a")));
        when(repository.findExcluding(List.of("actual", "a"), PageRequest.of(0, 2, SORT)))
            .thenReturn(List.of(product("b"), product("c")));

        ProductSuggestionsDTO suggestions = service.suggestions(List.of("Gamer"), List.of("actual"), 3, null, SORT);

        assertThat(suggestions.filled()).isTrue();
        assertThat(suggestions.products()).hasSize(3);
    }

    @Test
    void suggestsFromTheWholeCatalogWithoutCategories() {
        when(repository.findExcluding(List.of(), PageRequest.of(0, 3, SORT)))
            .thenReturn(List.of(product("a"), product("b"), product("c")));

        ProductSuggestionsDTO suggestions = service.suggestions(List.of(), null, 3, null, SORT);

        assertThat(suggestions.filled()).isTrue();
        assertThat(suggestions.products()).hasSize(3);
        verify(repository, never()).findByCategoriesExcluding(anyList(), anyList(), any());
    }

    private Product product(String id) {
        Product product = new Product();
        product.setId(id);
        product.setImageIds(List.of());
        return product;
    }
}
