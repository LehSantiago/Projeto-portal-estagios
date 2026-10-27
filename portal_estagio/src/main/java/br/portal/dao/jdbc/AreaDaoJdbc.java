package br.portal.dao.jdbc;

import br.portal.dao.AreaDao;
import br.portal.model.Area;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AreaDaoJdbc implements AreaDao {
    private final ConexaoFactory conexoes;

    public AreaDaoJdbc(ConexaoFactory conexoes) {
        this.conexoes = conexoes;
    }

    @Override
    public Area salvar(Area area) {
        try (Connection c = conexoes.abrir()) {
            if (area.getId() == null) {
                try (PreparedStatement ps = c.prepareStatement("INSERT INTO area (nome) VALUES (?) RETURNING id")) {
                    ps.setString(1, area.getNome());
                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        area.setId(rs.getLong(1));
                    }
                }
            } else {
                try (PreparedStatement ps = c.prepareStatement("UPDATE area SET nome = ? WHERE id = ?")) {
                    ps.setString(1, area.getNome());
                    ps.setLong(2, area.getId());
                    ps.executeUpdate();
                }
            }
            return area;
        } catch (SQLException e) {
            throw ConexaoFactory.traduzir(e, "salvar área");
        }
    }

    @Override
    public Optional<Area> buscarPorId(long id) {
        String sql = "SELECT id a_id, nome a_nome FROM area WHERE id = ?";
        try (Connection c = conexoes.abrir(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(Mapeadores.area(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw ConexaoFactory.traduzir(e, "buscar área");
        }
    }

    @Override
    public Optional<Area> buscarPorNome(String nome) {
        String sql = "SELECT id a_id, nome a_nome FROM area WHERE LOWER(nome) = LOWER(?)";
        try (Connection c = conexoes.abrir(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, nome);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(Mapeadores.area(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw ConexaoFactory.traduzir(e, "buscar área por nome");
        }
    }

    @Override
    public List<Area> listar() {
        String sql = "SELECT id a_id, nome a_nome FROM area ORDER BY nome";
        try (Connection c = conexoes.abrir();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Area> lista = new ArrayList<>();
            while (rs.next()) {
                lista.add(Mapeadores.area(rs));
            }
            return lista;
        } catch (SQLException e) {
            throw ConexaoFactory.traduzir(e, "listar áreas");
        }
    }

    @Override
    public void excluir(long id) {
        try (Connection c = conexoes.abrir(); PreparedStatement ps = c.prepareStatement("DELETE FROM area WHERE id = ?")) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw ConexaoFactory.traduzir(e, "excluir área");
        }
    }
}
