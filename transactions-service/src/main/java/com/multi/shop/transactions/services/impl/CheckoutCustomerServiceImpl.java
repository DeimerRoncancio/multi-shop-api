package com.multi.shop.transactions.services.impl;

import com.multi.shop.transactions.dtos.CustomerAddressDTO;
import com.multi.shop.transactions.dtos.UserTransactionDTO;
import com.multi.shop.transactions.entities.Address;
import com.multi.shop.transactions.entities.Customer;
import com.multi.shop.transactions.entities.Guest;
import com.multi.shop.transactions.entities.ShippingAddress;
import com.multi.shop.transactions.entities.Transaction;
import com.multi.shop.transactions.enums.TransactionStatus;
import com.multi.shop.transactions.mappers.CustomerMapper;
import com.multi.shop.transactions.mappers.TransactionMapper;
import com.multi.shop.transactions.repositories.AddressRepository;
import com.multi.shop.transactions.repositories.CustomersRepository;
import com.multi.shop.transactions.repositories.PaymentsRepository;
import com.multi.shop.transactions.security.CheckoutAccessToken;
import com.multi.shop.transactions.services.CheckoutCustomerService;
import com.multi.shop.transactions.identity.Account;
import com.multi.shop.transactions.identity.IdentityApi;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class CheckoutCustomerServiceImpl implements CheckoutCustomerService {
    private final PaymentsRepository repository;
    private final CustomersRepository customersRepository;
    private final AddressRepository addressRepository;
    private final IdentityApi identityApi;
    private final CheckoutAccessToken checkoutAccessToken;
    private final TransactionMapper transactionMapper;
    private final CustomerMapper customerMapper;

    public CheckoutCustomerServiceImpl(PaymentsRepository repository, CustomersRepository customersRepository, AddressRepository addressRepository, IdentityApi identityApi, CheckoutAccessToken checkoutAccessToken, TransactionMapper transactionMapper, CustomerMapper customerMapper) {
        this.repository = repository;
        this.customersRepository = customersRepository;
        this.addressRepository = addressRepository;
        this.identityApi = identityApi;
        this.checkoutAccessToken = checkoutAccessToken;
        this.transactionMapper = transactionMapper;
        this.customerMapper = customerMapper;
    }

    @Override
    @Transactional
    public Optional<Transaction> addUserToTransaction(UserTransactionDTO dto, String transactionId, String accessToken, String userIdentity) {
        return repository.findById(transactionId)
            .filter(transaction -> checkoutAccessToken.grants(transaction, accessToken))
            .map(transaction -> {
                if (transaction.getStatus() == TransactionStatus.APPROVED)
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Transaction is already paid");

                ShippingAddress shippingAddress = transactionMapper.toShippingAddress(dto.userAddress());
                transaction.setShippingAddress(shippingAddress);

                Customer customer = identityApi.findByIdentity(userIdentity)
                    .map(account -> saveAddress(customerOf(account), dto.userAddress()))
                    .orElseGet(() -> guestCustomerOf(dto));

                transaction.setCustomer(customer);
                return repository.save(transaction);
            });
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerAddressDTO> getSavedAddresses(String userIdentity) {
        return identityApi.findByIdentity(userIdentity)
            .flatMap(account -> customersRepository.findByUserEmail(account.email()))
            .map(customerMapper::toCustomerAddressDTOs)
            .orElse(List.of());
    }

    private Customer customerOf(Account account) {
        return customersRepository.findByUserEmail(account.email()).orElseGet(() -> {
            Customer customer = new Customer();
            customer.setUserEmail(account.email());
            return customer;
        });
    }

    private Customer guestCustomerOf(UserTransactionDTO dto) {
        String names = clean(dto.userNames());
        String email = clean(dto.userEmail() == null ? null : dto.userEmail().toLowerCase(Locale.ROOT));
        String phone = clean(dto.userPhone());

        return customersRepository
            .findFirstByGuest_UserNamesAndGuest_UserEmailAndGuest_UserPhone(names, email, phone)
            .orElseGet(() -> newGuestCustomer(new Guest(null, names, email, phone)));
    }

    private Customer newGuestCustomer(Guest guest) {
        Customer customer = new Customer();
        customer.setGuest(guest);
        return customersRepository.save(customer);
    }

    private static String clean(String value) {
        return value == null ? null : value.trim().replaceAll("\\s+", " ");
    }

    private Customer saveAddress(Customer customer, CustomerAddressDTO dto) {
        Address address = customer.getId() == null
            ? new Address()
            : addressRepository.findByCustomerAndAddressName(customer, dto.addressName())
                .orElseGet(Address::new);

        transactionMapper.updateAddress(dto, address);
        address.setCustomer(customer);

        if (!customer.getAddress().contains(address)) customer.getAddress().add(address);
        if (customer.getId() == null) customersRepository.save(customer);
        return customer;
    }
}
