package aula.microservicos.login;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

@RestController
public class UsuarioController {

    private final LoginService service;

    public UsuarioController(LoginService service) {
        this.service = service;
    }

    @Operation(summary = "Efetua login", description = "Valida usuário e senha na tabela usuarios. Acesso público. "
            + "Na 3ª falha consecutiva o usuário é bloqueado.", tags = { "login" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário e senha válidos", content = @Content),
            @ApiResponse(responseCode = "401", description = "Usuário e senha inválidos ou usuário bloqueado", content = @Content),
            @ApiResponse(responseCode = "400", description = "Corpo da requisição inválido", content = @Content) })
    @PostMapping("/login")
    public ResponseEntity<String> login(@Valid @RequestBody Credenciais credenciais) {
        if (service.login(credenciais.username(), credenciais.senha())) {
            return new ResponseEntity<>("Login efetuado com sucesso!", HttpStatus.OK);
        }
        return new ResponseEntity<>("Usuário ou senha inválidos!", HttpStatus.UNAUTHORIZED);
    }

    @Operation(summary = "Troca a senha", description = "Troca a senha do usuário informado, conferindo a senha atual. Acesso público.",
            tags = { "login" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Senha alterada", content = @Content),
            @ApiResponse(responseCode = "400", description = "Nova senha inválida", content = @Content),
            @ApiResponse(responseCode = "401", description = "Senha atual inválida ou usuário bloqueado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Usuário não localizado", content = @Content) })
    @PutMapping("/trocarsenha/{userid}")
    public ResponseEntity<String> trocarSenha(@Parameter(description = "Username do usuário", example = "joao") @PathVariable String userid,
            @Valid @RequestBody TrocaSenha troca) {
        service.trocarSenha(userid, troca);
        return new ResponseEntity<>("Senha alterada com sucesso!", HttpStatus.OK);
    }

    @Operation(summary = "Cadastra usuário", description = "Cadastra um novo usuário. Requer a role ROLE_MANUTENCAO.",
            tags = { "usuarios" }, security = @SecurityRequirement(name = "basicAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Usuário cadastrado", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = UsuarioResposta.class)) }),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Sem a role ROLE_MANUTENCAO", content = @Content),
            @ApiResponse(responseCode = "409", description = "Usuário já cadastrado", content = @Content) })
    @PostMapping("/")
    public ResponseEntity<UsuarioResposta> cadastrar(@Valid @RequestBody Credenciais credenciais) {
        return new ResponseEntity<>(service.cadastrar(credenciais), HttpStatus.CREATED);
    }

    @Operation(summary = "Desbloqueia usuário", description = "Zera as tentativas e desbloqueia o usuário. Requer a role ROLE_ADMIN.",
            tags = { "usuarios" }, security = @SecurityRequirement(name = "basicAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário desbloqueado", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = UsuarioResposta.class)) }),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Sem a role ROLE_ADMIN", content = @Content),
            @ApiResponse(responseCode = "404", description = "Usuário não localizado", content = @Content) })
    @PutMapping("/unlock/{userid}")
    public ResponseEntity<UsuarioResposta> desbloquear(
            @Parameter(description = "Username do usuário", example = "joao") @PathVariable String userid) {
        return new ResponseEntity<>(service.desbloquear(userid), HttpStatus.OK);
    }

    @Operation(summary = "Lista usuários", description = "Retorna todos os usuários cadastrados (sem a senha). Requer a role ROLE_MANUTENCAO.",
            tags = { "usuarios" }, security = @SecurityRequirement(name = "basicAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de usuários", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = UsuarioResposta.class))) }),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Sem a role ROLE_MANUTENCAO", content = @Content) })
    @GetMapping("/")
    public ResponseEntity<List<UsuarioResposta>> listar() {
        return new ResponseEntity<>(service.listar(), HttpStatus.OK);
    }
}
