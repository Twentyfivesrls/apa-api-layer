package com.twentyfive.apaapilayer.converters;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
@WritingConverter
public class LocalDateToStringConverter implements Converter<LocalDate, String> {
    @Override
    public String convert(LocalDate source) {
        return source.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }
}
