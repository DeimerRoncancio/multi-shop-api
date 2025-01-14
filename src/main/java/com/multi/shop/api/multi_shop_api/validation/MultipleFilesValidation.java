package com.multi.shop.api.multi_shop_api.validation;

import java.util.List;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.multipart.MultipartFile;

import com.multi.shop.api.multi_shop_api.entities.Product;

@Component
public class MultipleFilesValidation implements Validator {

    @Override
    public boolean supports(@NonNull Class<?> clazz) {
        return Product.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(@NonNull Object target, @NonNull Errors errors) {
        List<MultipartFile> files = ((List<?>) target).stream()
            .filter(item -> item instanceof MultipartFile)
            .map(item -> (MultipartFile) item)
            .toList();
        
        if (!files.stream().anyMatch(file -> !file.isEmpty() && file.getSize() > 0))
            errors.rejectValue("productImages", "", "debe tener minimo una imagen");
    }
}
