package com.marszalek.todo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * REST controller providing a simple liveness check endpoint.
 */
@RestController
@RequestMapping("/api")
public class HealthController {

    /**
     * Returns the current application status and server timestamp.
     *
     * @return a map containing {@code status}, {@code timestamp}, and {@code message} entries
     */
    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of(
                "status", "UP",
                "timestamp", LocalDateTime.now(),
                "message", "Todo API is running!"
        );
    }
}
