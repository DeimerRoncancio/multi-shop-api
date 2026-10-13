package com.multi.shop.catalog.services.impl;

import com.multi.shop.catalog.dtos.CategorySummaryDTO;
import com.multi.shop.catalog.entities.Product;
import com.multi.shop.catalog.mappers.ProductCategoryMapper;
import com.multi.shop.catalog.media.MediaApi;
import com.multi.shop.catalog.media.StoredImage;
import com.multi.shop.catalog.repositories.CategoryStats;
import com.multi.shop.catalog.repositories.ProductCategoryRepository;
import com.multi.shop.catalog.repositories.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductCategoryServiceImplTest {
    @Mock
    private ProductCategoryRepository repository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private ProductCategoryMapper categoryMapper;
    @Mock
    private MediaApi mediaApi;

    @InjectMocks
    private ProductCategoryServiceImpl service;

    @Test
    void summarizesEachCategoryWithItsCountLowestPriceAndPreviewProducts() {
        when(repository.findStats()).thenReturn(List.of(stats("gamer-id", "Gamer", 7, 170_000L)));
        when(productRepository.findTop4ByCategoriesIdOrderByIdAsc("gamer-id"))
            .thenReturn(List.of(product("p1", "Dualsense", 250_000L, List.of("img-2", "img-1"))));
        when(mediaApi.findAll(anyCollection())).thenReturn(List.of(
            new StoredImage("img-2", "dualsense-2.webp", "url-2", "cloud-2"),
            new StoredImage("img-1", "dualsense-1.webp", "url-1", "cloud-1")));

        List<CategorySummaryDTO> summaries = service.findSummaries();

        assertThat(summaries).hasSize(1);
        CategorySummaryDTO gamer = summaries.get(0);
        assertThat(gamer.categoryName()).isEqualTo("Gamer");
        assertThat(gamer.productCount()).isEqualTo(7);
        assertThat(gamer.fromPrice()).isEqualTo(170_000L);
        assertThat(gamer.products()).hasSize(1);
        assertThat(gamer.products().get(0).productName()).isEqualTo("Dualsense");
        assertThat(gamer.products().get(0).mainImage().imageUrl()).isEqualTo("url-1");
    }

    @Test
    void summarizesACategoryWithoutProducts() {
        when(repository.findStats()).thenReturn(List.of(stats("empty-id", "Vacia", 0, null)));
        when(productRepository.findTop4ByCategoriesIdOrderByIdAsc("empty-id")).thenReturn(List.of());
        when(mediaApi.findAll(anyCollection())).thenReturn(List.of());

        CategorySummaryDTO empty = service.findSummaries().get(0);

        assertThat(empty.productCount()).isZero();
        assertThat(empty.fromPrice()).isNull();
        assertThat(empty.products()).isEmpty();
    }

    private Product product(String id, String name, Long price, List<String> imageIds) {
        Product product = new Product();
        product.setId(id);
        product.setProductName(name);
        product.setPrice(price);
        product.setImageIds(imageIds);
        return product;
    }

    private CategoryStats stats(String id, String name, long count, Long fromPrice) {
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
                return fromPrice;
            }
        };
    }
}
