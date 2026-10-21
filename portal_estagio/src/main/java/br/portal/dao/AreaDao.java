package br.portal.dao;

import br.portal.model.Area;

import java.util.List;
import java.util.Optional;

public interface AreaDao {
    /** Insere (id nulo) ou atualiza (id preenchido). */
    Area salvar(Area area);

    Optional<Area> buscarPorId(long id);

    Optional<Area> buscarPorNome(String nome);

    List<Area> listar();

    void excluir(long id);
}
