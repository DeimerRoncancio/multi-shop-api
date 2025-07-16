package com.multi.shop.api.multi_shop_api.common.validation.validators;

import com.multi.shop.api.multi_shop_api.common.validation.NotEmptyFile;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public class NotEmptyFileValidation implements ConstraintValidator<NotEmptyFile, Object> {
    @Override
    public boolean isValid(Object target, ConstraintValidatorContext context) {
        if (target == null) return false;

        if (target instanceof List<?> files) {
            return files.stream()
                .map(item -> (MultipartFile) item)
                .noneMatch(MultipartFile::isEmpty);
        };

        MultipartFile file = (MultipartFile) target;
        return !file.isEmpty();
    }
}
