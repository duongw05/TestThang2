package com.example.baitapcuoiki2.utils;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class StatusConverter implements AttributeConverter<Status, Integer> {
    @Override
    public Integer convertToDatabaseColumn(Status status) {
        return status != null ? status.getValue() : null;
    }

    @Override
    public Status convertToEntityAttribute(Integer integer) {
        return integer != null ? Status.fromValue(integer) : null;
    }
}
