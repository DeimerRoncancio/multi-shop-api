package com.multi.shop.api.multi_shop_api.common.validation.validators;

import com.multi.shop.api.multi_shop_api.common.validation.ImageFormat;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;

public class ImageFormatValidation implements ConstraintValidator<ImageFormat, Object> {
    private int maxSize;

    @Override
    public void initialize(ImageFormat constraintAnnotation) {
        this.maxSize = constraintAnnotation.maxSize();
    }

    @Override
    public boolean isValid(Object target, ConstraintValidatorContext context) {
        if (target == null) return false;

        if (target instanceof List<?> list) {
            List<MultipartFile> files = list.stream()
                .map(item -> (MultipartFile) item)
                .toList();

            boolean sizeValid = files.stream().anyMatch(file -> file.getSize() > maxSize);

            if (sizeValid) {
                buildMessage(
                    builderMessage("El tamaño de la imagen debe ser menor a %.2f MB", maxSize),
                    context
                );
                return false;
            };

            return files.stream()
                .allMatch(file ->
                    Objects.requireNonNull(file.getContentType()).startsWith("image/")
                );
        }

        MultipartFile file = (MultipartFile) target;
        boolean sizeValid = file.getSize() > maxSize;

        if (sizeValid) {
            buildMessage(
                builderMessage("El tamaño de la imagen debe ser menor a %.2f MB", maxSize),
                context
            );
            return false;
        }

        return Objects.requireNonNull(file.getContentType()).startsWith("image/");
    }

    public void buildMessage(String message, ConstraintValidatorContext context) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message)
            .addConstraintViolation();
    }

    public String builderMessage(String message, int size) {
        return String.format(
            message, size / 1024.0 / 1024.0
        );
    }
}
