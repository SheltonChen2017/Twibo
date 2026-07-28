package edu.miis.web;

import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;

public class SignupForm {
    @NotBlank @Pattern(regexp = "[A-Za-z0-9_]{3,30}", message = "Use 3–30 letters, numbers, or underscores")
    private String username;
    @NotBlank @Size(min = 8, max = 72)
    private String password;
    @NotNull @Past @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate birthday;
    @NotBlank @Size(max = 120)
    private String securityQuestion;
    @NotBlank @Size(max = 100)
    private String securityAnswer;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public LocalDate getBirthday() { return birthday; }
    public void setBirthday(LocalDate birthday) { this.birthday = birthday; }
    public String getSecurityQuestion() { return securityQuestion; }
    public void setSecurityQuestion(String securityQuestion) { this.securityQuestion = securityQuestion; }
    public String getSecurityAnswer() { return securityAnswer; }
    public void setSecurityAnswer(String securityAnswer) { this.securityAnswer = securityAnswer; }
}
