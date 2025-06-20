package com.example.baitapcuoiki2.exception.CustomValidator;

import com.example.baitapcuoiki2.dto.request.CategorySearchRequest;
import com.example.baitapcuoiki2.exception.CustomException.ValidDateRange;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CategoryDateRangeValidator implements ConstraintValidator<ValidDateRange, CategorySearchRequest> {
    @Override
    public boolean isValid(CategorySearchRequest value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        if (value.getCreatedFrom() == null || value.getCreatedTo() == null) {
            return true;
        }

        return !value.getCreatedFrom().after(value.getCreatedTo());
    }
}
