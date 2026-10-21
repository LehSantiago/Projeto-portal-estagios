package br.portal.dao;

import br.portal.model.Modalidade;
import br.portal.model.SituacaoVaga;
import br.portal.model.Vaga;

import java.util.List;
import java.util.Optional;

public interface VagaDao {
    /** Insere (id nulo) ou atualiza (id preenchido). */
    Vaga salvar(Vaga vaga);

    Optional<Vaga> buscarPorId(long id);

    /** Vagas abertas; area e modalidade são filtros opcionais (null = sem filtro). */
    List<Vaga> listarAbertas(Long areaId, Modalidade modalidade);

    List<Vaga> listarPorEmpresa(long empresaId);

    boolean existeVagaNaArea(long areaId);

    long contarPorSituacao(SituacaoVaga situacao);
}
