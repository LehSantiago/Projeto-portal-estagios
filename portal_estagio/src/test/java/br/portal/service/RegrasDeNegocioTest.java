package br.portal.service;

import br.portal.app.Contexto;
import br.portal.excecao.AcessoNegadoException;
import br.portal.excecao.RegraNegocioException;
import br.portal.excecao.ValidacaoException;
import br.portal.model.Area;
import br.portal.model.Modalidade;
import br.portal.model.Usuario;
import br.portal.model.Vaga;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegrasDeNegocioTest {
    private Contexto ctx;
    private Usuario admin;
    private Usuario empresaA;
    private Usuario empresaB;
    private Usuario ana;
    private Usuario bruno;
    private Area tecnologia;
    private Vaga vagaDaA;

    @BeforeEach
    void preparar() {
        ctx = Contexto.criarEmMemoria();
        admin = ctx.usuarioService.cadastrarAdministradorDemonstracao("Admin", "admin@t.com");
        empresaA = ctx.usuarioService.cadastrarEmpresa("RH A", "a@t.com", "Empresa A");
        empresaB = ctx.usuarioService.cadastrarEmpresa("RH B", "b@t.com", "Empresa B");
        ana = ctx.usuarioService.cadastrarEstudante("Ana", "ana@t.com", "Computação");
        bruno = ctx.usuarioService.cadastrarEstudante("Bruno", "bruno@t.com", "Design");
        tecnologia = ctx.areaService.cadastrar(admin, "Tecnologia");
        vagaDaA = ctx.vagaService.publicar(empresaA, "Estágio Java", "Backend", tecnologia.getId(),
                "São Paulo", Modalidade.HIBRIDA, 30);
    }

    // ---- os três testes obrigatórios ----

    @Test
    void candidaturaDuplicadaEhRejeitada() {
        ctx.candidaturaService.candidatar(ana, vagaDaA.getId());
        assertThrows(RegraNegocioException.class,
                () -> ctx.candidaturaService.candidatar(ana, vagaDaA.getId()));
        assertEquals(1, ctx.candidaturaService.listarMinhas(ana).size());
    }

    @Test
    void candidaturaEmVagaEncerradaEhRejeitada() {
        ctx.vagaService.encerrar(empresaA, vagaDaA.getId());
        assertThrows(RegraNegocioException.class,
                () -> ctx.candidaturaService.candidatar(ana, vagaDaA.getId()));
    }

    @Test
    void empresaNaoAcessaVagaDeOutraEmpresa() {
        long id = vagaDaA.getId();
        assertThrows(AcessoNegadoException.class,
                () -> ctx.vagaService.encerrar(empresaB, id));
        assertThrows(AcessoNegadoException.class,
                () -> ctx.vagaService.editar(empresaB, id, "x", "y", tecnologia.getId(), "z", Modalidade.REMOTA, 10));
        assertThrows(AcessoNegadoException.class,
                () -> ctx.candidaturaService.listarCandidatos(empresaB, id));
    }

    // ---- demais cenários do roteiro ----

    @Test
    void emailRepetidoEhRejeitado() {
        assertThrows(RegraNegocioException.class,
                () -> ctx.usuarioService.cadastrarEstudante("Outra Ana", "ANA@t.com", "Direito"));
    }

    @Test
    void vagaComCamposAusentesOuCargaInvalidaEhRejeitada() {
        assertThrows(ValidacaoException.class, () -> ctx.vagaService.publicar(
                empresaA, " ", "d", tecnologia.getId(), "SP", Modalidade.REMOTA, 20));
        assertThrows(ValidacaoException.class, () -> ctx.vagaService.publicar(
                empresaA, "t", "d", tecnologia.getId(), "SP", Modalidade.REMOTA, 0));
        assertThrows(ValidacaoException.class, () -> ctx.vagaService.publicar(
                empresaA, "t", "d", tecnologia.getId(), "SP", Modalidade.REMOTA, -5));
    }

    @Test
    void consultaPublicaMostraSomenteAbertasEAplicaFiltros() {
        Area design = ctx.areaService.cadastrar(admin, "Design");
        Vaga remota = ctx.vagaService.publicar(empresaB, "UX", "Interfaces", design.getId(), "Remoto", Modalidade.REMOTA, 20);
        ctx.vagaService.encerrar(empresaA, vagaDaA.getId());

        assertEquals(1, ctx.vagaService.consultarAbertas(null, null).size());
        assertEquals(remota, ctx.vagaService.consultarAbertas(design.getId(), Modalidade.REMOTA).get(0));
        assertTrue(ctx.vagaService.consultarAbertas(tecnologia.getId(), null).isEmpty());
        assertEquals(1, ctx.vagaService.consultarAbertas(null, null, "interfaces").size());
    }

    @Test
    void estudanteNaoVeCandidaturasDeOutroEEmpresaNaoSeCandidata() {
        ctx.candidaturaService.candidatar(ana, vagaDaA.getId());
        assertTrue(ctx.candidaturaService.listarMinhas(bruno).isEmpty());
        assertThrows(AcessoNegadoException.class,
                () -> ctx.candidaturaService.candidatar(empresaA, vagaDaA.getId()));
        assertThrows(AcessoNegadoException.class,
                () -> ctx.candidaturaService.listarMinhas(empresaA));
    }

    @Test
    void areaUsadaPorVagaNaoPodeSerExcluidaEApenasAdminGerenciaAreas() {
        assertThrows(RegraNegocioException.class, () -> ctx.areaService.excluir(admin, tecnologia.getId()));
        assertThrows(AcessoNegadoException.class, () -> ctx.areaService.cadastrar(empresaA, "Nova"));
        Area livre = ctx.areaService.cadastrar(admin, "Livre");
        ctx.areaService.excluir(admin, livre.getId());
        assertEquals(1, ctx.areaService.listar().size());
    }

    @Test
    void encerrarPreservaCandidaturasAnteriores() {
        ctx.candidaturaService.candidatar(ana, vagaDaA.getId());
        ctx.vagaService.encerrar(empresaA, vagaDaA.getId());
        assertEquals(1, ctx.candidaturaService.listarCandidatos(empresaA, vagaDaA.getId()).size());
    }

    @Test
    void contagensAdministrativasSoParaAdmin() {
        ctx.candidaturaService.candidatar(ana, vagaDaA.getId());
        var c = ctx.relatorioService.contagens(admin);
        assertEquals(2, c.estudantes());
        assertEquals(2, c.empresas());
        assertEquals(1, c.vagasAbertas());
        assertEquals(0, c.vagasEncerradas());
        assertEquals(1, c.candidaturas());
        assertThrows(AcessoNegadoException.class, () -> ctx.relatorioService.contagens(ana));
    }
}
