package com.multi.shop.transactions.controllers;

import com.multi.shop.transactions.dtos.CustomerAddressDTO;
import com.multi.shop.transactions.dtos.SavedAddressDTO;
import com.multi.shop.transactions.services.AddressService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/app/addresses")
public class AddressesController {
    private final AddressService service;

    public AddressesController(AddressService service) {
        this.service = service;
    }

    @GetMapping
    public List<SavedAddressDTO> getAddresses(Principal principal) {
        return service.getAddresses(principal.getName());
    }

    @PostMapping
    public ResponseEntity<SavedAddressDTO> createAddress(@Valid @RequestBody CustomerAddressDTO dto, Principal principal) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createAddress(principal.getName(), dto));
    }

    @PutMapping("/{id}")
    public SavedAddressDTO updateAddress(@PathVariable String id, @Valid @RequestBody CustomerAddressDTO dto, Principal principal) {
        return service.updateAddress(principal.getName(), id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAddress(@PathVariable String id, Principal principal) {
        service.deleteAddress(principal.getName(), id);
        return ResponseEntity.noContent().build();
    }
}
