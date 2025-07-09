package com.multi.shop.api.multi_shop_api.common.validation;

import java.util.List;
import java.util.Objects;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.multipart.MultipartFile;

import com.multi.shop.api.multi_shop_api.images.entities.Image;

@Component
public class FileSizeValidation implements Validator {
    @Override
    public boolean supports(@NonNull Class<?> clazz) {
        return Image.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(@NonNull Object target, @NonNull Errors errors) {
        List<Object> list = ((List<?>) target).stream()
            .filter(Objects::nonNull)
            .map(item -> (Object) item).toList();

        MultipartFile file = (MultipartFile) list.get(1);
        String field = (String) list.get(0);
        String defaultMessage = "tiene un error. ";

        if (field.equals("imageUser") && !file.isEmpty() && file.getSize() > 1000000) {
            defaultMessage += "El tamaño de la imagen debe ser menor a 1MB";
            errors.rejectValue(field, "", defaultMessage);
        } else if (field.equals("productImages") && !file.isEmpty() && file.getSize() > 3000000) {
            defaultMessage += "El tamaño minímo de las imágenes debe ser 3MB";
            errors.rejectValue(field, "", defaultMessage);
        }
    }
}
