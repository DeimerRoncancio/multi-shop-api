package com.multi.shop.api.multi_shop_api.payments.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "transactions")
public class Transaction {
    @Id
    @UuidGenerator
    @JoinColumn(name = "id", updatable = false, nullable = false)
    private String id;
    private Date transactionDate;
    private Long totalPrice;
    private String status;

    @Column(name = "checkout_access_token_digest", length = 43, updatable = false)
    private String checkoutAccessTokenDigest;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @OneToOne(mappedBy = "transaction", cascade = CascadeType.ALL, orphanRemoval = true)
    private ShippingAddress shippingAddress;

    @OneToMany(mappedBy = "transaction", cascade = CascadeType.ALL, orphanRemoval = true)
    List<ProductItem> productItems;

    public Transaction() {
        this.productItems = new ArrayList<>();
    }

    public Transaction(String id, Date transactionDate, Long totalPrice, String status) {
        this.id = id;
        this.transactionDate = transactionDate;
        this.totalPrice = totalPrice;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(Date transactionDate) {
        this.transactionDate = transactionDate;
    }

    public Long getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Long totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCheckoutAccessTokenDigest() {
        return checkoutAccessTokenDigest;
    }

    public void setCheckoutAccessTokenDigest(String checkoutAccessTokenDigest) {
        this.checkoutAccessTokenDigest = checkoutAccessTokenDigest;
    }

    public List<ProductItem> getProductItems() {
        return productItems;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public ShippingAddress getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(ShippingAddress shippingAddress) {
        if (this.shippingAddress != null && this.shippingAddress != shippingAddress)
            this.shippingAddress.setTransaction(null);

        this.shippingAddress = shippingAddress;

        if (shippingAddress != null && shippingAddress.getTransaction() != this)
            shippingAddress.setTransaction(this);
    }

    public void setProductItems(List<ProductItem> productItems) {
        this.productItems = productItems;
    }
}
