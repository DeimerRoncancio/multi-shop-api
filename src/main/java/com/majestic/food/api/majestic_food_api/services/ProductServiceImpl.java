package com.majestic.food.api.majestic_food_api.services;

import com.majestic.food.api.majestic_food_api.entities.Product;
import com.majestic.food.api.majestic_food_api.entities.dtos.ProductCreateDTO;
import com.majestic.food.api.majestic_food_api.entities.dtos.ProductUpdateDTO;
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
    public Product save(ProductCreateDTO dto) {
        Product product = createProductFromDto(dto);

        return repository.save(product);
    }

    @Override
    @Transactional
    public Optional<Product> update(String id, ProductUpdateDTO dto) {
        Optional<Product> productOptional = repository.findById(id);

        productOptional.ifPresent(productDb -> {
            updateProductFromDto(dto, productDb);

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

    private void updateProductFromDto(ProductUpdateDTO dto, Product product) {
        product.setProductName(dto.getProductName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
    }

    private Product createProductFromDto(ProductCreateDTO dto) {
        Product product = new Product();
        
        product.setProductName(dto.getProductName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());

        return product;
    }
}
