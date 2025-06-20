package com.example.baitapcuoiki2.exception.CustomException;

import com.example.baitapcuoiki2.exception.CustomValidator.ProductImagesValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ProductImagesValidator.class)
public @interface ValidProductImages {
    String message() default "Danh sách ảnh không được để trống";

    Class<?>[] groups() default {};
    long maxSize() default 5 * 1024 * 1024; // Mặc định: 5MB

    Class<? extends Payload>[] payload() default {};
}
