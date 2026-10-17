package com.multi.shop.transactions.services.impl;

import com.multi.shop.transactions.dtos.CustomerAddressDTO;
import com.multi.shop.transactions.dtos.SavedAddressDTO;
import com.multi.shop.transactions.entities.Address;
import com.multi.shop.transactions.entities.Customer;
import com.multi.shop.transactions.identity.Account;
import com.multi.shop.transactions.identity.IdentityApi;
import com.multi.shop.transactions.mappers.CustomerMapper;
import com.multi.shop.transactions.mappers.TransactionMapper;
import com.multi.shop.transactions.repositories.AddressRepository;
import com.multi.shop.transactions.repositories.CustomersRepository;
import com.multi.shop.transactions.services.AddressService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class AddressServiceImpl implements AddressService {
    private final AddressRepository addressRepository;
    private final CustomersRepository customersRepository;
    private final IdentityApi identityApi;
    private final TransactionMapper transactionMapper;
    private final CustomerMapper customerMapper;

    public AddressServiceImpl(AddressRepository addressRepository, CustomersRepository customersRepository, IdentityApi identityApi, TransactionMapper transactionMapper, CustomerMapper customerMapper) {
        this.addressRepository = addressRepository;
        this.customersRepository = customersRepository;
        this.identityApi = identityApi;
        this.transactionMapper = transactionMapper;
        this.customerMapper = customerMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SavedAddressDTO> getAddresses(String userIdentity) {
        return customerMapper.toSavedAddressDTOs(
            addressRepository.findByCustomer_UserEmailOrderByAddressNameAsc(account(userIdentity).email())
        );
    }

    @Override
    @Transactional
    public SavedAddressDTO createAddress(String userIdentity, CustomerAddressDTO dto) {
        Customer customer = customerOf(account(userIdentity));
        ensureFreeName(customer, dto.addressName(), null);
        if (customer.getId() == null) customer = customersRepository.save(customer);

        Address address = new Address();
        transactionMapper.updateAddress(dto, address);
        address.setCustomer(customer);
        customer.getAddress().add(address);
        return customerMapper.toSavedAddressDTO(addressRepository.save(address));
    }

    @Override
    @Transactional
    public SavedAddressDTO updateAddress(String userIdentity, String id, CustomerAddressDTO dto) {
        Address address = ownAddress(userIdentity, id);
        ensureFreeName(address.getCustomer(), dto.addressName(), id);

        transactionMapper.updateAddress(dto, address);
        return customerMapper.toSavedAddressDTO(address);
    }

    @Override
    @Transactional
    public void deleteAddress(String userIdentity, String id) {
        Address address = ownAddress(userIdentity, id);
        address.getCustomer().getAddress().remove(address);
        addressRepository.delete(address);
    }

    private Account account(String userIdentity) {
        return identityApi.findByIdentity(userIdentity)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Cuenta no encontrada"));
    }

    private Customer customerOf(Account account) {
        return customersRepository.findByUserEmail(account.email()).orElseGet(() -> {
            Customer customer = new Customer();
            customer.setUserEmail(account.email());
            return customer;
        });
    }

    private Address ownAddress(String userIdentity, String id) {
        return addressRepository.findByIdAndCustomer_UserEmail(id, account(userIdentity).email())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Direccion no encontrada"));
    }

    private void ensureFreeName(Customer customer, String addressName, String currentId) {
        if (customer.getId() == null) return;

        addressRepository.findByCustomerAndAddressName(customer, addressName)
            .filter(found -> !found.getId().equals(currentId))
            .ifPresent(found -> {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya tienes una direccion con ese nombre");
            });
    }
}
