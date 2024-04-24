package com.twentyfive.apaapilayer.configurations;

import com.twentyfive.apaapilayer.converters.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import java.util.Arrays;

@Configuration
public class MongoConfig {

    @Bean
    public MongoCustomConversions customConversions() {
        return new MongoCustomConversions(Arrays.asList(
                new LocalDateToStringConverter(),
                new StringToLocalDateConverter(),
                new LocalTimeToStringConverter(),
                new StringToLocalTimeConverter(),
                new LocalDateTimeToStringConverter(),
                new StringToLocalDateTimeConverter()
        ));
    }
}

