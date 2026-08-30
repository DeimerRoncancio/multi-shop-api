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
    private String reference;
    private Date transactionDate;
    private String totalPrice;
    private String status;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "customer_id")
    private Customer customer;

//    @ManyToOne
//    @JoinColumn(name = "id_user")
//    User user;
//
//    @ManyToOne(cascade = CascadeType.ALL)
//    @JoinColumn(name = "id_user_reference")
//    UserTransaction userReference;

    @OneToMany(mappedBy = "transaction", cascade = CascadeType.ALL, orphanRemoval = true)
    List<ProductItem> productItems;

    public Transaction() {
        this.productItems = new ArrayList<>();
    }

    public Transaction(String id, String reference, Date transactionDate, String totalPrice, String status) {
        this.id = id;
        this.reference = reference;
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

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public Date getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(Date transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(String totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    //    public User getUser() {
//        return user;
//    }
//
//    public void setUser(User user) {
//        this.user = user;
//    }
//
//    public UserTransaction getUserReference() {
//        return userReference;
//    }
//
//    public void setUserReference(UserTransaction userReference) {
//        this.userReference = userReference;
//    }

    public void setProductItems(List<ProductItem> productItems) {
        this.productItems = productItems;
    }
}
