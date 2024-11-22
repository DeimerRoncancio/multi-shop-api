package com.majestic.food.api.majestic_food_api.services;

import com.majestic.food.api.majestic_food_api.entities.Product;
import com.majestic.food.api.majestic_food_api.entities.dtos.NewProductDTO;
import com.majestic.food.api.majestic_food_api.entities.dtos.UpdateProductDTO;
import com.majestic.food.api.majestic_food_api.mappers.ProductMapper;
import com.majestic.food.api.majestic_food_api.repositories.ProductRepository;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository repository;
    
    @Override
    @Transactional(readOnly = true)
    public List<Product> findAll() {
        return repository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Product> findOne(String id) {
        return repository.findById(id);
    }

    @Override
    @Transactional
    public Product save(NewProductDTO dto) {
        Product product = ProductMapper.mapper.productCreateDTOtoProduct(dto);

        return repository.save(product);
    }

    @Override
    @Transactional
    public Optional<Product> update(String id, UpdateProductDTO dto) {
        Optional<Product> productOptional = repository.findById(id);

        productOptional.ifPresent(productDb -> {
            ProductMapper.mapper.toUpdateProduct(dto, productDb);

            repository.save(productDb);
        });

        return productOptional;
    }

    @Override
    @Transactional
    public Optional<Product> delete(String id) {
        Optional<Product> productOptional = repository.findById(id);

        if (productOptional.isPresent())
            repository.delete(productOptional.get());
        
        return productOptional;
    }
}
