package com.multi.shop.api.multi_shop_api.payments.services.impl;

import com.multi.shop.api.multi_shop_api.payments.dtos.CustomerAddressDTO;
import com.multi.shop.api.multi_shop_api.payments.dtos.UserTransactionDTO;
import com.multi.shop.api.multi_shop_api.payments.entities.Address;
import com.multi.shop.api.multi_shop_api.payments.entities.Customer;
import com.multi.shop.api.multi_shop_api.payments.entities.Guest;
import com.multi.shop.api.multi_shop_api.payments.entities.ShippingAddress;
import com.multi.shop.api.multi_shop_api.payments.entities.Transaction;
import com.multi.shop.api.multi_shop_api.payments.mappers.CustomerMapper;
import com.multi.shop.api.multi_shop_api.payments.mappers.TransactionMapper;
import com.multi.shop.api.multi_shop_api.payments.repositories.AddressRepository;
import com.multi.shop.api.multi_shop_api.payments.repositories.CustomersRepository;
import com.multi.shop.api.multi_shop_api.payments.repositories.PaymentsRepository;
import com.multi.shop.api.multi_shop_api.payments.security.CheckoutAccessToken;
import com.multi.shop.api.multi_shop_api.payments.services.CheckoutCustomerService;
import com.multi.shop.api.multi_shop_api.users.entities.User;
import com.multi.shop.api.multi_shop_api.users.repositories.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class CheckoutCustomerServiceImpl implements CheckoutCustomerService {
    private final PaymentsRepository repository;
    private final CustomersRepository customersRepository;
    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    private final CheckoutAccessToken checkoutAccessToken;

    public CheckoutCustomerServiceImpl(PaymentsRepository repository, CustomersRepository customersRepository, AddressRepository addressRepository, UserRepository userRepository, CheckoutAccessToken checkoutAccessToken) {
        this.repository = repository;
        this.customersRepository = customersRepository;
        this.addressRepository = addressRepository;
        this.userRepository = userRepository;
        this.checkoutAccessToken = checkoutAccessToken;
    }

    @Override
    @Transactional
    public Optional<Transaction> addUserToTransaction(UserTransactionDTO dto, String transactionId, String accessToken, String userIdentity) {
        return repository.findById(transactionId)
            .filter(transaction -> checkoutAccessToken.grants(transaction, accessToken))
            .map(transaction -> {
                if ("APPROVED".equals(transaction.getStatus()))
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Transaction is already paid");

                ShippingAddress shippingAddress = TransactionMapper.MAPPER.toShippingAddress(dto.userAddress());
                transaction.setShippingAddress(shippingAddress);

                Customer customer = findUser(userIdentity)
                    .map(user -> saveAddress(customerOf(user), dto.userAddress()))
                    .orElseGet(() -> guestCustomerOf(transaction, dto));

                transaction.setCustomer(customer);
                return repository.save(transaction);
            });
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerAddressDTO> getSavedAddresses(String userIdentity) {
        return findUser(userIdentity)
            .flatMap(user -> customersRepository.findByUserEmail(user.getEmail()))
            .map(CustomerMapper.MAPPER::toCustomerAddressDTOs)
            .orElse(List.of());
    }

    private Optional<User> findUser(String userIdentity) {
        if (userIdentity == null || userIdentity.isBlank()) return Optional.empty();

        return userIdentity.matches("\\d+")
            ? userRepository.findByPhoneNumber(Long.parseLong(userIdentity))
            : userRepository.findByEmail(userIdentity);
    }

    private Customer customerOf(User user) {
        return customersRepository.findByUserEmail(user.getEmail()).orElseGet(() -> {
            Customer customer = new Customer();
            customer.setUser(user);
            return customer;
        });
    }

    private Customer guestCustomerOf(Transaction transaction, UserTransactionDTO dto) {
        Customer current = transaction.getCustomer();
        if (current == null || !current.isGuest()) return newGuestCustomer(dto);

        Guest guest = current.getGuest();
        guest.setUserNames(dto.userNames());
        guest.setUserEmail(dto.userEmail());
        guest.setUserPhone(dto.userPhone());
        return current;
    }

    private Customer newGuestCustomer(UserTransactionDTO dto) {
        Customer customer = new Customer();
        customer.setGuest(new Guest(null, dto.userNames(), dto.userEmail(), dto.userPhone()));
        return customersRepository.save(customer);
    }

    private Customer saveAddress(Customer customer, CustomerAddressDTO dto) {
        Address address = customer.getId() == null
            ? new Address()
            : addressRepository.findByCustomerAndAddressName(customer, dto.addressName())
                .orElseGet(Address::new);

        TransactionMapper.MAPPER.updateAddress(dto, address);
        address.setCustomer(customer);

        if (!customer.getAddress().contains(address)) customer.getAddress().add(address);
        if (customer.getId() == null) customersRepository.save(customer);
        return customer;
    }
}
