package com.marszalek.todo.util;

import com.marszalek.todo.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
@DisplayName("SecurityUtil Tests")
class SecurityUtilTest {

    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_EMAIL = "test@example.com";

    @InjectMocks
    private SecurityUtil securityUtil;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username(TEST_USERNAME)
                .email(TEST_EMAIL)
                .build();
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should return the authenticated user from security context")
    void getCurrentUser_shouldReturnUser_whenAuthenticated() {
        // Given
        var auth = new UsernamePasswordAuthenticationToken(testUser, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);

        // When
        var result = securityUtil.getCurrentUser();

        // Then
        assertThat(result).isEqualTo(testUser);
        assertThat(result.getUsername()).isEqualTo(TEST_USERNAME);
    }

    @Test
    @DisplayName("Should throw NullPointerException when security context has no authentication")
    void getCurrentUser_shouldThrow_whenNoAuthenticationPresent() {
        // Given — context is empty (cleared in setUp)

        // When / Then
        assertThatThrownBy(() -> securityUtil.getCurrentUser())
                .isInstanceOf(NullPointerException.class);
    }
}
