package com.example.baitapcuoiki2.exception.CustomValidator;

import com.example.baitapcuoiki2.dto.request.ProductSearchRequest;
import com.example.baitapcuoiki2.exception.CustomException.ProductValidateRange;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ProductDateRangeValidator implements ConstraintValidator<ProductValidateRange, ProductSearchRequest> {
    @Override
    public boolean isValid(ProductSearchRequest value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        if (value.getCreatedFrom() == null || value.getCreatedTo() == null) {
            return true;
        }

        return !value.getCreatedFrom().after(value.getCreatedTo());
    }

}
