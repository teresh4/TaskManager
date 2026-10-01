package com.example.task.exception;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DatabindException;
import tools.jackson.databind.exc.InvalidFormatException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(
            MethodArgumentNotValidException exception
    ) {
        Map<String, String> fields = new LinkedHashMap<>();
        for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
            fields.merge(fieldError.getField(), fieldError.getDefaultMessage(),
                    (first, next) -> first.equals(next) ? first : first + "; " + next);
        }
        return error(HttpStatus.BAD_REQUEST, "Validation failed", fields);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleUnreadableRequest(
        HttpMessageNotReadableException exception
    ) {
        Throwable cause = exception;
        while (cause != null && !(cause instanceof DatabindException)) {
            cause = cause.getCause();
        }
        if (cause instanceof DatabindException databindException) {
            String field = databindException.getPath().stream()
                    .map(JacksonException.Reference::getPropertyName)
                    .filter(Objects::nonNull)
                    .reduce((first, next) -> next)
                    .orElse("request");
            String message = "Недопустимое значение";
            if (databindException instanceof InvalidFormatException invalidFormat
                    && invalidFormat.getTargetType().isEnum()) {
                String allowedValues = Arrays.stream(invalidFormat.getTargetType().getEnumConstants())
                        .map(String::valueOf)
                        .collect(Collectors.joining(", "));
                message = "Допустимые значения: " + allowedValues;
            }
            return error(HttpStatus.BAD_REQUEST, "Validation failed", Map.of(field, message));
        }
        return error(HttpStatus.BAD_REQUEST, "Validation failed",
                Map.of("request", "Некорректное тело запроса"));
    }

    @ExceptionHandler(TaskNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleTaskNotFound(TaskNotFoundException exception) {
        return error(HttpStatus.NOT_FOUND, exception.getMessage(), Map.of());
    }

    private static ResponseEntity<ApiErrorResponse> error(
            HttpStatus status,
            String message,
            Map<String, String> fields
    ) {
        return ResponseEntity.status(status).body(new ApiErrorResponse(
                LocalDateTime.now(),
                status.value(),
                message,
                fields
        ));
    }
}
