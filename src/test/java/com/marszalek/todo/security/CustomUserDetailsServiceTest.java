package com.marszalek.todo.security;

import com.marszalek.todo.model.User;
import com.marszalek.todo.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomUserDetailsService Tests")
class CustomUserDetailsServiceTest {

    private static final String TEST_USERNAME = "testuser";
    private static final String UNKNOWN_USERNAME = "unknown";

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @DisplayName("Should return UserDetails when user exists")
    void loadUserByUsername_shouldReturnUserDetails_whenUserExists() {
        // Given
        var user = User.builder()
                .id(1L)
                .username(TEST_USERNAME)
                .email("test@example.com")
                .password("encodedPassword")
                .build();
        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(user));

        // When
        var result = customUserDetailsService.loadUserByUsername(TEST_USERNAME);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo(TEST_USERNAME);
        verify(userRepository).findByUsername(TEST_USERNAME);
    }

    @Test
    @DisplayName("Should throw UsernameNotFoundException when user does not exist")
    void loadUserByUsername_shouldThrowUsernameNotFoundException_whenUserNotFound() {
        // Given
        when(userRepository.findByUsername(UNKNOWN_USERNAME)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername(UNKNOWN_USERNAME))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining(UNKNOWN_USERNAME);

        verify(userRepository).findByUsername(UNKNOWN_USERNAME);
    }
}
