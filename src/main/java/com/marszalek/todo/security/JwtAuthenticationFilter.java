package com.marszalek.todo.security;

import com.marszalek.todo.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Security filter that validates JWT Bearer tokens on every incoming HTTP request.
 *
 * <p>This filter runs once per request. When a valid Bearer token is found
 * in the {@code Authorization} header and the current security context holds no
 * existing authentication, the filter loads the corresponding user details, validates
 * the token, and populates the {@link SecurityContextHolder} with a new authentication
 * object.</p>
 *
 * <p>Requests that carry no Bearer token, or carry an invalid one, are passed through
 * the filter chain unchanged. Downstream Spring Security mechanisms (e.g. the
 * authentication entry point configured in {@link SecurityConfig}) are then responsible
 * for returning the appropriate error response.</p>
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    /**
     * Intercepts each request to check for a JWT Bearer token in the Authorization header.
     *
     * <p>If a Bearer token is present, delegates to
     * {@link #authenticateIfRequired(String, HttpServletRequest)} before continuing
     * the filter chain.</p>
     *
     * @param request     the incoming HTTP request
     * @param response    the HTTP response
     * @param filterChain the remaining filter chain
     * @throws ServletException if a servlet error occurs during filtering
     * @throws IOException      if an I/O error occurs during filtering
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        jwtService.extractBearerToken(request)
                .ifPresent(token -> authenticateIfRequired(token, request));

        filterChain.doFilter(request, response);
    }

    /**
     * Authenticates the request principal for the given token if no authentication
     * is currently present in the security context.
     *
     * <p>The method loads user details using the username extracted from the token,
     * then validates the token against those details. On success it constructs a
     * {@link UsernamePasswordAuthenticationToken} and registers it in the
     * {@link SecurityContextHolder}.</p>
     *
     * @param token   the raw JWT token extracted from the request
     * @param request the originating HTTP request, used to build authentication details
     */
    private void authenticateIfRequired(String token, HttpServletRequest request) {
        String username = jwtService.extractUsername(token);

        if (username == null || SecurityContextHolder.getContext().getAuthentication() != null) {
            return;
        }

        var userDetails = userDetailsService.loadUserByUsername(username);

        if (jwtService.validateToken(token, userDetails)) {
            var authToken = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
            );
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }
    }
}