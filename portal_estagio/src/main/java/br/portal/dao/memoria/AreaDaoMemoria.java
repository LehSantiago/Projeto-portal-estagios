package br.portal.dao.memoria;

import br.portal.dao.AreaDao;
import br.portal.model.Area;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Implementação TEMPORÁRIA em memória. Será substituída por AreaDaoJdbc. */
public class AreaDaoMemoria implements AreaDao {
    private final Map<Long, Area> dados = new LinkedHashMap<>();
    private long proximoId = 1;

    @Override
    public Area salvar(Area area) {
        if (area.getId() == null) {
            area.setId(proximoId++);
        }
        dados.put(area.getId(), area);
        return area;
    }

    @Override
    public Optional<Area> buscarPorId(long id) {
        return Optional.ofNullable(dados.get(id));
    }

    @Override
    public Optional<Area> buscarPorNome(String nome) {
        return dados.values().stream()
                .filter(a -> a.getNome().equalsIgnoreCase(nome))
                .findFirst();
    }

    @Override
    public List<Area> listar() {
        return new ArrayList<>(dados.values());
    }

    @Override
    public void excluir(long id) {
        dados.remove(id);
    }
}
