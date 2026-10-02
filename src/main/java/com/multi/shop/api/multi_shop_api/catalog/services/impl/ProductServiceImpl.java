package com.multi.shop.api.multi_shop_api.catalog.services.impl;

import com.multi.shop.api.multi_shop_api.catalog.services.ImageNames;
import com.multi.shop.api.multi_shop_api.media.api.MediaApi;
import com.multi.shop.api.multi_shop_api.media.api.StoredImage;
import com.multi.shop.api.multi_shop_api.catalog.dtos.ProductDTO;
import com.multi.shop.api.multi_shop_api.catalog.dtos.ProductResponseDTO;
import com.multi.shop.api.multi_shop_api.catalog.dtos.VariantDTO;
import com.multi.shop.api.multi_shop_api.catalog.entities.Variant;
import com.multi.shop.api.multi_shop_api.catalog.mappers.VariantMapper;
import com.multi.shop.api.multi_shop_api.catalog.services.ProductCategoryService;
import com.multi.shop.api.multi_shop_api.catalog.services.ProductService;
import com.multi.shop.api.multi_shop_api.catalog.services.VariantService;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.multi.shop.api.multi_shop_api.catalog.entities.Product;
import com.multi.shop.api.multi_shop_api.catalog.entities.ProductCategory;
import com.multi.shop.api.multi_shop_api.catalog.mappers.ProductMapper;
import com.multi.shop.api.multi_shop_api.catalog.repositories.ProductRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {
    private static final Logger log = LoggerFactory.getLogger(ProductServiceImpl.class);
    private final ProductRepository repository;
    private final ProductCategoryService categoryService;
    private final MediaApi mediaApi;
    private final ProductMapper productMapper;
    private final VariantMapper variantMapper;

    public ProductServiceImpl(ProductRepository repository, ProductCategoryService categoryService, MediaApi mediaApi, ProductMapper productMapper, VariantMapper variantMapper) {
        this.repository = repository;
        this.categoryService = categoryService;
        this.mediaApi = mediaApi;
        this.productMapper = productMapper;
        this.variantMapper = variantMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponseDTO> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(this::toResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProductResponseDTO> findOne(String id) {
        return repository.findById(id).map(this::toResponseDTO);
    }

    @Override
    @Transactional
    public ProductDTO save(ProductDTO dto) {
        List<Variant> variants = new ArrayList<>();
        if (dto.variants() != null && !dto.variants().isEmpty()) {
            variants = dto.variants().stream().map(variantDto -> {
                String listValues = variantMapper.joinValues(variantDto.listValues());
                return new Variant(variantDto.name(), variantDto.tag(), listValues, variantDto.type());
            }).toList();
        }

        Product product = productMapper.productDTOtoProduct(dto, variants);

        List<ProductCategory> categoryList =  categoryService.findCategoriesByName(dto.categoriesList());
        product.setCategories(categoryList);

        List<StoredImage> images = renameImages(product.getProductName(), uploadImages(dto.images()));
        product.setImageIds(idsOf(images));

        repository.save(product);
        return productMapper.productToProductDTO(product, images);
    }

    @Override
    @Transactional
    public Optional<ProductDTO> update(String id, ProductDTO dto) {
        return repository.findById(id).map(productDb -> {
            List<ProductCategory> categoriesDb = categoryService.findCategoriesByName(dto.categoriesList());

            List<ProductCategory> productCategories = updateCategories(
                productDb.getCategories(), categoriesDb);

            updateVariants(productDb.getVariants(), dto.variants(), dto.variantsToRemove());

            List<StoredImage> productImages = updateImages(
                mediaApi.findAll(productDb.getImageIds()), dto.images(), dto.imagesToRemove());

            productDb.setCategories(productCategories);
            productMapper.toUpdateProduct(dto, productDb);
            productImages = renameImages(productDb.getProductName(), productImages);
            productDb.setImageIds(idsOf(productImages));

            repository.save(productDb);
            return productMapper.productToProductDTO(productDb, productImages);
        });
    }

    @Override
    @Transactional
    public Optional<Product> delete(String id) {
        return repository.findById(id).map(product -> {
            product.getImageIds().forEach(mediaApi::deleteAfterCommit);

            repository.delete(product);
            return product;
        });
    }

    @Override
    @Transactional
    public Page<ProductResponseDTO> search(String query, List<String> categories, Pageable pageable) {
        Page<Product> products = repository.findByProductNameOrCategories(query, categories, pageable);

        return products.map(this::toResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponseDTO> latestProducts() {
        return repository.findTop5ByOrderByCreatedAtDesc().stream().map(this::toResponseDTO).toList();
    }

    private ProductResponseDTO toResponseDTO(Product product) {
        return productMapper.productToResponseDTO(
            product, variantMapper.toVariantDTOs(product.getVariants()), mediaApi.findAll(product.getImageIds()));
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

    public List<StoredImage> updateImages(List<StoredImage> storedImages, List<MultipartFile> files, List<String> removeImageIds) {
        List<StoredImage> currentImages = new ArrayList<>(storedImages);

        if (removeImageIds != null && !removeImageIds.isEmpty()){
            Set<String> idsToRemove = new HashSet<>(removeImageIds);
            List<StoredImage> imagesToRemove = currentImages.stream()
                    .filter(img -> idsToRemove.contains(img.imageId()))
                    .toList();

            currentImages.removeAll(imagesToRemove);
            imagesToRemove.forEach(image -> mediaApi.deleteAfterCommit(image.id()));
        }

        if (files != null && !files.isEmpty()) {
            Set<String> imageNames = currentImages.stream()
                    .map(StoredImage::name)
                    .map(String::toLowerCase)
                    .collect(Collectors.toSet());

            List<MultipartFile> newFiles = files.stream()
                    .filter(file -> Optional.ofNullable(file.getOriginalFilename())
                            .map(String::toLowerCase)
                            .map(name -> !imageNames.contains(name))
                            .orElse(false))
                    .toList();

            currentImages.addAll(uploadImages(newFiles));
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
                String values = variantMapper.joinValues(dto.listValues());
                Variant newVariant = variantMapper.dtoToVariant(dto, values);

                currentVariants.add(newVariant);
            } else {
                Optional<Variant> variantOp = currentVariants.stream()
                        .filter(var -> var.getId().equals(dto.id()))
                        .findFirst();

                variantOp.ifPresent(variant -> {
                    String values = variantMapper.joinValues(dto.listValues());

                    variant.setName(dto.name());
                    variant.setTag(dto.tag());
                    variant.setType(dto.type());
                    variant.setValues(values);
                });
            }
        }
    }

    public List<StoredImage> renameImages(String productName, List<StoredImage> images) {
        List<StoredImage> ordered = ImageNames.sorted(images);
        List<StoredImage> renamed = new ArrayList<>();

        for (int index = 0; index < ordered.size(); index++) {
            StoredImage image = ordered.get(index);
            String name = ImageNames.build(productName, index + 1, image.name());
            mediaApi.rename(image.id(), name);
            renamed.add(image.withName(name));
        }

        return renamed;
    }

    private static List<String> idsOf(List<StoredImage> images) {
        return images.stream().map(StoredImage::id).collect(Collectors.toCollection(ArrayList::new));
    }

    public List<StoredImage> uploadImages(List<MultipartFile> files) {
        if (files == null) return List.of();

        return files.stream()
                .filter(file -> file != null && !file.isEmpty())
                .map(this::uploadImage)
                .toList();
    }

    public StoredImage uploadImage(MultipartFile file) {
        try {
            return mediaApi.upload(file);
        } catch (IOException e) {
            log.warn("Exception trying add image: {}", String.valueOf(e));
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "The image could not be uploaded");
        }
    }
}
