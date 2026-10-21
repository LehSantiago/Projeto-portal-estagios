package br.portal.service;

import br.portal.dao.CandidaturaDao;
import br.portal.excecao.RegraNegocioException;
import br.portal.model.Candidatura;
import br.portal.model.Perfil;
import br.portal.model.Usuario;
import br.portal.model.Vaga;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

public class CandidaturaService {
    private final CandidaturaDao candidaturaDao;
    private final VagaService vagaService;
    private final Clock relogio;

    public CandidaturaService(CandidaturaDao candidaturaDao, VagaService vagaService) {
        this(candidaturaDao, vagaService, Clock.systemDefaultZone());
    }

    /** O relógio é injetável para facilitar testes. */
    public CandidaturaService(CandidaturaDao candidaturaDao, VagaService vagaService, Clock relogio) {
        this.candidaturaDao = candidaturaDao;
        this.vagaService = vagaService;
        this.relogio = relogio;
    }

    public Candidatura candidatar(Usuario ator, long vagaId) {
        Validacao.exigirPerfil(ator, Perfil.ESTUDANTE, "se candidatar a vagas");
        Vaga vaga = vagaService.buscarPorId(vagaId);
        if (!vaga.isAberta()) {
            throw new RegraNegocioException("A vaga '" + vaga.getTitulo() + "' está encerrada.");
        }
        if (candidaturaDao.existePorEstudanteEVaga(ator.getId(), vagaId)) {
            throw new RegraNegocioException("Você já se candidatou a esta vaga.");
        }
        return candidaturaDao.salvar(new Candidatura(null, ator, vaga, LocalDate.now(relogio)));
    }

    /** O estudante só enxerga as próprias candidaturas (o id vem do ator, nunca de parâmetro). */
    public List<Candidatura> listarMinhas(Usuario ator) {
        Validacao.exigirPerfil(ator, Perfil.ESTUDANTE, "consultar suas candidaturas");
        return candidaturaDao.listarPorEstudante(ator.getId());
    }

    /**
     * A empresa só consulta candidatos de vagas suas. Lambda: ordena os candidatos por nome
     * com um Comparator, sem precisar de uma classe separada só para a ordenação.
     */
    public List<Candidatura> listarCandidatos(Usuario ator, long vagaId) {
        Vaga vaga = vagaService.buscarVagaDaEmpresa(ator, vagaId, "consultar candidatos de");
        return candidaturaDao.listarPorVaga(vaga.getId()).stream()
                .sorted(Comparator.comparing(c -> c.getEstudante().getNome().toLowerCase()))
                .toList();
    }
}
