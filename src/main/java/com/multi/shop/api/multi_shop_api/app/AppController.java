package com.multi.shop.api.multi_shop_api.app;

import com.multi.shop.api.multi_shop_api.catalog.api.CatalogApi;
import com.multi.shop.api.multi_shop_api.identity.api.IdentityApi;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/app")
public class AppController {
    private final IdentityApi identityApi;
    private final CatalogApi catalogApi;

    public AppController(IdentityApi identityApi, CatalogApi catalogApi) {
        this.identityApi = identityApi;
        this.catalogApi = catalogApi;
    }

    @GetMapping("/quantity")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Long> appStats() {
        Map<String, Long> appStats = new HashMap<>();

        appStats.put("users", identityApi.countUsers());
        appStats.put("products", catalogApi.countProducts());
        appStats.put("categories", catalogApi.countCategories());

        return appStats;
    }
}
