package br.portal.service;

import br.portal.dao.CandidaturaDao;
import br.portal.dao.UsuarioDao;
import br.portal.dao.VagaDao;
import br.portal.model.Perfil;
import br.portal.model.SituacaoVaga;
import br.portal.model.Usuario;

public class RelatorioService {
    /** Contagens administrativas. */
    public record Contagens(long estudantes, long empresas, long vagasAbertas,
                            long vagasEncerradas, long candidaturas) {
    }

    private final UsuarioDao usuarioDao;
    private final VagaDao vagaDao;
    private final CandidaturaDao candidaturaDao;

    public RelatorioService(UsuarioDao usuarioDao, VagaDao vagaDao, CandidaturaDao candidaturaDao) {
        this.usuarioDao = usuarioDao;
        this.vagaDao = vagaDao;
        this.candidaturaDao = candidaturaDao;
    }

    public Contagens contagens(Usuario ator) {
        Validacao.exigirPerfil(ator, Perfil.ADMINISTRADOR, "consultar as contagens administrativas");
        return new Contagens(
                usuarioDao.contarPorPerfil(Perfil.ESTUDANTE),
                usuarioDao.contarPorPerfil(Perfil.EMPRESA),
                vagaDao.contarPorSituacao(SituacaoVaga.ABERTA),
                vagaDao.contarPorSituacao(SituacaoVaga.ENCERRADA),
                candidaturaDao.contar());
    }
}
