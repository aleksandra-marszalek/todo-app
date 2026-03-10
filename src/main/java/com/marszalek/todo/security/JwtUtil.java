package com.marszalek.todo.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Low-level utility for JWT token creation, parsing, and validation.
 *
 * <p>This component handles raw JWT operations using the configured secret key and
 * expiration window. Application code should interact with {@link com.marszalek.todo.service.JwtService}
 * rather than this class directly.</p>
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    /**
     * Extracts the username (subject claim) from the given token.
     *
     * @param token the JWT token string
     * @return the username encoded as the token's subject
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extracts the expiration date from the given token.
     *
     * @param token the JWT token string
     * @return the expiration {@link Date} of the token
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extracts an arbitrary claim from the given token using the provided resolver function.
     *
     * @param <T>            the type of the claim value
     * @param token          the JWT token string
     * @param claimsResolver a function that maps the token's {@link Claims} to the desired value
     * @return the resolved claim value
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final var claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Validates a JWT token against the provided user details.
     *
     * <p>A token is considered valid if its subject matches the username from
     * {@code userDetails} and the token has not expired.</p>
     *
     * @param token       the JWT token to validate
     * @param userDetails the user details to validate against
     * @return {@code true} if the token is valid; {@code false} otherwise
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        final var username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    /**
     * Generates a new signed JWT token for the given username.
     *
     * @param username the username to encode as the token's subject
     * @return a compact, signed JWT token string
     */
    public String generateToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, username);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignKey())
                .compact();
    }

    private SecretKey getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
