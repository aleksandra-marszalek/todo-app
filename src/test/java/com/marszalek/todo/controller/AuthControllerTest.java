package com.marszalek.todo.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.marszalek.todo.AbstractIntegrationTest;
import com.marszalek.todo.model.dto.LoginRequest;
import com.marszalek.todo.model.dto.RegisterRequest;
import com.marszalek.todo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@Transactional
@DisplayName("Authentication Controller Integration Tests")
class AuthControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Should register user with valid data")
    void register_shouldSucceed_withValidData() throws Exception {
        // Given
        var registerRequest = new RegisterRequest();
        registerRequest.setUsername("newuser");
        registerRequest.setEmail("newuser@example.com");
        registerRequest.setPassword("password123");

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.email").value("newuser@example.com"));
    }

    @Test
    @DisplayName("Should fail to register with duplicate username")
    void register_shouldFail_withDuplicateUsername() throws Exception {
        // Given - create first user
        var firstUser = new RegisterRequest();
        firstUser.setUsername("existinguser");
        firstUser.setEmail("first@example.com");
        firstUser.setPassword("password123");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(firstUser)));

        // When - try to register with same username
        var duplicateUser = new RegisterRequest();
        duplicateUser.setUsername("existinguser");
        duplicateUser.setEmail("different@example.com");
        duplicateUser.setPassword("password123");

        // Then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateUser)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Username already taken")));
    }

    @Test
    @DisplayName("Should fail to register with duplicate email")
    void register_shouldFail_withDuplicateEmail() throws Exception {
        // Given - create first user
        var firstUser = new RegisterRequest();
        firstUser.setUsername("user1");
        firstUser.setEmail("same@example.com");
        firstUser.setPassword("password123");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(firstUser)));

        // When - try to register with same email
        var duplicateUser = new RegisterRequest();
        duplicateUser.setUsername("user2");
        duplicateUser.setEmail("same@example.com");
        duplicateUser.setPassword("password123");

        // Then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateUser)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Email already registered")));
    }

    @Test
    @DisplayName("Should fail to register with invalid email format")
    void register_shouldFail_withInvalidEmail() throws Exception {
        // Given
        var invalidRequest = new RegisterRequest();
        invalidRequest.setUsername("testuser");
        invalidRequest.setEmail("not-an-email");
        invalidRequest.setPassword("password123");

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should fail to register with short username")
    void register_shouldFail_withShortUsername() throws Exception {
        // Given
        var invalidRequest = new RegisterRequest();
        invalidRequest.setUsername("ab"); // Less than 3 characters
        invalidRequest.setEmail("test@example.com");
        invalidRequest.setPassword("password123");

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should fail to register with short password")
    void register_shouldFail_withShortPassword() throws Exception {
        // Given
        var invalidRequest = new RegisterRequest();
        invalidRequest.setUsername("testuser");
        invalidRequest.setEmail("test@example.com");
        invalidRequest.setPassword("12345"); // Less than 6 characters

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should fail to register with blank fields")
    void register_shouldFail_withBlankFields() throws Exception {
        // Given
        var emptyRequest = new RegisterRequest();
        emptyRequest.setUsername("");
        emptyRequest.setEmail("");
        emptyRequest.setPassword("");

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emptyRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should login successfully with correct credentials")
    void login_shouldSucceed_withCorrectCredentials() throws Exception {
        // Given - register a user first
        var registerRequest = new RegisterRequest();
        registerRequest.setUsername("testuser");
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password123");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)));

        // When - login with correct credentials
        var loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");

        // Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    @DisplayName("Should fail to login with wrong password")
    void login_shouldFail_withWrongPassword() throws Exception {
        // Given - register a user first
        var registerRequest = new RegisterRequest();
        registerRequest.setUsername("testuser");
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("correctpassword");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)));

        // When - login with wrong password
        var loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("wrongpassword");

        // Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(containsString("Invalid username or password")));
    }

    @Test
    @DisplayName("Should fail to login with non-existent username")
    void login_shouldFail_withNonExistentUser() throws Exception {
        // Given
        var loginRequest = new LoginRequest();
        loginRequest.setUsername("nonexistent");
        loginRequest.setPassword("password123");

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(containsString("Invalid username or password")));
    }

    @Test
    @DisplayName("Should fail to login with blank credentials")
    void login_shouldFail_withBlankCredentials() throws Exception {
        // Given
        var loginRequest = new LoginRequest();
        loginRequest.setUsername("");
        loginRequest.setPassword("");

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return valid JWT token that can be used for authenticated requests")
    void login_shouldReturnValidJwtToken() throws Exception {
        // Given - register and login
        var registerRequest = new RegisterRequest();
        registerRequest.setUsername("testuser");
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password123");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)));

        var loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");

        // When - get token from login
        var loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        var responseMap = objectMapper.readValue(loginResponse, java.util.Map.class);
        var token = (String) responseMap.get("token");

        // Then - use token to access protected endpoint
        mockMvc.perform(post("/api/todos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Test Todo\",\"completed\":false}"))
                .andExpect(status().isOk());
    }
}