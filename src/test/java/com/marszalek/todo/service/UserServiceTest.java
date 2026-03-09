package com.marszalek.todo.service;

import com.marszalek.todo.error.ErrorMessage;
import com.marszalek.todo.model.User;
import com.marszalek.todo.model.dto.RegisterRequest;
import com.marszalek.todo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Tests")
class UserServiceTest {

    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PASSWORD = "password123";
    private static final String ENCODED_PASSWORD = "encodedPassword";
    private static final Long TEST_USER_ID = 1L;

    private static final String USERNAME_TAKEN_ERROR = ErrorMessage.USERNAME_ALREADY_TAKEN.getMessage();
    private static final String EMAIL_TAKEN_ERROR = ErrorMessage.EMAIL_ALREADY_REGISTERED.getMessage();
    private static final String USER_NOT_FOUND_ERROR = ErrorMessage.USER_NOT_FOUND.getMessage();

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private RegisterRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = new RegisterRequest();
        validRequest.setUsername(TEST_USERNAME);
        validRequest.setEmail(TEST_EMAIL);
        validRequest.setPassword(TEST_PASSWORD);
    }

    @Test
    @DisplayName("Should register new user successfully")
    void shouldRegisterNewUserSuccessfully() {
        // Given
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn(ENCODED_PASSWORD);

        var savedUser = buildUser(TEST_USER_ID, TEST_USERNAME, TEST_EMAIL, ENCODED_PASSWORD);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // When
        var result = userService.register(validRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo(TEST_USERNAME);
        assertThat(result.getEmail()).isEqualTo(TEST_EMAIL);
        assertThat(result.getPassword()).isEqualTo(ENCODED_PASSWORD);

        verify(userRepository).existsByUsername(TEST_USERNAME);
        verify(userRepository).existsByEmail(TEST_EMAIL);
        verify(passwordEncoder).encode(TEST_PASSWORD);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when username already exists")
    void shouldThrowExceptionWhenUsernameExists() {
        // Given
        when(userRepository.existsByUsername(TEST_USERNAME)).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> userService.register(validRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(USERNAME_TAKEN_ERROR);

        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    @DisplayName("Should throw exception when email already exists")
    void shouldThrowExceptionWhenEmailExists() {
        // Given
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(TEST_EMAIL)).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> userService.register(validRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(EMAIL_TAKEN_ERROR);

        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    @DisplayName("Should hash password before saving")
    void shouldHashPasswordBeforeSaving() {
        // Given
        var hashedPassword = "hashedPassword123";
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(TEST_PASSWORD)).thenReturn(hashedPassword);

        var savedUser = buildUser(TEST_USER_ID, TEST_USERNAME, TEST_EMAIL, hashedPassword);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // When
        var result = userService.register(validRequest);

        // Then
        assertThat(result.getPassword()).isEqualTo(hashedPassword);
        assertThat(result.getPassword()).isNotEqualTo(TEST_PASSWORD);

        verify(passwordEncoder).encode(TEST_PASSWORD);
    }

    @Test
    @DisplayName("Should find user by username successfully")
    void shouldFindUserByUsernameSuccessfully() {
        // Given
        var existingUser = buildUser(TEST_USER_ID, TEST_USERNAME, TEST_EMAIL, ENCODED_PASSWORD);
        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(existingUser));

        // When
        var result = userService.findByUsername(TEST_USERNAME);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo(TEST_USERNAME);
        assertThat(result.getEmail()).isEqualTo(TEST_EMAIL);

        verify(userRepository).findByUsername(TEST_USERNAME);
    }

    @Test
    @DisplayName("Should throw exception when user not found by username")
    void shouldThrowExceptionWhenUserNotFound() {
        // Given
        var nonexistentUsername = "nonexistent";
        when(userRepository.findByUsername(nonexistentUsername)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.findByUsername(nonexistentUsername))
                .isInstanceOf(RuntimeException.class)
                .hasMessage(USER_NOT_FOUND_ERROR);

        verify(userRepository).findByUsername(nonexistentUsername);
    }

    @Test
    @DisplayName("Should check username existence before checking email")
    void shouldCheckUsernameBeforeEmail() {
        // Given
        when(userRepository.existsByUsername(TEST_USERNAME)).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> userService.register(validRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(USERNAME_TAKEN_ERROR);

        verify(userRepository).existsByUsername(TEST_USERNAME);
        verify(userRepository, never()).existsByEmail(anyString());
    }

    @Test
    @DisplayName("Should save user with correct data from request")
    void shouldSaveUserWithCorrectData() {
        // Given
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn(ENCODED_PASSWORD);

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            var user = (User) invocation.getArgument(0);
            user.setId(TEST_USER_ID);
            return user;
        });

        // When
        userService.register(validRequest);

        // Then
        verify(userRepository).save(argThat(user ->
                user.getUsername().equals(TEST_USERNAME) &&
                        user.getEmail().equals(TEST_EMAIL) &&
                        user.getPassword().equals(ENCODED_PASSWORD)
        ));
    }

    private User buildUser(Long id, String username, String email, String password) {
        return User.builder()
                .id(id)
                .username(username)
                .email(email)
                .password(password)
                .build();
    }
}