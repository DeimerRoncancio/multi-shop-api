package com.multi.shop.transactions;

import com.multi.shop.transactions.entities.Customer;
import com.multi.shop.transactions.entities.Guest;
import com.multi.shop.transactions.entities.ProductItem;
import com.multi.shop.transactions.entities.ShippingAddress;
import com.multi.shop.transactions.entities.Transaction;
import com.multi.shop.transactions.enums.TransactionStatus;
import com.multi.shop.transactions.repositories.CustomersRepository;
import com.multi.shop.transactions.repositories.PaymentsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PurchasesIntegrationTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private PaymentsRepository payments;

    @Autowired
    private CustomersRepository customers;

    @BeforeEach
    void cleanDatabase() {
        payments.deleteAll();
        customers.deleteAll();
    }

    @Test
    void usersSeeTheirPurchasesNewestFirstWithoutPendingOnes() throws Exception {
        IDENTITY.addAccount("Ana", "ana@correo.com", 3001234567L);
        Customer ana = userCustomer("ana@correo.com");
        save(ana, TransactionStatus.APPROVED, "2026-10-01T10:00:00Z", "Mando", 2, 250000L);
        save(ana, TransactionStatus.REJECTED, "2026-10-05T10:00:00Z", "Portatil", 1, 3500000L);
        save(ana, TransactionStatus.PENDING, "2026-10-06T10:00:00Z", "Nevera", 1, 900000L);
        save(userCustomer("otro@correo.com"), TransactionStatus.APPROVED, "2026-10-07T10:00:00Z", "Zapato", 1, 100000L);

        mvc.perform(get("/app/payments/purchases")
                .header("Authorization", "Bearer " + FakeIdentityService.userToken("ana@correo.com")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].status").value("REJECTED"))
            .andExpect(jsonPath("$[1].status").value("APPROVED"))
            .andExpect(jsonPath("$[1].totalPrice").value(500000))
            .andExpect(jsonPath("$[1].items[0].productName").value("Mando"))
            .andExpect(jsonPath("$[1].items[0].quantity").value(2))
            .andExpect(jsonPath("$[1].items[0].price").value(250000))
            .andExpect(jsonPath("$[1].shippingAddress.city").value("Bogota"))
            .andExpect(jsonPath("$[1].date").value("2026-10-01T10:05:00Z"));
    }

    @Test
    void userPurchasesNeedAToken() throws Exception {
        mvc.perform(get("/app/payments/purchases")).andExpect(status().isForbidden());
    }

    @Test
    void guestsSeeThePurchasesOfTheirEmail() throws Exception {
        Customer guest = guestCustomer("ana@correo.com");
        save(guest, TransactionStatus.APPROVED, "2026-10-01T10:00:00Z", "Mando", 1, 250000L);
        save(guestCustomer("otro@correo.com"), TransactionStatus.APPROVED, "2026-10-02T10:00:00Z", "Zapato", 1, 100000L);

        mvc.perform(get("/app/guests/purchases").param("email", " Ana@Correo.com "))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].items[0].productName").value("Mando"));
    }

    @Test
    void guestPurchasesNeedAnEmail() throws Exception {
        mvc.perform(get("/app/guests/purchases").param("email", " ")).andExpect(status().isBadRequest());
    }

    private Customer userCustomer(String email) {
        Customer customer = new Customer();
        customer.setUserEmail(email);
        return customers.save(customer);
    }

    private Customer guestCustomer(String email) {
        Customer customer = new Customer();
        customer.setGuest(new Guest(null, "Ana Perez", email, "3001234567"));
        return customers.save(customer);
    }

    private void save(Customer customer, TransactionStatus status, String createdAt, String product, int quantity, Long price) {
        Transaction transaction = new Transaction();
        transaction.setCustomer(customer);
        transaction.setStatus(status);
        transaction.setCreatedAt(Instant.parse(createdAt));
        if (status != TransactionStatus.PENDING) transaction.setTransactionDate(Instant.parse(createdAt).plusSeconds(300));

        ProductItem item = new ProductItem();
        item.setProductId("product-" + product);
        item.setProductName(product);
        item.setUnitPrice(price);
        item.setQuantity(quantity);
        item.setTransaction(transaction);
        transaction.getProductItems().add(item);
        transaction.setTotalPrice(transaction.calculateTotalPrice());

        ShippingAddress address = new ShippingAddress();
        address.setAddressName("Casa");
        address.setAddress("Calle 1");
        address.setCity("Bogota");
        address.setState("Cundinamarca");
        address.setCountry("Colombia");
        transaction.setShippingAddress(address);

        payments.save(transaction);
    }
}
