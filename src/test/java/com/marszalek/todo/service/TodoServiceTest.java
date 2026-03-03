package com.marszalek.todo.service;

import com.marszalek.todo.model.Todo;
import com.marszalek.todo.model.User;
import com.marszalek.todo.repository.TodoRepository;
import com.marszalek.todo.util.SecurityUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TodoService Tests")
class TodoServiceTest {

    @InjectMocks
    private TodoService todoService;

    @Mock
    private TodoRepository todoRepository;

    @Mock
    private SecurityUtil securityUtil;

    private User testUser;
    private Todo testTodo;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .build();

        testTodo = Todo.builder()
                .id(1L)
                .title("Test Todo")
                .description("Test Description")
                .completed(false)
                .user(testUser)
                .build();
    }

    @Test
    @DisplayName("Should return only current user's todos")
    void getAllTodos_shouldReturnCurrentUserTodos() {
        // Given
        when(securityUtil.getCurrentUser()).thenReturn(testUser);
        when(todoRepository.findByUserId(testUser.getId())).thenReturn(List.of(testTodo));

        // When
        var result = todoService.getAllTodos();

        // Then
        assertEquals(1, result.size());
        assertEquals(testTodo, result.getFirst());
        verify(securityUtil).getCurrentUser();
        verify(todoRepository).findByUserId(testUser.getId());
    }

    @Test
    @DisplayName("Should return todo when owned by current user")
    void getTodoById_shouldReturnTodo_whenOwnedByCurrentUser() {
        // Given
        when(securityUtil.getCurrentUser()).thenReturn(testUser);
        when(todoRepository.findById(1L)).thenReturn(Optional.of(testTodo));

        // When
        var result = todoService.getTodoById(1L);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testTodo, result.get());
    }

    @ParameterizedTest
    @ValueSource(longs = {2L, 3L, 99L})
    @DisplayName("Should return empty when todo owned by different user")
    void getTodoById_shouldReturnEmpty_whenTodoOwnedByDifferentUser(Long otherUserId) {
        // Given
        var otherUser = User.builder()
                .id(otherUserId)
                .username("user" + otherUserId)
                .build();

        when(securityUtil.getCurrentUser()).thenReturn(otherUser);
        when(todoRepository.findById(1L)).thenReturn(Optional.of(testTodo));

        // When
        var result = todoService.getTodoById(1L);

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Should assign todo to current user on save")
    void save_shouldAssignTodoToCurrentUser() {
        // Given
        var newTodo = Todo.builder()
                .title("New Todo")
                .completed(false)
                .build();

        when(securityUtil.getCurrentUser()).thenReturn(testUser);
        when(todoRepository.save(any(Todo.class))).thenReturn(newTodo);

        // When
        var result = todoService.save(newTodo);

        // Then
        assertEquals(testUser, result.getUser());
        verify(todoRepository).save(newTodo);
    }

    @Test
    @DisplayName("Should update todo when owned by current user")
    void updateTodo_shouldUpdateTodo_whenOwnedByCurrentUser() {
        // Given
        var updatedDetails = Todo.builder()
                .title("Updated Title")
                .description("Updated Description")
                .completed(true)
                .build();

        when(securityUtil.getCurrentUser()).thenReturn(testUser);
        when(todoRepository.findById(1L)).thenReturn(Optional.of(testTodo));
        when(todoRepository.save(any(Todo.class))).thenReturn(testTodo);

        // When
        var result = todoService.updateTodoById(1L, updatedDetails);

        // Then
        assertTrue(result.isPresent());
        assertEquals("Updated Title", result.get().getTitle());
        assertEquals("Updated Description", result.get().getDescription());
        assertTrue(result.get().getCompleted());
        verify(todoRepository).save(testTodo);
    }

    @Test
    @DisplayName("Should return empty when updating todo owned by different user")
    void updateTodo_shouldReturnEmpty_whenOwnedByDifferentUser() {
        // Given
        var otherUser = User.builder()
                .id(2L)
                .username("otheruser")
                .build();

        var updatedDetails = Todo.builder()
                .title("Updated Title")
                .build();

        when(securityUtil.getCurrentUser()).thenReturn(otherUser);
        when(todoRepository.findById(1L)).thenReturn(Optional.of(testTodo));

        // When
        var result = todoService.updateTodoById(1L, updatedDetails);

        // Then
        assertFalse(result.isPresent());
        verify(todoRepository, never()).save(any(Todo.class));
    }

    @Test
    @DisplayName("Should delete todo when owned by current user")
    void deleteTodo_shouldDeleteTodo_whenOwnedByCurrentUser() {
        // Given
        when(securityUtil.getCurrentUser()).thenReturn(testUser);
        when(todoRepository.findById(1L)).thenReturn(Optional.of(testTodo));

        // When
        var result = todoService.deleteTodo(1L);

        // Then
        assertTrue(result);
        verify(todoRepository).delete(testTodo);
    }

    @Test
    @DisplayName("Should return false when deleting todo owned by different user")
    void deleteTodo_shouldReturnFalse_whenOwnedByDifferentUser() {
        // Given
        var otherUser = User.builder()
                .id(2L)
                .username("otheruser")
                .build();

        when(securityUtil.getCurrentUser()).thenReturn(otherUser);
        when(todoRepository.findById(1L)).thenReturn(Optional.of(testTodo));

        // When
        var result = todoService.deleteTodo(1L);

        // Then
        assertFalse(result);
        verify(todoRepository, never()).delete(any(Todo.class));
    }
}