package com.example.baitapcuoiki2.exception.CustomValidator;

import com.example.baitapcuoiki2.exception.CustomException.ValidImageMaxSize;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public class ImageSizeValidator implements ConstraintValidator<ValidImageMaxSize, List<MultipartFile>> {
    private long maxSize;

    @Override
    public void initialize(ValidImageMaxSize constraintAnnotation) {
        this.maxSize = constraintAnnotation.maxSize();
    }

    @Override
    public boolean isValid(List<MultipartFile> files, ConstraintValidatorContext context) {
        if (files == null || files.isEmpty()) {
            return true;
        }

        for (MultipartFile file : files) {
            if (file != null && !file.isEmpty() && file.getSize() > maxSize) {
                // Tắt default message, set message mới với tham số
                context.disableDefaultConstraintViolation();

                context.buildConstraintViolationWithTemplate(
                        context.getDefaultConstraintMessageTemplate().replace("{0}", String.valueOf(maxSize / (1024 * 1024)))
                ).addConstraintViolation();
                return false;
            }
        }

        return true;
    }
}
