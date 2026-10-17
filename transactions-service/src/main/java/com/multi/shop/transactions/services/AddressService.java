package com.multi.shop.transactions.services;

import com.multi.shop.transactions.dtos.CustomerAddressDTO;
import com.multi.shop.transactions.dtos.SavedAddressDTO;

import java.util.List;

public interface AddressService {
    List<SavedAddressDTO> getAddresses(String userIdentity);
    SavedAddressDTO createAddress(String userIdentity, CustomerAddressDTO dto);
    SavedAddressDTO updateAddress(String userIdentity, String id, CustomerAddressDTO dto);
    void deleteAddress(String userIdentity, String id);
}
