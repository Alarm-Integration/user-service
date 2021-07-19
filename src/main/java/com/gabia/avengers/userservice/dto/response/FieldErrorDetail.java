package com.gabia.avengers.userservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.MessageSource;
import org.springframework.validation.FieldError;

import java.util.Locale;

@Setter
@Getter
@AllArgsConstructor
public class FieldErrorDetail {
    private String objectName;
    private String field;
    private String code;
    private String message;

    public static FieldErrorDetail create(FieldError fieldError, MessageSource messageSource, Locale locale) {
        return new FieldErrorDetail(
                fieldError.getObjectName(),
                fieldError.getField(),
                fieldError.getCode(),
                messageSource.getMessage(fieldError, locale));
    }
}
