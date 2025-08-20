package com.multi.shop.api.multi_shop_api.products.controllers;

import com.multi.shop.api.multi_shop_api.products.dtos.VariantDTO;
import com.multi.shop.api.multi_shop_api.products.dtos.VariantResponseDTO;
import com.multi.shop.api.multi_shop_api.products.services.VariantService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/app/variants")
@CrossOrigin(originPatterns = "*")
public class VariantController {
    private final VariantService service;

    public VariantController(VariantService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    public ResponseEntity<VariantResponseDTO> getVariant(@PathVariable String id) {
        return ResponseEntity.ok().body(service.findOne(id));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Page<VariantResponseDTO> getVariants(@PageableDefault Pageable pageable) {
        return service.findAll(pageable);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VariantDTO> addVariant(@RequestBody VariantDTO dto) {
        return ResponseEntity.ok().body(service.addVariant(dto));
    }


}
