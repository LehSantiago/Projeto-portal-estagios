package br.portal.dao.memoria;

import br.portal.dao.CandidaturaDao;
import br.portal.model.Candidatura;

import java.util.ArrayList;
import java.util.List;

/** Implementação TEMPORÁRIA em memória. Será substituída por CandidaturaDaoJdbc. */
public class CandidaturaDaoMemoria implements CandidaturaDao {
    private final List<Candidatura> dados = new ArrayList<>();
    private long proximoId = 1;

    @Override
    public Candidatura salvar(Candidatura candidatura) {
        if (candidatura.getId() == null) {
            candidatura.setId(proximoId++);
        }
        dados.add(candidatura);
        return candidatura;
    }

    @Override
    public boolean existePorEstudanteEVaga(long estudanteId, long vagaId) {
        return dados.stream().anyMatch(c ->
                c.getEstudante().getId() == estudanteId && c.getVaga().getId() == vagaId);
    }

    @Override
    public List<Candidatura> listarPorEstudante(long estudanteId) {
        return dados.stream().filter(c -> c.getEstudante().getId() == estudanteId).toList();
    }

    @Override
    public List<Candidatura> listarPorVaga(long vagaId) {
        return dados.stream().filter(c -> c.getVaga().getId() == vagaId).toList();
    }

    @Override
    public long contar() {
        return dados.size();
    }
}
