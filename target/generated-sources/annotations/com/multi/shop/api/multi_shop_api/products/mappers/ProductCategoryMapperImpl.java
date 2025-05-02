package com.multi.shop.api.multi_shop_api.products.mappers;

import com.multi.shop.api.multi_shop_api.products.entities.ProductCategory;
import com.multi.shop.api.multi_shop_api.products.entities.dtos.NewProductCategoryDTO;
import com.multi.shop.api.multi_shop_api.products.entities.dtos.UpdateProductCategoryDTO;
import javax.annotation.processing.Generated;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-05-01T22:58:34-0500",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.12 (Oracle Corporation)"
)
public class ProductCategoryMapperImpl implements ProductCategoryMapper {

    @Override
    public ProductCategory categoryCreateDTOtoCategory(NewProductCategoryDTO dto) {
        if ( dto == null ) {
            return null;
        }

        ProductCategory productCategory = new ProductCategory();

        productCategory.setCategoryName( dto.getCategoryName() );

        return productCategory;
    }

    @Override
    public void toUpdateCategory(UpdateProductCategoryDTO dto, ProductCategory category) {
        if ( dto == null ) {
            return;
        }

        category.setCategoryName( dto.getCategoryName() );
    }
}
