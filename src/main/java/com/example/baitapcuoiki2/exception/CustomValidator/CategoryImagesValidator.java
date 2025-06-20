package com.example.baitapcuoiki2.exception.CustomValidator;

import com.example.baitapcuoiki2.exception.CustomException.ValidCategoryImages;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public class CategoryImagesValidator implements ConstraintValidator<ValidCategoryImages, List<MultipartFile>> {
    @Override
    public boolean isValid(List<MultipartFile> value, ConstraintValidatorContext context) {
        return value != null && !value.isEmpty() && value.stream().allMatch(file -> !file.isEmpty());
    }
}
