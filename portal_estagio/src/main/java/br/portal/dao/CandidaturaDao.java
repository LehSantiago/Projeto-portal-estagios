package br.portal.dao;

import br.portal.model.Candidatura;

import java.util.List;

public interface CandidaturaDao {
    Candidatura salvar(Candidatura candidatura);

    boolean existePorEstudanteEVaga(long estudanteId, long vagaId);

    List<Candidatura> listarPorEstudante(long estudanteId);

    List<Candidatura> listarPorVaga(long vagaId);

    long contar();
}
