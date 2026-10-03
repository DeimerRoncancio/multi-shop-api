package com.multi.shop.catalog.repositories;

import com.multi.shop.catalog.entities.Variant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VariantRepository extends JpaRepository<Variant, String> {
    List<Variant> findByNameIn(List<String> variants);
}
