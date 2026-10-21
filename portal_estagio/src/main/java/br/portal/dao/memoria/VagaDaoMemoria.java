package br.portal.dao.memoria;

import br.portal.dao.VagaDao;
import br.portal.model.Modalidade;
import br.portal.model.SituacaoVaga;
import br.portal.model.Vaga;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Implementação TEMPORÁRIA em memória. Será substituída por VagaDaoJdbc. */
public class VagaDaoMemoria implements VagaDao {
    private final Map<Long, Vaga> dados = new LinkedHashMap<>();
    private long proximoId = 1;

    @Override
    public Vaga salvar(Vaga vaga) {
        if (vaga.getId() == null) {
            vaga.setId(proximoId++);
        }
        dados.put(vaga.getId(), vaga);
        return vaga;
    }

    @Override
    public Optional<Vaga> buscarPorId(long id) {
        return Optional.ofNullable(dados.get(id));
    }

    @Override
    public List<Vaga> listarAbertas(Long areaId, Modalidade modalidade) {
        return dados.values().stream()
                .filter(Vaga::isAberta)
                .filter(v -> areaId == null || v.getArea().getId().equals(areaId))
                .filter(v -> modalidade == null || v.getModalidade() == modalidade)
                .toList();
    }

    @Override
    public List<Vaga> listarPorEmpresa(long empresaId) {
        return dados.values().stream()
                .filter(v -> v.getEmpresa().getId() == empresaId)
                .toList();
    }

    @Override
    public boolean existeVagaNaArea(long areaId) {
        return dados.values().stream().anyMatch(v -> v.getArea().getId() == areaId);
    }

    @Override
    public long contarPorSituacao(SituacaoVaga situacao) {
        return dados.values().stream().filter(v -> v.getSituacao() == situacao).count();
    }
}
