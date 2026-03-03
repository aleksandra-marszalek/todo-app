package com.marszalek.todo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marszalek.todo.AbstractIntegrationTest;
import com.marszalek.todo.model.dto.RegisterRequest;
import com.marszalek.todo.model.Todo;
import com.marszalek.todo.model.User;
import com.marszalek.todo.repository.TodoRepository;
import com.marszalek.todo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@Transactional
@DisplayName("Todo Controller Integration Tests")
class TodoControllerIntegrationTest extends AbstractIntegrationTest {  // Extend base class

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TodoRepository todoRepository;

    private String jwtToken;
    private User testUser;

    @BeforeEach
    void setUp() throws Exception {
        // Clean up
        todoRepository.deleteAll();
        userRepository.deleteAll();

        // Register and login to get JWT token
        var registerRequest = new RegisterRequest();
        registerRequest.setUsername("testuser");
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password123");

        var registerResponse = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Extract token from response
        var responseMap = objectMapper.readValue(registerResponse, java.util.Map.class);
        jwtToken = (String) responseMap.get("token");

        // Get the created user
        testUser = userRepository.findByUsername("testuser").orElseThrow();
    }

    @Test
    @DisplayName("Should create todo when authenticated")
    void createTodo_shouldSucceed_whenAuthenticated() throws Exception {
        // Given
        var newTodo = Todo.builder()
                .title("Buy groceries")
                .description("Milk, eggs, bread")
                .completed(false)
                .build();

        // When & Then
        mockMvc.perform(post("/api/todos")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newTodo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Buy groceries"))
                .andExpect(jsonPath("$.description").value("Milk, eggs, bread"))
                .andExpect(jsonPath("$.completed").value(false));
    }

    @Test
    @DisplayName("Should fail to create todo when not authenticated")
    void createTodo_shouldFail_whenNotAuthenticated() throws Exception {
        // Given
        var newTodo = Todo.builder()
                .title("Buy groceries")
                .build();

        // When & Then
        mockMvc.perform(post("/api/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newTodo)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should fail to create todo with invalid data")
    void createTodo_shouldFail_whenInvalidData() throws Exception {
        // Given - todo with empty title (violates @NotBlank)
        var invalidTodo = Todo.builder()
                .title("")
                .completed(false)
                .build();

        // When & Then
        mockMvc.perform(post("/api/todos")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidTodo)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should get all todos for current user")
    void getAllTodos_shouldReturnOnlyCurrentUserTodos() throws Exception {
        // Given - create a todo for test user
        var todo = Todo.builder()
                .title("Test Todo")
                .user(testUser)
                .completed(false)
                .build();
        todoRepository.save(todo);

        // When & Then
        mockMvc.perform(get("/api/todos")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("Test Todo"));
    }

    @Test
    @DisplayName("Should not return todos from other users")
    void getAllTodos_shouldNotReturnOtherUsersTodos() throws Exception {
        // Given - create another user with a todo
        var otherUser = User.builder()
                .username("otheruser")
                .email("other@example.com")
                .password("password")
                .build();
        userRepository.save(otherUser);

        var otherTodo = Todo.builder()
                .title("Other's Todo")
                .user(otherUser)
                .completed(false)
                .build();
        todoRepository.save(otherTodo);

        // When & Then - should not see other user's todo
        mockMvc.perform(get("/api/todos")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("Should get todo by ID when owned by current user")
    void getTodoById_shouldSucceed_whenOwnedByCurrentUser() throws Exception {
        // Given
        var todo = Todo.builder()
                .title("My Todo")
                .user(testUser)
                .completed(false)
                .build();
        var savedTodo = todoRepository.save(todo);

        // When & Then
        mockMvc.perform(get("/api/todos/" + savedTodo.getId())
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("My Todo"));
    }

    @Test
    @DisplayName("Should return 404 when todo owned by different user")
    void getTodoById_shouldReturn404_whenOwnedByDifferentUser() throws Exception {
        // Given - create another user with a todo
        var otherUser = User.builder()
                .username("otheruser")
                .email("other@example.com")
                .password("password")
                .build();
        userRepository.save(otherUser);

        var otherTodo = Todo.builder()
                .title("Other's Todo")
                .user(otherUser)
                .completed(false)
                .build();
        var savedTodo = todoRepository.save(otherTodo);

        // When & Then
        mockMvc.perform(get("/api/todos/" + savedTodo.getId())
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should update todo when owned by current user")
    void updateTodo_shouldSucceed_whenOwnedByCurrentUser() throws Exception {
        // Given
        var todo = Todo.builder()
                .title("Original Title")
                .user(testUser)
                .completed(false)
                .build();
        var savedTodo = todoRepository.save(todo);

        var updatedTodo = Todo.builder()
                .title("Updated Title")
                .description("Updated Description")
                .completed(true)
                .build();

        // When & Then
        mockMvc.perform(put("/api/todos/" + savedTodo.getId())
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedTodo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"))
                .andExpect(jsonPath("$.description").value("Updated Description"))
                .andExpect(jsonPath("$.completed").value(true));
    }

    @Test
    @DisplayName("Should delete todo when owned by current user")
    void deleteTodo_shouldSucceed_whenOwnedByCurrentUser() throws Exception {
        // Given
        var todo = Todo.builder()
                .title("To Delete")
                .user(testUser)
                .completed(false)
                .build();
        var savedTodo = todoRepository.save(todo);

        // When & Then
        mockMvc.perform(delete("/api/todos/" + savedTodo.getId())
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk());

        // Verify it's actually deleted
        mockMvc.perform(get("/api/todos/" + savedTodo.getId())
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return empty list when user has no todos")
    void getAllTodos_shouldReturnEmptyList_whenNoTodos() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/todos")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("Should return 404 when todo does not exist")
    void getTodoById_shouldReturn404_whenTodoDoesNotExist() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/todos/99999")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 400 when updating todo with invalid data")
    void updateTodo_shouldReturn400_whenInvalidData() throws Exception {
        // Given
        var todo = Todo.builder()
                .title("Original Title")
                .user(testUser)
                .completed(false)
                .build();
        var savedTodo = todoRepository.save(todo);

        var invalidUpdate = Todo.builder()
                .title("")
                .completed(true)
                .build();

        // When & Then
        mockMvc.perform(put("/api/todos/" + savedTodo.getId())
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUpdate)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 404 when updating todo owned by different user")
    void updateTodo_shouldReturn404_whenOwnedByDifferentUser() throws Exception {
        // Given
        var otherUser = User.builder()
                .username("otheruser")
                .email("other@example.com")
                .password("password")
                .build();
        userRepository.save(otherUser);

        var otherTodo = Todo.builder()
                .title("Other's Todo")
                .user(otherUser)
                .completed(false)
                .build();
        var savedTodo = todoRepository.save(otherTodo);

        var updatedTodo = Todo.builder()
                .title("Updated Title")
                .completed(true)
                .build();

        // When & Then
        mockMvc.perform(put("/api/todos/" + savedTodo.getId())
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedTodo)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 404 when updating non-existent todo")
    void updateTodo_shouldReturn404_whenTodoDoesNotExist() throws Exception {
        // Given
        var updatedTodo = Todo.builder()
                .title("Updated Title")
                .completed(true)
                .build();

        // When & Then
        mockMvc.perform(put("/api/todos/99999")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedTodo)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 404 when deleting todo owned by different user")
    void deleteTodo_shouldReturn404_whenOwnedByDifferentUser() throws Exception {
        // Given
        var otherUser = User.builder()
                .username("otheruser")
                .email("other@example.com")
                .password("password")
                .build();
        userRepository.save(otherUser);

        var otherTodo = Todo.builder()
                .title("Other's Todo")
                .user(otherUser)
                .completed(false)
                .build();
        var savedTodo = todoRepository.save(otherTodo);

        // When & Then
        mockMvc.perform(delete("/api/todos/" + savedTodo.getId())
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 404 when deleting non-existent todo")
    void deleteTodo_shouldReturn404_whenTodoDoesNotExist() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/todos/99999")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNotFound());
    }
}