package aula.microservicos.login;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GerenciamentoException {

    @ExceptionHandler(UsuarioInexistenteException.class)
    public ResponseEntity<String> handleUsuarioInexistente(UsuarioInexistenteException e) {
        return new ResponseEntity<>("Usuário não localizado!", HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UsuarioExistenteException.class)
    public ResponseEntity<String> handleUsuarioExistente(UsuarioExistenteException e) {
        return new ResponseEntity<>("Usuário já cadastrado!", HttpStatus.CONFLICT);
    }

    @ExceptionHandler(CredenciaisInvalidasException.class)
    public ResponseEntity<String> handleCredenciaisInvalidas(CredenciaisInvalidasException e) {
        return new ResponseEntity<>("Senha atual inválida ou usuário bloqueado!", HttpStatus.UNAUTHORIZED);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String, String> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
        return errors;
    }
}
