package com.multi.shop.catalog.services.impl;

import com.multi.shop.catalog.dtos.HomeDTO;
import com.multi.shop.catalog.entities.Product;
import com.multi.shop.catalog.mappers.ProductMapper;
import com.multi.shop.catalog.mappers.VariantMapper;
import com.multi.shop.catalog.media.MediaApi;
import com.multi.shop.catalog.repositories.CategoryStats;
import com.multi.shop.catalog.repositories.ProductCategoryRepository;
import com.multi.shop.catalog.repositories.ProductRepository;
import com.multi.shop.catalog.services.ProductCategoryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplHomeTest {
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
    void buildsTheHomeWithItsSectionsTopAndBudgetProducts() {
        when(categoryRepository.findStatsByProductCount())
            .thenReturn(List.of(stats("ropa-id", "Ropa", 20), stats("gamer-id", "Gamer", 2)));
        when(repository.findByCategoriesId("ropa-id", PageRequest.of(0, 12, Sort.by("productName"))))
            .thenReturn(List.of(product("a"), product("b"), product("c")));
        when(repository.findByCategoriesId("gamer-id", PageRequest.of(0, 12, Sort.by("productName"))))
            .thenReturn(List.of(product("d")));
        when(repository.findAll(PageRequest.of(0, 4, Sort.by("price").descending())))
            .thenReturn(new PageImpl<>(List.of(product("d"), product("a"))));
        when(repository.findAll(PageRequest.of(0, 12, Sort.by("price"))))
            .thenReturn(new PageImpl<>(List.of(product("c"))));

        HomeDTO home = service.home();

        assertThat(home.sections()).hasSize(2);
        assertThat(home.sections().get(0).categoryName()).isEqualTo("Ropa");
        assertThat(home.sections().get(0).productCount()).isEqualTo(20);
        assertThat(home.sections().get(0).products()).hasSize(3);
        assertThat(home.sections().get(1).categoryName()).isEqualTo("Gamer");
        assertThat(home.sections().get(1).products()).hasSize(1);
        assertThat(home.top()).hasSize(2);
        assertThat(home.budget()).hasSize(1);
    }

    private Product product(String id) {
        Product product = new Product();
        product.setId(id);
        product.setImageIds(List.of());
        return product;
    }

    private CategoryStats stats(String id, String name, long count) {
        return new CategoryStats() {
            @Override
            public String getId() {
                return id;
            }

            @Override
            public String getCategoryName() {
                return name;
            }

            @Override
            public long getProductCount() {
                return count;
            }

            @Override
            public Long getFromPrice() {
                return null;
            }
        };
    }
}
