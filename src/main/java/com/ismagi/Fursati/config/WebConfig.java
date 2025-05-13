package com.ismagi.Fursati.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new StringToLocalDateConverter());
    }

    private static class StringToLocalDateConverter implements Converter<String, LocalDate> {
        
        @Override
        public LocalDate convert(String source) {
            if (source == null || source.isEmpty()) {
                return null;
            }
            
            // Try to parse with yyyy-MM format (append "-01" for day)
            try {
                if (source.matches("\\d{4}-\\d{2}")) {
                    return LocalDate.parse(source + "-01");
                }
            } catch (DateTimeParseException e) {
                // Fall back to standard parsing below
            }
            
            // Try standard ISO date format
            try {
                return LocalDate.parse(source);
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Invalid date format. Use yyyy-MM or yyyy-MM-dd.");
            }
        }
    }
}