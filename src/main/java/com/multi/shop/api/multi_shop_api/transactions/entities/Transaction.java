package com.multi.shop.api.multi_shop_api.transactions.entities;

import com.multi.shop.api.multi_shop_api.transactions.enums.TransactionStatus;
import com.multi.shop.api.multi_shop_api.catalog.api.CatalogProduct;
import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

@Entity
@Table(name = "transactions")
public class Transaction {
    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private String id;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "transaction_date")
    private Instant transactionDate;

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

    public Transaction(String id, Instant transactionDate, Long totalPrice, TransactionStatus status) {
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(Instant transactionDate) {
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

    public void addItem(String productId, int quantity) {
        ProductItem item = new ProductItem();
        item.setProductId(productId);
        item.setTransaction(this);
        item.setQuantity(quantity);
        productItems.add(item);
    }

    public List<String> productIds() {
        return productItems.stream()
            .map(ProductItem::getProductId)
            .filter(Objects::nonNull)
            .distinct()
            .toList();
    }

    public Long calculateTotalPrice(Map<String, CatalogProduct> products) {
        return productItems.stream()
            .filter(item -> priceOf(item, products) != null)
            .mapToLong(item -> priceOf(item, products) * item.getQuantity())
            .sum();
    }

    public Stream<ProductItem> payableItems(Map<String, CatalogProduct> products) {
        return productItems.stream()
            .filter(item -> priceOf(item, products) != null)
            .filter(item -> item.getQuantity() > 0);
    }

    public long payableAmountInCents(Map<String, CatalogProduct> products) {
        return payableItems(products)
            .mapToLong(item -> priceOf(item, products) * 100 * item.getQuantity())
            .sum();
    }

    private static Long priceOf(ProductItem item, Map<String, CatalogProduct> products) {
        CatalogProduct product = item.getProductId() == null ? null : products.get(item.getProductId());
        return product == null ? null : product.price();
    }
}
