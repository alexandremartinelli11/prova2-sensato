package aula.microservicos.login;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoginService {

    // o usuario e bloqueado ao atingir esta quantidade de falhas consecutivas
    static final int MAX_TENTATIVAS = 3;

    private final UsuarioRepository usuarios;
    private final LoginAttemptRepository attempts;
    private final LoginMetrica metrica;

    public LoginService(UsuarioRepository usuarios, LoginAttemptRepository attempts, LoginMetrica metrica) {
        this.usuarios = usuarios;
        this.attempts = attempts;
        this.metrica = metrica;
    }

    @Transactional
    public boolean login(String username, String senha) {
        Optional<Usuario> usuario = usuarios.findById(username);
        // usuario inexistente conta como login invalido, mas nao gera registro em login_attempts
        if (usuario.isEmpty()) {
            metrica.loginInvalido();
            return false;
        }

        LoginAttempt tentativa = tentativaDe(username);
        LocalDateTime agora = LocalDateTime.now();

        if (tentativa.locked) {
            metrica.loginInvalido();
            return false;
        }

        if (usuario.get().senha.equals(senha)) {
            tentativa.failedAttempts = 0;
            tentativa.updatedAt = agora;
            attempts.save(tentativa);
            metrica.loginValido();
            return true;
        }

        tentativa.failedAttempts++;
        tentativa.lastFailedAt = agora;
        tentativa.updatedAt = agora;
        if (tentativa.failedAttempts >= MAX_TENTATIVAS) {
            tentativa.locked = true;
            tentativa.lockedAt = agora;
        }
        attempts.save(tentativa);
        metrica.loginInvalido();
        return false;
    }

    @Transactional
    public void trocarSenha(String username, TrocaSenha troca) {
        Usuario usuario = usuarios.findById(username).orElseThrow(UsuarioInexistenteException::new);
        if (tentativaDe(username).locked || !usuario.senha.equals(troca.atual())) {
            throw new CredenciaisInvalidasException();
        }
        usuario.senha = troca.nova();
        usuarios.save(usuario);
    }

    @Transactional
    public UsuarioResposta cadastrar(Credenciais credenciais) {
        if (usuarios.existsById(credenciais.username())) {
            throw new UsuarioExistenteException();
        }
        usuarios.save(new Usuario(credenciais.username(), credenciais.senha()));
        return new UsuarioResposta(credenciais.username(), false, 0);
    }

    @Transactional
    public UsuarioResposta desbloquear(String username) {
        if (!usuarios.existsById(username)) {
            throw new UsuarioInexistenteException();
        }
        LoginAttempt tentativa = tentativaDe(username);
        tentativa.failedAttempts = 0;
        tentativa.locked = false;
        tentativa.lockedAt = null;
        tentativa.updatedAt = LocalDateTime.now();
        attempts.save(tentativa);
        return new UsuarioResposta(username, false, 0);
    }

    public List<UsuarioResposta> listar() {
        List<UsuarioResposta> lista = new ArrayList<>();
        for (Usuario usuario : usuarios.findAll()) {
            LoginAttempt tentativa = tentativaDe(usuario.username);
            lista.add(new UsuarioResposta(usuario.username, tentativa.locked, tentativa.failedAttempts));
        }
        return lista;
    }

    // o registro em login_attempts e criado sob demanda, na primeira tentativa de login
    private LoginAttempt tentativaDe(String username) {
        return attempts.findById(username).orElseGet(() -> new LoginAttempt(username));
    }
}
