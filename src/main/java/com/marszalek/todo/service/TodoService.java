package com.marszalek.todo.service;

import com.marszalek.todo.model.Todo;
import com.marszalek.todo.repository.TodoRepository;
import com.marszalek.todo.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service for managing {@link Todo} entities scoped to the currently authenticated user.
 *
 * <p>All operations are automatically filtered to the user resolved from the active
 * security context, ensuring users can only access and modify their own data.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TodoService {

    private final TodoRepository todoRepository;
    private final SecurityUtil securityUtil;

    /**
     * Creates and persists a new todo item assigned to the currently authenticated user.
     *
     * @param todo the todo to save; its {@code user} field will be set automatically
     * @return the saved {@link Todo} with its generated ID populated
     * @throws DataAccessException if the database write fails
     */
    public Todo save(Todo todo) {
        var currentUser = securityUtil.getCurrentUser();
        todo.setUser(currentUser);
        log.debug("Saving todo '{}' for user '{}'", todo.getTitle(), currentUser.getUsername());
        try {
            return todoRepository.save(todo);
        } catch (DataAccessException e) {
            log.error("Failed to save todo for user {}: {}", currentUser.getUsername(), e.getMessage());
            throw e;
        }
    }

    /**
     * Retrieves a todo by ID, provided it belongs to the currently authenticated user.
     *
     * <p>Returns {@link Optional#empty()} if the todo does not exist or is owned by
     * a different user, preventing both 404 and 403 information leakage.</p>
     *
     * @param id the ID of the todo to retrieve
     * @return an {@link Optional} containing the matching {@link Todo}, or empty if not found/not owned
     */
    public Optional<Todo> getTodoById(Long id) {
        var currentUser = securityUtil.getCurrentUser();
        var todo = todoRepository.findById(id);

        if (todo.isPresent() && !todo.get().getUser().getId().equals(currentUser.getId())) {
            log.warn("User {} attempted to access todo {} owned by another user",
                    currentUser.getUsername(), id);
            return Optional.empty();
        }

        return todo;
    }

    /**
     * Returns all todos belonging to the currently authenticated user.
     *
     * @return a list of the current user's todos; never {@code null}
     */
    public List<Todo> getAllTodos() {
        var currentUser = securityUtil.getCurrentUser();
        log.debug("Fetching todos for user: {}", currentUser.getUsername());
        return todoRepository.findByUserId(currentUser.getId());
    }

    /**
     * Partially updates an existing todo owned by the currently authenticated user.
     *
     * <p>Only non-blank values from {@code todo} are applied. The {@code completed}
     * flag is always updated regardless of its value.</p>
     *
     * @param id   the ID of the todo to update
     * @param todo the update payload containing the new field values
     * @return an {@link Optional} containing the updated {@link Todo}, or empty if not found/not owned
     */
    public Optional<Todo> updateTodoById(Long id, Todo todo) {
        var currentUser = securityUtil.getCurrentUser();
        return todoRepository.findById(id)
                .filter(t -> t.getUser().getId().equals(currentUser.getId()))
                .map(t -> {
                    if (todo.getTitle() != null && !todo.getTitle().isBlank()) {
                        t.setTitle(todo.getTitle());
                    }
                    if (todo.getDescription() != null && !todo.getDescription().isBlank()) {
                        t.setDescription(todo.getDescription());
                    }
                    t.setCompleted(todo.getCompleted());
                    return todoRepository.save(t);
                });
    }

    /**
     * Deletes a todo by ID if it belongs to the currently authenticated user.
     *
     * @param id the ID of the todo to delete
     * @return {@code true} if the todo was found and deleted; {@code false} if it does not
     * exist or belongs to a different user
     */
    public boolean deleteTodo(Long id) {
        var currentUser = securityUtil.getCurrentUser();
        return todoRepository.findById(id)
                .filter(todo -> todo.getUser().getId().equals(currentUser.getId()))
                .map(todo -> {
                    log.debug("Deleting todo {} for user {}", id, currentUser.getUsername());
                    todoRepository.delete(todo);
                    return true;
                })
                .orElse(false);
    }
}
