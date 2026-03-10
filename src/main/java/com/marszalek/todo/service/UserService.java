package com.marszalek.todo.service;

import com.marszalek.todo.error.ErrorMessage;
import com.marszalek.todo.model.User;
import com.marszalek.todo.model.dto.RegisterRequest;
import com.marszalek.todo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for user account management including registration and retrieval.
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Registers a new user from the given request.
     *
     * <p>Validates that neither the username nor the email is already in use before
     * persisting the account. The password is stored as a BCrypt hash.</p>
     *
     * @param request the registration payload containing username, email, and plaintext password
     * @return the newly created and persisted {@link User}
     * @throws IllegalArgumentException if the username or email is already registered
     */
    @Transactional
    public User register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException(ErrorMessage.USERNAME_ALREADY_TAKEN.getMessage());
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException(ErrorMessage.EMAIL_ALREADY_REGISTERED.getMessage());
        }

        var user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        return userRepository.save(user);
    }

    /**
     * Retrieves a user by their username.
     *
     * @param username the username to look up
     * @return the matching {@link User}
     * @throws RuntimeException if no user with the given username exists
     */
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException(ErrorMessage.USER_NOT_FOUND.getMessage()));
    }
}
