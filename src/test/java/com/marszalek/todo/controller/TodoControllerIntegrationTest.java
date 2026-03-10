package com.marszalek.todo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marszalek.todo.AbstractIntegrationTest;
import com.marszalek.todo.model.Todo;
import com.marszalek.todo.model.User;
import com.marszalek.todo.model.dto.RegisterRequest;
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

import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@Transactional
@DisplayName("Todo Controller Integration Tests")
class TodoControllerIntegrationTest extends AbstractIntegrationTest {

    private static final String AUTH_HEADER = "Authorization";
    private static final String TODO_USERNAME = "testuser";
    private static final String TODO_USER_EMAIL = "test@example.com";
    private static final String TODO_USER_PASSWORD = "password123";
    private static final String OTHER_USERNAME = "otheruser";
    private static final String OTHER_USER_EMAIL = "other@example.com";

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
        todoRepository.deleteAll();
        userRepository.deleteAll();

        var registerRequest = new RegisterRequest();
        registerRequest.setUsername(TODO_USERNAME);
        registerRequest.setEmail(TODO_USER_EMAIL);
        registerRequest.setPassword(TODO_USER_PASSWORD);

        var registerResponse = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        var responseMap = objectMapper.readValue(registerResponse, Map.class);
        jwtToken = (String) responseMap.get("token");
        testUser = userRepository.findByUsername(TODO_USERNAME).orElseThrow();
    }

    @Test
    @DisplayName("Should create todo when authenticated")
    void createTodo_shouldSucceed_whenAuthenticated() throws Exception {
        var newTodo = Todo.builder()
                .title("Buy groceries")
                .description("Milk, eggs, bread")
                .completed(false)
                .build();

        mockMvc.perform(post("/api/todos")
                        .header(AUTH_HEADER, "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newTodo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Buy groceries"))
                .andExpect(jsonPath("$.description").value("Milk, eggs, bread"))
                .andExpect(jsonPath("$.completed").value(false));
    }

    @Test
    @DisplayName("Should return 401 when creating todo without authentication")
    void createTodo_shouldFail_whenNotAuthenticated() throws Exception {
        var newTodo = Todo.builder()
                .title("Buy groceries")
                .build();

        mockMvc.perform(post("/api/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newTodo)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should return 400 when creating todo with invalid data")
    void createTodo_shouldFail_whenInvalidData() throws Exception {
        var invalidTodo = Todo.builder()
                .title("")
                .completed(false)
                .build();

        mockMvc.perform(post("/api/todos")
                        .header(AUTH_HEADER, "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidTodo)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should get all todos for current user")
    void getAllTodos_shouldReturnOnlyCurrentUserTodos() throws Exception {
        var todo = Todo.builder()
                .title("Test Todo")
                .user(testUser)
                .completed(false)
                .build();
        todoRepository.save(todo);

        mockMvc.perform(get("/api/todos")
                        .header(AUTH_HEADER, "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("Test Todo"));
    }

    @Test
    @DisplayName("Should not return todos from other users")
    void getAllTodos_shouldNotReturnOtherUsersTodos() throws Exception {
        var otherUser = savedUser(OTHER_USERNAME, OTHER_USER_EMAIL);

        var otherTodo = Todo.builder()
                .title("Other's Todo")
                .user(otherUser)
                .completed(false)
                .build();
        todoRepository.save(otherTodo);

        mockMvc.perform(get("/api/todos")
                        .header(AUTH_HEADER, "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("Should get todo by ID when owned by current user")
    void getTodoById_shouldSucceed_whenOwnedByCurrentUser() throws Exception {
        var savedTodo = todoRepository.save(Todo.builder()
                .title("My Todo")
                .user(testUser)
                .completed(false)
                .build());

        mockMvc.perform(get("/api/todos/" + savedTodo.getId())
                        .header(AUTH_HEADER, "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("My Todo"));
    }

    @Test
    @DisplayName("Should return 404 when todo owned by different user")
    void getTodoById_shouldReturn404_whenOwnedByDifferentUser() throws Exception {
        var otherUser = savedUser(OTHER_USERNAME, OTHER_USER_EMAIL);
        var savedTodo = todoRepository.save(Todo.builder()
                .title("Other's Todo")
                .user(otherUser)
                .completed(false)
                .build());

        mockMvc.perform(get("/api/todos/" + savedTodo.getId())
                        .header(AUTH_HEADER, "Bearer " + jwtToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 404 when todo does not exist")
    void getTodoById_shouldReturn404_whenTodoDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/todos/99999")
                        .header(AUTH_HEADER, "Bearer " + jwtToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should update todo when owned by current user")
    void updateTodo_shouldSucceed_whenOwnedByCurrentUser() throws Exception {
        var savedTodo = todoRepository.save(Todo.builder()
                .title("Original Title")
                .user(testUser)
                .completed(false)
                .build());

        var updatedTodo = Todo.builder()
                .title("Updated Title")
                .description("Updated Description")
                .completed(true)
                .build();

        mockMvc.perform(put("/api/todos/" + savedTodo.getId())
                        .header(AUTH_HEADER, "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedTodo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"))
                .andExpect(jsonPath("$.description").value("Updated Description"))
                .andExpect(jsonPath("$.completed").value(true));
    }

    @Test
    @DisplayName("Should return 400 when updating todo with invalid data")
    void updateTodo_shouldReturn400_whenInvalidData() throws Exception {
        var savedTodo = todoRepository.save(Todo.builder()
                .title("Original Title")
                .user(testUser)
                .completed(false)
                .build());

        var invalidUpdate = Todo.builder()
                .title("")
                .completed(true)
                .build();

        mockMvc.perform(put("/api/todos/" + savedTodo.getId())
                        .header(AUTH_HEADER, "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUpdate)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 404 when updating todo owned by different user")
    void updateTodo_shouldReturn404_whenOwnedByDifferentUser() throws Exception {
        var otherUser = savedUser(OTHER_USERNAME, OTHER_USER_EMAIL);
        var savedTodo = todoRepository.save(Todo.builder()
                .title("Other's Todo")
                .user(otherUser)
                .completed(false)
                .build());

        var updatedTodo = Todo.builder().title("Updated Title").completed(true).build();

        mockMvc.perform(put("/api/todos/" + savedTodo.getId())
                        .header(AUTH_HEADER, "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedTodo)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 404 when updating non-existent todo")
    void updateTodo_shouldReturn404_whenTodoDoesNotExist() throws Exception {
        var updatedTodo = Todo.builder().title("Updated Title").completed(true).build();

        mockMvc.perform(put("/api/todos/99999")
                        .header(AUTH_HEADER, "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedTodo)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should delete todo when owned by current user")
    void deleteTodo_shouldSucceed_whenOwnedByCurrentUser() throws Exception {
        var savedTodo = todoRepository.save(Todo.builder()
                .title("To Delete")
                .user(testUser)
                .completed(false)
                .build());

        mockMvc.perform(delete("/api/todos/" + savedTodo.getId())
                        .header(AUTH_HEADER, "Bearer " + jwtToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/todos/" + savedTodo.getId())
                        .header(AUTH_HEADER, "Bearer " + jwtToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 404 when deleting todo owned by different user")
    void deleteTodo_shouldReturn404_whenOwnedByDifferentUser() throws Exception {
        var otherUser = savedUser(OTHER_USERNAME, OTHER_USER_EMAIL);
        var savedTodo = todoRepository.save(Todo.builder()
                .title("Other's Todo")
                .user(otherUser)
                .completed(false)
                .build());

        mockMvc.perform(delete("/api/todos/" + savedTodo.getId())
                        .header(AUTH_HEADER, "Bearer " + jwtToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 404 when deleting non-existent todo")
    void deleteTodo_shouldReturn404_whenTodoDoesNotExist() throws Exception {
        mockMvc.perform(delete("/api/todos/99999")
                        .header(AUTH_HEADER, "Bearer " + jwtToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return empty list when user has no todos")
    void getAllTodos_shouldReturnEmptyList_whenNoTodos() throws Exception {
        mockMvc.perform(get("/api/todos")
                        .header(AUTH_HEADER, "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    private User savedUser(String username, String email) {
        return userRepository.save(User.builder()
                .username(username)
                .email(email)
                .password("password")
                .build());
    }
}
