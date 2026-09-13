package com.multi.shop.api.multi_shop_api.payments.mappers;

import com.multi.shop.api.multi_shop_api.payments.dtos.CheckoutProductItemDTO;
import com.multi.shop.api.multi_shop_api.payments.dtos.CheckoutSummaryDTO;
import com.multi.shop.api.multi_shop_api.payments.dtos.CustomerAddressDTO;
import com.multi.shop.api.multi_shop_api.payments.dtos.CustomerSummaryDTO;
import com.multi.shop.api.multi_shop_api.payments.entities.Customer;
import com.multi.shop.api.multi_shop_api.payments.entities.ProductItem;
import com.multi.shop.api.multi_shop_api.payments.entities.Transaction;
import com.multi.shop.api.multi_shop_api.products.entities.Product;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface CheckoutMapper {
    CheckoutMapper MAPPER = Mappers.getMapper(CheckoutMapper.class);

    default CheckoutSummaryDTO toCheckoutSummaryDTO(Transaction transaction) {
        Customer customer = transaction.getCustomer();
        CustomerSummaryDTO customerSummary = customer == null
            ? null
            : CustomerMapper.MAPPER.toCustomerSummaryDTO(customer);
        List<CustomerAddressDTO> addresses = customer == null
            ? List.of()
            : CustomerMapper.MAPPER.toCustomerAddressDTOs(customer);
        List<CheckoutProductItemDTO> items = transaction.getProductItems().stream()
            .map(this::toCheckoutProductItemDTO)
            .toList();

        return new CheckoutSummaryDTO(
            transaction.getId(),
            transaction.getStatus(),
            transaction.getTotalPrice(),
            customerSummary,
            addresses,
            TransactionMapper.MAPPER.toCustomerAddressDTO(transaction.getShippingAddress()),
            items
        );
    }

    default CheckoutProductItemDTO toCheckoutProductItemDTO(ProductItem item) {
        Product product = item.getProduct();

        return new CheckoutProductItemDTO(
            product == null ? null : product.getId(),
            product == null ? null : product.getProductName(),
            product == null ? null : product.getPrice(),
            item.getQuantity()
        );
    }
}
