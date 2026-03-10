package com.marszalek.todo.service;

import com.marszalek.todo.security.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtService Tests")
class JwtServiceTest {

    private static final String TEST_TOKEN = "header.payload.signature";
    private static final String TEST_USERNAME = "testuser";
    private static final String BEARER_TOKEN = "Bearer " + TEST_TOKEN;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private HttpServletRequest request;

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private JwtService jwtService;

    @Test
    @DisplayName("Should extract Bearer token from Authorization header")
    void extractBearerToken_shouldReturnToken_whenBearerHeaderPresent() {
        // Given
        when(request.getHeader("Authorization")).thenReturn(BEARER_TOKEN);

        // When
        Optional<String> result = jwtService.extractBearerToken(request);

        // Then
        assertThat(result).isPresent().contains(TEST_TOKEN);
    }

    @Test
    @DisplayName("Should return empty when Authorization header is absent")
    void extractBearerToken_shouldReturnEmpty_whenHeaderAbsent() {
        // Given
        when(request.getHeader("Authorization")).thenReturn(null);

        // When
        Optional<String> result = jwtService.extractBearerToken(request);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should return empty when Authorization header does not start with Bearer")
    void extractBearerToken_shouldReturnEmpty_whenNotBearerScheme() {
        // Given
        when(request.getHeader("Authorization")).thenReturn("Basic dXNlcjpwYXNz");

        // When
        Optional<String> result = jwtService.extractBearerToken(request);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should return empty when Authorization header is only the Bearer prefix")
    void extractBearerToken_shouldReturnEmptyString_whenOnlyBearerPrefix() {
        // Given
        when(request.getHeader("Authorization")).thenReturn("Bearer ");

        // When
        Optional<String> result = jwtService.extractBearerToken(request);

        // Then
        assertThat(result).isPresent().contains("");
    }

    @Test
    @DisplayName("Should delegate extractUsername to JwtUtil")
    void extractUsername_shouldDelegateToJwtUtil() {
        // Given
        when(jwtUtil.extractUsername(TEST_TOKEN)).thenReturn(TEST_USERNAME);

        // When
        String result = jwtService.extractUsername(TEST_TOKEN);

        // Then
        assertThat(result).isEqualTo(TEST_USERNAME);
        verify(jwtUtil).extractUsername(TEST_TOKEN);
    }

    @Test
    @DisplayName("Should return true when token is valid")
    void validateToken_shouldReturnTrue_whenTokenIsValid() {
        // Given
        when(jwtUtil.validateToken(TEST_TOKEN, userDetails)).thenReturn(true);

        // When
        boolean result = jwtService.validateToken(TEST_TOKEN, userDetails);

        // Then
        assertThat(result).isTrue();
        verify(jwtUtil).validateToken(TEST_TOKEN, userDetails);
    }

    @Test
    @DisplayName("Should return false when token is invalid")
    void validateToken_shouldReturnFalse_whenTokenIsInvalid() {
        // Given
        when(jwtUtil.validateToken(TEST_TOKEN, userDetails)).thenReturn(false);

        // When
        boolean result = jwtService.validateToken(TEST_TOKEN, userDetails);

        // Then
        assertThat(result).isFalse();
        verify(jwtUtil).validateToken(TEST_TOKEN, userDetails);
    }

    @Test
    @DisplayName("Should delegate generateToken to JwtUtil")
    void generateToken_shouldDelegateToJwtUtil() {
        // Given
        when(jwtUtil.generateToken(TEST_USERNAME)).thenReturn(TEST_TOKEN);

        // When
        String result = jwtService.generateToken(TEST_USERNAME);

        // Then
        assertThat(result).isEqualTo(TEST_TOKEN);
        verify(jwtUtil).generateToken(TEST_USERNAME);
    }
}
