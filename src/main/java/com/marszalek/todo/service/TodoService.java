package com.marszalek.todo.service;

import com.marszalek.todo.model.Todo;
import com.marszalek.todo.model.User;
import com.marszalek.todo.repository.TodoRepository;
import com.marszalek.todo.util.SecurityUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static org.apache.logging.log4j.util.Strings.isNotBlank;

@Slf4j
@AllArgsConstructor
@Service
public class TodoService {

    private final TodoRepository todoRepository;
    private final SecurityUtil securityUtil;

    private User getCurrentUser() {
        return securityUtil.getCurrentUser();
    }

    public Todo save(Todo todo) {
        User currentUser = securityUtil.getCurrentUser();
        todo.setUser(currentUser);

        log.debug("Saving todo '{}' for user '{}'", todo.getTitle(), currentUser.getUsername());

        try {
            return todoRepository.save(todo);
        } catch (DataAccessException e) {
            log.error("Failed to save todo for user {}: {}", currentUser.getUsername(), e.getMessage());
            throw e;
        }
    }

    public Optional<Todo> getTodoById(Long id) {
        User currentUser = securityUtil.getCurrentUser();
        Optional<Todo> todo = todoRepository.findById(id);

        if (todo.isPresent() && !todo.get().getUser().getId().equals(currentUser.getId())) {
            log.warn("User {} attempted to access todo {} owned by another user",
                    currentUser.getUsername(), id);
            return Optional.empty();
        }

        return todo;
    }

    public List<Todo> getAllTodos() {
        User currentUser = securityUtil.getCurrentUser();
        log.debug("Fetching todos for user: {}", currentUser.getUsername());
        return todoRepository.findByUserId(currentUser.getId());
    }

    public Optional<Todo> updateTodoById(Long id, Todo todo) {
        var currentUser = getCurrentUser();
        return todoRepository.findById(id)
                .filter(t -> t.getUser().getId().equals(currentUser.getId())) // Only update if owned by user
                .map(t -> {
                    if (isNotBlank(todo.getTitle())) {
                        t.setTitle(todo.getTitle());
                    }
                    if (isNotBlank(todo.getDescription())) {
                        t.setDescription(todo.getDescription());
                    }
                    t.setCompleted(todo.getCompleted());
                    return todoRepository.save(t);
                });
    }

    public boolean deleteTodo(Long id) {
        User currentUser = securityUtil.getCurrentUser();

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