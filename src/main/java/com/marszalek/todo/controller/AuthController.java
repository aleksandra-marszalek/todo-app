package com.marszalek.todo.controller;


import com.marszalek.todo.error.ErrorMessage;
import com.marszalek.todo.model.dto.AuthResponse;
import com.marszalek.todo.model.dto.LoginRequest;
import com.marszalek.todo.model.dto.RegisterRequest;
import com.marszalek.todo.service.JwtService;
import com.marszalek.todo.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * REST controller for authentication operations: user registration and login.
 *
 * <p>Successful responses include a signed JWT token that must be supplied as a
 * {@code Bearer} token in the {@code Authorization} header for all protected endpoints.</p>
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserService userService;

    /**
     * Registers a new user account and returns an authentication token.
     *
     * @param request the registration payload containing username, email, and password
     * @return {@code 201 Created} with an {@link AuthResponse} on success,
     *         or {@code 409 Conflict} if the username or email is already taken
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            var user = userService.register(request);
            var token = jwtService.generateToken(user.getUsername());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new AuthResponse(token, user.getUsername(), user.getEmail()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Authenticates a user with their credentials and returns a JWT token.
     *
     * @param request the login payload containing username and password
     * @return {@code 200 OK} with an {@link AuthResponse} on success,
     *         or {@code 401 Unauthorized} if the credentials are invalid
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
            var user = userService.findByUsername(request.getUsername());
            var token = jwtService.generateToken(user.getUsername());
            return ResponseEntity.ok(new AuthResponse(token, user.getUsername(), user.getEmail()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", ErrorMessage.INVALID_CREDENTIALS.getMessage()));
        }
    }
}
