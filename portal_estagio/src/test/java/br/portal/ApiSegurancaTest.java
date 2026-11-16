package br.portal;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes da API REST com o Spring Security REAL (filtros, sessão, BCrypt, CSRF, perfis).
 * Perfil "memoria": os DAOs ficam em memória, então não é preciso PostgreSQL.
 * Todos os dados de cada teste usam e-mails/áreas com sufixo único, para os testes não interferirem.
 */
@SpringBootTest(properties = {
        "portal.admin.email=admin@portal.test",
        "portal.admin.password=Admin12345"})
@AutoConfigureMockMvc
@ActiveProfiles("memoria")
class ApiSegurancaTest {
    private static final String SENHA = "Senha1234";

    @Autowired
    private MockMvc mvc;

    // ------------------------------------------------------------ ferramentas dos testes

    private static String unico(String prefixo) {
        return prefixo + "-" + UUID.randomUUID();
    }

    private static String email(String prefixo) {
        return unico(prefixo) + "@teste.com";
    }

    private ResultActions enviar(MockHttpServletRequestBuilder req, MockHttpSession sessao) throws Exception {
        if (sessao != null) {
            req.session(sessao);
        }
        return mvc.perform(req);
    }

    private ResultActions postJson(String url, String json, MockHttpSession sessao) throws Exception {
        return enviar(post(url).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(json), sessao);
    }

    private ResultActions putJson(String url, String json, MockHttpSession sessao) throws Exception {
        return enviar(put(url).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(json), sessao);
    }

    private ResultActions getComo(String url, MockHttpSession sessao) throws Exception {
        return enviar(get(url), sessao);
    }

    private static String jsonEstudante(String email, String senha) {
        return """
                {"nome":"Aluno Teste","email":"%s","curso":"Computação","senha":"%s"}""".formatted(email, senha);
    }

    private static String jsonEmpresa(String email, String senha) {
        return """
                {"nome":"RH Teste","email":"%s","nomeEmpresa":"Empresa Teste","senha":"%s"}""".formatted(email, senha);
    }

    private static String jsonVaga(long areaId, int carga) {
        return """
                {"titulo":"Estágio Java","descricao":"Apoio ao backend","areaId":%d,
                 "localizacao":"São Paulo","modalidade":"HIBRIDA","cargaHorariaSemanal":%d}""".formatted(areaId, carga);
    }

    private MockHttpSession login(String email, String senha) throws Exception {
        MvcResult r = postJson("/api/auth/login",
                """
                {"email":"%s","senha":"%s"}""".formatted(email, senha), null)
                .andExpect(status().isOk()).andReturn();
        return (MockHttpSession) r.getRequest().getSession(false);
    }

    private MockHttpSession novoEstudante() throws Exception {
        String e = email("aluno");
        postJson("/api/auth/cadastro/estudante", jsonEstudante(e, SENHA), null).andExpect(status().isCreated());
        return login(e, SENHA);
    }

    private MockHttpSession novaEmpresa() throws Exception {
        String e = email("empresa");
        postJson("/api/auth/cadastro/empresa", jsonEmpresa(e, SENHA), null).andExpect(status().isCreated());
        return login(e, SENHA);
    }

    private MockHttpSession admin() throws Exception {
        return login("admin@portal.test", "Admin12345");
    }

    private static long idDe(MvcResult r) throws Exception {
        return ((Number) JsonPath.read(r.getResponse().getContentAsString(), "$.id")).longValue();
    }

    private long novaArea() throws Exception {
        MvcResult r = postJson("/api/admin/areas", "{\"nome\":\"" + unico("Area") + "\"}", admin())
                .andExpect(status().isCreated()).andReturn();
        return idDe(r);
    }

    private long novaVaga(MockHttpSession empresa) throws Exception {
        MvcResult r = postJson("/api/vagas", jsonVaga(novaArea(), 30), empresa)
                .andExpect(status().isCreated()).andReturn();
        return idDe(r);
    }

    // ------------------------------------------------------------ cadastro e login

    @Test
    void cadastroValidoDevolve201SemExporSenha() throws Exception {
        String corpo = postJson("/api/auth/cadastro/estudante", jsonEstudante(email("ok"), SENHA), null)
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        assertFalse(corpo.toLowerCase().contains("senha"));
        assertFalse(corpo.contains("$2"));
    }

    @Test
    void emailDuplicadoEhRejeitadoComConflito() throws Exception {
        String e = email("dup");
        postJson("/api/auth/cadastro/estudante", jsonEstudante(e, SENHA), null).andExpect(status().isCreated());
        postJson("/api/auth/cadastro/empresa", jsonEmpresa(e.toUpperCase(), SENHA), null)
                .andExpect(status().isConflict());
    }

    @Test
    void cadastroInvalidoDevolve400() throws Exception {
        postJson("/api/auth/cadastro/estudante", jsonEstudante("nao-e-email", SENHA), null)
                .andExpect(status().isBadRequest());
        postJson("/api/auth/cadastro/estudante", jsonEstudante(email("fraca"), "curta1"), null)
                .andExpect(status().isBadRequest());
        postJson("/api/auth/cadastro/empresa", jsonEmpresa(email("fraca"), "somenteletras"), null)
                .andExpect(status().isBadRequest());
    }

    @Test
    void loginCorretoIdentificaOUsuarioSemDevolverSenha() throws Exception {
        String e = email("login");
        postJson("/api/auth/cadastro/estudante", jsonEstudante(e, SENHA), null).andExpect(status().isCreated());

        MvcResult r = postJson("/api/auth/login", "{\"email\":\"" + e + "\",\"senha\":\"" + SENHA + "\"}", null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.perfil").value("ESTUDANTE"))
                .andReturn();
        String corpo = r.getResponse().getContentAsString();
        assertFalse(corpo.toLowerCase().contains("senha"));

        getComo("/api/auth/me", (MockHttpSession) r.getRequest().getSession(false))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(e));
    }

    @Test
    void loginComSenhaIncorretaDevolve401() throws Exception {
        String e = email("errada");
        postJson("/api/auth/cadastro/estudante", jsonEstudante(e, SENHA), null).andExpect(status().isCreated());
        postJson("/api/auth/login", "{\"email\":\"" + e + "\",\"senha\":\"SenhaErrada99\"}", null)
                .andExpect(status().isUnauthorized());
        postJson("/api/auth/login", "{\"email\":\"" + email("naoexiste") + "\",\"senha\":\"" + SENHA + "\"}", null)
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logoutEncerraASessao() throws Exception {
        MockHttpSession sessao = novoEstudante();
        getComo("/api/candidaturas/minhas", sessao).andExpect(status().isOk());

        postJson("/api/auth/logout", "", sessao).andExpect(status().isNoContent());

        getComo("/api/candidaturas/minhas", sessao).andExpect(status().isUnauthorized());
    }

    @Test
    void postSemTokenCsrfEhBloqueado() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"a@b.com\",\"senha\":\"Senha1234\"}"))
                .andExpect(status().isForbidden());
    }

    // ------------------------------------------------------------ acesso sem login e perfil inadequado

    @Test
    void rotasPrivadasSemAutenticacaoDevolvem401() throws Exception {
        getComo("/api/candidaturas/minhas", null).andExpect(status().isUnauthorized());
        getComo("/api/vagas/minhas", null).andExpect(status().isUnauthorized());
        getComo("/api/vagas/1/candidatos", null).andExpect(status().isUnauthorized());
        getComo("/api/admin/relatorios", null).andExpect(status().isUnauthorized());
        getComo("/api/auth/me", null).andExpect(status().isUnauthorized());
        postJson("/api/vagas", jsonVaga(1, 20), null).andExpect(status().isUnauthorized());
        postJson("/api/vagas/1/candidaturas", "", null).andExpect(status().isUnauthorized());
    }

    @Test
    void consultaPublicaDeVagasEAreasNaoExigeLogin() throws Exception {
        getComo("/api/vagas", null).andExpect(status().isOk());
        getComo("/api/areas", null).andExpect(status().isOk());
    }

    @Test
    void perfilInadequadoDevolve403() throws Exception {
        MockHttpSession estudante = novoEstudante();
        MockHttpSession empresa = novaEmpresa();
        long vagaId = novaVaga(empresa);

        // estudante não publica vaga nem vê "minhas vagas"
        postJson("/api/vagas", jsonVaga(novaArea(), 20), estudante).andExpect(status().isForbidden());
        getComo("/api/vagas/minhas", estudante).andExpect(status().isForbidden());
        // empresa não se candidata nem vê candidaturas de estudante
        postJson("/api/vagas/" + vagaId + "/candidaturas", "", empresa).andExpect(status().isForbidden());
        getComo("/api/candidaturas/minhas", empresa).andExpect(status().isForbidden());
        // somente administrador gerencia áreas e relatórios
        postJson("/api/admin/areas", "{\"nome\":\"Invasao\"}", estudante).andExpect(status().isForbidden());
        postJson("/api/admin/areas", "{\"nome\":\"Invasao\"}", empresa).andExpect(status().isForbidden());
        getComo("/api/admin/areas", estudante).andExpect(status().isForbidden());
        getComo("/api/admin/relatorios", empresa).andExpect(status().isForbidden());
    }

    @Test
    void cadastroPublicoNaoCriaAdministrador() throws Exception {
        String e = email("tentativa");
        // o cliente tenta mandar "perfil": o campo simplesmente não existe no DTO e é ignorado
        postJson("/api/auth/cadastro/estudante",
                """
                {"nome":"Esperto","email":"%s","curso":"Direito","senha":"%s","perfil":"ADMINISTRADOR"}""".formatted(e, SENHA),
                null).andExpect(status().isCreated());

        MockHttpSession sessao = login(e, SENHA);
        getComo("/api/auth/me", sessao).andExpect(jsonPath("$.perfil").value("ESTUDANTE"));
        getComo("/api/admin/relatorios", sessao).andExpect(status().isForbidden());

        // e não existe rota pública de cadastro de administrador
        int codigo = postJson("/api/auth/cadastro/administrador", jsonEmpresa(email("adm"), SENHA), null)
                .andReturn().getResponse().getStatus();
        assertTrue(codigo >= 400 && codigo < 500, "esperava erro 4xx, veio " + codigo);
    }

    // ------------------------------------------------------------ regras de negócio pela API

    @Test
    void candidaturaDuplicadaDevolve409() throws Exception {
        MockHttpSession empresa = novaEmpresa();
        MockHttpSession estudante = novoEstudante();
        long vagaId = novaVaga(empresa);

        postJson("/api/vagas/" + vagaId + "/candidaturas", "", estudante).andExpect(status().isCreated());
        postJson("/api/vagas/" + vagaId + "/candidaturas", "", estudante).andExpect(status().isConflict());
        getComo("/api/candidaturas/minhas", estudante).andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void candidaturaEmVagaEncerradaDevolve409() throws Exception {
        MockHttpSession empresa = novaEmpresa();
        MockHttpSession estudante = novoEstudante();
        long vagaId = novaVaga(empresa);

        enviar(patch("/api/vagas/" + vagaId + "/encerrar").with(csrf()), empresa)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.situacao").value("ENCERRADA"));

        postJson("/api/vagas/" + vagaId + "/candidaturas", "", estudante).andExpect(status().isConflict());
    }

    @Test
    void empresaNaoAcessaNemAlteraVagaDeOutraEmpresa() throws Exception {
        MockHttpSession dona = novaEmpresa();
        MockHttpSession outra = novaEmpresa();
        long vagaId = novaVaga(dona);

        getComo("/api/vagas/" + vagaId + "/candidatos", outra).andExpect(status().isForbidden());
        putJson("/api/vagas/" + vagaId, jsonVaga(novaArea(), 25), outra).andExpect(status().isForbidden());
        enviar(patch("/api/vagas/" + vagaId + "/encerrar").with(csrf()), outra).andExpect(status().isForbidden());

        // a dona continua com acesso
        getComo("/api/vagas/" + vagaId + "/candidatos", dona).andExpect(status().isOk());
        putJson("/api/vagas/" + vagaId, jsonVaga(novaArea(), 25), dona).andExpect(status().isOk());
    }

    @Test
    void estudanteVeSomenteAsPropriasCandidaturas() throws Exception {
        MockHttpSession empresa = novaEmpresa();
        MockHttpSession ana = novoEstudante();
        MockHttpSession bruno = novoEstudante();
        long vagaId = novaVaga(empresa);

        postJson("/api/vagas/" + vagaId + "/candidaturas", "", ana).andExpect(status().isCreated());

        getComo("/api/candidaturas/minhas", ana)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].vagaId").value(vagaId));
        getComo("/api/candidaturas/minhas", bruno)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void validacaoDeVagaDevolve400() throws Exception {
        MockHttpSession empresa = novaEmpresa();
        long areaId = novaArea();
        postJson("/api/vagas", jsonVaga(areaId, 0), empresa).andExpect(status().isBadRequest());
        postJson("/api/vagas", jsonVaga(areaId, -5), empresa).andExpect(status().isBadRequest());
        postJson("/api/vagas", "{\"titulo\":\"\",\"descricao\":\"\"}", empresa).andExpect(status().isBadRequest());
        getComo("/api/vagas/0", null).andExpect(status().isBadRequest());
        getComo("/api/vagas?modalidade=INEXISTENTE", null).andExpect(status().isBadRequest());
        getComo("/api/vagas/999999", null).andExpect(status().isNotFound());
    }

    @Test
    void administradorGerenciaAreasEAreaEmUsoNaoSaiDoSistema() throws Exception {
        MockHttpSession admin = admin();
        MockHttpSession empresa = novaEmpresa();

        long areaId = idDe(postJson("/api/admin/areas", "{\"nome\":\"" + unico("Area") + "\"}", admin)
                .andExpect(status().isCreated()).andReturn());
        getComo("/api/admin/areas", admin).andExpect(status().isOk());
        putJson("/api/admin/areas/" + areaId, "{\"nome\":\"" + unico("Renomeada") + "\"}", admin)
                .andExpect(status().isOk());

        postJson("/api/vagas", jsonVaga(areaId, 20), empresa).andExpect(status().isCreated());
        enviar(delete("/api/admin/areas/" + areaId).with(csrf()), admin).andExpect(status().isConflict());

        long livre = novaArea();
        enviar(delete("/api/admin/areas/" + livre).with(csrf()), admin).andExpect(status().isNoContent());

        getComo("/api/admin/relatorios", admin).andExpect(status().isOk())
                .andExpect(jsonPath("$.estudantes").exists());
    }

    // ------------------------------------------------------------ dados sensíveis

    @Test
    void respostasNuncaExpoemSenhaHashOuDetalhesInternos() throws Exception {
        MockHttpSession empresa = novaEmpresa();
        MockHttpSession estudante = novoEstudante();
        long vagaId = novaVaga(empresa);
        postJson("/api/vagas/" + vagaId + "/candidaturas", "", estudante).andExpect(status().isCreated());

        // respostas de sucesso: não pode haver campo nem valor relacionado a senha/hash
        String[] sucesso = {
                getComo("/api/auth/me", estudante).andReturn().getResponse().getContentAsString(),
                getComo("/api/vagas", null).andReturn().getResponse().getContentAsString(),
                getComo("/api/vagas/" + vagaId, null).andReturn().getResponse().getContentAsString(),
                getComo("/api/vagas/minhas", empresa).andReturn().getResponse().getContentAsString(),
                getComo("/api/vagas/" + vagaId + "/candidatos", empresa).andReturn().getResponse().getContentAsString(),
                getComo("/api/candidaturas/minhas", estudante).andReturn().getResponse().getContentAsString(),
        };
        for (String corpo : sucesso) {
            assertFalse(corpo.toLowerCase().contains("senha"), "vazou 'senha': " + corpo);
            assertFalse(corpo.contains("$2a$") || corpo.contains("$2b$"), "vazou hash: " + corpo);
        }

        // respostas de erro: sem nome de exceção, pacote Java ou SQL
        String[] erros = {
                postJson("/api/auth/login", "{\"email\":\"x@y.com\",\"senha\":\"Qualquer123\"}", null)
                        .andReturn().getResponse().getContentAsString(),
                getComo("/api/vagas/999999", null).andReturn().getResponse().getContentAsString(),
                postJson("/api/auth/cadastro/estudante", jsonEstudante("invalido", "x"), null)
                        .andReturn().getResponse().getContentAsString(),
        };
        for (String corpo : erros) {
            String minusculo = corpo.toLowerCase();
            assertFalse(corpo.contains("$2a$") || corpo.contains("$2b$"), "vazou hash: " + corpo);
            assertFalse(minusculo.contains("exception") || minusculo.contains("org.springframework")
                    || minusculo.contains("java.") || minusculo.contains("sql"), "vazou detalhe interno: " + corpo);
        }
    }
}
