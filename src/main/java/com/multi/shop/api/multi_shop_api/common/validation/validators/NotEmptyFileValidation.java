package com.multi.shop.api.multi_shop_api.common.validation.validators;

import com.multi.shop.api.multi_shop_api.common.validation.NotEmptyFile;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;

public class NotEmptyFileValidation implements ConstraintValidator<NotEmptyFile, Object> {
    @Override
    public boolean isValid(Object target, ConstraintValidatorContext context) {
        if (target == null) return false;

        if (target instanceof List<?> files) {
            return files.stream()
                .filter(Objects::nonNull)
                .map(MultipartFile.class::cast)
                .noneMatch(MultipartFile::isEmpty);
        };

        MultipartFile file = (MultipartFile) target;
        return !file.isEmpty();
    }
}
