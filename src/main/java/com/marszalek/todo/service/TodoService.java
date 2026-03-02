package com.marszalek.todo.service;

import com.marszalek.todo.model.Todo;
import com.marszalek.todo.model.User;
import com.marszalek.todo.repository.TodoRepository;
import com.marszalek.todo.util.SecurityUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static org.apache.logging.log4j.util.Strings.isNotBlank;

@Slf4j
@AllArgsConstructor
@Service
public class TodoService {

    private final TodoRepository todoRepository;

    private User getCurrentUser() {
        return SecurityUtil.getCurrentUser();
    }

    public Todo save(Todo todo) {
        var currentUser = getCurrentUser();
        todo.setUser(currentUser);
        return todoRepository.save(todo);
    }

    public Optional<Todo> getTodoById(Long id) {
        var currentUser = getCurrentUser();
        Optional<Todo> todo = todoRepository.findById(id);

        if (todo.isPresent() && !todo.get().getUser().getId().equals(currentUser.getId())) {
            log.warn("User {} attempted viewing todo {} without access", currentUser.getId(), todo.get().getId());
            return Optional.empty(); // Don't let users see other people's todos
        }

        return todo;
    }

    public List<Todo> getAllTodos() {
        var currentUser = getCurrentUser();
        return todoRepository.findByUserId(currentUser.getId());
    }

    public boolean deleteTodo(Long id) {
        User currentUser = getCurrentUser();

        return todoRepository.findById(id)
                .filter(todo -> todo.getUser().getId().equals(currentUser.getId())) // Only delete if owned by user
                .map(todo -> {
                    todoRepository.delete(todo);
                    return true;
                })
                .orElse(false);
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
}