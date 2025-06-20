package com.example.baitapcuoiki2.validations;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Set;

public class ValidatorUtil {
    public static <T> void validateRequest(T request, Validator validator, MessageSource messageSource) {
        Set<ConstraintViolation<T>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (ConstraintViolation<T> violation : violations) {
                String message = messageSource.getMessage(
                        violation.getMessageTemplate(),
                        null,
                        violation.getMessage(),
                        LocaleContextHolder.getLocale()
                );
                sb.append(message).append("; ");
            }
            String errorMessage = sb.toString();
            if (errorMessage.endsWith("; ")) {
                errorMessage = errorMessage.substring(0, errorMessage.length() - 2);
            }
            throw new IllegalArgumentException(errorMessage);
        }
    }
}
