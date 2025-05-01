package com.multi.shop.api.multi_shop_api.mappers;

import com.multi.shop.api.multi_shop_api.entities.Image;
import com.multi.shop.api.multi_shop_api.entities.Product;
import com.multi.shop.api.multi_shop_api.entities.ProductCategory;
import com.multi.shop.api.multi_shop_api.entities.dtos.NewProductDTO;
import com.multi.shop.api.multi_shop_api.entities.dtos.UpdateProductDTO;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-05-01T18:48:30-0500",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.12 (Oracle Corporation)"
)
public class ProductMapperImpl implements ProductMapper {

    @Override
    public Product productCreateDTOtoProduct(NewProductDTO dto) {
        if ( dto == null ) {
            return null;
        }

        Product product = new Product();

        product.setProductName( dto.getProductName() );
        product.setDescription( dto.getDescription() );
        product.setPrice( dto.getPrice() );
        List<ProductCategory> list = dto.getCategories();
        if ( list != null ) {
            product.setCategories( new ArrayList<ProductCategory>( list ) );
        }
        List<Image> list1 = dto.getProductImages();
        if ( list1 != null ) {
            product.setProductImages( new ArrayList<Image>( list1 ) );
        }

        return product;
    }

    @Override
    public void toUpdateProduct(UpdateProductDTO dto, Product product) {
        if ( dto == null ) {
            return;
        }

        product.setProductName( dto.getProductName() );
        product.setDescription( dto.getDescription() );
        product.setPrice( dto.getPrice() );
    }
}
