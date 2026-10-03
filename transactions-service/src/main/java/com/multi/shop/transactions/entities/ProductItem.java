package com.multi.shop.transactions.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "product_items")
public class ProductItem {
    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    String id;

    @Column(name = "product_id")
    private String productId;

    @Column(name = "product_name")
    private String productName;

    @Column(name = "unit_price")
    private Long unitPrice;

    @ManyToOne
    @JoinColumn(name = "transaction_id")
    private Transaction transaction;

    private int quantity;

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Long getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(Long unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
