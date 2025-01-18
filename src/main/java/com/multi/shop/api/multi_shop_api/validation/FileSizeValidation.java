package com.multi.shop.api.multi_shop_api.validation;

import java.util.List;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.multipart.MultipartFile;

import com.multi.shop.api.multi_shop_api.entities.Image;

@Component
public class FileSizeValidation implements Validator {

    @Override
    public boolean supports(@NonNull Class<?> clazz) {
        return Image.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(@NonNull Object target, @NonNull Errors errors) {
        List<Object> list = ((List<?>) target).stream()
            .filter(item -> item instanceof Object)
            .map(item -> (Object) item).toList();

        MultipartFile file = (MultipartFile) list.get(1);
        String field = (String) list.get(0);

        if (field.equals("imageUser") && !file.isEmpty() && file.getSize() > 1000000) {
            errors.rejectValue(field, "", "tiene un error. El tamaño de la imagen debe ser menor a 1M");
        } else if (field.equals("productImages") && !file.isEmpty() && file.getSize() > 3000000) {
            errors.rejectValue(field, "", "tiene un error. El tamaño minimo de las imagenes debe ser 5M");
        }
    }
}
