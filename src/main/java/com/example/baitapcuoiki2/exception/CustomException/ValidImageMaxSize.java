package com.example.baitapcuoiki2.exception.CustomException;

import com.example.baitapcuoiki2.exception.CustomValidator.ImageSizeValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = ImageSizeValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidImageMaxSize {
    // Thay vì message cứng, dùng key để hỗ trợ i18n
    String message() default "Vượt quá giới hạn cho phép";

    long maxSize() default 5 * 1024 * 1024;

    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
