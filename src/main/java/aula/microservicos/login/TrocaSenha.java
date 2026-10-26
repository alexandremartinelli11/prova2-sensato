package aula.microservicos.login;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(name = "TrocaSenha", description = "Senha atual e nova senha")
public record TrocaSenha(

        @Schema(description = "Senha atual", example = "123", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "A senha atual é obrigatória")
        String atual,

        @Schema(description = "Nova senha (máximo 10 caracteres)", example = "nova123", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "A nova senha é obrigatória")
        @Size(max = 10, message = "A nova senha deve ter no máximo 10 caracteres")
        String nova) {
}
