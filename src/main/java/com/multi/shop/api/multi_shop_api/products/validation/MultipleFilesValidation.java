package com.multi.shop.api.multi_shop_api.products.validation;

import java.util.List;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.multi.shop.api.multi_shop_api.products.entities.Product;
import org.springframework.web.multipart.MultipartFile;

@Component
public class MultipleFilesValidation implements Validator {
    @Override
    public boolean supports(@NonNull Class<?> clazz) {
        return Product.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(@NonNull Object target, @NonNull Errors errors) {
        String field = "productImages";

        if (target == null) {
            errors.rejectValue(field, "", "debe tener minimo una imagen");
            return;
        }

        List<MultipartFile> files = ((List<?>) target)
            .stream()
            .map(item -> (MultipartFile) item)
            .toList();

        if (files.stream().allMatch(MultipartFile::isEmpty))
            errors.rejectValue(field, "", "tiene elementos vacíos");
        else if (files.stream().anyMatch(file -> !file.getContentType().startsWith("image/")))
            errors.rejectValue(field, "", "tiene elementos que no son imágenes");
    }
}
