package com.marszalek.todo.error;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Standardised error response body returned by the API when a request fails.
 *
 * @param timestamp the date and time the error occurred
 * @param status    the HTTP status code
 * @param error     the HTTP status name (e.g. {@code BAD_REQUEST})
 * @param message   a human-readable summary of the error
 * @param path      the request path that triggered the error
 * @param errors    a list of individual field validation errors, or an empty list when not applicable
 */
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        List<String> errors) {
}
