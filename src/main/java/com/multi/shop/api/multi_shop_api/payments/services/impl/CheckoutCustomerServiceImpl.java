package com.multi.shop.api.multi_shop_api.payments.services.impl;

import com.multi.shop.api.multi_shop_api.payments.dtos.CustomerAddressDTO;
import com.multi.shop.api.multi_shop_api.payments.dtos.CustomerCheckoutDTO;
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
import com.multi.shop.api.multi_shop_api.payments.services.CheckoutCustomerService;
import com.multi.shop.api.multi_shop_api.users.entities.User;
import com.multi.shop.api.multi_shop_api.users.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class CheckoutCustomerServiceImpl implements CheckoutCustomerService {
    private final PaymentsRepository repository;
    private final CustomersRepository customersRepository;
    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    public CheckoutCustomerServiceImpl(PaymentsRepository repository, CustomersRepository customersRepository, AddressRepository addressRepository, UserRepository userRepository) {
        this.repository = repository;
        this.customersRepository = customersRepository;
        this.addressRepository = addressRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Customer> getCustomer(String transactionId) {
        Optional<Transaction> transactionOp = repository.findById(transactionId);
        Customer customer = new Customer();

        if (transactionOp.isPresent()) customer = transactionOp.get().getCustomer();

        return Optional.ofNullable(customer);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CustomerCheckoutDTO> getCheckoutCustomer(String transactionId, String email) {
        return repository.findById(transactionId).flatMap(transaction -> {
            Customer customer = transaction.getCustomer();

            if (customer != null) {
                return customerHasEmail(customer, email)
                    ? Optional.of(CustomerMapper.MAPPER.toCustomerCheckoutDTO(customer, transaction))
                    : Optional.empty();
            }

            return customersRepository
                .findByUser_EmailOrGuest_UserEmail(email, email)
                .map(customerMatch -> CustomerMapper.MAPPER.toCustomerCheckoutDTO(customerMatch, transaction));
        });
    }

    @Override
    @Transactional
    public Optional<Transaction> addUserToTransaction(UserTransactionDTO dto, String transactionId) {
        return repository.findById(transactionId).map(transaction -> {
            Customer customer = findOrCreateCustomer(dto);
            Address address = findOrCreateAddress(dto.userAddress(), customer);

            address.setCustomer(customer);
            if (!customer.getAddress().contains(address))
                customer.getAddress().add(address);

            if (customer.getId() == null) customersRepository.save(customer);
            transaction.setCustomer(customer);
            ShippingAddress shippingAddress = TransactionMapper.MAPPER.toShippingAddress(dto.userAddress());
            transaction.setShippingAddress(shippingAddress);
            return repository.save(transaction);
        });
    }

    private boolean customerHasEmail(Customer customer, String email) {
        return customer.isGuest()
            ? email.equals(customer.getGuest().getUserEmail())
            : customer.getUser() != null && email.equals(customer.getUser().getEmail());
    }

    private Customer findOrCreateCustomer(UserTransactionDTO dto) {
        return customersRepository
            .findByUser_EmailOrGuest_UserEmail(dto.userEmail(), dto.userEmail())
            .map(customer -> {
                if (customer.isGuest()) {
                    customer.getGuest().setUserNames(dto.userNames());
                    customer.getGuest().setUserEmail(dto.userEmail());
                    customer.getGuest().setUserPhone(dto.userPhone());
                }

                return customer;
            }).orElseGet(() -> {
                Customer newCustomer = new Customer();

                Optional<User> user = userRepository.findByEmail(dto.userEmail());
                if (user.isPresent()) {
                    newCustomer.setUser(user.get());
                } else {
                    Guest guest = new Guest();
                    guest.setUserNames(dto.userNames());
                    guest.setUserEmail(dto.userEmail());
                    guest.setUserPhone(dto.userPhone());
                    newCustomer.setGuest(guest);
                }

                return newCustomer;
            });
    }

    private Address findOrCreateAddress(CustomerAddressDTO dto, Customer customer) {
        Address address = customer.getId() == null
            ? new Address()
            : addressRepository.findByCustomerAndAddressName(customer, dto.addressName())
                .orElseGet(Address::new);

        return TransactionMapper.MAPPER.updateAddress(dto, address);
    }
}
