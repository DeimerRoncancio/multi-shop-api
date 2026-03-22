package com.multi.shop.api.multi_shop_api.products.services.impl;

import com.multi.shop.api.multi_shop_api.images.services.ImageService;
import com.multi.shop.api.multi_shop_api.products.dtos.ProductDTO;
import com.multi.shop.api.multi_shop_api.products.dtos.ProductResponseDTO;
import com.multi.shop.api.multi_shop_api.products.dtos.VariantDTO;
import com.multi.shop.api.multi_shop_api.products.entities.Variant;
import com.multi.shop.api.multi_shop_api.products.mappers.VariantMapper;
import com.multi.shop.api.multi_shop_api.products.services.ProductCategoryService;
import com.multi.shop.api.multi_shop_api.products.services.ProductService;
import com.multi.shop.api.multi_shop_api.products.services.VariantService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.multi.shop.api.multi_shop_api.images.entities.Image;
import com.multi.shop.api.multi_shop_api.products.entities.Product;
import com.multi.shop.api.multi_shop_api.products.entities.ProductCategory;
import com.multi.shop.api.multi_shop_api.products.mappers.ProductMapper;
import com.multi.shop.api.multi_shop_api.products.repositories.ProductRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {
    private final Logger logger = LoggerFactory.getLogger(ProductServiceImpl.class);
    private final ProductRepository repository;
    private final ProductCategoryService categoryService;
    private final ImageService imageService;

    public ProductServiceImpl(ProductRepository repository, ProductCategoryService categoryService, ImageService imageService) {
        this.repository = repository;
        this.categoryService = categoryService;
        this.imageService = imageService;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponseDTO> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(product -> {
            List<VariantDTO> variants = product.getVariants().stream().map(var -> {
                List<String> values = List.of(var.getValues().split("\\|"));
                return new VariantDTO(var.getId(), var.getName(), var.getType(), var.getTag(), values);
            }).toList();

            return ProductMapper.MAPPER.productToResponseDTO(product, variants);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProductResponseDTO> findOne(String id) {
        return repository.findById(id).map(product -> {
            List<VariantDTO> variants = product.getVariants().stream().map(variant -> {
                List<String> values = List.of(variant.getValues().split("\\|"));
                return new VariantDTO(variant.getId(), variant.getName(), variant.getType(), variant.getTag(), values);
            }).toList();

            return ProductMapper.MAPPER.productToResponseDTO(product, variants);
        });
    }

    @Override
    @Transactional
    public ProductDTO save(ProductDTO dto) {
        List<Variant> variants = new ArrayList<>();
        if (dto.variants() != null && !dto.variants().isEmpty()) {
            variants = dto.variants().stream().map(variantDto -> {
                String listValues = String.join("|", variantDto.listValues());
                return new Variant(variantDto.name(), variantDto.tag(), listValues, variantDto.type());
            }).toList();
        }

        Product product = ProductMapper.MAPPER.productDTOtoProduct(dto, variants);

        List<ProductCategory> categoryList =  categoryService.findCategoriesByName(dto.categoriesList());
        product.setCategories(categoryList);

        dto.images().stream()
                .filter(Objects::nonNull)
                .map(this::uploadImage)
                .forEach(product.getProductImages()::add);

        repository.save(product);
        return ProductMapper.MAPPER.productToProductDTO(product);
    }

    @Override
    @Transactional
    public Optional<ProductDTO> update(String id, ProductDTO dto) {
        return repository.findById(id).map(productDb -> {
            List<ProductCategory> categoriesDb = categoryService.findCategoriesByName(dto.categoriesList());

            List<ProductCategory> productCategories = updateCategories(
                productDb.getCategories(), categoriesDb);

            updateVariants(productDb.getVariants(), dto.variants(), dto.variantsToRemove());

            List<Image> productImages = updateImages(
                productDb.getProductImages(), dto.images(), dto.imagesToRemove());

            productDb.setCategories(productCategories);
            productDb.setProductImages(productImages);
            ProductMapper.MAPPER.toUpdateProduct(dto, productDb);

            repository.save(productDb);
            return ProductMapper.MAPPER.productToProductDTO(productDb);
        });
    }

    @Override
    @Transactional
    public Optional<Product> delete(String id) {
        return repository.findById(id).map(product -> {
            if (!product.getProductImages().isEmpty())
                product.getProductImages().forEach(this::deleteImage);

            repository.delete(product);
            return product;
        });
    }

    @Override
    @Transactional
    public Page<ProductResponseDTO> search(String query, List<String> categories, Pageable pageable) {
        Page<Product> products = repository.findByProductNameOrCategories(query, categories, pageable);

        return products.map(product -> {
            List<VariantDTO> variants = product.getVariants().stream().map(variant -> {
                List<String> values = List.of(variant.getValues().split("\\|"));
                return new VariantDTO(variant.getId(), variant.getName(), variant.getType(), variant.getTag(), values);
            }).toList();
            return ProductMapper.MAPPER.productToResponseDTO(product, variants);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponseDTO> latestProducts() {
        return repository.findTop5ByOrderByCreatedAtDesc().stream().map(product -> {
            List<VariantDTO> variants = product.getVariants().stream().map(var -> {
                List<String> values = List.of(var.getValues().split("\\|"));
                return new VariantDTO(var.getId(), var.getName(), var.getType(), var.getTag(), values);
            }).toList();

            return ProductMapper.MAPPER.productToResponseDTO(product, variants);
        }).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Long productsSize() {
        return repository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> productsStats() {
        Map<String, Long> productsStats = new HashMap<>();

        productsStats.put("totalProducts", productsSize());
        productsStats.put("productsWithVariants", repository.countProductsWithVariants());

        return productsStats;
    }

    public List<Image> updateImages(List<Image> currentImages, List<MultipartFile> files, List<String> removeImageIds) {
        if (removeImageIds != null && !removeImageIds.isEmpty()){
            Set<String> idsToRemove = new HashSet<>(removeImageIds);
            List<Image> imagesToRemove = currentImages.stream()
                    .filter(img -> idsToRemove.contains(img.getImageId()))
                    .toList();

            currentImages.removeAll(imagesToRemove);
            imagesToRemove.forEach(this::deleteImage);
        }

        if (files != null && !files.isEmpty()) {
            Set<String> imageNames = currentImages.stream()
                    .map(Image::getName)
                    .map(String::toLowerCase)
                    .collect(Collectors.toSet());

            files.stream()
                    .filter(file -> Optional.ofNullable(file.getOriginalFilename())
                            .map(String::toLowerCase)
                            .map(name -> !imageNames.contains(name))
                            .orElse(false))
                    .map(this::uploadImage)
                    .forEach(currentImages::add);
        }

        return currentImages;
    }

    public List<ProductCategory> updateCategories(List<ProductCategory> currentCategories, List<ProductCategory> categories) {
        currentCategories.removeIf(cat -> !categories.contains(cat));

        categories.stream()
                .filter(cats -> !currentCategories.contains(cats))
                .forEach(currentCategories::add);

        return currentCategories;
    }

    public void updateVariants(List<Variant> currentVariants, List<VariantDTO> variants, List<String> variantsToRemove) {
        if (variantsToRemove != null && !variantsToRemove.isEmpty())
            currentVariants.removeIf(var -> variantsToRemove.contains(var.getId()));

        if (variants == null || variants.isEmpty()) return;
        for (VariantDTO dto : variants) {
            if (dto.id() == null || dto.id().isEmpty()) {
                String values = String.join("|", dto.listValues());
                Variant newVariant = VariantMapper.MAPPER.dtoToVariant(dto, values);

                currentVariants.add(newVariant);
            } else {
                Optional<Variant> variantOp = currentVariants.stream()
                        .filter(var -> var.getId().equals(dto.id()))
                        .findFirst();

                variantOp.ifPresent(variant -> {
                    String values = String.join("|", dto.listValues());

                    variant.setName(dto.name());
                    variant.setTag(dto.tag());
                    variant.setType(dto.type());
                    variant.setValues(values);
                });
            }
        }
    }

    public void deleteImage(Image image) {
        try {
            imageService.deleteImage(image);
        } catch(IOException e) {
            logger.warn("Exception to try delete image {}: {}", image.getImageId(), e.getMessage());
        }
    }

    public Image uploadImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            logger.warn("File is null or empty");
            return null;
        }

        try {
            return imageService.uploadImage(file);
        } catch (IOException e) {
            logger.warn("Exception trying add image: {}", String.valueOf(e));
            return null;
        }
    }
}
