package com.example.baitapcuoiki2.exception.CustomException;

import com.example.baitapcuoiki2.exception.CustomValidator.CategoryImagesValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CategoryImagesValidator.class)
public @interface ValidCategoryImages {
    String message() default "Danh sách ảnh không được để trống";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
