package aula.microservicos.login;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(name = "Credenciais", description = "Usuário e senha (login e cadastro)")
public record Credenciais(

        @Schema(description = "Nome do usuário", example = "joao", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "O username é obrigatório")
        @Size(max = 100, message = "O username deve ter no máximo 100 caracteres")
        String username,

        @Schema(description = "Senha do usuário (máximo 10 caracteres)", example = "123", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "A senha é obrigatória")
        @Size(max = 10, message = "A senha deve ter no máximo 10 caracteres")
        String senha) {
}
