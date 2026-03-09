package com.marszalek.todo.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorMessage {

    USERNAME_ALREADY_TAKEN("Username is already taken"),
    EMAIL_ALREADY_REGISTERED("Email is already registered"),
    USER_NOT_FOUND("User not found"),
    INVALID_CREDENTIALS("Invalid username or password");

    private final String message;

    @Override
    public String toString() {
        return message;
    }
}
