package com.marszalek.todo.util;

import com.marszalek.todo.model.User;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Utility for accessing the currently authenticated user from the Spring Security context.
 *
 * <p>This component is intended for use within service classes that operate on behalf of the
 * authenticated principal. It assumes the security context is populated, which is guaranteed
 * for any request reaching a protected endpoint.</p>
 */
@Component
public class SecurityUtil {

    /**
     * Returns the {@link User} entity for the currently authenticated principal.
     *
     * @return the authenticated {@link User}
     * @throws ClassCastException   if the principal stored in the context is not a {@link User}
     * @throws NullPointerException if no authentication is present in the security context
     */
    public User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
