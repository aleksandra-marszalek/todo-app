package com.marszalek.todo.service;

import com.marszalek.todo.security.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Service responsible for JWT token operations, including token generation,
 * validation, claim extraction, and Bearer token parsing from HTTP requests.
 *
 * <p>This service acts as the primary facade for JWT-related operations across
 * the application, delegating low-level token parsing to {@link JwtUtil} while
 * providing higher-level utilities for the authentication layer.</p>
 *
 * <p>Components such as controllers and security filters should depend on this
 * service rather than on {@link JwtUtil} directly.</p>
 */
@Service
@RequiredArgsConstructor
public class JwtService {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtUtil jwtUtil;

    /**
     * Extracts the JWT Bearer token from the HTTP request's Authorization header.
     *
     * <p>Returns an empty {@link Optional} if the header is absent or does not
     * conform to the {@code Bearer <token>} format.</p>
     *
     * @param request the incoming HTTP request
     * @return an {@link Optional} containing the raw token string if a valid
     *         Bearer header is present, or {@link Optional#empty()} otherwise
     */
    public Optional<String> extractBearerToken(HttpServletRequest request) {
        String authHeader = request.getHeader(AUTHORIZATION_HEADER);
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            return Optional.empty();
        }
        return Optional.of(authHeader.substring(BEARER_PREFIX.length()));
    }

    /**
     * Extracts the username (subject claim) from the given JWT token.
     *
     * @param token the JWT token string
     * @return the username encoded as the token's subject
     */
    public String extractUsername(String token) {
        return jwtUtil.extractUsername(token);
    }

    /**
     * Validates a JWT token against the provided user details.
     *
     * <p>A token is considered valid if its subject matches the username in
     * {@code userDetails} and the token has not yet expired.</p>
     *
     * @param token       the JWT token to validate
     * @param userDetails the user details to validate against
     * @return {@code true} if the token is valid; {@code false} otherwise
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        return jwtUtil.validateToken(token, userDetails);
    }

    /**
     * Generates a new signed JWT token for the given username.
     *
     * @param username the username to encode as the token's subject
     * @return a signed JWT token string
     */
    public String generateToken(String username) {
        return jwtUtil.generateToken(username);
    }
}
