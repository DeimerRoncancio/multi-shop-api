package com.multi.shop.catalog.services.impl;

import com.multi.shop.catalog.mappers.ProductMapper;
import com.multi.shop.catalog.mappers.VariantMapper;
import com.multi.shop.catalog.media.MediaApi;
import com.multi.shop.catalog.repositories.ProductCategoryRepository;
import com.multi.shop.catalog.repositories.ProductRepository;
import com.multi.shop.catalog.services.ProductCategoryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplSearchTest {
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
    void ordersByTheAllowedFieldsAndBreaksTiesById() {
        Pageable asked = PageRequest.of(2, 12, Sort.by(Sort.Order.desc("price"), Sort.Order.asc("productName")));

        assertThat(searchWith(asked)).isEqualTo(PageRequest.of(2, 12,
            Sort.by(Sort.Order.desc("price"), Sort.Order.asc("productName"), Sort.Order.asc("id"))));
    }

    @Test
    void ignoresFieldsThatCannotBeUsedToOrder() {
        Pageable asked = PageRequest.of(0, 10, Sort.by("categories", "foo", "createdAt"));

        assertThat(searchWith(asked).getSort()).isEqualTo(Sort.by("createdAt", "id"));
    }

    @Test
    void keepsTheDefaultOrderWhenNoneIsAsked() {
        assertThat(searchWith(PageRequest.of(0, 10)).getSort().isUnsorted()).isTrue();
        assertThat(searchWith(PageRequest.of(0, 10, Sort.by("foo"))).getSort().isUnsorted()).isTrue();
    }

    private Pageable searchWith(Pageable asked) {
        when(repository.findByProductNameOrCategories(any(), any(), any(), any(), any())).thenReturn(Page.empty());
        service.search(null, null, null, null, asked);

        ArgumentCaptor<Pageable> used = ArgumentCaptor.forClass(Pageable.class);
        verify(repository, atLeastOnce())
            .findByProductNameOrCategories(any(), any(), any(), any(), used.capture());
        return used.getValue();
    }
}
