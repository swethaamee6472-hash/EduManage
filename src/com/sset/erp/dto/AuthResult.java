package com.sset.erp.dto;

import com.sset.erp.model.User;

/**
 * Data Transfer Object representing the outcome of an authentication attempt.
 * Encapsulates success flag, user-friendly message, and the authenticated User object.
 */
public class AuthResult {
    private final boolean success;
    private final String message;
    private final User user;

    public AuthResult(boolean success, String message, User user) {
        this.success = success;
        this.message = message;
        this.user = user;
    }

    public static AuthResult success(String message, User user) {
        return new AuthResult(true, message, user);
    }

    public static AuthResult failure(String message) {
        return new AuthResult(false, message, null);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public User getUser() {
        return user;
    }

    @Override
    public String toString() {
        return "AuthResult{" + "success=" + success + ", message='" + message + '\'' + ", user=" + user + '}';
    }
}
