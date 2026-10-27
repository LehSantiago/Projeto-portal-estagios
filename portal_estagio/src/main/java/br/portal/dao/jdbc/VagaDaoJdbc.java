package br.portal.dao.jdbc;

import br.portal.dao.VagaDao;
import br.portal.model.Modalidade;
import br.portal.model.SituacaoVaga;
import br.portal.model.Vaga;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class VagaDaoJdbc implements VagaDao {
    private final ConexaoFactory conexoes;

    public VagaDaoJdbc(ConexaoFactory conexoes) {
        this.conexoes = conexoes;
    }

    @Override
    public Vaga salvar(Vaga vaga) {
        try (Connection c = conexoes.abrir()) {
            if (vaga.getId() == null) {
                String sql = "INSERT INTO vaga (empresa_id, area_id, titulo, descricao, localizacao, "
                        + "modalidade, carga_horaria_semanal, situacao) VALUES (?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";
                try (PreparedStatement ps = c.prepareStatement(sql)) {
                    ps.setLong(1, vaga.getEmpresa().getId());
                    ps.setLong(2, vaga.getArea().getId());
                    ps.setString(3, vaga.getTitulo());
                    ps.setString(4, vaga.getDescricao());
                    ps.setString(5, vaga.getLocalizacao());
                    ps.setString(6, vaga.getModalidade().name());
                    ps.setInt(7, vaga.getCargaHorariaSemanal());
                    ps.setString(8, vaga.getSituacao().name());
                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        vaga.setId(rs.getLong(1));
                    }
                }
            } else {
                // a empresa dona nunca muda, por isso empresa_id fica fora do UPDATE
                String sql = "UPDATE vaga SET area_id = ?, titulo = ?, descricao = ?, localizacao = ?, "
                        + "modalidade = ?, carga_horaria_semanal = ?, situacao = ? WHERE id = ?";
                try (PreparedStatement ps = c.prepareStatement(sql)) {
                    ps.setLong(1, vaga.getArea().getId());
                    ps.setString(2, vaga.getTitulo());
                    ps.setString(3, vaga.getDescricao());
                    ps.setString(4, vaga.getLocalizacao());
                    ps.setString(5, vaga.getModalidade().name());
                    ps.setInt(6, vaga.getCargaHorariaSemanal());
                    ps.setString(7, vaga.getSituacao().name());
                    ps.setLong(8, vaga.getId());
                    ps.executeUpdate();
                }
            }
            return vaga;
        } catch (SQLException e) {
            throw ConexaoFactory.traduzir(e, "salvar vaga");
        }
    }

    @Override
    public Optional<Vaga> buscarPorId(long id) {
        String sql = Mapeadores.SELECT_VAGA + " WHERE v.id = ?";
        try (Connection c = conexoes.abrir(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(Mapeadores.vaga(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw ConexaoFactory.traduzir(e, "buscar vaga");
        }
    }

    @Override
    public List<Vaga> listarAbertas(Long areaId, Modalidade modalidade) {
        // O SQL é montado só com trechos fixos; os VALORES sempre entram por parâmetro (?).
        StringBuilder sql = new StringBuilder(Mapeadores.SELECT_VAGA).append(" WHERE v.situacao = 'ABERTA'");
        List<Object> parametros = new ArrayList<>();
        if (areaId != null) {
            sql.append(" AND v.area_id = ?");
            parametros.add(areaId);
        }
        if (modalidade != null) {
            sql.append(" AND v.modalidade = ?");
            parametros.add(modalidade.name());
        }
        sql.append(" ORDER BY v.id");
        return listar(sql.toString(), parametros, "listar vagas abertas");
    }

    @Override
    public List<Vaga> listarPorEmpresa(long empresaId) {
        return listar(Mapeadores.SELECT_VAGA + " WHERE v.empresa_id = ? ORDER BY v.id",
                List.of(empresaId), "listar vagas da empresa");
    }

    @Override
    public boolean existeVagaNaArea(long areaId) {
        String sql = "SELECT EXISTS (SELECT 1 FROM vaga WHERE area_id = ?)";
        try (Connection c = conexoes.abrir(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, areaId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getBoolean(1);
            }
        } catch (SQLException e) {
            throw ConexaoFactory.traduzir(e, "verificar uso da área");
        }
    }

    @Override
    public long contarPorSituacao(SituacaoVaga situacao) {
        String sql = "SELECT COUNT(*) FROM vaga WHERE situacao = ?";
        try (Connection c = conexoes.abrir(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, situacao.name());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            throw ConexaoFactory.traduzir(e, "contar vagas");
        }
    }

    private List<Vaga> listar(String sql, List<Object> parametros, String operacao) {
        try (Connection c = conexoes.abrir(); PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < parametros.size(); i++) {
                ps.setObject(i + 1, parametros.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<Vaga> lista = new ArrayList<>();
                while (rs.next()) {
                    lista.add(Mapeadores.vaga(rs));
                }
                return lista;
            }
        } catch (SQLException e) {
            throw ConexaoFactory.traduzir(e, operacao);
        }
    }
}
