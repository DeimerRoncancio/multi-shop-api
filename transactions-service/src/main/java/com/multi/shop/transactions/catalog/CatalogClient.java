package com.multi.shop.transactions.catalog;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "catalog", url = "${catalog.service.url}")
public interface CatalogClient {
    @GetMapping("/internal/products/{id}")
    CatalogProduct findProduct(@PathVariable("id") String id);

    @GetMapping("/internal/products")
    List<CatalogProduct> findProducts(@RequestParam("ids") List<String> ids);
}
