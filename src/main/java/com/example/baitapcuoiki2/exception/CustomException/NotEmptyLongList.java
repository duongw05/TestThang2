package com.example.baitapcuoiki2.exception.CustomException;

import com.example.baitapcuoiki2.exception.CustomValidator.NotEmptyLongListValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NotEmptyLongListValidator.class)
public @interface NotEmptyLongList {
    String message() default "List must not be empty or contain null";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
