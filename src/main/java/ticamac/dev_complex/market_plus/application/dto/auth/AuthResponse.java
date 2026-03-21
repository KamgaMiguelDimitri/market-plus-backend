package ticamac.dev_complex.market_plus.application.dto.auth;

import java.util.UUID;

public class AuthResponse {

    private String token;
    private String type = "Bearer";
    private UUID userId;
    private String email;
    private String firstName;
    private String lastName;
    private String role;

    public AuthResponse(String token, UUID userId, String email,
            String firstName, String lastName, String role) {
        this.token = token;
        this.userId = userId;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
    }

    public String getToken() {
        return token;
    }

    public String getType() {
        return type;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getRole() {
        return role;
    }
}