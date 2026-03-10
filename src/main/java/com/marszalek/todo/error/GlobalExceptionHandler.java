package com.marszalek.todo.error;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;

/**
 * Global exception handler that translates application exceptions into structured
 * {@link ErrorResponse} payloads.
 *
 * <p>Annotated with {@link Hidden} to exclude it from the OpenAPI documentation,
 * as error shapes are documented on individual endpoint operations.</p>
 */
@Hidden
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles bean validation failures triggered by {@code @Valid} on controller method parameters.
     *
     * <p>Each field error is included in the response as a {@code "field: message"} string.</p>
     *
     * @param ex      the validation exception containing individual field errors
     * @param request the current web request, used to populate the error path
     * @return {@code 400 Bad Request} with a structured {@link ErrorResponse}
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex, WebRequest request) {

        var errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();

        var errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.name(),
                "Validation failed",
                request.getDescription(false).replace("uri=", ""),
                errors
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
}
