package com.multi.shop.api.multi_shop_api.payments.entities;

import com.multi.shop.api.multi_shop_api.payments.enums.TransactionStatus;
import com.multi.shop.api.multi_shop_api.products.entities.Product;
import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

@Entity
@Table(name = "transactions")
public class Transaction {
    @Id
    @UuidGenerator
    @JoinColumn(name = "id", updatable = false, nullable = false)
    private String id;
    private Date transactionDate;
    private Long totalPrice;
    @Enumerated(EnumType.STRING)
    private TransactionStatus status;

    @Column(name = "checkout_access_token_digest", length = 43, updatable = false)
    private String checkoutAccessTokenDigest;

    @Column(name = "stripe_session_id")
    private String stripeSessionId;

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

    public Transaction(String id, Date transactionDate, Long totalPrice, TransactionStatus status) {
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

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }

    public String getCheckoutAccessTokenDigest() {
        return checkoutAccessTokenDigest;
    }

    public void setCheckoutAccessTokenDigest(String checkoutAccessTokenDigest) {
        this.checkoutAccessTokenDigest = checkoutAccessTokenDigest;
    }

    public String getStripeSessionId() {
        return stripeSessionId;
    }

    public void setStripeSessionId(String stripeSessionId) {
        this.stripeSessionId = stripeSessionId;
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

    public void addItem(Product product, int quantity) {
        ProductItem item = new ProductItem();
        item.setProduct(product);
        item.setTransaction(this);
        item.setQuantity(quantity);
        productItems.add(item);
    }

    public Long calculateTotalPrice() {
        return productItems.stream()
            .filter(item -> item.getProduct() != null && item.getProduct().getPrice() != null)
            .mapToLong(item -> item.getProduct().getPrice() * item.getQuantity())
            .sum();
    }

    public Stream<ProductItem> payableItems() {
        return productItems.stream()
            .filter(item -> item.getProduct() != null && item.getProduct().getPrice() != null)
            .filter(item -> item.getQuantity() > 0);
    }

    public long payableAmountInCents() {
        return payableItems()
            .mapToLong(item -> item.getProduct().getPrice() * 100 * item.getQuantity())
            .sum();
    }
}
