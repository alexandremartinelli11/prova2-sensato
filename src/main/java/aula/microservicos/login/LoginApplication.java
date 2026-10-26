package aula.microservicos.login;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

@SpringBootApplication
@OpenAPIDefinition(info = @Info(title = "Controle de Login", version = "1.0",
        description = "MS-AV-02 - Segurança e Observabilidade: login, troca de senha, cadastro, desbloqueio e listagem de usuários"))
@SecurityScheme(name = "basicAuth", type = SecuritySchemeType.HTTP, scheme = "basic",
        description = "Operadores do sistema (JdbcUserDetailsManager): admin/admin (ROLE_ADMIN) e manutencao/manutencao (ROLE_MANUTENCAO)")
public class LoginApplication {

    public static void main(String[] args) {
        SpringApplication.run(LoginApplication.class, args);
    }

}
