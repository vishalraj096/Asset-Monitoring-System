package com.app.assetmonitoringsystem.dto;

/**
 * DTO for authentication responses containing JWT token.
 */
public class AuthResponse {

    private String token;
    private String email;
    private String role;
    private String message;

    // Constructors
    public AuthResponse() {}

    public AuthResponse(String token, String email, String role, String message) {
        this.token = token;
        this.email = email;
        this.role = role;
        this.message = message;
    }

    // Getters and Setters
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
