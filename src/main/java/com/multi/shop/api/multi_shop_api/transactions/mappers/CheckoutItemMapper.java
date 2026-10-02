package com.multi.shop.api.multi_shop_api.transactions.mappers;

import com.multi.shop.api.multi_shop_api.catalog.api.CatalogApi;
import com.multi.shop.api.multi_shop_api.catalog.api.CatalogProduct;
import com.multi.shop.api.multi_shop_api.transactions.dtos.CheckoutProductItemDTO;
import com.multi.shop.api.multi_shop_api.transactions.entities.ProductItem;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class CheckoutItemMapper {
    private final CatalogApi catalogApi;

    public CheckoutItemMapper(CatalogApi catalogApi) {
        this.catalogApi = catalogApi;
    }

    @Named("checkoutItems")
    public List<CheckoutProductItemDTO> toCheckoutProductItemDTOs(List<ProductItem> items) {
        if (items == null) return null;

        Map<String, CatalogProduct> products = catalogApi.findProducts(
            items.stream().map(ProductItem::getProductId).filter(Objects::nonNull).toList());

        return items.stream()
            .map(item -> toCheckoutProductItemDTO(item, products.get(item.getProductId())))
            .toList();
    }

    private static CheckoutProductItemDTO toCheckoutProductItemDTO(ProductItem item, CatalogProduct product) {
        return product == null
            ? new CheckoutProductItemDTO(null, null, null, item.getQuantity())
            : new CheckoutProductItemDTO(product.id(), product.productName(), product.price(), item.getQuantity());
    }
}
