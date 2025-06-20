package com.example.baitapcuoiki2.exception.CustomValidator;

import com.example.baitapcuoiki2.exception.CustomException.UniqueProductCode;
import com.example.baitapcuoiki2.repository.ProductRepository;
import com.example.baitapcuoiki2.utils.Status;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UniqueProductCodeValidator implements ConstraintValidator<UniqueProductCode, String> {

    @Autowired
    private ProductRepository productRepository;

    @Override
    public void initialize(UniqueProductCode constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        if (s == null || s.trim().isEmpty()) {
            return true; // Không kiểm tra nếu null hoặc rỗng
        }

        System.out.println("check: "+productRepository.existsActiveProductCode(s, Status.ACTIVE));

        System.out.println("CHECK UNIQUE: " + s);
        return !productRepository.existsActiveProductCode(s,Status.ACTIVE);
    }
}
