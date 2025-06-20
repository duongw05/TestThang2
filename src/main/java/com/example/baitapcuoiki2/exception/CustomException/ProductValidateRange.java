package com.example.baitapcuoiki2.exception.CustomException;

import com.example.baitapcuoiki2.exception.CustomValidator.ProductDateRangeValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ProductDateRangeValidator.class)
public @interface ProductValidateRange {
    String message() default "validDateRange.invalid";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
