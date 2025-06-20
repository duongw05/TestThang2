package com.example.baitapcuoiki2.exception.CustomValidator;

import com.example.baitapcuoiki2.exception.CustomException.UniqueCategoryCode;
import com.example.baitapcuoiki2.repository.CategoryRepository;
import com.example.baitapcuoiki2.utils.Status;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UniqueCategoryCodeValidator implements ConstraintValidator<UniqueCategoryCode, String> {
    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    public boolean isValid(String categoryCode, ConstraintValidatorContext context) {
        if (categoryCode == null || categoryCode.trim().isEmpty()) {
            return true;
        }

        if (categoryRepository.existsByCategoryCodeAndStatus(categoryCode, Status.ACTIVE)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Mã danh mục đã bị trùng")
                    .addConstraintViolation();
            return false;
        }
        return true;
    }
}
