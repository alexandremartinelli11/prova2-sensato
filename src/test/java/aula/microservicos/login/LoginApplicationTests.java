package aula.microservicos.login;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import io.micrometer.core.instrument.MeterRegistry;

@SpringBootTest
@Transactional
class LoginApplicationTests {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private MeterRegistry registry;

    private MockMvc mvc() {
        return MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    private static String json(String a, String va, String b, String vb) {
        return "{\"" + a + "\":\"" + va + "\",\"" + b + "\":\"" + vb + "\"}";
    }

    private ResultActions login(String username, String senha) throws Exception {
        return mvc().perform(post("/login").contentType(MediaType.APPLICATION_JSON)
                .content(json("username", username, "senha", senha)));
    }

    private double contador(String nome) {
        return registry.get(nome).counter().count();
    }

    private double bloqueados() {
        return registry.get("usuarios.bloqueados").gauge().value();
    }

    @Test
    void loginValidoEInvalido() throws Exception {
        double validos = contador("login.validos");
        double invalidos = contador("login.invalidos");

        login("joao", "123").andExpect(status().isOk());
        login("joao", "errada").andExpect(status().isUnauthorized());
        login("naoexiste", "123").andExpect(status().isUnauthorized());

        assertEquals(validos + 1, contador("login.validos"));
        assertEquals(invalidos + 2, contador("login.invalidos"));
    }

    @Test
    void bloqueiaNaTerceiraFalha() throws Exception {
        double bloqueadosAntes = bloqueados();

        login("maria", "x").andExpect(status().isUnauthorized());
        login("maria", "x").andExpect(status().isUnauthorized());
        // ainda nao bloqueado apos 2 falhas
        login("maria", "456").andExpect(status().isOk());

        login("maria", "x").andExpect(status().isUnauthorized());
        login("maria", "x").andExpect(status().isUnauthorized());
        login("maria", "x").andExpect(status().isUnauthorized());
        // bloqueado: mesmo com a senha correta retorna 401
        login("maria", "456").andExpect(status().isUnauthorized());

        assertEquals(bloqueadosAntes + 1, bloqueados());
        mvc().perform(get("/").with(httpBasic("manutencao", "manutencao")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.username=='maria')].locked").value(true))
                .andExpect(jsonPath("$[?(@.username=='maria')].failedAttempts").value(3));
    }

    @Test
    void desbloqueioSomenteAdmin() throws Exception {
        for (int i = 0; i < 3; i++) {
            login("joao", "x");
        }
        login("joao", "123").andExpect(status().isUnauthorized());

        mvc().perform(put("/unlock/joao")).andExpect(status().isUnauthorized());
        mvc().perform(put("/unlock/joao").with(httpBasic("manutencao", "manutencao"))).andExpect(status().isForbidden());
        mvc().perform(put("/unlock/naoexiste").with(httpBasic("admin", "admin"))).andExpect(status().isNotFound());
        mvc().perform(put("/unlock/joao").with(httpBasic("admin", "admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.locked").value(false));

        login("joao", "123").andExpect(status().isOk());
    }

    @Test
    void trocaDeSenha() throws Exception {
        mvc().perform(put("/trocarsenha/joao").contentType(MediaType.APPLICATION_JSON).content(json("atual", "errada", "nova", "abc")))
                .andExpect(status().isUnauthorized());
        mvc().perform(put("/trocarsenha/naoexiste").contentType(MediaType.APPLICATION_JSON).content(json("atual", "123", "nova", "abc")))
                .andExpect(status().isNotFound());
        mvc().perform(put("/trocarsenha/joao").contentType(MediaType.APPLICATION_JSON).content(json("atual", "123", "nova", "senhamuitolonga")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.nova").exists());
        mvc().perform(put("/trocarsenha/joao").contentType(MediaType.APPLICATION_JSON).content(json("atual", "123", "nova", "abc")))
                .andExpect(status().isOk());

        login("joao", "123").andExpect(status().isUnauthorized());
        login("joao", "abc").andExpect(status().isOk());
    }

    @Test
    void trocaDeSenhaDeUsuarioBloqueadoRetorna401() throws Exception {
        for (int i = 0; i < 3; i++) {
            login("maria", "x");
        }
        mvc().perform(put("/trocarsenha/maria").contentType(MediaType.APPLICATION_JSON).content(json("atual", "456", "nova", "abc")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void cadastroSomenteManutencao() throws Exception {
        String novo = json("username", "pedro", "senha", "789");

        mvc().perform(post("/").contentType(MediaType.APPLICATION_JSON).content(novo)).andExpect(status().isUnauthorized());
        mvc().perform(post("/").with(httpBasic("admin", "admin")).contentType(MediaType.APPLICATION_JSON).content(novo))
                .andExpect(status().isForbidden());
        mvc().perform(post("/").with(httpBasic("manutencao", "errada")).contentType(MediaType.APPLICATION_JSON).content(novo))
                .andExpect(status().isUnauthorized());
        mvc().perform(post("/").with(httpBasic("manutencao", "manutencao")).contentType(MediaType.APPLICATION_JSON).content(novo))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("pedro"));
        mvc().perform(post("/").with(httpBasic("manutencao", "manutencao")).contentType(MediaType.APPLICATION_JSON).content(novo))
                .andExpect(status().isConflict());
        mvc().perform(post("/").with(httpBasic("manutencao", "manutencao")).contentType(MediaType.APPLICATION_JSON)
                .content(json("username", "", "senha", "senhamuitolonga")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.username").exists())
                .andExpect(jsonPath("$.senha").exists());

        login("pedro", "789").andExpect(status().isOk());
    }

    @Test
    void listagemSomenteManutencaoESemSenha() throws Exception {
        mvc().perform(get("/")).andExpect(status().isUnauthorized());
        mvc().perform(get("/").with(httpBasic("admin", "admin"))).andExpect(status().isForbidden());
        mvc().perform(get("/").with(httpBasic("manutencao", "manutencao")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.username=='joao')]").exists())
                .andExpect(jsonPath("$[0].senha").doesNotExist());
    }

    @Test
    void swaggerPublico() throws Exception {
        mvc().perform(get("/v3/api-docs")).andExpect(status().isOk());
    }
}
