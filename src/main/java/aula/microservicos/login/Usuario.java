package aula.microservicos.login;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    public String username;

    // o schema do enunciado (VARCHAR(10)) nao comporta hash, por isso a senha fica em texto puro
    public String senha;

    public Usuario() {
    }

    public Usuario(String username, String senha) {
        this.username = username;
        this.senha = senha;
    }
}
