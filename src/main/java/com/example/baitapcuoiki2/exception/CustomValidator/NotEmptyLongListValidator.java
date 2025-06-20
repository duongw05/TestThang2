package com.example.baitapcuoiki2.exception.CustomValidator;

import com.example.baitapcuoiki2.exception.CustomException.NotEmptyLongList;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;
import java.util.Objects;

public class NotEmptyLongListValidator implements ConstraintValidator<NotEmptyLongList, List<Long>> {
    @Override
    public boolean isValid(List<Long> value, ConstraintValidatorContext context) {
        return value != null && !value.isEmpty() && value.stream().allMatch(Objects::nonNull);
    }
}
