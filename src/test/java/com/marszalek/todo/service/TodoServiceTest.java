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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TodoService Tests")
class TodoServiceTest {

    private static final Long TEST_USER_ID = 1L;
    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_EMAIL = "test@example.com";
    private static final Long TEST_TODO_ID = 1L;
    private static final String TEST_TITLE = "Test Todo";
    private static final String TEST_DESCRIPTION = "Test Description";

    @Mock
    private TodoRepository todoRepository;

    @Mock
    private SecurityUtil securityUtil;

    @InjectMocks
    private TodoService todoService;

    private User testUser;
    private Todo testTodo;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(TEST_USER_ID)
                .username(TEST_USERNAME)
                .email(TEST_EMAIL)
                .build();

        testTodo = Todo.builder()
                .id(TEST_TODO_ID)
                .title(TEST_TITLE)
                .description(TEST_DESCRIPTION)
                .completed(false)
                .user(testUser)
                .build();
    }

    @Test
    @DisplayName("Should return only current user's todos")
    void getAllTodos_shouldReturnCurrentUserTodos() {
        // Given
        when(securityUtil.getCurrentUser()).thenReturn(testUser);
        when(todoRepository.findByUserId(TEST_USER_ID)).thenReturn(List.of(testTodo));

        // When
        var result = todoService.getAllTodos();

        // Then
        assertThat(result).hasSize(1).containsExactly(testTodo);
        verify(securityUtil).getCurrentUser();
        verify(todoRepository).findByUserId(TEST_USER_ID);
    }

    @Test
    @DisplayName("Should return empty list when user has no todos")
    void getAllTodos_shouldReturnEmptyList_whenNoTodos() {
        // Given
        when(securityUtil.getCurrentUser()).thenReturn(testUser);
        when(todoRepository.findByUserId(TEST_USER_ID)).thenReturn(List.of());

        // When
        var result = todoService.getAllTodos();

        // Then
        assertThat(result).isEmpty();
        verify(todoRepository).findByUserId(TEST_USER_ID);
    }

    @Test
    @DisplayName("Should return todo when owned by current user")
    void getTodoById_shouldReturnTodo_whenOwnedByCurrentUser() {
        // Given
        when(securityUtil.getCurrentUser()).thenReturn(testUser);
        when(todoRepository.findById(TEST_TODO_ID)).thenReturn(Optional.of(testTodo));

        // When
        var result = todoService.getTodoById(TEST_TODO_ID);

        // Then
        assertThat(result).isPresent().contains(testTodo);
    }

    @ParameterizedTest
    @ValueSource(longs = {2L, 3L, 99L})
    @DisplayName("Should return empty when todo owned by different user")
    void getTodoById_shouldReturnEmpty_whenTodoOwnedByDifferentUser(Long otherUserId) {
        // Given
        var otherUser = User.builder().id(otherUserId).username("user" + otherUserId).build();
        when(securityUtil.getCurrentUser()).thenReturn(otherUser);
        when(todoRepository.findById(TEST_TODO_ID)).thenReturn(Optional.of(testTodo));

        // When
        var result = todoService.getTodoById(TEST_TODO_ID);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should return empty when todo not found by ID")
    void getTodoById_shouldReturnEmpty_whenTodoNotFound() {
        // Given
        when(securityUtil.getCurrentUser()).thenReturn(testUser);
        when(todoRepository.findById(99L)).thenReturn(Optional.empty());

        // When
        var result = todoService.getTodoById(99L);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should assign todo to current user on save")
    void save_shouldAssignTodoToCurrentUser() {
        // Given
        var newTodo = Todo.builder().title("New Todo").completed(false).build();
        when(securityUtil.getCurrentUser()).thenReturn(testUser);
        when(todoRepository.save(any(Todo.class))).thenReturn(newTodo);

        // When
        var result = todoService.save(newTodo);

        // Then
        assertThat(result.getUser()).isEqualTo(testUser);
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
        when(todoRepository.findById(TEST_TODO_ID)).thenReturn(Optional.of(testTodo));
        when(todoRepository.save(any(Todo.class))).thenReturn(testTodo);

        // When
        var result = todoService.updateTodoById(TEST_TODO_ID, updatedDetails);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getTitle()).isEqualTo("Updated Title");
        assertThat(result.get().getDescription()).isEqualTo("Updated Description");
        assertThat(result.get().getCompleted()).isTrue();
        verify(todoRepository).save(testTodo);
    }

    @Test
    @DisplayName("Should return empty when updating todo owned by different user")
    void updateTodo_shouldReturnEmpty_whenOwnedByDifferentUser() {
        // Given
        var otherUser = User.builder().id(2L).username("otheruser").build();
        var updatedDetails = Todo.builder().title("Updated Title").build();

        when(securityUtil.getCurrentUser()).thenReturn(otherUser);
        when(todoRepository.findById(TEST_TODO_ID)).thenReturn(Optional.of(testTodo));

        // When
        var result = todoService.updateTodoById(TEST_TODO_ID, updatedDetails);

        // Then
        assertThat(result).isEmpty();
        verify(todoRepository, never()).save(any(Todo.class));
    }

    @Test
    @DisplayName("Should return empty when updating non-existent todo")
    void updateTodo_shouldReturnEmpty_whenTodoNotFound() {
        // Given
        when(securityUtil.getCurrentUser()).thenReturn(testUser);
        when(todoRepository.findById(99L)).thenReturn(Optional.empty());
        var updatedDetails = Todo.builder().title("New Title").completed(false).build();

        // When
        var result = todoService.updateTodoById(99L, updatedDetails);

        // Then
        assertThat(result).isEmpty();
        verify(todoRepository, never()).save(any(Todo.class));
    }

    @Test
    @DisplayName("Should preserve existing title when update title is blank")
    void updateTodo_shouldPreserveTitle_whenNewTitleIsBlank() {
        // Given
        var updateWithBlankTitle = Todo.builder()
                .title("")
                .description("New Description")
                .completed(true)
                .build();

        when(securityUtil.getCurrentUser()).thenReturn(testUser);
        when(todoRepository.findById(TEST_TODO_ID)).thenReturn(Optional.of(testTodo));
        when(todoRepository.save(any(Todo.class))).thenReturn(testTodo);

        // When
        var result = todoService.updateTodoById(TEST_TODO_ID, updateWithBlankTitle);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getTitle()).isEqualTo(TEST_TITLE);
    }

    @Test
    @DisplayName("Should preserve existing description when update description is blank")
    void updateTodo_shouldPreserveDescription_whenNewDescriptionIsBlank() {
        // Given
        var updateWithBlankDescription = Todo.builder()
                .title("New Title")
                .description("")
                .completed(false)
                .build();

        when(securityUtil.getCurrentUser()).thenReturn(testUser);
        when(todoRepository.findById(TEST_TODO_ID)).thenReturn(Optional.of(testTodo));
        when(todoRepository.save(any(Todo.class))).thenReturn(testTodo);

        // When
        var result = todoService.updateTodoById(TEST_TODO_ID, updateWithBlankDescription);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getDescription()).isEqualTo(TEST_DESCRIPTION);
    }

    @Test
    @DisplayName("Should delete todo when owned by current user")
    void deleteTodo_shouldDeleteTodo_whenOwnedByCurrentUser() {
        // Given
        when(securityUtil.getCurrentUser()).thenReturn(testUser);
        when(todoRepository.findById(TEST_TODO_ID)).thenReturn(Optional.of(testTodo));

        // When
        var result = todoService.deleteTodo(TEST_TODO_ID);

        // Then
        assertThat(result).isTrue();
        verify(todoRepository).delete(testTodo);
    }

    @Test
    @DisplayName("Should return false when deleting todo owned by different user")
    void deleteTodo_shouldReturnFalse_whenOwnedByDifferentUser() {
        // Given
        var otherUser = User.builder().id(2L).username("otheruser").build();
        when(securityUtil.getCurrentUser()).thenReturn(otherUser);
        when(todoRepository.findById(TEST_TODO_ID)).thenReturn(Optional.of(testTodo));

        // When
        var result = todoService.deleteTodo(TEST_TODO_ID);

        // Then
        assertThat(result).isFalse();
        verify(todoRepository, never()).delete(any(Todo.class));
    }

    @Test
    @DisplayName("Should return false when deleting non-existent todo")
    void deleteTodo_shouldReturnFalse_whenTodoNotFound() {
        // Given
        when(securityUtil.getCurrentUser()).thenReturn(testUser);
        when(todoRepository.findById(99L)).thenReturn(Optional.empty());

        // When
        var result = todoService.deleteTodo(99L);

        // Then
        assertThat(result).isFalse();
        verify(todoRepository, never()).delete(any(Todo.class));
    }
}
