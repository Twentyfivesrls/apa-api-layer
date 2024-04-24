package com.twentyfive.apaapilayer.converters;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
@WritingConverter
public class LocalTimeToStringConverter implements Converter<LocalTime, String> {
    @Override
    public String convert(LocalTime source) {
        return source.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }
}
