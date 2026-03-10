package com.marszalek.todo.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("JwtUtil Tests")
class JwtUtilTest {

    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_SECRET = "testSecretKeyForTestingOnlyNotForProduction123456789";
    private static final long TEST_EXPIRATION_MS = 3_600_000L;
    private static final long EXPIRED_EXPIRATION_MS = -1L;

    private JwtUtil jwtUtil;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtUtil, "expiration", TEST_EXPIRATION_MS);
        userDetails = new User(TEST_USERNAME, "password", List.of());
    }

    @Test
    @DisplayName("Should generate a non-null token for a given username")
    void generateToken_shouldReturnNonNullToken() {
        // When
        var token = jwtUtil.generateToken(TEST_USERNAME);

        // Then
        assertThat(token).isNotBlank();
    }

    @Test
    @DisplayName("Should extract the correct username from a generated token")
    void extractUsername_shouldReturnCorrectUsername() {
        // Given
        var token = jwtUtil.generateToken(TEST_USERNAME);

        // When
        var extracted = jwtUtil.extractUsername(token);

        // Then
        assertThat(extracted).isEqualTo(TEST_USERNAME);
    }

    @Test
    @DisplayName("Should extract a future expiration date from a freshly generated token")
    void extractExpiration_shouldReturnFutureDate() {
        // Given
        var token = jwtUtil.generateToken(TEST_USERNAME);

        // When
        var expiration = jwtUtil.extractExpiration(token);

        // Then
        assertThat(expiration).isInTheFuture();
    }

    @Test
    @DisplayName("Should validate a valid token successfully")
    void validateToken_shouldReturnTrue_whenTokenIsValid() {
        // Given
        var token = jwtUtil.generateToken(TEST_USERNAME);

        // When
        var valid = jwtUtil.validateToken(token, userDetails);

        // Then
        assertThat(valid).isTrue();
    }

    @Test
    @DisplayName("Should reject a token generated for a different username")
    void validateToken_shouldReturnFalse_whenUsernameMismatch() {
        // Given
        var token = jwtUtil.generateToken("differentuser");

        // When
        var valid = jwtUtil.validateToken(token, userDetails);

        // Then
        assertThat(valid).isFalse();
    }

    @Test
    @DisplayName("Should reject an expired token")
    void validateToken_shouldReturnFalse_whenTokenIsExpired() {
        // Given
        ReflectionTestUtils.setField(jwtUtil, "expiration", EXPIRED_EXPIRATION_MS);
        var expiredToken = jwtUtil.generateToken(TEST_USERNAME);

        // Restore normal expiration before validating
        ReflectionTestUtils.setField(jwtUtil, "expiration", TEST_EXPIRATION_MS);

        // When / Then
        assertThatThrownBy(() -> jwtUtil.validateToken(expiredToken, userDetails))
                .isInstanceOf(io.jsonwebtoken.ExpiredJwtException.class);
    }
}
