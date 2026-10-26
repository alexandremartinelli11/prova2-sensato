package aula.microservicos.login;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "login_attempts")
public class LoginAttempt {

    @Id
    public String username;

    public int failedAttempts;

    public boolean locked;

    public LocalDateTime lastFailedAt;

    public LocalDateTime lockedAt;

    public LocalDateTime updatedAt;

    public LoginAttempt() {
    }

    public LoginAttempt(String username) {
        this.username = username;
    }
}
