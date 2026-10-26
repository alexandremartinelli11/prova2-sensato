package aula.microservicos.login;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "Usuario", description = "Usuário cadastrado (a senha nunca é devolvida)")
public record UsuarioResposta(

        @Schema(description = "Nome do usuário", example = "joao")
        String username,

        @Schema(description = "Indica se o usuário está bloqueado por excesso de tentativas", example = "false")
        boolean locked,

        @Schema(description = "Quantidade de tentativas de login inválidas consecutivas", example = "0")
        int failedAttempts) {
}
