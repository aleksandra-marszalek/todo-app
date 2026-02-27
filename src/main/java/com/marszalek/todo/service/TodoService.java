package com.marszalek.todo.service;

import com.marszalek.todo.model.Todo;
import com.marszalek.todo.repository.TodoRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static org.apache.logging.log4j.util.Strings.isNotBlank;

@AllArgsConstructor
@Service
public class TodoService {

    private final TodoRepository todoRepository;

    public Todo save(Todo todo) {
        return todoRepository.save(todo);
    }

    public Optional<Todo> getTodoById(Long id) {
        return todoRepository.findById(id);
    }

    public List<Todo> getAllTodos() {
        return todoRepository.findAll();
    }

    public boolean deleteTodo(Long id) {
        if (todoRepository.existsById(id)) {
            todoRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public Optional<Todo> updateTodoById(Long id, Todo todo) {
        return todoRepository.findById(id)
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