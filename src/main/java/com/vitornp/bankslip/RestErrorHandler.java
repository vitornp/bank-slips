package com.vitornp.bankslip;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.stream.Collectors;

import static com.google.common.base.CaseFormat.LOWER_CAMEL;
import static com.google.common.base.CaseFormat.LOWER_UNDERSCORE;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

@ControllerAdvice
public class RestErrorHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(UNPROCESSABLE_ENTITY)
    @ResponseBody
    public Error processValidationError(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .collect(Collectors.toMap(p-> toSnakeCase(p.getField()), FieldError::getDefaultMessage));

        return new Error(UNPROCESSABLE_ENTITY, fieldErrors);
    }

    private String toSnakeCase(String value) {
        return LOWER_CAMEL.to(LOWER_UNDERSCORE, value);
    }

    @Getter
    private static class Error {
        private final OffsetDateTime timestamp;
        private final int error;
        private final String status;
        private final Map<String, String> errors;

        Error(HttpStatus httpStatus, Map<String, String> errors) {
            this.timestamp = OffsetDateTime.now();
            this.error = httpStatus.value();
            this.status = httpStatus.getReasonPhrase();
            this.errors = errors;
        }
    }

}
