package com.multi.shop.api.multi_shop_api.orders.mappers;

import com.multi.shop.api.multi_shop_api.orders.entities.Order;
import com.multi.shop.api.multi_shop_api.orders.entities.dtos.NewOrderDTO;
import com.multi.shop.api.multi_shop_api.orders.entities.dtos.UpdateOrderDTO;
import com.multi.shop.api.multi_shop_api.products.entities.Product;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-05-01T22:58:34-0500",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.12 (Oracle Corporation)"
)
public class OrderMapperImpl implements OrderMapper {

    @Override
    public Order orderCreateDTOtoOrder(NewOrderDTO dto) {
        if ( dto == null ) {
            return null;
        }

        Order order = new Order();

        order.setOrderName( dto.getOrderName() );
        order.setNotes( dto.getNotes() );
        order.setOrderDate( dto.getOrderDate() );
        order.setUser( dto.getUser() );

        return order;
    }

    @Override
    public void toUpdateOrder(UpdateOrderDTO dto, Order order) {
        if ( dto == null ) {
            return;
        }

        order.setOrderName( dto.getOrderName() );
        order.setNotes( dto.getNotes() );
        order.setOrderDate( dto.getOrderDate() );
        order.setUser( dto.getUser() );
        if ( order.getProduct() != null ) {
            List<Product> list = dto.getProduct();
            if ( list != null ) {
                order.getProduct().clear();
                order.getProduct().addAll( list );
            }
            else {
                order.setProduct( null );
            }
        }
        else {
            List<Product> list = dto.getProduct();
            if ( list != null ) {
                order.setProduct( new ArrayList<Product>( list ) );
            }
        }
    }
}
