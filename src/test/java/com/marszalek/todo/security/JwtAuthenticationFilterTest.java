package com.marszalek.todo.security;

import com.marszalek.todo.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAuthenticationFilter Tests")
class JwtAuthenticationFilterTest {

    private static final String TEST_TOKEN = "header.payload.signature";
    private static final String TEST_USERNAME = "testuser";

    @Mock
    private JwtService jwtService;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter filter;

    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        userDetails = new User(TEST_USERNAME, "password", List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should continue filter chain without authentication when no Bearer token is present")
    void doFilterInternal_shouldContinueChain_whenNoBearerTokenPresent() throws Exception {
        // Given
        when(jwtService.extractBearerToken(request)).thenReturn(Optional.empty());

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verifyNoInteractions(userDetailsService);
    }

    @Test
    @DisplayName("Should authenticate user and continue chain when token is valid")
    void doFilterInternal_shouldAuthenticateUser_whenTokenIsValid() throws Exception {
        // Given
        when(jwtService.extractBearerToken(request)).thenReturn(Optional.of(TEST_TOKEN));
        when(jwtService.extractUsername(TEST_TOKEN)).thenReturn(TEST_USERNAME);
        when(userDetailsService.loadUserByUsername(TEST_USERNAME)).thenReturn(userDetails);
        when(jwtService.validateToken(TEST_TOKEN, userDetails)).thenReturn(true);

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo(TEST_USERNAME);
    }

    @Test
    @DisplayName("Should continue chain without authentication when token is invalid")
    void doFilterInternal_shouldNotAuthenticate_whenTokenIsInvalid() throws Exception {
        // Given
        when(jwtService.extractBearerToken(request)).thenReturn(Optional.of(TEST_TOKEN));
        when(jwtService.extractUsername(TEST_TOKEN)).thenReturn(TEST_USERNAME);
        when(userDetailsService.loadUserByUsername(TEST_USERNAME)).thenReturn(userDetails);
        when(jwtService.validateToken(TEST_TOKEN, userDetails)).thenReturn(false);

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("Should not overwrite existing authentication when user is already authenticated")
    void doFilterInternal_shouldSkipAuthentication_whenAlreadyAuthenticated() throws Exception {
        // Given
        when(jwtService.extractBearerToken(request)).thenReturn(Optional.of(TEST_TOKEN));
        when(jwtService.extractUsername(TEST_TOKEN)).thenReturn(TEST_USERNAME);

        var existingAuth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(existingAuth);

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(userDetailsService);
        verify(jwtService, never()).validateToken(any(), any());
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isEqualTo(existingAuth);
    }

    @Test
    @DisplayName("Should continue chain without authentication when username cannot be extracted from token")
    void doFilterInternal_shouldNotAuthenticate_whenUsernameIsNull() throws Exception {
        // Given
        when(jwtService.extractBearerToken(request)).thenReturn(Optional.of(TEST_TOKEN));
        when(jwtService.extractUsername(TEST_TOKEN)).thenReturn(null);

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verifyNoInteractions(userDetailsService);
    }
}
