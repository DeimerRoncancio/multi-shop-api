package com.multi.shop.api.multi_shop_api.products.services.impl;

import com.multi.shop.api.multi_shop_api.common.exceptions.NotFoundException;
import com.multi.shop.api.multi_shop_api.products.dtos.VariantDTO;
import com.multi.shop.api.multi_shop_api.products.dtos.VariantResponseDTO;
import com.multi.shop.api.multi_shop_api.products.entities.Variant;
import com.multi.shop.api.multi_shop_api.products.mappers.VariantMapper;
import com.multi.shop.api.multi_shop_api.products.repositories.VariantRepository;
import com.multi.shop.api.multi_shop_api.products.services.VariantService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class VariantServiceImpl implements VariantService {
    private final VariantRepository repository;
    private final VariantMapper variantMapper;

    public VariantServiceImpl(VariantRepository repository, VariantMapper variantMapper) {
        this.repository = repository;
        this.variantMapper = variantMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VariantResponseDTO> findAll(Pageable pageable){
        Page<Variant> variants = repository.findAll(pageable);

        return variants.map(var ->
            variantMapper.variantToDTO(var, variantMapper.splitValues(var.getValues())));
    }

    @Override
    @Transactional(readOnly = true)
    public VariantResponseDTO findOne(String id) {
        Variant variant = repository.findById(id).orElseThrow(() -> new NotFoundException("Variant not found"));

        return variantMapper.variantToDTO(variant, variantMapper.splitValues(variant.getValues()));
    }

    @Override
    @Transactional
    public VariantDTO addVariant(VariantDTO newVariant) {
        String values = variantMapper.joinValues(newVariant.listValues());
        repository.save(variantMapper.dtoToVariant(newVariant, values));
        return newVariant;
    }

    @Override
    @Transactional
    public Optional<VariantDTO> updateVariant(String id, VariantDTO variantDTO){
        return repository.findById(id).map(variantDb -> {
            String values = variantMapper.joinValues(variantDTO.listValues());

            variantDb.setName(variantDTO.name());
            variantDb.setTag(variantDTO.tag());
            variantDb.setValues(values);
            repository.save(variantDb);

            return variantDTO;
        });
    }

    @Override
    @Transactional
    public Optional<Variant> deleteVariant(String id) {
        return repository.findById(id).map(variantDb -> {
            repository.delete(variantDb);
            return variantDb;
        });
    }

    @Override
    @Transactional
    public List<Variant> findVariantsByName(List<String> variants) {
        return repository.findByNameIn(variants);
    }
}
