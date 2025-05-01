package com.multi.shop.api.multi_shop_api.mappers;

import com.multi.shop.api.multi_shop_api.entities.ProductCategory;
import com.multi.shop.api.multi_shop_api.entities.dtos.NewProductCategoryDTO;
import com.multi.shop.api.multi_shop_api.entities.dtos.UpdateProductCategoryDTO;
import javax.annotation.processing.Generated;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-05-01T18:48:30-0500",
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
