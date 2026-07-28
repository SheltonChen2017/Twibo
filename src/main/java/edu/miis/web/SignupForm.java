package edu.miis.web;

import jakarta.validation.constraints.*;

public class SignupForm {
    @NotBlank
    @Pattern(regexp = "[A-Za-z0-9_]{3,30}", message = "Use 3-30 letters, numbers, or underscores")
    private String username;

    @NotBlank
    @Size(min = 8, max = 72, message = "Use 8-72 characters")
    private String password;

    @NotBlank
    @Size(min = 8, max = 72, message = "Use 8-72 characters")
    private String recoveryPhrase;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRecoveryPhrase() { return recoveryPhrase; }
    public void setRecoveryPhrase(String recoveryPhrase) { this.recoveryPhrase = recoveryPhrase; }
}
