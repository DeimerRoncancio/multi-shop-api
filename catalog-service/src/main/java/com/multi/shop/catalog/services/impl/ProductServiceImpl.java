package com.multi.shop.catalog.services.impl;

import com.multi.shop.catalog.services.ImageNames;
import com.multi.shop.catalog.media.MediaApi;
import com.multi.shop.catalog.media.StoredImage;
import com.multi.shop.catalog.dtos.HomeDTO;
import com.multi.shop.catalog.dtos.HomeSectionDTO;
import com.multi.shop.catalog.dtos.ProductDTO;
import com.multi.shop.catalog.dtos.ProductResponseDTO;
import com.multi.shop.catalog.dtos.ProductSuggestionsDTO;
import com.multi.shop.catalog.dtos.VariantDTO;
import com.multi.shop.catalog.entities.Variant;
import com.multi.shop.catalog.mappers.VariantMapper;
import com.multi.shop.catalog.services.ProductCategoryService;
import com.multi.shop.catalog.services.ProductService;
import com.multi.shop.catalog.services.VariantService;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.multi.shop.catalog.entities.Product;
import com.multi.shop.catalog.entities.ProductCategory;
import com.multi.shop.catalog.mappers.ProductMapper;
import com.multi.shop.catalog.repositories.CategoryStats;
import com.multi.shop.catalog.repositories.ProductCategoryRepository;
import com.multi.shop.catalog.repositories.ProductRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {
    private static final Logger log = LoggerFactory.getLogger(ProductServiceImpl.class);
    private static final int SECTION_SIZE = 12;
    private static final int TOP_SIZE = 4;
    private static final int MAX_IDS = 100;
    private final ProductRepository repository;
    private final ProductCategoryRepository categoryRepository;
    private final ProductCategoryService categoryService;
    private final MediaApi mediaApi;
    private final ProductMapper productMapper;
    private final VariantMapper variantMapper;

    public ProductServiceImpl(ProductRepository repository, ProductCategoryRepository categoryRepository, ProductCategoryService categoryService, MediaApi mediaApi, ProductMapper productMapper, VariantMapper variantMapper) {
        this.repository = repository;
        this.categoryRepository = categoryRepository;
        this.categoryService = categoryService;
        this.mediaApi = mediaApi;
        this.productMapper = productMapper;
        this.variantMapper = variantMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponseDTO> findAll(Pageable pageable) {
        Page<Product> products = repository.findAll(pageable);
        Map<String, StoredImage> images = imagesOf(products.getContent());

        return products.map(product -> toResponseDTO(product, images));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProductResponseDTO> findOne(String id) {
        return repository.findById(id).map(product -> toResponseDTO(product, imagesOf(List.of(product))));
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
    public Page<ProductResponseDTO> search(String query, List<String> categories, Long minPrice, Long maxPrice,
    Pageable pageable) {
        boolean reversed = minPrice != null && maxPrice != null && minPrice > maxPrice;
        Page<Product> products = repository.findByProductNameOrCategories(query, categories,
            reversed ? maxPrice : minPrice, reversed ? minPrice : maxPrice, pageable);
        Map<String, StoredImage> images = imagesOf(products.getContent());

        return products.map(product -> toResponseDTO(product, images));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductSuggestionsDTO suggestions(List<String> categories, List<String> exclude, int size, Integer min, Sort sort) {
        List<String> known = new ArrayList<>(exclude == null ? List.of() : exclude);

        List<Product> products = new ArrayList<>();
        if (categories != null && !categories.isEmpty())
            products.addAll(repository.findByCategoriesExcluding(categories, known, PageRequest.of(0, size, sort)));

        boolean filled = products.size() < Math.min(min == null ? size : min, size);
        if (filled) {
            products.forEach(product -> known.add(product.getId()));
            products.addAll(repository.findExcluding(known, PageRequest.of(0, size - products.size(), sort)));
        }

        Map<String, StoredImage> images = imagesOf(products);
        return new ProductSuggestionsDTO(
            products.stream().map(product -> toResponseDTO(product, images)).toList(), filled);
    }

    @Override
    @Transactional(readOnly = true)
    public HomeDTO home() {
        List<CategoryStats> stats = categoryRepository.findStatsByProductCount();
        List<List<Product>> sections = stats.stream()
            .map(category -> repository.findByCategoriesId(
                category.getId(), PageRequest.of(0, SECTION_SIZE, Sort.by("productName"))))
            .toList();
        List<Product> top = repository.findAll(PageRequest.of(0, TOP_SIZE, Sort.by("price").descending())).getContent();
        List<Product> budget = repository.findAll(PageRequest.of(0, SECTION_SIZE, Sort.by("price"))).getContent();

        List<Product> all = new ArrayList<>(top);
        all.addAll(budget);
        sections.forEach(all::addAll);
        Map<String, StoredImage> images = imagesOf(all);

        List<HomeSectionDTO> sectionDTOs = new ArrayList<>();
        for (int index = 0; index < stats.size(); index++) {
            CategoryStats category = stats.get(index);
            sectionDTOs.add(new HomeSectionDTO(
                category.getCategoryName(), category.getProductCount(), toResponseDTOs(sections.get(index), images)));
        }

        return new HomeDTO(sectionDTOs, toResponseDTOs(top, images), toResponseDTOs(budget, images));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponseDTO> findByIds(List<String> ids) {
        if (ids == null || ids.isEmpty()) return List.of();

        List<String> unique = ids.stream().filter(Objects::nonNull).distinct().limit(MAX_IDS).toList();
        List<Product> products = repository.findAllById(unique);
        return toResponseDTOs(products, imagesOf(products));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponseDTO> latestProducts() {
        List<Product> products = repository.findTop5ByOrderByCreatedAtDesc();
        Map<String, StoredImage> images = imagesOf(products);

        return products.stream().map(product -> toResponseDTO(product, images)).toList();
    }

    private ProductResponseDTO toResponseDTO(Product product, Map<String, StoredImage> images) {
        List<StoredImage> productImages = product.getImageIds().stream()
            .map(images::get)
            .filter(Objects::nonNull)
            .toList();

        return productMapper.productToResponseDTO(
            product, variantMapper.toVariantDTOs(product.getVariants()), productImages);
    }

    private List<ProductResponseDTO> toResponseDTOs(List<Product> products, Map<String, StoredImage> images) {
        return products.stream().map(product -> toResponseDTO(product, images)).toList();
    }

    private Map<String, StoredImage> imagesOf(List<Product> products) {
        List<String> ids = products.stream()
            .flatMap(product -> product.getImageIds().stream())
            .distinct()
            .toList();

        return mediaApi.findAll(ids).stream()
            .collect(Collectors.toMap(StoredImage::id, Function.identity(), (first, second) -> first));
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
