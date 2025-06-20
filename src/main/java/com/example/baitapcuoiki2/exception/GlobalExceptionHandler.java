package com.example.baitapcuoiki2.exception;

import com.example.baitapcuoiki2.exception.CustomException.NotFoundException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @Autowired
    private MessageSource messageSource;

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Locale locale = LocaleContextHolder.getLocale();

        String errorDetails = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> {
                    String fieldName = fieldError.getField();
                    String errorMsg;

                    if (isListLongField(fieldName)) {
                        errorMsg = messageSource.getMessage(
                                "conversion.error.listLong",
                                new Object[] { fieldName, String.valueOf(fieldError.getRejectedValue()) },
                                "Invalid List<Long> format for field: " + fieldName,
                                locale
                        );
                    } else {
                        String messageKey = "typeMismatch".equals(fieldError.getCode()) ? "type.mismatch" : "validation.error";
                        errorMsg = messageSource.getMessage(
                                messageKey,
                                new Object[] { fieldName, String.valueOf(fieldError.getRejectedValue()) },
                                fieldError.getDefaultMessage(),
                                locale
                        );
                    }

                    return "Field: " + fieldName + " - Error: " + errorMsg;
                })
                .collect(Collectors.joining("; "));

        Map<String, String> errors = new HashMap<>();
        errors.put("message", errorDetails.isEmpty() ? messageSource.getMessage("validation.failed", null, locale) : errorDetails);

        ErrorResponse response = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                messageSource.getMessage("validation.failed", null, locale),
                new Date(),
                errors
        );

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    private boolean isListLongField(String fieldName) {
        return fieldName.equals("categoryIds") || fieldName.equals("categories") || fieldName.equals("anotherListField");
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParam(MissingServletRequestParameterException ex) {

        Locale locale = LocaleContextHolder.getLocale();
        String message = messageSource.getMessage("missing.parameter", new Object[]{ex.getParameterName()}, locale);

        Map<String, String> errors = new HashMap<>();
        errors.put("message", message);

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                message,
                new Date(),
                errors
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException ex) {

        Locale locale = LocaleContextHolder.getLocale();
        String errorDetails = ex.getConstraintViolations().stream()
                .map(violation -> {
                    String path = violation.getPropertyPath().toString();
                    String message = messageSource.getMessage(violation.getMessage(), null, locale);
                    return "Field: " + path + " - Error: " + message;
                })
                .collect(Collectors.joining("; "));

        Map<String, String> errors = new HashMap<>();
        errors.put("message", errorDetails.isEmpty() ? messageSource.getMessage("constraint.violation", null, locale) : errorDetails);

        ErrorResponse response = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                messageSource.getMessage("constraint.violation", null, locale),
                new Date(),
                errors
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleInvalidDate(HttpMessageNotReadableException ex) {

        System.out.println("chạy vào handleInvalidDate");

        Locale locale = LocaleContextHolder.getLocale();
        String errorKey = ex.getCause() instanceof InvalidFormatException ? "invalid.date.format" : "invalid.json";
        String message = messageSource.getMessage(errorKey, null, locale);

        Map<String, String> errors = new HashMap<>();
        errors.put("message", message);

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                message,
                new Date(),
                errors
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<ErrorResponse> handleAllExceptions(Exception ex) {
//
//        System.out.println("chạy vào handleAllExceptions");
//
//        Locale locale = LocaleContextHolder.getLocale();
//        String message = messageSource.getMessage("internal.server.error", null, locale);
//
//        Map<String, String> errors = new HashMap<>();
//        errors.put("message", ex.getMessage() != null ? ex.getMessage() : message);
//
//        ErrorResponse response = new ErrorResponse(
//                HttpStatus.INTERNAL_SERVER_ERROR.value(),
//                message,
//                new Date(),
//                errors
//        );
//        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
//    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFoundException(NotFoundException ex) {

        System.out.println("chạy vào handleNotFoundException");

        Locale locale = LocaleContextHolder.getLocale();
        String runtimeErrorMessage = messageSource.getMessage("runtime.error", null, locale);

        String detailedMessage = ex.getMessage();

        Map<String, String> errors = new HashMap<>();
        errors.put("message", detailedMessage);

        ErrorResponse response = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                runtimeErrorMessage,
                new Date(),
                errors
        );
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        System.out.println("chạy vào handleTypeMismatch");

        Locale locale = LocaleContextHolder.getLocale();
        String fieldName = ex.getName() != null ? ex.getName() : "unknown";
        String message = messageSource.getMessage("runtime.error", null, locale);
        String errorDetails = "Field: " + fieldName + " - Error: " +
                messageSource.getMessage("invalid.param.value", new Object[]{fieldName}, locale);

        Map<String, String> errors = new HashMap<>();
        errors.put("message", errorDetails);

        ErrorResponse response = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                message,
                new Date(),
                errors
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBindException(BindException ex) {

        System.out.println("chạy vào handleBindException");

        Locale locale = LocaleContextHolder.getLocale();
        String errorDetails = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> {
                    String fieldName = fieldError.getField();
                    String errorMsg = messageSource.getMessage(fieldError.getDefaultMessage(), null, locale);
                    return "Field: " + fieldName + " - Error: " + errorMsg;
                })
                .collect(Collectors.joining("; "));

        Map<String, String> errors = new HashMap<>();
        errors.put("message", errorDetails.isEmpty() ? messageSource.getMessage("invalid.pagination", null, locale) : errorDetails);

        ErrorResponse response = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                messageSource.getMessage("invalid.pagination", null, locale),
                new Date(),
                errors
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex) {

        System.out.println("chạy vào handleRuntimeException");

        Locale locale = LocaleContextHolder.getLocale();
        String errorDetails;

        if (ex.getCause() instanceof MethodArgumentNotValidException validationEx) {
            errorDetails = validationEx.getBindingResult().getFieldErrors().stream()
                    .map(fieldError -> {
                        String fieldName = fieldError.getField();
                        String errorMsg = messageSource.getMessage(fieldError.getDefaultMessage(), null, locale);
                        return "Field: " + fieldName + " - Error: " + errorMsg;
                    })
                    .collect(Collectors.joining("; "));
        } else {
            errorDetails = ex.getMessage() != null ? ex.getMessage() : messageSource.getMessage("runtime.error", null, locale);
            System.out.println("Lỗi runtime: " + errorDetails);
        }

        Map<String, String> errors = new HashMap<>();
        errors.put("message", errorDetails);

        ErrorResponse response = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                messageSource.getMessage("runtime.error", null, locale),
                new Date(),
                errors
        );
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }


}