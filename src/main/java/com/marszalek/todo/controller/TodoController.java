package com.marszalek.todo.controller;

import com.marszalek.todo.model.Todo;
import com.marszalek.todo.service.TodoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller exposing CRUD operations for {@link Todo} items.
 *
 * <p>All endpoints require authentication. Each operation is automatically scoped to the
 * currently authenticated user — users cannot access or modify each other's todos.</p>
 */
@RestController
@RequestMapping("/api/todos")
@RequiredArgsConstructor
public class TodoController {

    private final TodoService todoService;

    /**
     * Returns all todos belonging to the currently authenticated user.
     *
     * @return a list of the user's todos; empty list if none exist
     */
    @GetMapping
    public List<Todo> getAllTodos() {
        return todoService.getAllTodos();
    }

    /**
     * Creates a new todo for the currently authenticated user.
     *
     * @param todo the todo payload; must pass bean validation
     * @return the persisted {@link Todo} with its generated ID
     */
    @PostMapping
    public Todo createTodo(@Valid @RequestBody Todo todo) {
        return todoService.save(todo);
    }

    /**
     * Retrieves a specific todo by ID if it belongs to the current user.
     *
     * @param id the ID of the todo to retrieve
     * @return {@code 200 OK} with the {@link Todo}, or {@code 404 Not Found} if it does not
     *         exist or belongs to a different user
     */
    @GetMapping("/{id}")
    public ResponseEntity<Todo> getTodoById(@PathVariable Long id) {
        return todoService.getTodoById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Updates an existing todo owned by the current user.
     *
     * <p>Only non-blank field values from the request body are applied.</p>
     *
     * @param id   the ID of the todo to update
     * @param todo the update payload; must pass bean validation
     * @return {@code 200 OK} with the updated {@link Todo}, or {@code 404 Not Found} if it does not
     *         exist or belongs to a different user
     */
    @PutMapping("/{id}")
    public ResponseEntity<Todo> updateTodo(@PathVariable Long id, @Valid @RequestBody Todo todo) {
        return todoService.updateTodoById(id, todo)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Deletes a todo owned by the current user.
     *
     * @param id the ID of the todo to delete
     * @return {@code 200 OK} if deleted, or {@code 404 Not Found} if it does not exist or belongs
     *         to a different user
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTodo(@PathVariable Long id) {
        if (todoService.deleteTodo(id)) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}
