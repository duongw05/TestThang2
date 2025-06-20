package com.example.baitapcuoiki2.exception.CustomException;

import com.example.baitapcuoiki2.exception.CustomValidator.UniqueCategoryCodeValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = UniqueCategoryCodeValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface UniqueCategoryCode {
    String message() default "Mã danh mục đã bị trùng";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
