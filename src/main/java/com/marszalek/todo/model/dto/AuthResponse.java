package com.marszalek.todo.model.dto;

/**
 * Response payload returned after a successful authentication or registration.
 *
 * @param token    the signed JWT token to be used in subsequent requests
 * @param username the authenticated user's username
 * @param email    the authenticated user's email address
 */
public record AuthResponse(String token, String username, String email) {
}
