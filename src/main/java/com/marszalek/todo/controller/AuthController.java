package com.marszalek.todo.controller;


import com.marszalek.todo.error.ErrorMessage;
import com.marszalek.todo.model.dto.AuthResponse;
import com.marszalek.todo.model.dto.LoginRequest;
import com.marszalek.todo.model.dto.RegisterRequest;
import com.marszalek.todo.security.JwtUtil;
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

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            var user = userService.register(request);

            var token = jwtUtil.generateToken(user.getUsername());

            var response = new AuthResponse(token, user.getUsername(), user.getEmail());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            var user = userService.findByUsername(request.getUsername());
            var token = jwtUtil.generateToken(user.getUsername());

            var response = new AuthResponse(token, user.getUsername(), user.getEmail());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", ErrorMessage.INVALID_CREDENTIALS.getMessage()));
        }
    }
}
