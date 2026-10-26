package aula.microservicos.login;

import org.springframework.data.repository.CrudRepository;

public interface LoginAttemptRepository extends CrudRepository<LoginAttempt, String> {

    long countByLockedTrue();
}
