package com.multi.shop.api.multi_shop_api.common.controllers;

import com.multi.shop.api.multi_shop_api.products.services.ProductCategoryService;
import com.multi.shop.api.multi_shop_api.products.services.ProductService;
import com.multi.shop.api.multi_shop_api.users.services.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/app")
@CrossOrigin(originPatterns = "*")
public class AppController {
    public final UserService userService;
    public final ProductService productService;
    public final ProductCategoryService categoryService;

    public AppController(UserService userService, ProductService productService, ProductCategoryService categoryService) {
        this.userService = userService;
        this.productService = productService;
        this.categoryService = categoryService;
    }

    @GetMapping("/quantity")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Integer> appStats() {
        Map<String, Integer> appStats = new HashMap<>();

        appStats.put("users", userService.usersSize());
        appStats.put("products", productService.productsSize());
        appStats.put("categories", categoryService.categoriesSize());

        return appStats;
    }
}
