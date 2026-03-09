package com.marszalek.todo.service;

import com.marszalek.todo.error.ErrorMessage;
import com.marszalek.todo.model.User;
import com.marszalek.todo.model.dto.RegisterRequest;
import com.marszalek.todo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

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

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException(ErrorMessage.USER_NOT_FOUND.getMessage()));
    }
}
